<script setup lang="ts">
import BaseComponent from "@/components/wrappers/BaseComponent.vue";
import DataElementList from "@/components/DataElementList.vue";
import ValueSetSelector from "@/components/ValueSetSelector.vue";
import { Role, Status } from "@/types/enums";
import type {
  FhirValueSetDefinition,
  Questionnaire,
  QuestionnaireOut,
  ServerDefinition,
} from "@/types/types";
import request, { getProject, getVersionKeys } from "@/utils/api";
import { mdiArrowRight, mdiCheck, mdiClose, mdiPlus } from "@mdi/js";
import type { IdTokenClaims } from "oidc-client-ts";
import { computed, inject, onMounted, provide, ref, watch, type Ref } from "vue";
import { useRouter } from "vue-router";
import type { IconValue } from "vuetify/lib/composables/icons.mjs";

const user = inject<Ref<IdTokenClaims | undefined>>("user");

const router = useRouter();
const projectId = computed(() => Number(router.currentRoute.value.params.pid));
const projectTitle = computed(() => String(router.currentRoute.value.params.title));

const questionnaire: Ref<Questionnaire | undefined> = ref();
const loading = ref(false);
const tab = ref("single-project");
const route = computed(() => router.currentRoute.value.name?.toString() ?? "");
watch(
  route,
  (name) => {
    if (name === tab.value) {
      return;
    } else {
      tab.value = name;
    }
  },
  { immediate: true },
);
// TODO broken at the moment (see version redirect)
watch(
  tab,
  (name) => {
    if (route.value === name) {
      return;
    } else {
      router.push({
        name,
        params: {
          pid: projectId.value,
          title: projectTitle.value,
        },
      });
    }
  } /* NOT immediate! */,
);

const valueSetsByServer = computed(() =>
  questionnaire.value?.valueSets.reduce(
    (acc, val) => (acc[val.server] ? acc[val.server]?.push(val) : (acc[val.server] = [val]), acc),
    {} as { [index: string]: FhirValueSetDefinition[] },
  ),
);

provide("project", questionnaire);

const myRoles = computed(
  () =>
    questionnaire.value?.participants.find(
      (participant) => participant.username === user?.value?.preferred_username,
    )?.roles ?? [],
);
const isAdmin = computed(() => myRoles.value.includes(Role.ADMIN));

const unannotated = computed(
  () =>
    elements.value.filter((e) =>
      e.annotations.every(
        (a) =>
          a.status === Status.REJECTED ||
          a.actions.every(
            (action) =>
              action.actionType !== "submission" ||
              action.authorName !== user?.value?.preferred_username,
          ),
      ),
    ) ?? [],
);
const annotatable = computed(
  () => myRoles.value?.some((role) => role === Role.ANNOTATE) && unannotated.value.length,
);

const uncorrected = computed(
  () =>
    elements.value.flatMap((e) =>
      e.annotations.filter(
        (a) =>
          a.status === Status.OPEN &&
          a.actions.every((action) => action.authorName !== user?.value?.preferred_username),
      ),
    ) ?? [],
);
const correctable = computed(
  () => myRoles.value?.some((role) => role === Role.FEEDBACK) && uncorrected.value.length,
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
  loading.value = true;
  questionnaire.value = await getProject(id);
  loading.value = false;
};

const selected: Ref<number[] | undefined> = ref();
watch(
  myRoles,
  (val) => {
    if (isAdmin.value || val.includes(Role.FEEDBACK)) {
      if (!selected.value?.length) {
        selected.value = [];
      } // if is admin and we've already selected some, keep going
    } else {
      selected.value = undefined;
    }
  },
  { immediate: true },
);

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
      title: projectTitle.value,
    },
  });
};

/** versions */
const versions: Ref<string[]> = ref([]);
watch(
  questionnaire,
  async (newVal) =>
    (versions.value = isAdmin.value && newVal ? ((await getVersionKeys(newVal.id)) ?? []) : []),
  { immediate: true },
);

/** editing */
const valueSetEdit: Ref<{ id: number; set: FhirValueSetDefinition | undefined }[]> = ref([]);
const valueSetKey = ref(0);
const loadingEdits = ref(false);
watch(
  questionnaire,
  (newVal) => {
    valueSetEdit.value = (newVal?.valueSets ?? []).map((set, id) => ({ id, set }));
    valueSetKey.value = valueSetEdit.value.length ?? 0;
  },
  { deep: true, immediate: true },
);

const editFeedbackIcon: Ref<IconValue | undefined> = ref();
const saveEdits = async () => {
  loadingEdits.value = true;
  const response = questionnaire.value
    ? await request(
        `/questionnaire/${questionnaire.value.id}`,
        "PUT",
        JSON.stringify({
          id: undefined,
          title: undefined,
          description: undefined,
          dataElements: undefined,
          options: questionnaire.value.options,
          valueSets: valueSetEdit.value.map((row) => row.set),
          participants: undefined,
          groupId: undefined,
        } as QuestionnaireOut),
      )
    : undefined;
  if (response?.ok) {
    editFeedbackIcon.value = mdiCheck;
    setTimeout(() => (editFeedbackIcon.value = undefined), 2_000);
    await get(projectId.value);
  } else {
    editFeedbackIcon.value = mdiClose;
    setTimeout(() => (editFeedbackIcon.value = undefined), 5_000);
  }
  loadingEdits.value = false;
};
</script>

<template>
  <base-component :back-button-link="{ name: 'projects' }">
    <v-sheet :min-height="200" class="sheet">
      <template v-if="questionnaire">
        <h1>{{ questionnaire.title }}</h1>
        <p>{{ questionnaire.description }}</p>
        <br />
        <v-tabs v-model="tab" color="primary">
          <v-tab value="single-project" text="Datenelemente" />
          <v-tab v-if="versions?.length" value="project-version-index" text="Versionen" />
          <v-tab value="project-value-sets" text="Codequellen" />
        </v-tabs>
        <v-tabs-window v-model="tab">
          <v-tabs-window-item value="single-project">
            <h2>Datenelemente</h2>
            <v-text-field v-model="deSearch" label="Suche"></v-text-field>
            <v-row gap="8px">
              <v-btn v-if="annotatable" @click="annotateAll" color="primary">
                {{ elements.length }} Datenelemente annotieren
              </v-btn>
              <!-- TODO implement -->
              <v-btn v-if="correctable && false" @click="correctAll" color="primary">
                {{ toCorrect.length }} Annotationen korrigieren
              </v-btn>
              <v-btn v-if="isAdmin" @click="createVersion" color="primary" variant="outlined">
                Version erstellen
              </v-btn>
            </v-row>
            <data-element-list
              v-model="selected"
              :project-id="projectId"
              :project-title="questionnaire.title"
              :data-elements="elements"
              :my-roles="myRoles"
              :allow-edit-selection="false"
              :highlight-unnanotated="false"
              :annotated-crf="false"
              show-actions
            />
          </v-tabs-window-item>
          <v-tabs-window-item value="project-version-index">
            <h2>Versionen</h2>
            <v-list>
              <v-list-item
                v-for="(key, i) in versions"
                :key="key"
                :title="key"
                :class="i % 2 === 0 ? 'bg-grey-lighten-3' : undefined"
              >
                <template #append>
                  <v-icon-btn
                    :icon="mdiArrowRight"
                    @click="
                      router.push({
                        name: 'single-version',
                        params: { pid: projectId, title: projectTitle, key },
                      })
                    "
                  />
                </template>
              </v-list-item>
            </v-list>
          </v-tabs-window-item>
          <v-tabs-window-item value="project-value-sets">
            <h2>Projektinformationen</h2>
            <v-form :disabled="loadingEdits" @submit.prevent="saveEdits">
              <h3>Codequellen</h3>
              <div v-if="!isAdmin">
                <template v-for="(group, key) in valueSetsByServer" :key="key">
                  <v-list-subheader>Server {{ key }}</v-list-subheader>
                  <v-list-item
                    v-for="valueSet in group"
                    :key="`${valueSet.url}|${valueSet.version}`"
                  >
                    <v-list-item-title>{{ valueSet.display }}</v-list-item-title>
                    <v-list-item-subtitle>{{ valueSet.url }}</v-list-item-subtitle>
                  </v-list-item>
                </template>
              </div>
              <div v-else>
                <value-set-selector
                  v-for="(row, i) in valueSetEdit"
                  :key="row.id"
                  v-model="row.set"
                  :allowed-servers="questionnaire.allowedServers ?? []"
                  :disabled="false"
                  :display-controls="valueSetEdit.length > 1"
                  @remove="valueSetEdit.splice(i, 1)"
                />
                <v-btn
                  variant="text"
                  :disabled="loading"
                  :prepend-icon="mdiPlus"
                  @click="valueSetEdit.push({ id: valueSetKey++, set: undefined })"
                >
                  Hinzufügen
                </v-btn>
              </div>
              <!--
            <h3>Akzeptanzbedingungen</h3>
            TODO
            -->
              <v-btn
                v-if="isAdmin"
                :disabled="!!editFeedbackIcon || loadingEdits"
                :prepend-icon="editFeedbackIcon"
                color="primary"
                type="submit"
                block
              >
                Änderungen abschicken
              </v-btn>
            </v-form>
          </v-tabs-window-item>
        </v-tabs-window>
      </template>
      <h3 v-else-if="loading">Lädt &hellip;</h3>
      <template v-else>
        <h3>Fehler</h3>
        <p>Fehler beim Abrufen des Projekts.</p>
      </template>
    </v-sheet>
  </base-component>
</template>

<style scoped>
.sheet {
  padding-left: 20%;
  padding-right: 20%;
}
.participant-row {
  display: grid;
  grid-template-columns: 1fr 2fr auto;
}
</style>
