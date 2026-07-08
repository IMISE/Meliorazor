<script setup lang="ts">
import type { Variable } from "@/types/types";
import DataElementInfo from "./DataElementInfo.vue";
import type { Role } from "@/types/enums.ts";
import { ref, watch, type Ref } from "vue";
import { passthroughSelection } from "@/utils/vueUtils.ts";

const model = defineModel<number[] | undefined>();

const selected: Ref<number[] | undefined> = ref(
  model.value === undefined ? undefined : model.value,
);
watch(selected, (newVal, oldVal) => passthroughSelection(newVal, oldVal, model));
watch(model, (newVal, oldVal) => passthroughSelection(newVal, oldVal, selected));

defineProps<{
  projectId: number;
  projectTitle: string;
  dataElements: Variable[];
  myRoles: Role[];
  allowEditSelection: boolean;
  showActions: boolean;
}>();
</script>

<template>
  <v-item-group :max="0">
    <v-item v-for="element in dataElements" :key="element.id">
      <data-element-info
        v-model="selected"
        :project-id="projectId"
        :project-title="projectTitle"
        :data-element="element"
        :my-roles="myRoles"
        :allow-edit-selection="allowEditSelection"
        :show-actions="showActions"
      />
    </v-item>
    <v-item v-if="!dataElements?.length">
      <i>Keine Datenelemente gefunden.</i>
    </v-item>
  </v-item-group>
</template>
