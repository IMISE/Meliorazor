<script setup lang="ts">
import auth from "@/auth";
import DataElementList from "@/components/DataElementList.vue";
import { Role, Status } from "@/types/enums";
import type { Questionnaire } from "@/types/types";
import getEnv from "@/utils/env";
import { clean } from "@/utils/utils";
import type { IdTokenClaims } from "oidc-client-ts";
import { computed, inject, onMounted, provide, ref, watch, type Ref } from "vue";
import { useRouter } from "vue-router";

const user = inject<Ref<IdTokenClaims | undefined>>("user");

const router = useRouter();
const projectId = computed(() => Number(router.currentRoute.value.params.pid));
// const projectName = computed(() => String(router.currentRoute.value.params.title));

const questionnaire: Ref<Questionnaire | undefined> = ref();
const loading = ref(false);

provide("project", questionnaire);

const roles = computed(
  () =>
    questionnaire.value?.participants.find(
      (participant) => participant.username === user?.value?.preferred_username,
    )?.roles ?? [],
);

const unannotated = computed(
  () =>
    elements.value.filter((e) => e.annotations.every((a) => a.status === Status.REJECTED)) ?? [],
);
const annotatable = computed(
  () => roles.value?.some((role) => role === Role.ANNOTATE) && unannotated.value.length,
);

const uncorrected = computed(
  () =>
    elements.value.flatMap((e) =>
      e.annotations.filter(
        (a) => a.status === Status.OPEN && a.authorName !== user?.value?.preferred_username,
      ),
    ) ?? [],
);
const correctable = computed(
  () => roles.value?.some((role) => role === Role.FEEDBACK) && uncorrected.value.length,
);

const deSearch = ref("");
const elements = computed(
  () =>
    (deSearch.value
      ? ((s: string) =>
          questionnaire.value?.dataElements.filter(
            (de) =>
              de.label?.toLocaleLowerCase().includes(s) ||
              de.variableName?.toLocaleLowerCase().includes(s),
          ))(deSearch.value.toLocaleLowerCase())
      : questionnaire.value?.dataElements.filter((de) => de)) ?? [],
);

onMounted(() => get(projectId.value));
watch(projectId, (newVal) => get(newVal));

const get = async (id: number) => {
  const u = await auth.getUser();
  if (!u?.access_token) return;

  loading.value = true;
  try {
    const response = await fetch(`${getEnv("VITE_API_BASE")}/questionnaire/${id}`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${u.access_token}`,
      },
    });
    if (!response.ok) return;
    questionnaire.value = (await response.json()) as Questionnaire;
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
};

const selection: Ref<{ data: number[] | undefined }> = ref({
  data: roles.value.some((role) => role == Role.ADMIN || role == Role.FEEDBACK) ? [] : undefined,
});
const selected = computed({
  get: () => selection.value.data,
  set: (newVal: number[] | undefined) => (selection.value.data = newVal),
});
watch(roles, (val) => {
  if (val.some((role) => role == Role.ADMIN || role == Role.FEEDBACK)) {
    if (!selected.value?.length) {
      selected.value = [];
    } // if is admin and we've already selected some, keep going
  } else {
    selected.value = undefined;
  }
});

const toCorrect = computed(() =>
  selected.value?.length ? selected.value : uncorrected.value.map((annotation) => annotation.id),
);

const annotateAll = () =>
  router.push({
    name: "create-annotation",
    params: {
      projectId: projectId.value,
      elementIds: elements.value.map((de) => de.id).join(","),
    },
  });

const correctAll = () => {
  router.push({
    name: "correct-annotation",
    params: {
      projectId: projectId.value,
      annotationIds: toCorrect.value.join(","),
    },
  });
};

const createVersion = () => {
  router.push({
    name: "create-version",
    params: {
      pid: projectId.value,
      title: clean(questionnaire.value?.title),
    },
    query: {
      annotations: toCorrect.value.join(","),
    },
  });
};
</script>

<template>
  <v-sheet :min-height="200" class="sheet">
    <template v-if="questionnaire">
      <h1>{{ questionnaire.title }}</h1>
      <p>{{ questionnaire.description }}</p>
      <br />
      <h2>Datenelemente</h2>
      <v-text-field v-model="deSearch" label="Suche"></v-text-field>
      <v-btn v-if="annotatable" @click="annotateAll" color="primary">
        {{ elements.length }} Datenelemente annotieren
      </v-btn>
      <!-- TODO implement -->
      <v-btn v-if="correctable" @click="correctAll" color="primary">
        {{ toCorrect.length }} Annotationen korrigieren
      </v-btn>
      <v-btn
        v-if="selected?.length && roles.includes(Role.ADMIN)"
        @click="createVersion"
        color="primary"
      >
        Version erstellen
      </v-btn>
      <data-element-list
        v-model="selection.data"
        :project-id="projectId"
        :project-title="questionnaire.title"
        :data-elements="elements"
        :my-roles="roles"
        allow-edit-selection
        show-actions
      />
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
  padding-left: 20%;
  padding-right: 20%;
}
</style>
