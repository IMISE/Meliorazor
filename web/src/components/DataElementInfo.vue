<script setup lang="ts">
import type { Annotation, Variable } from "@/types/types";
import { computed, ref, watch, type Ref } from "vue";
import { Role, status, Status } from "@/types/enums";
import AnnotationBadge from "./AnnotationBadge.vue";
import { useRouter } from "vue-router";
import { clean } from "@/utils/utils";
import { mdiMagnify, mdiPencilPlus } from "@mdi/js";

const model = defineModel<number[] | undefined>();

const selected: Ref<number[] | undefined> = ref(model === undefined ? undefined : []);
watch(selected, (newVal, oldVal) => {
  if (!model.value) return;
  const _new = newVal ?? [];
  const _old = oldVal ?? [];
  const created = _new.filter((el) => !_old.includes(el));
  const removed = _old.filter((el) => !_new.includes(el));

  model.value = model.value.filter((el) => removed.includes(el));
  model.value.push(...created);
});

const {
  dataElement: data,
  projectId,
  projectTitle,
} = defineProps<{
  projectId: number;
  projectTitle: string;
  dataElement: Variable;
  myRoles: Role[];
}>();

// const user = inject("user") as Ref<IdTokenClaims | undefined>;
const router = useRouter();

const detailsOpenAnnotations = ref(false);
const detailsAcceptedAnnotations = ref(false);

const annotations = computed(() =>
  data.annotations.reduce(
    (acc, val) => (acc[(val.status ?? Status.OPEN) as string]!.push(val), acc),
    status.reduce(
      (obj, status) => ((obj[status] = []), obj),
      {} as { [index: string]: Annotation[] },
    ),
  ),
);

const groupAnnotationsByCodeSystem = (annotations: Annotation[]) => {
  return annotations.reduce(
    (acc, val) =>
      acc[val.code.system]
        ? (acc[val.code.system]!.push(val), acc)
        : ((acc[val.code.system] = [val]), acc),
    {} as { [index: string]: Annotation[] },
  );
};

const acceptedAnnotations = computed(() => annotations.value[Status.ACCEPTED as string]!);
const openAnnotations = computed(() => annotations.value[Status.OPEN as string]!);
const groupedAcceptedAnnotations = computed(() =>
  groupAnnotationsByCodeSystem(acceptedAnnotations.value),
);
const groupedOpenAnnotations = computed(() => groupAnnotationsByCodeSystem(openAnnotations.value));

const acceptedTooltip = computed(
  () => `${acceptedAnnotations.value.length} akzeptierte  Annotationen`,
);
const openTooltip = computed(
  () => `${openAnnotations.value.length} offene, vorgeschlagene Annotationen`,
);

/*
const myAnnotations = computed(() =>
  [...openAnnotations.value, ...acceptedAnnotations.value].filter(
    (a) => a.authorName === user.value?.preferred_username,
  ),
);
*/

/*
const correctableIds = computed(() =>
  (openAnnotations.value.length ? openAnnotations.value : data.annotations)
    .map((a) => a.id)
    .join(","),
);
*/

const openDataElement = () =>
  router.push({
    name: "data-element",
    params: {
      pid: projectId,
      title: clean(projectTitle),
      eid: data.id,
      name: clean(data.variableName),
    },
  });
const annotate = () =>
  router.push({
    name: "create-annotation",
    params: {
      projectId: projectId,
      elementIds: data.id,
    },
  });
</script>

<template>
  <div class="data-element-container">
    <v-row class="data-element-title-row">
      <div class="annotations-aggregated">
        <v-tooltip :text="acceptedTooltip">
          <template #activator="{ props }">
            <div
              v-bind="props"
              @click="detailsAcceptedAnnotations = !detailsAcceptedAnnotations"
              class="accepted-annotations-aggregated"
            >
              {{ acceptedAnnotations.length ? acceptedAnnotations.length : "-" }}
            </div>
          </template>
        </v-tooltip>
        <v-tooltip :text="openTooltip">
          <template #activator="{ props }">
            <div
              v-bind="props"
              @click="detailsOpenAnnotations = !detailsOpenAnnotations"
              class="open-annotations-aggregated"
            >
              {{ openAnnotations.length ? openAnnotations.length : "-" }}
            </div>
          </template>
        </v-tooltip>
      </div>
      <v-divider vertical />
      <div>
        <span class="varlabel">{{ dataElement.label }}</span>
        <br />
        <var v-if="dataElement.variableName" class="varname">{{ dataElement.variableName }}</var>
      </div>
      <v-divider vertical />
      <div class="badge-container">
        <v-tooltip text="Öffnen">
          <template #activator="{ props }">
            <v-chip
              @click="openDataElement"
              :prepend-icon="mdiMagnify"
              class="data-element-chip"
              v-bind="props"
            ></v-chip>
          </template>
        </v-tooltip>
        <v-tooltip v-if="myRoles.includes(Role.ANNOTATE)" text="Annotieren">
          <template #activator="{ props }">
            <v-chip
              @click="annotate"
              :prepend-icon="mdiPencilPlus"
              class="data-element-chip"
              v-bind="props"
            ></v-chip>
          </template>
        </v-tooltip>
      </div>
    </v-row>
    <template v-if="acceptedAnnotations.length && detailsAcceptedAnnotations">
      <v-divider />
      <v-row>
        <v-col cols="2">Akzeptierte Annotationen</v-col>
        <v-col cols="10">
          <template v-if="acceptedAnnotations.length">
            <template
              v-for="(group, system) in groupedAcceptedAnnotations"
              :key="`${dataElement.id}-a${system}`"
            >
              {{ system }}:
              <div class="badge-container">
                <annotation-badge
                  v-for="annotation in group"
                  v-model="selected"
                  :key="`${dataElement.id}-${annotation.id}`"
                  :annotation="annotation"
                  :data-element-id="dataElement.id!"
                  :data-element-name="dataElement.variableName!"
                  :project-id="projectId"
                  :project-name="projectTitle"
                />
              </div>
            </template>
          </template>
        </v-col>
      </v-row>
    </template>
    <template v-if="openAnnotations.length && detailsOpenAnnotations">
      <v-divider />
      <v-row>
        <v-col cols="2">Offene Annotationen</v-col>
        <v-col>
          <template v-if="openAnnotations.length">
            <template
              v-for="(group, system) in groupedOpenAnnotations"
              :key="`${dataElement.id}-o${system}`"
            >
              {{ system }}:
              <div class="badge-container">
                <annotation-badge
                  v-for="annotation in group"
                  v-model="selected"
                  :key="`${dataElement.id}-${annotation.id}`"
                  :annotation="annotation"
                  :data-element-id="dataElement.id!"
                  :data-element-name="dataElement.variableName!"
                  :project-id="projectId"
                  :project-name="projectTitle"
                />
              </div>
            </template>
          </template>
        </v-col>
      </v-row>
    </template>
  </div>
</template>

<style lang="css" scoped>
.varlabel {
  font-size: large;
}
.varname {
  display: inline-block;
  font-size: small;
}
.data-element-container {
  display: block;
  background-color: var(--color-background-mute);
  margin: 20px;
  padding: 10px;
}
.data-element-main {
  font-size: 16pt;
}
.data-element-title-row {
  display: grid;
  /** open&accepted annotations; divider; name; divider; actions */
  grid-template-columns: auto auto 4fr auto 1fr;
  grid-template-rows: auto;
  gap: 10px;
}
.badge-container {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin: 5px;
}
.annotations-aggregated {
  display: grid;
  grid-template-columns: 1fr;
  grid-template-rows: 1fr 1fr;
}
</style>
