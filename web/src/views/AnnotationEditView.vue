<script setup lang="ts">
import AnnotationCommentor from "@/components/AnnotationCommentor.vue";
import AnnotationInfo from "@/components/AnnotationInfo.vue";
import BaseComponent from "@/components/BaseComponent.vue";
import { Role } from "@/types/enums";
import type { Annotation, Questionnaire } from "@/types/types";
import { getAnnotation, getProject } from "@/utils/api";
import { getAnnotatableFromProject } from "@/utils/utils";
import type { IdTokenClaims } from "oidc-client-ts";
import { inject } from "vue";
import { computed, ref, watch, type Ref } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();
const user = inject<Ref<IdTokenClaims | undefined>>("user");

const annotationId = computed(() =>
  router.currentRoute.value.params.pid !== undefined
    ? Number(router.currentRoute.value.params.aid as string)
    : undefined,
);
const dataElementId = computed(() =>
  router.currentRoute.value.params.pid !== undefined
    ? Number(router.currentRoute.value.params.eid as string)
    : undefined,
);
const projectId = computed(() =>
  router.currentRoute.value.params.pid !== undefined
    ? Number(router.currentRoute.value.params.pid as string)
    : undefined,
);

const project: Ref<Questionnaire | undefined> = ref();
const annotation: Ref<Annotation | undefined> = ref();
const loadingProject = ref(false);
const loadingAnnotation = ref(false);

const roles = computed(
  () =>
    project.value?.participants.find(
      (participant) => participant.username === user?.value?.preferred_username,
    )?.roles ?? [],
);

const loadAnnotation = async () => {
  annotation.value = await getAnnotation(
    dataElementId.value,
    annotationId.value,
    loadingAnnotation,
  );
};

watch(annotationId, loadAnnotation, { immediate: true });
watch(
  projectId,
  async (newId, oldId) => (project.value = await getProject(newId, oldId, loadingProject)),
  { immediate: true },
);
</script>

<template>
  <base-component :back-button-link="-1">
    <p v-if="loadingProject">Lädt &hellip;</p>
    <template v-else-if="annotation && project && dataElementId">
      <annotation-info
        :annotated-element="getAnnotatableFromProject(annotation.id, project)"
        :annotation="annotation"
        :value-sets="project.valueSets"
        :my-roles="roles"
      />
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
