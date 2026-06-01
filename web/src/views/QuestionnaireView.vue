<script setup lang="ts">
import auth from '@/auth';
import DataElementList from '@/components/DataElementList.vue';
import { Role, Status } from '@/stores/enums';
import type { Questionnaire } from '@/stores/types';
import type { IdTokenClaims } from 'oidc-client-ts';
import { computed, onMounted, ref, watch, type Ref } from 'vue';
import { useRouter } from 'vue-router';

const me: Ref<IdTokenClaims | undefined> = ref(undefined);

const router = useRouter();
const projectId = computed(() => Number(router.currentRoute.value.params.pid));
// const projectName = computed(() => String(useRouter().currentRoute.value.params.title));

const questionnaire: Ref<Questionnaire | undefined> = ref(undefined);
const loading = ref(false);

const roles = computed(() => questionnaire.value?.participants.find((participant) => participant.username === me.value?.preferred_username)?.roles ?? []);

const unannotated = computed(() => questionnaire.value?.dataElements.filter((e) => e.annotations.every((a) => a.status === Status.REJECTED)) ?? []);
const annotatable = computed(() => roles.value?.some((role) => role === Role.ANNOTATE) && unannotated.value.length);

const uncorrected = computed(() => questionnaire.value?.dataElements.flatMap((e) => e.annotations.filter((a) => a.status === Status.OPEN && a.authorName !== me.value?.preferred_username)) ?? []);
const correctable = computed(() => roles.value?.some((role) => role === Role.FEEDBACK) && uncorrected.value.length);

onMounted(() => get(projectId.value))
watch(projectId, () => get(projectId.value));

const get = async (id: number) => {
  const u = await auth.getUser();
  me.value = u?.profile;
  if (!u?.access_token) return;

  loading.value = true;
  try {
    const response = await fetch(`${import.meta.env.VITE_API_BASE}/questionnaire/${id}`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${u.access_token}`,
      }
    });
    if (!response.ok) return;
    questionnaire.value = await response.json() as Questionnaire;
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
}

const annotateAll = () => router.push({ name: "create-annotation", params: {
  projectId: projectId.value,
  elementIds: questionnaire.value?.dataElements.map((de) => de.id).join(","),
}});

</script>

<template>
  <v-sheet :min-height="200" class="sheet">
    <template v-if="questionnaire">
      <h1>{{ questionnaire.title }}</h1>
      <p>{{ questionnaire.description }}</p>
      <v-btn v-if="annotatable" @click="annotateAll">Annotieren!</v-btn>
      <v-btn v-else-if="correctable">Annotationen korrigieren</v-btn>
      <data-element-list :project-id="questionnaire.id" :project-name="questionnaire.title" :data-elements="questionnaire.dataElements" :my-roles="roles" />
    </template>
    <template v-else-if="loading">
      <h1>Laden...</h1>
    </template>
    <template v-else>
      <h1>Fehler</h1>
      <p>Fehler beim Abrufen des Projekts.</p>
    </template>
  </v-sheet>
</template>

<style scoped>
.sheet {
  padding: 10%;
}
</style>
