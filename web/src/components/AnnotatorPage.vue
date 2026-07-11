<script setup lang="ts">
import CodeAnnotator from "@/components/CodeAnnotator.vue";
import CodeDisplay from "@/components/CodeDisplay.vue";
import CodeSearch from "@/components/CodeSearch.vue";
import DataElementSummary from "@/components/DataElementSummary.vue";
import type { CodeInfo, InformedCodeMapping, Variable } from "@/types/types";
import { mdiArrowLeft, mdiArrowRight } from "@mdi/js";
import { ref, watch, type Ref } from "vue";
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
</script>

<template>
  <v-row cols="12" class="full-width">
    <v-btn v-if="showPrev" :href="prev" class="navigate" :prepend-icon="mdiArrowLeft">Zurück</v-btn>
    <v-btn v-if="showNext" :href="next" class="navigate" :append-icon="mdiArrowRight">
      Weiter
    </v-btn>
  </v-row>
  <br />
  <v-row v-if="!selectedCode" size="12" class="full-width">
    <v-col :cols="focusCode ? 6 : 12">
      <data-element-summary :data-element="dataElement" />
      <code-search
        :initial-search="dataElement.label?.replaceAll('_', ' ') ?? ''"
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
    <code-annotator :data-element-id="dataElement.id!" :code="selectedCode" @edit="unselect" />
  </v-col>
</template>

<style scoped>
.navigate {
  background-color: lightblue;
  color: black;
}
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
