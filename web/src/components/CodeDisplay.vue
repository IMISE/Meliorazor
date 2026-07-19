<script setup lang="ts">
import type { FhirTerminologyCode } from "@/types/fhir.js";
import CodeChildren from "./CodeChildren.vue";
import type { CodeInfo } from "@/types/types";
import { getCode } from "@/utils/fhirApi";
import { toFhirCode } from "@/utils/fhirUtils.ts";
import { compareCodes } from "@/utils/terminologyCodes";
import { watch } from "vue";
import { ref, type Ref } from "vue";
import { mdiClose } from "@mdi/js";

const { codeInfo, server } = defineProps<{
  codeInfo: CodeInfo;
  server: string;
  knownPredicates: string[];
  showSelectButton: boolean;
}>();

defineEmits<{
  (e: "selection", v: FhirTerminologyCode): void;
  (e: "focus", v: FhirTerminologyCode): void;
  (e: "exit"): void;
}>();

const parents: Ref<CodeInfo[]> = ref([codeInfo]);
const loadingParent = ref(false);

const loadParent = async (parentCode: string) => {
  loadingParent.value = true;
  try {
    const response = await getCode(
      parentCode,
      codeInfo.self.system,
      codeInfo.self.systemVersion,
      server,
    );
    if (!response) return;
    parents.value.unshift(response);
  } catch (e) {
    console.error(e);
  } finally {
    loadingParent.value = false;
  }
};

watch(
  () => codeInfo,
  (newVal) => {
    const parent = parents.value.findIndex((val) => compareCodes(val.self, newVal.self));
    if (parent !== -1) {
      if (parent + 1 < parents.value.length) {
        parents.value.splice(parent + 1);
      }
    } else {
      parents.value = [newVal];
    }
  },
);
</script>

<template>
  <v-btn :prepend-icon="mdiClose" text="Schließen" variant="plain" @click="$emit('exit')" />
  <br />
  <template v-if="codeInfo && (codeInfo.inactive || codeInfo.definition || knownPredicates.length)">
    <h3>Info</h3>
    <v-alert v-if="knownPredicates.length" type="warning" title="Schon verwendet">
      Das ausgewählte Element haben Sie schon mit diesem Code verbunden. Konkret wurden folgende
      Prädikate verwendet:
      <ul>
        <li v-for="predicate in knownPredicates" :key="predicate">
          <code>{{ predicate }}</code>
        </li>
      </ul>
    </v-alert>
    <template v-if="codeInfo.inactive">
      <br />
      <v-alert
        type="warning"
        title="Inaktiv"
        text="Dieser Code wurde von der Terminologie als inaktiv markiert."
      />
    </template>
    <ul v-if="codeInfo.definition">
      <li>{{ codeInfo.definition }}</li>
    </ul>
    <br />
  </template>
  <h3>Hierarchie</h3>
  <div>
    <v-btn
      v-if="parents[0]?.parents[0]"
      @click="loadParent(parents[0].parents[0])"
      variant="outlined"
      color="primary"
      :disabled="loadingParent"
      :loading="loadingParent"
      block
    >
      Nächste Elterngeneration laden
    </v-btn>
    <!-- Parents of current selection -->
    <template
      v-for="(parent, i) in parents"
      :key="`${codeInfo.self.system}-${codeInfo.self.code}-${i}-${parent.self.system}-${parent.self.code}`"
    >
      <div
        v-if="i !== parents.length - 1"
        style="cursor: pointer"
        @click="$emit('focus', toFhirCode(parent))"
      >
        {{ parent.self.display }} (<var>{{ parent.self.code }}</var
        >)
      </div>
    </template>
    <!-- Current selection & Children -->
    <code-children
      v-if="codeInfo"
      @focus="(v: FhirTerminologyCode) => $emit('focus', v)"
      :depth="0"
      :max-cols="6"
      :code="codeInfo.self"
      :info="codeInfo"
      :server="server"
    />
  </div>
  <template v-if="codeInfo?.alternativeNames.length">
    <br />
    <h3>Alternative Namen</h3>
    <ul>
      <li v-for="name in codeInfo.alternativeNames" :key="name">
        {{ name }}
      </li>
    </ul>
  </template>
  <v-btn
    v-if="showSelectButton"
    block
    color="primary"
    @click="$emit('selection', toFhirCode(codeInfo))"
  >
    Auswählen
  </v-btn>
</template>
