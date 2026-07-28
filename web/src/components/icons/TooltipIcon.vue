<script setup lang="ts">
defineProps<{
  icon: string;
  text: string;
}>();
</script>

<template>
  <v-tooltip :text="text">
    <template #activator="{ props }">
      <v-icon v-bind="props" :icon="icon" />
    </template>
  </v-tooltip>
</template>
