<script setup lang="ts">
import auth from '@/auth';
import AnnotationInfo from '@/components/AnnotationInfo.vue';
import type { Annotation, Questionnaire } from '@/types/types';
import type { Ref } from 'vue';
import { computed, onMounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const annotationId = computed(() => router.currentRoute.value.params.aid);
const dataElementId = computed(() => router.currentRoute.value.params.eid);
const projectId = computed(() => router.currentRoute.value.params.pid);

const project: Ref<Questionnaire | undefined> = ref(undefined);
const annotation: Ref<Annotation | undefined> = ref(undefined);
const loadingProject = ref(false);
const loadingAnnotation = ref(false);


const getAnnotation = async (newId: string[] | string | undefined, oldId: string[] | string | undefined) => {
  if (loadingAnnotation.value || newId === oldId) {
    return;
  } else if (!newId || !dataElementId.value) {
    annotation.value = undefined;
    return;
  }
  try {
    loadingAnnotation.value = true;
    const u = await auth.getUser();
    if (!u?.access_token) return;
    const options = {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${u.access_token}`,
      }
    };

    const response = await fetch(`${import.meta.env.VITE_API_BASE}/dataElement/${dataElementId.value}/annotation/${annotationId.value}`, options);
    annotation.value = await response.json() as Annotation;
  } catch (e) {
    console.error(e);
  } finally {
    loadingAnnotation.value = false;
  }
}

const getProject = async (newId: string[] | string | undefined, oldId: string[] | string | undefined) => {
  if (loadingProject.value || newId === oldId) {
    return;
  } else if (!newId) {
    annotation.value = undefined;
    return;
  }
  try {
    loadingProject.value = true;
    const u = await auth.getUser();
    if (!u?.access_token) return;
    const options = {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${u.access_token}`,
      }
    };

    const response = await fetch(`${import.meta.env.VITE_API_BASE}/questionnaire/${projectId.value}`, options);
    project.value = await response.json() as Questionnaire;
  } catch (e) {
    console.error(e);
  } finally {
    loadingProject.value = false;
  }
};

watch(annotationId, getAnnotation);
watch(projectId, getProject);

onMounted(() => Promise.allSettled([getAnnotation(annotationId.value, undefined ), getProject(projectId.value, undefined)]))

</script>

<template>
  <p v-if="loadingProject || loadingAnnotation">Lädt ...</p>
  <annotation-info v-else-if="annotation && project" :annotation="annotation" :value-sets="project.valueSets" />
  <p v-else>Nicht gefunden :-(</p>
</template>
