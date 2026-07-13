<script setup lang="ts">
import QuestionnaireList from "@/components/QuestionnaireList.vue";
import type { Questionnaire } from "@/types/types";
import { getProjects } from "@/utils/api";
import { mdiPlus, mdiRefresh } from "@mdi/js";
import { onMounted, ref, type Ref } from "vue";

// TODO sort
const questionnaires: Ref<Questionnaire[]> = ref([]);
const loading = ref(false);

const getQuestionnaires = async () => (questionnaires.value = (await getProjects(loading)) ?? []);

onMounted(getQuestionnaires);
</script>

<template>
  <h1>Projekte (Fragebögen)</h1>
  <br />
  <h3 v-if="loading">Lädt &hellip;</h3>
  <questionnaire-list
    v-else
    :questionnaires="questionnaires"
    display-controls
    display-link
    @delete="getQuestionnaires"
  />
  <v-row>
    <v-btn
      :prepend-icon="mdiPlus"
      color="primary"
      @click="$router.push({ name: 'create-project' })"
    >
      Neu
    </v-btn>
    <v-btn
      @click="getQuestionnaires"
      :prepend-icon="mdiRefresh"
      :disabled="loading"
      :loading="loading"
    >
      Aktualisieren
    </v-btn>
  </v-row>
</template>
