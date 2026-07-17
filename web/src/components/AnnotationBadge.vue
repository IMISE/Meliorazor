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
  dashedBorder: boolean;
}>();

const router = useRouter();

const hint = computed(() => `${props.annotation.code.code} in ${props.annotation.code.system}`);

const go = async () =>
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

const contained = computed(() => model.value?.some((xId) => props.annotation.id === xId));
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
  <div>
    <template v-if="allowEditSelection && modelValue !== undefined">
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
    <v-tooltip :text="hint">
      <template #activator="{ props }">
        <div v-bind="props" @click="go" style="display: inline">
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
          <v-chip
            style="border-radius: 0 20px 20px 0"
            class="bg-info"
            :class="dashedBorder ? 'dashed-border' : undefined"
            aria-label="Code"
            inline
          >
            {{ annotation.code.display }}
          </v-chip>
        </div>
      </template>
    </v-tooltip>
  </div>
</template>

<style scoped>
.dashed-border {
  border: 2px dashed black;
}
</style>
