<script setup lang="ts">
import CodeAnnotator from "@/components/CodeAnnotator.vue";
import CodeDisplay from "@/components/CodeDisplay.vue";
import CodeSearch from "@/components/CodeSearch.vue";
import DataElementSummary from "@/components/DataElementSummary.vue";
import type {
  Annotatable,
  CodeInfo,
  FhirValueSetDefinition,
  InformedCodeMapping,
  Variable,
} from "@/types/types";
import { mdiArrowLeft, mdiArrowRight, mdiCheckDecagramOutline } from "@mdi/js";
import { computed, inject, ref, watch, type Ref } from "vue";
import { getCode } from "@/utils/fhirApi.ts";
import { codeEquals } from "@/utils/fhirUtils";
import type { FhirTerminologyCode } from "@/types/fhir";
import type { IdTokenClaims } from "oidc-client-ts";

const user = inject("user") as Ref<IdTokenClaims | undefined>;

const emit = defineEmits(["success", "exit"]);

const props = defineProps<{
  valueSets: FhirValueSetDefinition[];
  dataElement: Variable;
  /** url to previous data element */ prev: string;
  /** url to next data elemnt */ next: string;
  showPrev: boolean;
  showNext: boolean;
}>();

// further information
const focusCode: Ref<InformedCodeMapping | undefined> = ref();
const focusCodeInfo: Ref<CodeInfo | undefined> = ref();
const selectedCode: Ref<FhirTerminologyCode | null | undefined> = ref();
const loadingFocus = ref(false);

/** selected to-annotate; data element itself or choice */
const selectedElement: Ref<Annotatable> = ref(props.dataElement);

/** previous annotations by me */
const previousAnnotations = computed(() =>
  selectedElement.value.annotations.filter((a) =>
    a.actions.some((action) => action.authorName === user.value?.preferred_username),
  ),
);
const previousAnnotationsPredicates = computed(() =>
  previousAnnotations.value
    .filter((annotation) => !annotation.negate)
    .filter((annotation) => codeEquals(annotation.code, focusCode.value))
    .map((annotation) => annotation.predicate),
);

const search = ref("");
watch(selectedElement, (val) => (search.value = val.label), { immediate: true });

const unselect = () => {
  focusCode.value = undefined;
  selectedCode.value = undefined;
};

const focus = (value: InformedCodeMapping) => {
  if (focusCode.value && codeEquals(focusCode.value, value)) {
    unselect();
  } else {
    focusCode.value = value;
  }
};

const updateFocus = (value: FhirTerminologyCode) => {
  focus({
    ...value,
    codeSystemName: focusCode.value!.codeSystemName,
    valueSet: focusCode.value!.valueSet,
  });
};

const successFeedback = ref(false);
const reload = async () => {
  emit("success");
  unselect();
  successFeedback.value = true;
};

// populate focusCodeInfo
watch(focusCode, async (newCode, oldCode) => {
  if (!newCode?.valueSet || newCode === oldCode) return;
  focusCodeInfo.value = undefined;
  loadingFocus.value = true;

  try {
    const response = await getCode(
      newCode.code,
      newCode.system,
      newCode.systemVersion,
      newCode.valueSet.server,
    );
    if (!response) return;
    focusCodeInfo.value = response;
  } catch (e) {
    console.error(e);
  } finally {
    loadingFocus.value = false;
  }
});

// we update data element on success to get new annotations
// when data element changes, update selected value as well to reflect change
watch(props, ({ dataElement: val }) => {
  const tmp = selectedElement.value;
  selectedElement.value =
    val.id === tmp.id
      ? val
      : val.unit?.id === tmp.id
        ? val.unit!
        : props.dataElement.choices!.find((choice) => choice.id === tmp.id)!;
});

const autocompleteChoices = computed(() => [
  { type: "subheader", title: "Variable selbst" },
  { title: props.dataElement.label, value: props.dataElement },
  ...(props.dataElement.unit
    ? [
        { type: "divider" },
        { type: "subheader", title: "ggf. Einheit" },
        { title: props.dataElement.unit.label, value: props.dataElement.unit },
      ]
    : []),
  ...(props.dataElement.choices?.length
    ? [
        { type: "divider" },
        { type: "subheader", title: "Auswahloptionen" },
        ...props.dataElement.choices.map((choice) => ({ title: choice.label, value: choice })),
      ]
    : []),
]);

defineExpose({
  back() {
    if (selectedCode.value === undefined) {
      unselect();
    } else {
      emit("exit");
    }
  },
});
</script>

<template>
  <v-row cols="12">
    <v-col cols="6">
      <v-btn
        v-if="showPrev"
        cols="6"
        :href="prev"
        variant="outlined"
        color="primary"
        :prepend-icon="mdiArrowLeft"
        spaced="start"
        style="padding-right: 20px"
        block
      >
        Zurück
      </v-btn>
    </v-col>
    <v-col>
      <v-btn
        v-if="showNext"
        :href="next"
        variant="outlined"
        color="primary"
        :append-icon="mdiArrowRight"
        spaced="end"
        style="padding-left: 20px"
        block
      >
        Weiter
      </v-btn>
      <v-btn
        v-else
        color="primary"
        :prepend-icon="mdiCheckDecagramOutline"
        :disabled="
          !selectedElement.annotations.length ||
          selectedCode !== undefined ||
          focusCode !== undefined
        "
        block
        @click="emit('exit')"
      >
        Abschließen
      </v-btn>
    </v-col>
  </v-row>
  <br />
  <v-row v-if="selectedCode === undefined" size="12">
    <v-col :cols="focusCode ? 6 : 12">
      <v-select
        v-model="selectedElement"
        :items="autocompleteChoices"
        label="Zu annotierendes Element"
        item-title="title"
        item-value="value"
        :disabled="!dataElement.choices?.length && !dataElement.unit"
      >
        <template #selection="{ item }">
          {{ item.value?.label ?? (item as { [index: string]: unknown })["label"] }}
          <v-chip v-if="item.value?.annotations?.length" style="margin-left: 4px">
            {{ item.value.annotations.length }} vorgeschlagene Annotationen
          </v-chip>
        </template>
        <template #item="{ props, item }">
          <v-list-item
            v-if="item.value"
            v-bind="props"
            :title="item.title"
            link
            @click="() => (selectedElement = item.value)"
          >
            <template #append>
              <v-chip v-if="item.value?.annotations?.length">
                {{ item.value.annotations.length }} vorgeschlagene Annotation<template
                  v-if="item.value.annotations.length > 1"
                  >en
                </template>
              </v-chip>
            </template>
          </v-list-item>
        </template>
      </v-select>
      <data-element-summary :data-element="dataElement" />
      <code-search
        v-model="search"
        :valueSets="valueSets"
        :previously-annotated="
          previousAnnotations.map((annotation) => annotation.code).filter((c) => c !== null)
        "
        :focus="focusCode"
        @focus="focus"
      />
      <v-divider />oder<v-divider />
      <v-btn color="warning" @click="() => (selectedCode = null)"
        >Ich glaube, es gibt keinen passenden Code.</v-btn
      >
    </v-col>
    <v-col v-if="focusCode !== undefined && focusCodeInfo" cols="6">
      <div style="position: sticky; top: 70px; right: 0px">
        <code-display
          v-if="!loadingFocus"
          :codeInfo="focusCodeInfo"
          :server="focusCode.valueSet.server"
          :known-predicates="previousAnnotationsPredicates"
          show-select-button
          @exit="unselect"
          @focus="updateFocus"
          @selection="(v: FhirTerminologyCode) => (selectedCode = v)"
        />
      </div>
    </v-col>
    <v-col v-else-if="loadingFocus" cols="6">
      <v-progress-circular class="loading-spinner" color="primary" indeterminate />
    </v-col>
  </v-row>
  <div v-else style="display: flex; justify-content: center; align-items: center">
    <code-annotator
      v-model="selectedElement"
      :data-element="dataElement!"
      :code="selectedCode"
      :known-predicates="previousAnnotationsPredicates"
      editable
      @success="reload"
      @edit="unselect"
    />
  </div>
  <v-snackbar
    v-model="successFeedback"
    class="feedback"
    location="bottom"
    prepend-icon="$success"
    color="success"
    :timeout="2000"
    title="Erstellen erfolgreich"
    text="Annotation wurde erstellt."
  />
</template>

<style scoped>
.loading-spinner {
  position: sticky;
  left: 75%;
  top: 50%;
}
</style>
