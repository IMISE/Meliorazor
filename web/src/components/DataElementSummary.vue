<script setup lang="ts">
import type { Variable } from "@/types/types";

defineProps<{
  dataElement: Variable;
}>();
</script>

<template>
  <h3>
    <template v-if="dataElement.variableName">
      [<var>{{ dataElement.variableName }}</var
      >]
    </template>
    {{ dataElement.label }}
  </h3>
  <template v-if="dataElement.choices?.length">
    Auswahlmöglichkeiten
    <ul>
      <li v-for="choice in dataElement.choices" :key="`${dataElement.id}-${choice.id}`">
        {{ choice.label }} (<var>{{ choice.code }}</var
        >)
      </li>
    </ul>
  </template>
  <template v-if="dataElement.metadata?.length">
    Weitere Informationen
    <ul>
      <li v-for="m in dataElement.metadata" :key="`${dataElement.id}-${m.key}-${m.value}`">
        {{ m.key }}: {{ m.value }}
      </li>
    </ul>
  </template>
</template>
