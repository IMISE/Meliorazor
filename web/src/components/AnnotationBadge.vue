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
  displaySelectionIcon: boolean;
  dashedBorder: boolean;
}>();

const router = useRouter();

const hint = computed(() => `${props.annotation.code.code} in ${props.annotation.code.system}`);

const goToAnnotation = async (author: string) =>
  router.push({
    name: "focus-annotation",
    params: {
      aid: props.annotation.authorNameToId[author],
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
    model.value?.push(...Object.values(props.annotation.authorNameToId));
  }
};
const unselect = () => {
  if (props.allowEditSelection) {
    Object.values(props.annotation.authorNameToId).forEach((id) =>
      model.value?.splice(model.value.indexOf(id), 1),
    );
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
        <v-menu>
          <template #activator="{ props: menuProps }">
            <v-tooltip :text="hint">
              <template #activator="{ props: tooltipProps }">
                <div
                  v-bind="mergeProps(menuProps, tooltipProps)"
                  @click="
                    () =>
                      annotation.authorNames.length === 1
                        ? goToAnnotation(annotation.authorNames[0]!)
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
          <v-list v-if="annotation.authorNames.length > 1">
            <v-list-item
              v-for="(author, i) of annotation.authorNames"
              :key="author"
              :title="author"
              :subtitle="`Autor ${i + 1}`"
              link
              @click="() => goToAnnotation(author)"
            />
          </v-list>
        </v-menu>
      </div>
    </template>
  </v-hover>
</template>

<style scoped>
.dashed-border {
  border: 2px dashed black;
}
</style>
