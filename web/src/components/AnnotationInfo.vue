<script setup lang="ts">
import CodeDisplay from "./CodeDisplay.vue";
import { Status } from "@/types/enums";
import type { Annotation, CodeInfo, FhirValueSetDefinition } from "@/types/types";
import { findCode } from "@/utils/fhirApi";
import { ref, type Ref } from "vue";
import { computed } from "vue";
import SingleComment from "./SingleComment.vue";

const { annotation, valueSets } = defineProps<{
  annotation: Annotation;
  valueSets: FhirValueSetDefinition[];
}>();

const statusText = computed(() => {
  switch (annotation.status) {
    case Status.OPEN:
      return "Offen";
    case Status.ACCEPTED:
      return "Akzeptiert";
    case Status.REJECTED:
    default:
      return "Abgelehnt";
  }
});

const code: Ref<CodeInfo | undefined> = ref(undefined);
const server: Ref<string | undefined> = ref(undefined);
const loading = ref(false);

const getAnnotatedCode = async () => {
  if (loading.value) return;
  if (!code.value) {
    loading.value = true;
    const result = await findCode(
      annotation.code.code,
      annotation.code.system,
      valueSets.map((set) => set.server),
    );
    code.value = result.code;
    server.value = result.server;
    loading.value = false;
  }
};
</script>

<template>
  <v-row cols="12">
    <v-col :cols="code ? 6 : 12">
      <ul>
        <li>Status: {{ statusText }}</li>
        <li>Erstellt von: {{ annotation.authorName }}</li>
        <li>Konfidenz: {{ annotation.confidence }}</li>
        <li>
          {{ annotation.predicate }}
          <v-btn v-if="valueSets?.length" variant="text" @click="getAnnotatedCode">
            {{ annotation.code.display }}
          </v-btn>
          <template v-else>
            {{ annotation.code.display }}
            ({{ annotation.code.code }} in {{ annotation.code.system }})
          </template>
        </li>
        <li v-if="annotation.comment">Kommentar bei Erstellung: {{ annotation.comment }}</li>
      </ul>
      <br />
      <h3>Diskussion</h3>
      <template v-if="annotation.comments?.length">
        <single-comment
          v-for="comment in annotation.comments"
          :key="comment.id"
          :comment="comment"
        />
      </template>
      <template v-else>
        <i>Keine Kommentare vorhanden.</i>
      </template>
    </v-col>
    <v-col v-if="code && server" cols="6">
      <code-display :code-info="code" :server="server" :show-select-button="false" />
    </v-col>
  </v-row>
</template>
