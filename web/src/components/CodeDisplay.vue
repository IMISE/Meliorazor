<script setup lang="ts">
import type { CodeInfo, CodeMapping, InformedCodeMapping } from '@/stores/types';
import { getCode } from '@/utils/fhirApi';
import { ref, type Ref } from 'vue';

const { codeInfo, server } = defineProps<{
  codeInfo: CodeInfo,
  server: string,
  showSelectButton: boolean | undefined,
}>();

defineEmits<{
  (e: "selection", v: CodeMapping): void,
  (e: "focus", v: CodeMapping): void,
}>();

const parents: Ref<CodeInfo[]> = ref([]);
const loadingParent = ref(false);

const loadParent = async (parentCode: string) => {
  loadingParent.value = true;
  try {
    const response = await getCode(parentCode, codeInfo.self.system, server);
    if (!response) return;
    parents.value.unshift(response);
  } catch (e) {
    console.error(e);
  } finally {
    loadingParent.value = false;
  }
}

</script>

<template>
  <br /><h3>Info</h3>
  <li>
    <ul>Inaktiv? {{ codeInfo?.inactive ? "Ja" : "Nein" }}</ul>
    <ul v-if="codeInfo?.definition">{{ codeInfo.definition }}</ul>
  </li>
  <br /><h3>Hierarchie</h3>
  <div>
    <v-btn v-if="parents[0]?.parents[0]" @click="loadParent(parents[0].parents[0])" variant="text" :disabled="loadingParent" :loading="loadingParent" block>Nächste Elterngeneration laden</v-btn>
    <!-- Parents of current selection -->
    <v-row v-for="parent in parents">
      {{ parent.self.display }} (<var>{{ parent.self.code }}</var>)
    </v-row>
    <!-- Current selection & Children -->
     <v-row cols="6">
       <code-children v-if="codeInfo" @focus="(v: CodeMapping) => $emit('focus', v)" :depth="0" :max-cols="6" :code="codeInfo.self" :info="codeInfo" :server="server" />
     </v-row>
  </div>
  <template v-if="codeInfo?.alternativeNames.length">
    <br /><h3>Alternative Namen</h3>
    <v-list lines="one">
      <v-list-item v-for="name in codeInfo.alternativeNames" :title="name" class="wrap-text" />
    </v-list>
  </template>
  <v-btn @click="$emit('selection', codeInfo.self)" block>Auswählen</v-btn>
</template>
