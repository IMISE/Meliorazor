<script setup lang="ts">
import type { Annotation, Variable } from '@/types/types';
import { computed, inject, ref, type Ref } from 'vue';
import { Role, roles, status, Status } from '@/types/enums';
import type { IdTokenClaims } from 'oidc-client-ts';
import AnnotationBadge from './AnnotationBadge.vue';
import { useRouter } from 'vue-router';
import { clean } from '@/utils/utils';
import { mdiCheckboxBlankCircleOutline, mdiCheckCircleOutline, mdiMagnify, mdiPlus } from '@mdi/js';

const { dataElement: data, projectId, projectTitle } = defineProps<{
  projectId: number,
  projectTitle: string,
  dataElement: Variable,
  myRoles: Role[],
}>();

const user = inject("user") as Ref<IdTokenClaims | undefined>;
const router = useRouter();

const detailsOpenAnnotations = ref(false);
const detailsAcceptedAnnotations = ref(false);

const annotations = computed(() => data.annotations.reduce((acc, val) =>
  (acc[(val.status ?? Status.OPEN) as string]!.push(val), acc),
  status.reduce((obj, status) => (obj[status] = [], obj), {} as { [index: string]: Annotation[] })
));

const acceptedAnnotations = computed(() => annotations.value[Status.ACCEPTED as string]!);
const openAnnotations = computed(() => annotations.value[Status.OPEN as string]!);

const acceptedTooltip = computed(() =>
  `${acceptedAnnotations.value.length} akzeptierte  Annotationen`);
const openTooltip = computed(() =>
  `${openAnnotations.value.length} offene, vorgeschlagene Annotationen`);

const myAnnotations = computed(() => [...openAnnotations.value, ...acceptedAnnotations.value].filter((a) => a.authorName === user.value?.preferred_username));

const correctableIds = computed(() =>
  (openAnnotations.value.length ? openAnnotations.value : data.annotations).map((a) => a.id).join(",")
);

const openDataElement = () => router.push({
    name: "data-element",
    params: {
      pid: projectId,
      title: clean(projectTitle),
      eid: data.id,
      name: clean(data.variableName),
  },
});
const annotate = () => router.push({
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
      <div class="data-element-main">
        <var v-if="dataElement.variableName" class="varname">[{{ dataElement.variableName }}]</var>
        <span>{{ dataElement.label }}</span>
      </div>
      <v-tooltip v-if="acceptedAnnotations.length && !detailsAcceptedAnnotations" :text="acceptedTooltip">
        <template #activator="{ props }">
          <v-chip
            @click="detailsAcceptedAnnotations = true"
            :prepend-icon="mdiCheckCircleOutline"
            color="white"
            class="data-element-chip"
            v-bind="props"
          >
            {{ acceptedAnnotations.length }}
          </v-chip>
        </template>
      </v-tooltip>
      <v-tooltip v-if="openAnnotations.length && !detailsOpenAnnotations" :text="openTooltip">
        <template #activator="{ props }">
          <v-chip
            @click="detailsOpenAnnotations = true"
            :prepend-icon="mdiCheckboxBlankCircleOutline"
            color="white"
            class="data-element-chip"
            v-bind="props"
          >
            {{ openAnnotations.length }}
          </v-chip>
        </template>
      </v-tooltip>
      <v-tooltip text="Öffnen">
        <template #activator="{ props }">
          <v-chip
            @click="openDataElement"
            :prepend-icon="mdiMagnify"
            class="data-element-chip"
            color="white"
            v-bind="props"
          ></v-chip>
        </template>
      </v-tooltip>
      <v-tooltip v-if="myRoles.includes(Role.ANNOTATE)" text="Annotieren">
        <template #activator="{ props }">
          <v-chip
            @click="annotate"
            :prepend-icon="mdiPlus"
            class="data-element-chip"
            color="white"
            v-bind="props"
          ></v-chip>
        </template>
      </v-tooltip>
    </v-row>
    <template v-if="acceptedAnnotations.length && detailsAcceptedAnnotations">
      <v-divider />
      <v-row>
        <v-col cols="2">Akzeptierte Annotationen</v-col>
        <v-col cols="10">
          <template v-if="acceptedAnnotations.length">
            <div class="annotation-badge-container">
              <annotation-badge
                v-for="annotation in acceptedAnnotations"
                :annotation="annotation"
                :data-element-id="dataElement.id!"
                :data-element-name="dataElement.variableName!"
                :project-id="projectId"
                :project-name="projectTitle"
              />
            </div>
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
            <div class="annotation-badge-container">
              <annotation-badge
                v-for="(annotation, i) in openAnnotations"
                :annotation="annotation"
                :data-element-id="dataElement.id!"
                :data-element-name="dataElement.variableName!"
                :project-id="projectId"
                :project-name="projectTitle"
              />
            </div>
          </template>
        </v-col>
      </v-row>
    </template>
  </div>
</template>

<style lang="css" scoped>
.varname {
  display: inline-block;
  padding-right: 10px;
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
  display: flex;
  gap: 10px;
}
.annotation-badge-container {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin: 5px;
}
</style>
