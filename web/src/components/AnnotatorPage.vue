<script setup lang="ts">
import CodeAnnotator from "@/components/CodeAnnotator.vue";
import CodeDisplay from "@/components/CodeDisplay.vue";
import CodeSearch from "@/components/CodeSearch.vue";
import DataElementSummary from "@/components/DataElementSummary.vue";
import type { Annotatable, CodeInfo, InformedCodeMapping, Variable } from "@/types/types";
import { mdiArrowLeft, mdiArrowRight } from "@mdi/js";
import { computed, ref, watch, type Ref } from "vue";
import { getCode } from "@/utils/fhirApi.ts";
import { codeEquals } from "@/utils/fhirUtils";
import type { FhirTerminologyCode, FhirValueSetDefinition } from "@/types/fhir";

const { dataElement } = defineProps<{
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
const selectedCode: Ref<FhirTerminologyCode | undefined> = ref();
const loadingFocus = ref(false);

/** selected to-annotate; data element itself or choice */
const selectedElement: Ref<Annotatable> = ref(dataElement);

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

const autocompleteChoices = computed(() => [
  { type: "subheader", title: "Variable selbst" },
  { title: dataElement.label, value: dataElement },
  { type: "divider" },
  { type: "subheader", title: "Auswahloptionen" },
  ...(dataElement.choices?.map((choice) => ({ title: choice.label, value: choice })) ?? []),
]);
</script>

<template>
  <v-row cols="12" class="full-width">
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
    </v-col>
  </v-row>
  <br />
  <v-row v-if="!selectedCode" size="12" class="full-width">
    <v-col :cols="focusCode ? 6 : 12">
      <template v-if="dataElement.choices?.length">
        <v-autocomplete
          v-model="selectedElement"
          :items="autocompleteChoices"
          label="Zu annotatierendes Element"
          item-title="title"
          item-value="value"
        >
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
        </v-autocomplete>
      </template>
      <data-element-summary :data-element="dataElement" />
      <code-search
        :key="selectedElement?.label"
        :initial-search="selectedElement?.label ?? dataElement.label ?? ''"
        :valueSets="valueSets"
        :focus="focusCode"
        @focus="focus"
      />
    </v-col>
    <v-col v-if="focusCode && focusCodeInfo" cols="6">
      <code-display
        v-if="!loadingFocus"
        :codeInfo="focusCodeInfo"
        :server="focusCode.valueSet.server"
        show-select-button
        @focus="updateFocus"
        @selection="(v: FhirTerminologyCode) => (selectedCode = v)"
      />
    </v-col>
    <v-col v-else-if="loadingFocus" cols="6">
      <v-progress-circular class="loading-spinner" color="primary" indeterminate />
    </v-col>
  </v-row>
  <v-col v-else cols="12">
    <code-annotator
      v-model="selectedElement"
      :data-element="dataElement!"
      :code="selectedCode"
      @success="unselect"
      @edit="unselect"
    />
  </v-col>
</template>

<style scoped>
.loading-spinner {
  position: sticky;
  left: 75%;
  top: 50%;
}
.save {
  background-color: greenyellow;
}
.close {
  background-color: red;
}
.wrap-text {
  white-space: normal;
}
.full-width {
  width: 100%;
}
</style>
