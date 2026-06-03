<script setup lang="ts">
import type { Variable } from '@/types/types';
import DataElementInfo from './DataElementInfo.vue';
import type { Role } from '@/types/enums.ts';

defineProps<{
  projectId: number,
  projectName: string,
  dataElements: Variable[],
  myRoles: Role[],
}>();
</script>

<template>
  <br />
  <h2>Datenelemente</h2>
  <v-item-group :max="0">
    <v-item v-for="element in dataElements" :key="element.id">
      <data-element-info :project-id="projectId" :project-title="projectName" :data-element="element" :my-roles="myRoles" />
    </v-item>
    <v-item v-if="!dataElements?.length">
      <i>Keine Datenelemente gefunden.</i>
    </v-item>
  </v-item-group>
</template>
