<script setup lang="ts">
import type { FhirTerminologyCode } from "@/types/fhir.js";
import CodeChildren from "./CodeChildren.vue";
import type { CodeInfo } from "@/types/types";
import { getCode } from "@/utils/fhirApi";
import { toFhirCode } from "@/utils/fhirUtils.ts";
import { compareCodes } from "@/utils/terminologyCodes";
import { mdiCheck, mdiClose } from "@mdi/js";
import { watch } from "vue";
import { ref, type Ref } from "vue";

const { codeInfo, server } = defineProps<{
  codeInfo: CodeInfo;
  server: string;
  showSelectButton: boolean;
}>();

defineEmits<{
  (e: "selection", v: FhirTerminologyCode): void;
  (e: "focus", v: FhirTerminologyCode): void;
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
  <br />
  <h3>Info</h3>
  <ul>
    <li>
      Inaktiv?
      <template v-if="codeInfo?.inactive"> <v-icon :icon="mdiCheck" /> Ja</template>
      <template v-else> <v-icon :icon="mdiClose" /> Nein</template>
    </li>
    <li v-if="codeInfo?.definition">
      {{ codeInfo.definition }}
    </li>
  </ul>
  <br />
  <h3>Hierarchie</h3>
  <div>
    <v-btn
      v-if="parents[0]?.parents[0]"
      @click="loadParent(parents[0].parents[0])"
      variant="text"
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
      <v-row v-if="i !== parents.length - 1" @click="$emit('focus', toFhirCode(parent))">
        {{ parent.self.display }} (<var>{{ parent.self.code }}</var
        >)
      </v-row>
    </template>
    <!-- Current selection & Children -->
    <v-row cols="6">
      <code-children
        v-if="codeInfo"
        @focus="(v: FhirTerminologyCode) => $emit('focus', v)"
        :depth="0"
        :max-cols="6"
        :code="codeInfo.self"
        :info="codeInfo"
        :server="server"
      />
    </v-row>
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
