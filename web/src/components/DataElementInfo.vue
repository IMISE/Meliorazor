<script setup lang="ts">
import type { Annotation, Variable } from "@/types/types";
import { computed, ref } from "vue";
import { Role, status, Status } from "@/types/enums";
import AnnotationBadge from "./AnnotationBadge.vue";
import { useRouter } from "vue-router";
import { clean } from "@/utils/utils";
import { mdiChevronDown, mdiChevronUp, mdiClose, mdiMenuOpen, mdiPencilPlus } from "@mdi/js";
import { fieldTypesNotDisplayedWithoutChoices, supportedFieldTypes } from "@/types/redCap.ts";

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
  highlightUnannotated: boolean;
  annotatedCrf: boolean;
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

const groupAnnotationsByCodeSystem = (annotations: Annotation[]) =>
  annotations.reduce(
    (acc, val) =>
      acc[val.code.system]
        ? (acc[val.code.system]!.push(val), acc)
        : ((acc[val.code.system] = [val]), acc),
    {} as { [index: string]: Annotation[] },
  );

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
const fieldChoices = computed(() => data.choices?.map((c) => c.label) ?? []);
const required = computed(
  () => data.metadata.find((m) => m.key === "Required Field")?.value === "y",
);

const displayMockInput = computed(
  () =>
    supportedFieldTypes.includes(data.fieldType ?? "") &&
    (!fieldTypesNotDisplayedWithoutChoices.includes(data.fieldType ?? "") ||
      fieldChoices.value.length),
);
</script>

<template>
  <div
    :class="highlightUnannotated && dataElement.annotations.length === 0 ? 'bg-error' : 'bg-mute'"
    class="data-element-container"
  >
    <v-row class="data-element-title-row">
      <div v-if="!annotatedCrf" class="annotations-aggregated">
        <v-tooltip :text="acceptedTooltip">
          <template #activator="{ props }">
            <div
              v-if="acceptedAnnotations?.length"
              v-bind="props"
              @click="detailsAcceptedAnnotations = !detailsAcceptedAnnotations"
              style="cursor: pointer"
            >
              <template v-if="modelValue?.length"
                >{{ selectedAcceptedAnnotations.length }}/</template
              >{{ acceptedAnnotations.length }}
              <v-icon :icon="detailsAcceptedAnnotations ? mdiChevronUp : mdiChevronDown" />
            </div>
            <div v-else v-bind="props">-</div>
          </template>
        </v-tooltip>
        <v-tooltip :text="openTooltip">
          <template #activator="{ props }">
            <div
              v-if="openAnnotations.length"
              v-bind="props"
              @click="detailsOpenAnnotations = !detailsOpenAnnotations"
              style="cursor: pointer"
            >
              <template v-if="modelValue?.length">{{ selectedOpenAnnotations.length }}/</template
              >{{ openAnnotations.length }}
              <v-icon :icon="detailsOpenAnnotations ? mdiChevronUp : mdiChevronDown" />
            </div>
            <div v-else v-bind="props">-</div>
          </template>
        </v-tooltip>
      </div>
      <v-divider v-if="!annotatedCrf" vertical />
      <div :style="'grid-column: 3 / span ' + (displayMockInput ? 1 : 2)">
        <span class="varlabel">{{ dataElement.label }}</span>
        <br />
        <var v-if="dataElement.variableName" class="varname">{{ dataElement.variableName }}</var>
      </div>
      <div
        v-if="dataElement.annotations.length && annotatedCrf"
        class="badge-container"
        :style="'grid-row: 2; grid-column: 3 / span' + (displayMockInput ? 1 : 2)"
      >
        <annotation-badge
          v-for="annotation in dataElement.annotations"
          v-model="model"
          :key="`${annotation.id}-${annotation.actions.length}`"
          :annotation="annotation"
          :data-element-id="dataElement.id!"
          :data-element-name="dataElement.variableName!"
          :project-id="projectId"
          :project-name="projectTitle"
          :allow-edit-selection="allowEditSelection"
          :dashed-border="false"
        />
      </div>

      <div v-if="displayMockInput">
        <v-number-input
          v-if="dataElement.fieldType === 'number'"
          style="grid-column: 4 / span 1"
          control-variant="stacked"
          :label="dataElement.label"
          :hint="dataElement.metadata.find((e) => e.key === 'Field Note')?.value"
          readonly
          :min="
            Number(dataElement.metadata.find((pair) => pair.key === 'Text Validation Min')?.value)
          "
          :max="
            Number(dataElement.metadata.find((pair) => pair.key === 'Text Validation Max')?.value)
          "
          :required="required"
        />
        <v-text-field
          v-else-if="dataElement.fieldType === 'text'"
          style="grid-column: 4 / span 1"
          :label="dataElement.label"
          :hint="dataElement.metadata.find((e) => e.key === 'Field Note')?.value"
          readonly
          :required="required"
        />
        <v-radio-group v-else-if="dataElement.fieldType === 'radio'" readonly :required="required">
          <v-radio
            v-for="(choice, i) in fieldChoices"
            :key="choice"
            :label="choice"
            :class="
              highlightUnannotated && data.choices![i]!.annotations.length === 0
                ? 'bg-error'
                : 'bg-mute'
            "
            readonly
          >
            <template v-if="dataElement.choices![i]!.annotations?.length && annotatedCrf" #label>
              {{ dataElement.choices![i]!.label }}
              <div class="badge-container">
                <annotation-badge
                  v-for="annotation in dataElement.choices![i]!.annotations"
                  v-model="model"
                  :key="`${annotation.id}-${annotation.actions.length}`"
                  :annotation="annotation"
                  :data-element-id="dataElement.id!"
                  :data-element-name="dataElement.variableName!"
                  :project-id="projectId"
                  :project-name="projectTitle"
                  :allow-edit-selection="allowEditSelection"
                  :dashed-border="false"
                />
              </div>
            </template>
          </v-radio>
        </v-radio-group>
        <v-slider
          v-else-if="dataElement.fieldType === 'slider'"
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
          v-else-if="dataElement.fieldType === 'file'"
          :label="dataElement.label"
          readonly
          :required="required"
        />
      </div>

      <template v-if="showActions">
        <v-divider vertical />
        <div class="badge-container">
          <v-tooltip text="Öffnen">
            <template #activator="{ props }">
              <v-chip @click="openDataElement" class="data-element-chip" v-bind="props">
                <v-icon :icon="mdiMenuOpen" />
              </v-chip>
            </template>
          </v-tooltip>
          <v-tooltip v-if="myRoles.includes(Role.ANNOTATE)" text="Annotieren">
            <template #activator="{ props }">
              <v-chip @click="annotate" class="data-element-chip" v-bind="props">
                <v-icon :icon="mdiPencilPlus" />
              </v-chip>
            </template>
          </v-tooltip>
        </div>
      </template>
    </v-row>
    <template v-if="acceptedAnnotations.length && detailsAcceptedAnnotations">
      <v-divider />
      <v-row>
        <v-col cols="2">Akzeptierte Annotationen</v-col>
        <v-col cols="9">
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
                  :key="`${annotation.id}-${annotation.actions.length}`"
                  :annotation="annotation"
                  :data-element-id="dataElement.id!"
                  :data-element-name="dataElement.variableName!"
                  :project-id="projectId"
                  :project-name="projectTitle"
                  :allow-edit-selection="allowEditSelection"
                  :dashed-border="false"
                />
              </div>
            </template>
          </template>
        </v-col>
        <v-col cols="1">
          <v-icon-btn
            variant="plain"
            :icon="mdiClose"
            @click="detailsAcceptedAnnotations = false"
          />
        </v-col>
      </v-row>
    </template>
    <template v-if="openAnnotations.length && detailsOpenAnnotations">
      <v-divider />
      <v-row>
        <v-col cols="2">Offene Annotationen</v-col>
        <v-col cols="9">
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
                  :key="`${annotation.id}-${annotation.actions.length}`"
                  :annotation="annotation"
                  :data-element-id="dataElement.id!"
                  :data-element-name="dataElement.variableName!"
                  :project-id="projectId"
                  :project-name="projectTitle"
                  :allow-edit-selection="allowEditSelection"
                  dashed-border
                />
              </div>
            </template>
          </template>
        </v-col>
        <v-col cols="1">
          <v-icon-btn variant="plain" :icon="mdiClose" @click="detailsOpenAnnotations = false" />
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
  margin: 20px;
  padding: 10px;
}
.bg-mute {
  background-color: var(--color-background-mute);
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
