<script setup lang="ts">
import type { Annotation } from "@/types/types";
import { mdiArrowRight, mdiPlus } from "@mdi/js";
import { useRouter } from "vue-router";
import StatusIcon from "./StatusIcon.vue";

const { projectId, dataElementId } = defineProps<{
  projectId: number;
  projectName: string;
  dataElementId: number;
  dataElementName: string;
  annotations: Annotation[];
  showAddButton: boolean;
}>();

const router = useRouter();

const annotate = async () => {
  await router.push({
    name: "create-annotation",
    params: {
      projectId,
      elementIds: dataElementId,
    },
  });
};
</script>

<template>
  <v-list lines="one" stripe>
    <v-list-item
      v-if="showAddButton"
      :prepend-icon="mdiPlus"
      title="Annotation hinzufügen"
      @click="annotate"
    />
    <v-divider />
    <v-list-item
      v-for="(annotation, i) in annotations"
      :class="i % 2 === 0 ? 'bg-grey-lighten-3' : undefined"
      :key="annotation.id"
      :append-icon="mdiArrowRight"
    >
      <!-- TODO mehr Metadaten zur Annotation anzeigen? -->
      <v-list-item-title>{{ annotation.code.display }}</v-list-item-title>
      <v-list-item-subtitle>{{ annotation.authorName }}</v-list-item-subtitle>
      <template #prepend>
        <status-icon :status="annotation.status" :include-text="false" />
      </template>
      <template #append>
        <v-chip style="margin: 0 4px">{{ annotation.predicate }}</v-chip>
        <v-chip style="margin: 0 4px">{{ annotation.confidence }}%</v-chip>
        <router-link
          :to="{
            name: 'focus-annotation',
            params: {
              aid: annotation.id,
              subject: annotation.code.display,
            },
          }"
        >
          <v-icon :icon="mdiArrowRight"></v-icon>
        </router-link>
      </template>
    </v-list-item>
  </v-list>
</template>
