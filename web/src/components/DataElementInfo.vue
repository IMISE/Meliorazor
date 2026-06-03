<script setup lang="ts">
import type { Variable } from '@/types/types';
import { computed, inject, type Ref } from 'vue';
import { Role, Status } from '@/types/enums';
import { mdiCheckboxBlankCircleOutline, mdiCheckCircleOutline } from '@mdi/js';
import type { IdTokenClaims } from 'oidc-client-ts';

const { dataElement: data} = defineProps<{
  projectId: number,
  projectTitle: string,
  dataElement: Variable,
  myRoles: Role[],
}>();

const user = inject("user") as Ref<IdTokenClaims | undefined>;

const acceptedAnnotations = computed(() =>
  data.annotations.filter((a) => a.status === Status.ACCEPTED));
const openAnnotations = computed(() =>
  data.annotations.filter((a) => a.status === Status.OPEN));
const clean = (input: string | null) =>
  (input || "undef").toLowerCase()
    .replaceAll("ä", "ae")
    .replaceAll("ö", "oe")
    .replaceAll("ü", "ue")
    .replaceAll("ß", "ss")
    .replace(/[^a-z0-9-]/gi, "_")
    .replace(/_{2,}/g, "_")
    .replace(/(^_|_$)/g, "");

const acceptedTooltip = computed(() =>
  `${acceptedAnnotations.value.length} akzeptierte  Annotationen`);
const openTooltip = computed(() =>
  `${openAnnotations.value.length} offene, vorgeschlagene Annotationen`);

const myAnnotations = computed(() => [...openAnnotations.value, ...acceptedAnnotations.value].filter((a) => a.authorName === user.value?.preferred_username));

const correctableIds = computed(() =>
  (openAnnotations.value.length ? openAnnotations.value : data.annotations).map((a) => a.id).join(",")
);

</script>

<template>
  <v-row :class="myAnnotations.length ? 'mine' : 'unrelated'">
    <v-col cols="10">
      <router-link :to="{ name: 'data-element', params: {
          pid: projectId,
          title: clean(projectTitle),
          eid: dataElement.id,
          name: clean(dataElement.variableName),
      }}" class="variable">
        <v-tooltip v-if="acceptedAnnotations.length" :text="acceptedTooltip">
          <template #activator="{ props }">
            <v-chip :prepend-icon="mdiCheckCircleOutline" color="white" v-bind="props" class="accepted">
              {{ acceptedAnnotations.length }}
            </v-chip>
          </template>
        </v-tooltip>
        <v-tooltip v-if="openAnnotations.length" :text="openTooltip">
          <template #activator="{ props }">
            <v-chip :prepend-icon="mdiCheckboxBlankCircleOutline" v-bind="props" class="open">
              {{ openAnnotations.length }}
            </v-chip>
          </template>
        </v-tooltip>
        <var v-if="dataElement.variableName" class="varname">[{{ dataElement.variableName }}]</var>
        <span>{{ dataElement.label }}</span>
      </router-link>
    </v-col>
    <v-col cols="2">
      <v-btn v-if="myRoles.includes(Role.ANNOTATE)" :href="`/annotate/${projectId}-${dataElement.id}`">Annotieren</v-btn>
      <v-btn v-if="myRoles.includes(Role.FEEDBACK) && data.annotations.length" :href="`/annotations/${projectId}-${correctableIds}`">
        <span v-if="openAnnotations.length">Korrigieren</span>
        <i v-else>Überprüfen</i>
      </v-btn>
    </v-col>
  </v-row>
</template>

<style lang="css" scoped>
.accepted {
  background-color: #028A0F;
}
.mine {
  background-color: yellow;
}
.variable {
  display: block;
  margin: 20px;
  padding: 10px;
  font-size: 16pt;
  background-color: var(--color-background-mute);
}
.varname {
  display: inline-block;
  padding-right: 10px;
}
</style>
