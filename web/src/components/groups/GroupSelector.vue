<script setup lang="ts">
import type { Group } from "@/types/types";
import { shorten } from "@/utils/utils";
import { mdiPencil } from "@mdi/js";

defineModel<Group>();

defineProps<{
  options: Group[];
  disabled?: boolean;
  editable?: boolean;
}>();
</script>

<template>
  <v-list lines="one">
    <v-list-item :key="modelValue?.id">
      <v-list-item-title>
        <template v-if="modelValue">
          {{ modelValue.name }}
        </template>
        <i v-else>Keine Gruppe ausgewählt</i>
      </v-list-item-title>
      <v-list-item-subtitle>
        <span color="primary">Ausgewählte Gruppe</span>
      </v-list-item-subtitle>
      <template #append>
        <v-menu>
          <template v-if="editable !== false" #activator="{ props }">
            <v-btn v-bind="props" :disabled="disabled" :icon="mdiPencil" variant="plain" />
          </template>
          <v-list>
            <v-list-item
              v-for="item in options"
              :key="item.id"
              link
              @click="() => $emit('update:modelValue', item)"
            >
              <v-list-item-title>{{ item.name }}</v-list-item-title>
              <v-list-item-subtitle>{{ shorten(item.description, 50) }}</v-list-item-subtitle>
            </v-list-item>
          </v-list>
        </v-menu>
      </template>
    </v-list-item>
  </v-list>
</template>
