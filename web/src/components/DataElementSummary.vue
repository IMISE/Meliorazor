<script setup lang="ts">
import type { Variable } from '@/stores/types';

defineProps<{
  dataElement: Variable,
}>();
</script>

<template>
  <h3>{{ dataElement.label }}</h3><br />
  <h6 v-if="dataElement.variableName">{{ dataElement.variableName }}</h6>
  <v-expansion-panels v-if="dataElement.metadata.length">
    <v-expansion-panel v-if="dataElement.choices?.length" title="Auswahlmöglichkeiten">
      <v-expansion-panel-text>
        <template v-for="choice in dataElement.choices">
          {{ choice.name }} (<var>{{ choice.code }}</var>)<br />
        </template>
      </v-expansion-panel-text>
    </v-expansion-panel>
    <v-expansion-panel v-if="dataElement.metadata.length" title="Weitere Informationen">
      <v-expansion-panel-text>
        <template v-for="m in dataElement.metadata">
          {{ m.key }}: {{ m.value }}<br />
        </template>
      </v-expansion-panel-text>
    </v-expansion-panel>
  </v-expansion-panels>
</template>
