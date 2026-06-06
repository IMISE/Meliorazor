<script setup lang="ts">
import type { Annotation } from '@/types/types';
import { computed } from 'vue';
import { useRouter } from 'vue-router';

const props = defineProps<{
  annotation: Annotation,
  projectId: number,
  projectName: string,
  dataElementId: number,
  dataElementName: string,
}>();

const router = useRouter();

const hint = computed(() => `${props.annotation.code.code} in ${props.annotation.code.system}`);

const goToAnnotation = async () => router.push({
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
</script>

<template>
  <div>
    <v-chip style="border-radius: 20px 0 0 20px; background-color: darkcyan" inline>
      {{ annotation.predicate }}
    </v-chip>
    <v-divider vertical color="white" :thickness="2" />
    <v-tooltip :text="hint">
      <template #activator="{ props }">
          <v-chip v-bind="props" @click="goToAnnotation" style="border-radius: 0 20px 20px 0; background-color: darkcyan" inline>
            {{ annotation.code.display }}
          </v-chip>
      </template>
    </v-tooltip>
  </div>
</template>
