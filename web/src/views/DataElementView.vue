<script setup lang="ts">
import auth from '@/auth';
import AnnotationList from '@/components/AnnotationList.vue';
import type { Variable } from '@/types/types';
import { mdiReload } from '@mdi/js';
import { watch } from 'vue';
import { computed, onMounted, ref, type Ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const dataElementId = computed(() => Number(router.currentRoute.value.params.eid));
const projectId = computed(() => Number(router.currentRoute.value.params.pid));
const projectName = computed(() => String(router.currentRoute.value.params.title));

const data: Ref<Variable | undefined> = ref();
const loading = ref(true);

const get = async (elementId: number) => {
  loading.value = true;
  try {
    const u = await auth.getUser();
    if (!u?.access_token || !elementId) return;
    const options = {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${u.access_token}`,
      }
    };

    const response = await fetch(`${import.meta.env.VITE_API_BASE}/dataElement/${elementId}`, options);
    data.value = await response.json() as Variable;
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
}

onMounted(() => get(dataElementId.value));
watch(dataElementId, () => get(dataElementId.value));

</script>
<template>
  <template v-if="data">
    <h1>{{ data.label }}</h1>
    <h3>{{ data.variableName }}</h3>
    <br />
    <h2 v-if="data.annotations.length">Annotationen</h2>
    <annotation-list :project-id="projectId" :project-name="projectName" :data-element-id="data.id!" :data-element-name="data.label" :annotations="data.annotations" />
  </template>
  <template v-else-if="loading">
    Lade ...
  </template>
  <template v-else>
    <h1>404</h1>
    <p>Dieses Datenelement konnte nicht gefunden werden.</p>
    <router-link to="..">Zurück zum Projekt</router-link>
    <v-btn @click="() => get(dataElementId)" :prepend-icon="mdiReload">Nochmal probieren</v-btn>
  </template>
</template>
