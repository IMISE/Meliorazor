<script setup lang="ts">
import auth from "@/auth";
import AnnotationCommentor from "@/components/AnnotationCommentor.vue";
import AnnotationInfo from "@/components/AnnotationInfo.vue";
import BaseComponent from "@/components/BaseComponent.vue";
import { Role } from "@/types/enums";
import type { Annotation, Questionnaire } from "@/types/types";
import getEnv from "@/utils/env";
import type { IdTokenClaims } from "oidc-client-ts";
import { computed, onMounted, ref, watch, type Ref } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();
const annotationId = computed(() =>
  router.currentRoute.value.params.pid !== undefined
    ? (router.currentRoute.value.params.aid as string)
    : undefined,
);
const dataElementId = computed(() =>
  router.currentRoute.value.params.pid !== undefined
    ? (router.currentRoute.value.params.eid as string)
    : undefined,
);
const projectId = computed(() =>
  router.currentRoute.value.params.pid !== undefined
    ? (router.currentRoute.value.params.pid as string)
    : undefined,
);

const me: Ref<IdTokenClaims | undefined> = ref();
const project: Ref<Questionnaire | undefined> = ref();
const annotation: Ref<Annotation | undefined> = ref();
const loadingProject = ref(false);
const loadingAnnotation = ref(false);

const roles = computed(
  () =>
    project.value?.participants.find(
      (participant) => participant.username === me.value?.preferred_username,
    )?.roles ?? [],
);

const loadAnnotation = async () => {
  try {
    loadingAnnotation.value = true;
    const u = await auth.getUser();
    me.value = u?.profile;
    if (!u?.access_token) return;
    const options = {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${u.access_token}`,
      },
    };

    const response = await fetch(
      `${getEnv("VITE_API_BASE")}/dataElement/${dataElementId.value}/annotation/${annotationId.value}`,
      options,
    );
    annotation.value = (await response.json()) as Annotation;
  } catch (e) {
    console.error(e);
  } finally {
    loadingAnnotation.value = false;
  }
};

const getAnnotation = async (newId: string | undefined, oldId: string | undefined) => {
  if (loadingAnnotation.value || newId === oldId) {
    return;
  } else if (!newId || !dataElementId.value) {
    annotation.value = undefined;
    return;
  }
  await loadAnnotation();
};

const getProject = async (newId: string | undefined, oldId: string | undefined) => {
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
        Authorization: `Bearer ${u.access_token}`,
      },
    };

    const response = await fetch(
      `${getEnv("VITE_API_BASE")}/questionnaire/${projectId.value}`,
      options,
    );
    project.value = (await response.json()) as Questionnaire;
  } catch (e) {
    console.error(e);
  } finally {
    loadingProject.value = false;
  }
};

watch(annotationId, getAnnotation);
watch(projectId, getProject);

onMounted(() =>
  Promise.allSettled([
    getAnnotation(annotationId.value, undefined),
    getProject(projectId.value, undefined),
  ]),
);
</script>

<template>
  <base-component :back-button-link="-1">
    <p v-if="loadingProject || loadingAnnotation">Lädt ...</p>
    <template v-else-if="annotation && project && dataElementId">
      <annotation-info :annotation="annotation" :value-sets="project.valueSets" :my-roles="roles" />
      <annotation-commentor
        v-if="roles.includes(Role.FEEDBACK)"
        @reload="loadAnnotation"
        :annotation-id="annotation.id"
        :data-element-id="dataElementId"
      />
    </template>
    <template v-else>
      <h3>Fehler</h3>
      <p>Fehler beim Abrufen der Annotation.</p>
    </template>
  </base-component>
</template>
