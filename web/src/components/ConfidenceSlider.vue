<script setup lang="ts">
defineModel<number>();
defineProps<{
  editable: boolean;
}>();
</script>

<template>
  <v-slider
    :model-value="modelValue"
    @update:model-value="(val) => $emit('update:modelValue', val)"
    :readonly="!editable"
    :step="5"
    show-ticks="always"
    :min="0"
    :max="100"
    :ticks="{
      10: 'Sehr unsicher',
      30: 'Eher unsicher',
      50: 'Weder noch',
      70: 'Eher sicher',
      90: 'Sehr sicher',
    }"
    label="Konfidenz"
    thumb-label="hover"
    color="primary"
  >
    <template #thumb-label="{ modelValue: sliderModelValue }">{{ sliderModelValue }}%</template>
  </v-slider>
</template>
