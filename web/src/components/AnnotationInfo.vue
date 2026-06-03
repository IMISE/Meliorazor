<script setup lang="ts">
import { Status } from '@/types/enums';
import type { Annotation, CodeInfo, FhirValueSetDefinition } from '@/types/types';
import { findCode } from '@/utils/fhirApi';
import type { Ref } from 'vue';
import { ref } from 'vue';
import { computed } from 'vue';

const { annotation, valueSets } = defineProps<{
  annotation: Annotation,
  valueSets: FhirValueSetDefinition[],
}>();

const statusText = computed(() => {
  switch (annotation.status) {
    case Status.OPEN: return "Offen";
    case Status.ACCEPTED: return "Akzeptiert";
    case Status.REJECTED: return "Abgelehnt";
  }
});

const code: Ref<CodeInfo | undefined> = ref(undefined);
const server: Ref<string | undefined> = ref(undefined);
const loading = ref(false);

const getAnnotatedCode = async () => {
  if (loading.value) return;
  if (!code.value) {
    loading.value = true;
    const result = await findCode(annotation.code.code, annotation.code.system, valueSets.map((set) => set.server));
    code.value = result.code;
    server.value = result.server;
    loading.value = false;
  }
}
</script>

<template>
  <v-row cols="12">
    <v-col :cols="code ? 6 : 12">
      <ul>
        <li>Status: {{ statusText }}</li>
        <li>Erstellt von: {{ annotation.authorName }}</li>
        <li><v-btn variant="text" @click="getAnnotatedCode">{{ annotation.code.display }}</v-btn></li>
      </ul>
      <template v-if="annotation.comments?.length">
        <comment v-for="comment in annotation.comments" :comment="comment" />
      </template>
      <template v-else>
        <i>Keine Kommentare vorhanden.</i>
      </template>
    </v-col>
    <v-col v-if="code" cols="6">
      <code-display :code-info="code" :server="server" />
    </v-col>
  </v-row>
</template>
