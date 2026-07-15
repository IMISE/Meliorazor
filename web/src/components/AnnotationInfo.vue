<script setup lang="ts">
import CodeDisplay from "./CodeDisplay.vue";
import type { Annotatable, Annotation, CodeInfo } from "@/types/types";
import { findCode } from "@/utils/fhirApi";
import { watch, ref, type Ref } from "vue";
import SingleComment from "./SingleComment.vue";
import type { FhirValueSetDefinition } from "@/types/fhir.js";
import StatusIcon from "./StatusIcon.vue";
import CodeAnnotator from "./CodeAnnotator.vue";

const props = defineProps<{
  annotatedElement: Annotatable;
  annotation: Annotation;
  valueSets: FhirValueSetDefinition[];
}>();

const code: Ref<CodeInfo | undefined> = ref();
const server: Ref<string | undefined> = ref();
const loading = ref(false);

const annotatable: Ref<Annotatable | undefined> = ref();
watch(props, (newVal) => (annotatable.value = newVal.annotatedElement), { immediate: true });

const getAnnotatedCode = async () => {
  if (loading.value) return;
  if (!code.value) {
    loading.value = true;
    const result = await findCode(
      props.annotation.code.code,
      props.annotation.code.system,
      props.annotation.code.systemVersion,
      props.valueSets.map((set) => set.server),
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
      <code-annotator
        v-model="annotatable"
        :data-element="undefined"
        :code="annotation.code"
        :editable="false"
        :initial-comment="annotation.comment"
        :initial-confidence="annotation.confidence"
        :initial-predicate="annotation.predicate"
        @edit="getAnnotatedCode"
      />
      <ul>
        <li>Status: <status-icon :status="annotation.status" include-text /></li>
        <li>Erstellt von: {{ annotation.authorName }}</li>
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
