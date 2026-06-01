<script setup lang="ts">
import auth from '@/auth';
import QuestionnaireList from '@/components/QuestionnaireList.vue';
import type { Questionnaire } from '@/stores/types';
import { mdiPlus, mdiRefresh } from '@mdi/js';
import { onMounted, ref, type Ref } from 'vue';

// TODO sort
const questionnaires: Ref<Questionnaire[]> = ref([]);
const loading = ref(false);
const isLoggedIn = ref(false);

const get = async () => {
  isLoggedIn.value = !!(await auth.getUser())?.access_token;
  await getQuestionnaires();
}

const getQuestionnaires = async () => {
  const u = await auth.getUser();
  if (!u?.access_token) return;
  try {
    const response = await fetch(`${import.meta.env.VITE_API_BASE}/questionnaire`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${u.access_token}`,
      }
    });
    questionnaires.value = await response.json() as Questionnaire[];
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
};

onMounted(() => getQuestionnaires());

</script>

<template>
  <h1>Projekte (Fragebögen)</h1><br />
  <questionnaire-list :questionnaires="questionnaires" />
  <v-row>
    <v-btn v-if="isLoggedIn" :prepend-icon="mdiPlus" @click="$router.push({ name: 'create-project' })">Neu</v-btn>
    <v-btn @click="get" :prepend-icon="mdiRefresh" :disabled="loading" :loading="loading">Aktualisieren</v-btn>
  </v-row>
</template>
