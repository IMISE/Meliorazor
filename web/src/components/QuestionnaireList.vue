<script setup lang="ts">
import type { Questionnaire } from "@/types/types";
import QuestionnaireInfo from "./QuestionnaireInfo.vue";

defineProps<{
  questionnaires: Questionnaire[];
}>();
</script>

<template>
  <v-item-group v-if="questionnaires.length">
    <v-item v-for="(q, i) in questionnaires" :key="`${questionnaires.length}-${i}-${q.id}`">
      <QuestionnaireInfo :questionnaire="q" />
    </v-item>
  </v-item-group>
  <p v-else>Keine Elemente.</p>
</template>
