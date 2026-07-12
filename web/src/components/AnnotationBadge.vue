<script setup lang="ts">
import type { Annotation } from "@/types/types";
import { mdiCheckCircleOutline, mdiCircleOutline } from "@mdi/js";
import { computed } from "vue";
import { useRouter } from "vue-router";

const model = defineModel<number[] | undefined>();

const props = defineProps<{
  annotation: Annotation;
  projectId: number;
  projectName: string;
  dataElementId: number;
  dataElementName: string;
  allowEditSelection: boolean;
  displaySelectionIcon: boolean;
  dashedBorder: boolean;
}>();

const router = useRouter();

const hint = computed(() => `${props.annotation.code.code} in ${props.annotation.code.system}`);

const goToAnnotation = async () =>
  router.push({
    name: "focus-annotation",
    params: {
      aid: props.annotation.id,
      subject: props.annotation.code.display,
      eid: props.dataElementId,
      name: props.dataElementName,
      pid: props.projectId,
      title: props.projectName,
    },
  });

const contained = computed(() => model.value?.includes(props.annotation?.id));
const select = () => {
  if (props.allowEditSelection) {
    model.value?.push(props.annotation.id);
  }
};
const unselect = () => {
  if (props.allowEditSelection) {
    model.value?.splice(model.value.indexOf(props.annotation.id), 1);
  }
};
</script>

<template>
  <v-hover>
    <template #default="{ isHovering, props: hoverProps }">
      <div v-bind="hoverProps">
        <template
          v-if="
            displaySelectionIcon &&
            (isHovering || contained || !allowEditSelection) &&
            modelValue !== undefined
          "
        >
          <v-icon
            v-if="contained"
            :icon="mdiCheckCircleOutline"
            @click="unselect"
            :key="modelValue?.join(',') + '|on'"
          />
          <v-icon
            v-else
            :icon="mdiCircleOutline"
            @click="select"
            :key="modelValue?.join(',') + '|off'"
          />
        </template>
        <v-chip
          style="border-radius: 20px 0 0 20px"
          class="bg-info"
          :class="dashedBorder ? 'dashed-border' : undefined"
          aria-label="Prädikat"
          inline
        >
          {{ annotation.predicate }}
        </v-chip>
        <v-divider vertical color="white" :thickness="2" />
        <v-tooltip :text="hint">
          <template #activator="{ props: tooltipProps }">
            <v-chip
              v-bind="tooltipProps"
              @click="goToAnnotation"
              style="border-radius: 0 20px 20px 0"
              class="bg-info"
              :class="dashedBorder ? 'dashed-border' : undefined"
              aria-label="Code"
              inline
            >
              {{ annotation.code.display }}
            </v-chip>
          </template>
        </v-tooltip>
      </div>
    </template>
  </v-hover>
</template>

<style scoped>
.dashed-border {
  border: 2px dashed black;
}
</style>
