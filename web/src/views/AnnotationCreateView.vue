<script setup lang="ts">
import auth from "@/auth";
import AnnotatorPage from "@/components/AnnotatorPage.vue";
import TabbedInterface from "@/components/TabbedInterface.vue";
import type { Questionnaire, Variable } from "@/types/types";
import getEnv from "@/utils/env";
import { computed, onMounted, ref, watch, type Ref } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();
const elementIds = computed(() => router.currentRoute.value.params.elementIds);
const projectId = computed(() => router.currentRoute.value.params.projectId);

// TODO: API call
// 1. fetch project, if projectId provided
//  - extract elementIds from project and use them from now on
// 2. fetch elements from elementIds

const project: Ref<Questionnaire | undefined> = ref(undefined);
const dataElements: Ref<Variable[] | undefined> = computed(() =>
  project.value?.dataElements?.filter(
    (variable) => !elementIds.value?.length || elementIds.value.includes(String(variable.id)),
  ),
);

const getData = async () => {
  const u = await auth.getUser();
  if (!u?.access_token || !projectId.value) return;
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
};

onMounted(getData);
watch(projectId, getData);
</script>

<template>
  <tabbed-interface
    v-if="project && dataElements?.length"
    :data="dataElements"
    :title="(e) => `[${e.variableName}] ${e.label}`"
    v-slot="{ element, index, input }"
    class="full-full"
  >
    <annotator-page
      :value-sets="project.valueSets"
      :key="`${element.id}-${index}-${input.length}`"
      :data-element="element"
      :show-prev="index > 0"
      :show-next="index < input.length - 1"
      :prev="`#${input?.[index - 1]?.id ?? 0}`"
      :next="`#${input?.[index + 1]?.id ?? 0}`"
      class="full"
    />
  </tabbed-interface>
</template>

<style scoped>
.full-full {
  width: 80vw;
  height: 100vh;
}
.full {
  width: 100%;
  height: 100%;
}
</style>
