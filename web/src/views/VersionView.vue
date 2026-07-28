<script setup lang="ts">
import BaseComponent from "@/components/wrappers/BaseComponent.vue";
import DataElementList from "@/components/DataElementList.vue";
import type { Questionnaire, Variable, Version } from "@/types/types";
import request, { getProject } from "@/utils/api";
import { exportToSssomJson, exportToSssomTsv } from "@/utils/export";
import { clean, download } from "@/utils/utils";
import { ref, watch, type Ref } from "vue";
import { computed } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();
const projectId = computed(() => Number(router.currentRoute.value.params.pid));
const projectTitle = computed(() => String(router.currentRoute.value.params.title));
const versionKey = computed(() => String(router.currentRoute.value.params.key));

const version: Ref<Version | undefined> = ref();
const questionnaire: Ref<Questionnaire | undefined> = ref();
const loadingVersion = ref(false);
const loadingProject = ref(false);

const annotationIds = computed(
  () => version.value?.annotations.map((annotation) => annotation.id) ?? [],
);
const elements = computed(
  () =>
    questionnaire.value?.dataElements.map(
      (el) =>
        ({
          ...el,
          annotations: el.annotations.filter((anno) => annotationIds.value.includes(anno.id)),
        }) as Variable,
    ) ?? [],
);

const filePrefix = computed(
  () => `${clean(questionnaire.value?.title ?? projectTitle.value ?? "")} ${versionKey.value}`,
);

watch(
  versionKey,
  async (newVal) => {
    loadingVersion.value = true;
    version.value = newVal
      ? await (
          await request(`/questionnaire/${projectId.value}/version/${newVal}`, "GET", undefined)
        )?.json()
      : undefined;
    loadingVersion.value = false;
  },
  { immediate: true },
);
watch(
  projectId,
  async (newVal) => {
    loadingProject.value = true;
    questionnaire.value = await getProject(newVal);
    loadingProject.value = false;
  },
  { immediate: true },
);
</script>

<template>
  <base-component
    :back-button-link="{
      name: 'project-version-index',
      params: { pid: projectId, title: projectTitle },
    }"
  >
    <template v-if="version">
      <h2>{{ version.key }}</h2>
      <p v-if="version.description">{{ version.description }}</p>
      <template v-if="questionnaire">
        <h3>Annotierter Fragebogen</h3>
        <v-menu>
          <template #activator="{ props }">
            <v-btn v-if="elements?.length" v-bind="props" color="primary"> Exportieren </v-btn>
          </template>
          <v-list>
            <v-list-item
              link
              @click="
                download(
                  'application/sssom+json',
                  `${filePrefix}.sssom.json`,
                  exportToSssomJson(questionnaire!, versionKey),
                )
              "
              title="SSSOM/JSON"
            />
            <v-list-item
              link
              @click="
                download(
                  'text/sssom+tab-separated-values',
                  `${filePrefix}.sssom.tsv`,
                  exportToSssomTsv(questionnaire!, versionKey),
                )
              "
              title="SSSOM/TSV"
            />
          </v-list>
        </v-menu>
        <data-element-list
          :project-id="projectId"
          :project-title="projectTitle"
          :data-elements="elements"
          :my-roles="[]"
          :allow-edit-selection="false"
          :highlight-unnanotated="false"
          :show-actions="false"
          annotated-crf
        />
      </template>
      <h3 v-else-if="loadingProject">Lädt &hellip;</h3>
      <template v-else>
        <h3>Fehler</h3>
        <p>Konnte Projekt nicht laden. Versuchen Sie bitte, sich neu einzuloggen.</p>
      </template>
    </template>
    <h3 v-else-if="loadingVersion">Lädt &hellip;</h3>
    <template v-else>
      <h3>Fehler</h3>
      <p>Konnte Version nicht laden. Versuchen Sie bitte, sich neu einzuloggen.</p>
    </template>
  </base-component>
</template>
