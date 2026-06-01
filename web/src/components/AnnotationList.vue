<script setup lang="ts">
import { Status } from '@/stores/enums';
import type { Annotation } from '@/stores/types';
import { mdiArrowRight, mdiCheckboxBlankCircleOutline, mdiCheckCircleOutline, mdiCloseCircleOutline } from '@mdi/js';

defineProps<{
  projectId: number,
  projectName: string,
  dataElementId: number,
  dataElementName: string,
  annotations: Annotation[],
}>();

const icon = (status: Status) => {
  switch (status) {
    case Status.ACCEPTED: return mdiCheckCircleOutline;
    case Status.OPEN: return mdiCheckboxBlankCircleOutline;
    case Status.REJECTED: return mdiCloseCircleOutline;
  }
}
</script>

<template>
  <v-list lines="one">
    <v-list-item
      v-for="annotation in annotations"
      :key="annotation.id"
      :prepend-icon="icon(annotation.status)"
      :append-icon="mdiArrowRight"
      :class="annotation.status"
    >
      <!-- TODO mehr Metadaten zur Annotation anzeigen? -->
      <v-list-item-title>{{ annotation.code.display }}</v-list-item-title>
      <v-list-item-subtitle>{{ annotation.authorName }}</v-list-item-subtitle>
      <template #append>
        <router-link :to="{ name: 'focus-annotation', params: {
          aid: annotation.id,
          subject: annotation.code.display,
        }}">
          <v-icon :icon="mdiArrowRight"></v-icon>
        </router-link>
      </template>
    </v-list-item>
  </v-list>
</template>

<style>

.ACCEPTED .v-list-item__prepend .v-icon {
  color: white;
  background-color: #028A0F;
  border-radius: 100%;
}
.OPEN .v-list-item__prepend .v-icon {
  background-color: lightgray;
  border-radius: 50%;
}
.REJECTED .v-list-item__prepend .v-icon {
  color: white;
  background-color: #8a0202;
  border-radius: 50%;
}

</style>
