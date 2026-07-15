<script setup lang="ts">
import type { MultiAuthorAnnotation } from "@/types/types";
import { mdiCheckCircleOutline, mdiCircleOutline } from "@mdi/js";
import { computed, mergeProps } from "vue";
import { useRouter } from "vue-router";

const model = defineModel<number[] | undefined>();

const props = defineProps<{
  annotation: MultiAuthorAnnotation;
  projectId: number;
  projectName: string;
  dataElementId: number;
  dataElementName: string;
  allowEditSelection: boolean;
  dashedBorder: boolean;
}>();

const router = useRouter();

const hint = computed(() => `${props.annotation.code.code} in ${props.annotation.code.system}`);

const goToAnnotation = async (author: string) =>
  router.push({
    name: "focus-annotation",
    params: {
      aid: props.annotation.authors.find((a) => a.authorName === author)!.id,
      subject: props.annotation.code.display,
      eid: props.dataElementId,
      name: props.dataElementName,
      pid: props.projectId,
      title: props.projectName,
    },
  });

const contained = computed(() =>
  model.value?.some((xId) => props.annotation.authors.some((y) => xId === y.id)),
);
const select = () => {
  if (props.allowEditSelection) {
    model.value?.push(...props.annotation.authors.map((annotation) => annotation.id));
  }
};
const unselect = () => {
  if (props.allowEditSelection) {
    props.annotation.authors.forEach((annotation) =>
      model.value?.splice(model.value.indexOf(annotation.id), 1),
    );
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
    <v-menu>
      <template #activator="{ props: menuProps }">
        <v-tooltip :text="hint">
          <template #activator="{ props: tooltipProps }">
            <div
              v-bind="mergeProps(menuProps, tooltipProps)"
              @click="
                () =>
                  annotation.authors.length === 1
                    ? goToAnnotation(annotation.authors[0]!.authorName)
                    : undefined
              "
              style="display: inline"
            >
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
      </template>
      <v-list v-if="annotation.authors.length > 1">
        <v-list-item
          v-for="author of annotation.authors"
          :key="author.authorName"
          :title="author.authorName"
          :subtitle="
            author.comment.length > 20
              ? author.comment.substring(0, 19) + '&hellip;'
              : author.comment
          "
          link
          @click="() => goToAnnotation(author.authorName)"
        >
          <template #append>
            <!-- TODO: color indication? more labels? -->
            <v-chip :text="`${author.confidence}%`" />
          </template>
        </v-list-item>
      </v-list>
    </v-menu>
  </div>
</template>

<style scoped>
.dashed-border {
  border: 2px dashed black;
}
</style>
