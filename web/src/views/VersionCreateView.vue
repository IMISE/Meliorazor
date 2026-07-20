<script setup lang="ts">
import BaseComponent from "@/components/BaseComponent.vue";
import DataElementList from "@/components/DataElementList.vue";
import QuestionnaireList from "@/components/QuestionnaireList.vue";
import type { Questionnaire } from "@/types/types";
import { createVersion, getProject, getVersionKeys } from "@/utils/api";
import { clean, paramToString } from "@/utils/utils";
import { computed, onMounted, ref, watch, type Ref } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();
const projectId = computed(() => Number(paramToString(router.currentRoute.value.params.pid)));

const currentStep = ref(1);

const selectedAnnotationIds: Ref<number[]> = ref([]);
const project: Ref<Questionnaire | undefined> = ref();
const otherVersions: Ref<string[] | undefined> = ref();
const loading = ref(true);
const creating = ref(false);
/** whether the form is in a valid state */
const isValid = ref();

const errorState = ref(false);
const key = ref("");
const description = ref("");

/** Filter out every annotation not selected */
const onlySelectedAnnotations = computed(() =>
  (project.value?.dataElements ?? []).map((element) => ({
    ...element,
    annotations: element.annotations.filter((annotation) =>
      selectedAnnotationIds.value.includes(annotation.id),
    ),
    choices: (element.choices ?? []).map((choice) => ({
      ...choice,
      annotations: choice.annotations.filter((annotation) =>
        selectedAnnotationIds.value.includes(annotation.id),
      ),
    })),
  })),
);

const load = async (newId: number, oldId: number | undefined = undefined) => {
  loading.value = true;
  project.value = await getProject(newId, oldId);
  otherVersions.value = await getVersionKeys(newId);
  loading.value = false;
};
watch(projectId, load);
onMounted(async () => {
  await router.isReady();
  load(projectId.value);
});

const create = async () => {
  const response = await createVersion(
    projectId.value,
    {
      key: key.value,
      description: description.value,
      annotationIds: selectedAnnotationIds.value,
    },
    creating,
  );
  const error = async () => {
    errorState.value = true;
    setTimeout(() => (errorState.value = false), 10_000);
    otherVersions.value = await getVersionKeys(projectId.value);
  };

  // rails!
  if (!response?.ok) {
    await error();
  } else {
    const id = await response.text();
    if (Number.isNaN(Number(id))) {
      await error();
    } else {
      // TODO: go to version view or at least versions tab
      return router.push({
        name: "single-project",
        params: {
          pid: projectId.value,
          title: clean(project.value?.title),
        },
      });
    }
  }
};

const keyRules = ref([
  (v: string) => !!v.length || "darf nicht leer sein",
  (v: string) => !otherVersions.value?.includes(v) || "darf sich im Projekt nicht doppeln",
]);
</script>

<template>
  <base-component
    :back-button-link="
      currentStep > 1
        ? undefined
        : { name: 'single-project', params: { pid: projectId, title: clean(project?.title) } }
    "
    @clicked="currentStep--"
  >
    <h1>Version erstellen</h1>
    <v-stepper
      v-if="project"
      v-model="currentStep"
      :items="['Projekt', 'Annotationen auswählen', 'Auswahl Überprüfen', 'Benennung']"
    >
      <template #[`item.1`]>
        <v-card title="Projekt">
          <questionnaire-list
            :questionnaires="project ? [project] : []"
            :display-controls="false"
            :display-link="false"
          />
        </v-card>
      </template>
      <template #[`item.2`]>
        <v-card title="Annotation auswählen">
          <data-element-list
            v-model="selectedAnnotationIds"
            :data-elements="project.dataElements"
            :project-id="projectId"
            :project-title="project.title"
            :my-roles="[]"
            :allow-edit-selection="true"
            :highlight-unnanotated="false"
            :show-actions="false"
            annotated-crf
          />
        </v-card>
      </template>
      <template #[`item.3`]>
        <v-card title="Auswahl Überprüfen">
          <data-element-list
            :project-id="projectId"
            :project-title="project.title"
            :data-elements="onlySelectedAnnotations"
            :my-roles="[]"
            :allow-edit-selection="false"
            :highlight-unnanotated="true"
            :show-actions="false"
            annotated-crf
          />
        </v-card>
      </template>
      <template #[`item.4`]>
        <v-form v-model="isValid" validate-on="input" @submit.prevent="create">
          <v-text-field
            v-model="key"
            :rules="keyRules"
            label="Versionsschlüssel"
            required
          ></v-text-field>
          <v-textarea v-model="description" label="Beschreibung"></v-textarea>
          <v-btn
            :disabled="creating || !isValid"
            :loading="creating"
            size="x-large"
            type="submit"
            :color="errorState ? 'danger' : 'primary'"
            block
          >
            Erstellen
          </v-btn>
          <template v-if="errorState">Da ist etwas schiefgelaufen.</template>
        </v-form>
      </template>
    </v-stepper>
    <h3 v-else-if="loading">Lädt &hellip;</h3>
    <template v-else>
      <h3>Fehler</h3>
      <p>Konnte Projekt nicht laden. Versuchen Sie bitte, sich neu einzuloggen.</p>
    </template>
  </base-component>
</template>
