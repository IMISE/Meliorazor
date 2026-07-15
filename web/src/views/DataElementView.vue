<script setup lang="ts">
import auth from "@/auth";
import AnnotationList from "@/components/AnnotationList.vue";
import BaseComponent from "@/components/BaseComponent.vue";
import type { Variable } from "@/types/types";
import getEnv from "@/utils/env";
import { watch } from "vue";
import { computed, onMounted, ref, type Ref } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();
const dataElementId = computed(() => Number(router.currentRoute.value.params.eid));
const projectId = computed(() => Number(router.currentRoute.value.params.pid));
const projectTitle = computed(() => String(router.currentRoute.value.params.title));

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
        Authorization: `Bearer ${u.access_token}`,
      },
    };

    const response = await fetch(`${getEnv("VITE_API_BASE")}/dataElement/${elementId}`, options);
    data.value = (await response.json()) as Variable;
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
};

onMounted(() => get(dataElementId.value));
watch(dataElementId, () => get(dataElementId.value));
</script>

<template>
  <base-component
    :back-button-link="{ name: 'single-project', params: { pid: projectId, title: projectTitle } }"
  >
    <template v-if="loading">Lädt &hellip;</template>
    <template v-else-if="data">
      <h1>{{ data.label }}</h1>
      <h3>{{ data.variableName }}</h3>
      <br />
      <h2 v-if="data.annotations.length">Annotationen</h2>
      <annotation-list
        show-add-button
        :project-id="projectId"
        :project-name="projectTitle"
        :data-element-id="data.id!"
        :data-element-name="data.label"
        :annotations="data.annotations"
      />
    </template>
    <template v-else>
      <h3>Fehler</h3>
      <p>Fehler beim Abrufen dieser Ressource.</p>
    </template>
  </base-component>
</template>
