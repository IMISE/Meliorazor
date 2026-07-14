<script setup lang="ts">
import type { Annotation, MultiAuthorAnnotation, Variable } from "@/types/types";
import { computed, ref } from "vue";
import { Role, status, Status } from "@/types/enums";
import AnnotationBadge from "./AnnotationBadge.vue";
import { useRouter } from "vue-router";
import { clean } from "@/utils/utils";
import { mdiMagnify, mdiPencilPlus } from "@mdi/js";
import { fieldTypesNotDisplayedWithoutChoices, supportedFieldTypes } from "@/types/redCap.ts";
import { codeEquals } from "@/utils/fhirUtils.ts";

const model = defineModel<number[] | undefined>();

const {
  dataElement: data,
  projectId,
  projectTitle,
} = defineProps<{
  projectId: number;
  projectTitle: string;
  dataElement: Variable;
  myRoles: Role[];
  allowEditSelection: boolean;
  showActions: boolean;
}>();

// const user = inject("user") as Ref<IdTokenClaims | undefined>;
const router = useRouter();

const detailsOpenAnnotations = ref(false);
const detailsAcceptedAnnotations = ref(false);
const selectedAcceptedAnnotations = computed(
  () =>
    acceptedAnnotations.value.filter((annotation) => model.value?.includes(annotation.id)) ?? [],
);
const selectedOpenAnnotations = computed(
  () => openAnnotations.value.filter((annotation) => model.value?.includes(annotation.id)) ?? [],
);

const annotations = computed(() =>
  data.annotations.reduce(
    (acc, val) => (acc[(val.status ?? Status.OPEN) as string]!.push(val), acc),
    status.reduce(
      (obj, status) => ((obj[status] = []), obj),
      {} as { [index: string]: Annotation[] },
    ),
  ),
);

/** Merge equal annotations, so that multiple equal annotations from possibly different people are displayed separately */
const mergeEqualAnnotations = (annotations: Annotation[]) => {
  return annotations.reduce((acc, val) => {
    const i = acc.findIndex(
      (group) => codeEquals(group.code, val.code) && group.predicate === val.predicate,
    );
    if (i < 0) {
      const multiAuthor: MultiAuthorAnnotation = {
        ...val,
        authorNames: [val.authorName],
        authorNameToId: {},
      };
      multiAuthor.authorNameToId[val.authorName] = val.id;
      acc.push(multiAuthor);
    } else {
      acc[i]!.authorNames.push(val.authorName);
      acc[i]!.authorNameToId[val.authorName] = val.id;
    }
    return acc;
  }, [] as MultiAuthorAnnotation[]);
};

const groupAnnotationsByCodeSystem = (annotations: Annotation[]) => {
  const groups = annotations.reduce(
    (acc, val) =>
      acc[val.code.system]
        ? (acc[val.code.system]!.push(val), acc)
        : ((acc[val.code.system] = [val]), acc),
    {} as { [index: string]: Annotation[] },
  );
  const mergedGroups = {} as { [index: string]: MultiAuthorAnnotation[] };
  for (const key in groups) {
    mergedGroups[key] = mergeEqualAnnotations(groups[key]!);
  }
  return mergedGroups;
};

const acceptedAnnotations = computed(() => annotations.value[Status.ACCEPTED as string]!);
const openAnnotations = computed(() => annotations.value[Status.OPEN as string]!);
const groupedAcceptedAnnotations = computed(() =>
  groupAnnotationsByCodeSystem(acceptedAnnotations.value),
);
const groupedOpenAnnotations = computed(() => groupAnnotationsByCodeSystem(openAnnotations.value));

const tooltipSelected = (count: number) =>
  model.value?.length ? `; davon ${count > 1 ? "sind" : "ist"} ${count} ausgewählt` : "";
const acceptedTooltip = computed(
  () =>
    `${acceptedAnnotations.value.length} akzeptierte  Annotationen${tooltipSelected(
      selectedAcceptedAnnotations.value.length,
    )}`,
);
const openTooltip = computed(
  () =>
    `${openAnnotations.value.length} offene, vorgeschlagene Annotationen${tooltipSelected(
      selectedOpenAnnotations.value.length,
    )}`,
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

/** stuff for form view */
const fieldChoices = computed(() => data.choices?.map((c) => c.name) ?? []);
const required = computed(
  () => data.metadata.find((m) => m.key === "Required Field")?.value === "y",
);
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
              <template v-if="modelValue?.length && acceptedAnnotations.length"
                >{{ selectedAcceptedAnnotations.length }}/</template
              >{{ acceptedAnnotations.length ? acceptedAnnotations.length : "-" }}
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
              <template v-if="modelValue?.length && openAnnotations.length"
                >{{ selectedOpenAnnotations.length }}/</template
              >{{ openAnnotations.length ? openAnnotations.length : "-" }}
            </div>
          </template>
        </v-tooltip>
      </div>
      <v-divider vertical />
      <div
        :style="
          'grid-column: 3 / span ' +
          (supportedFieldTypes.includes(dataElement.type ?? '') &&
          (!fieldTypesNotDisplayedWithoutChoices.includes(dataElement.type ?? '') ||
            fieldChoices.length)
            ? 1
            : 2)
        "
      >
        <span class="varlabel">{{ dataElement.label }}</span>
        <br />
        <var v-if="dataElement.variableName" class="varname">{{ dataElement.variableName }}</var>
      </div>
      <template v-if="supportedFieldTypes.includes(dataElement.type ?? '')">
        <v-text-field
          v-if="dataElement.type === 'text'"
          style="grid-column: 4 / span 1"
          :label="dataElement.label"
          :hint="dataElement.metadata.find((e) => e.key === 'Field Note')?.value"
          readonly
          :required="required"
        />
        <v-select
          v-else-if="dataElement.type === 'dropdown'"
          v-model="fieldChoices"
          :items="fieldChoices"
          chips
          multiple
          readonly
          :required="required"
        />
        <v-radio-group v-else-if="dataElement.type === 'radio'" readonly :required="required">
          <v-radio v-for="choice in fieldChoices" :key="choice" :label="choice" readonly />
        </v-radio-group>
        <v-slider
          v-else-if="dataElement.type === 'slider'"
          :min="fieldChoices[0]"
          :max="fieldChoices[fieldChoices.length - 1]"
          step="1"
          :ticks="fieldChoices"
          show-ticks="always"
          tick-size="4"
          readonly
          :required="required"
        />
        <v-file-input
          v-else-if="dataElement.type === 'file'"
          :label="dataElement.label"
          readonly
          :required="required"
        />
      </template>

      <template v-if="showActions">
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
      </template>
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
                  v-model="model"
                  :key="`${dataElement.id}-${annotation.id}`"
                  :annotation="annotation"
                  :data-element-id="dataElement.id!"
                  :data-element-name="dataElement.variableName!"
                  :project-id="projectId"
                  :project-name="projectTitle"
                  :allow-edit-selection="allowEditSelection"
                  display-selection-icon
                  :dashed-border="false"
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
                  v-model="model"
                  :key="`${dataElement.id}-${annotation.id}`"
                  :annotation="annotation"
                  :data-element-id="dataElement.id!"
                  :data-element-name="dataElement.variableName!"
                  :project-id="projectId"
                  :project-name="projectTitle"
                  :allow-edit-selection="allowEditSelection"
                  display-selection-icon
                  dashed-border
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
  width: 100%;
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
  grid-template-columns: auto auto 2fr 2fr auto 1fr;
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
