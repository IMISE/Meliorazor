<script setup lang="ts">
import type { Questionnaire } from '@/stores/types';

defineProps<{
  questionnaire: Questionnaire
}>();

const clean = (input: string | null) =>
  (input || "undef").toLowerCase()
    .replaceAll("ä", "ae")
    .replaceAll("ö", "oe")
    .replaceAll("ü", "ue")
    .replaceAll("ß", "ss")
    .replace(/[^a-z0-9-]/gi, "_")
    .replace(/_{2,}/g, "_")
    .replace(/(^_|_$)/g, "");

</script>

<template>
  <a :href="`/projects/${questionnaire.id}-${clean(questionnaire.title.substring(0,20))}/`">
    <h3>{{ questionnaire.title }}</h3>
    <p>{{
      questionnaire.description.length > 50
        ? questionnaire.description.substring(0, 47) + "…"
        : questionnaire.description
    }}</p>
  </a>
</template>
