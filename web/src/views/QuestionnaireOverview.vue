<script setup lang="ts">
import auth from "@/auth";
import QuestionnaireList from "@/components/QuestionnaireList.vue";
import type { Questionnaire } from "@/types/types";
import getEnv from "@/utils/env";
import { mdiPlus, mdiRefresh } from "@mdi/js";
import { onMounted, ref, type Ref } from "vue";

// TODO sort
const questionnaires: Ref<Questionnaire[]> = ref([]);
const loading = ref(false);
const isLoggedIn = ref(false);

const getQuestionnaires = async () => {
  loading.value = true;
  try {
    const u = await auth.getUser();
    if (!u?.access_token) return;
    isLoggedIn.value = true;

    const response = await fetch(`${getEnv("VITE_API_BASE")}/questionnaire`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${u.access_token}`,
      },
    });
    questionnaires.value = (await response.json()) as Questionnaire[];
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
};

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
      v-if="isLoggedIn"
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
