<script setup lang="ts">
import type { Variable } from "@/types/types";
import DataElementInfo from "./DataElementInfo.vue";
import type { Role } from "@/types/enums.ts";

const model = defineModel<number[] | undefined>();

defineProps<{
  projectId: number;
  projectTitle: string;
  dataElements: Variable[];
  myRoles: Role[];
  allowEditSelection: boolean;
  highlightUnnanotated: boolean;
  annotatedCrf: boolean;
  showActions: boolean;
}>();
</script>

<template>
  <v-item-group :max="0">
    <v-item v-for="element in dataElements" :key="element.id">
      <data-element-info
        v-model="model"
        :project-id="projectId"
        :project-title="projectTitle"
        :data-element="element"
        :my-roles="myRoles"
        :allow-edit-selection="allowEditSelection"
        :highlight-unannotated="highlightUnnanotated"
        :show-actions="showActions"
        :annotated-crf="annotatedCrf"
      />
    </v-item>
    <v-item v-if="!dataElements?.length">
      <i>Keine Datenelemente gefunden.</i>
    </v-item>
  </v-item-group>
</template>
