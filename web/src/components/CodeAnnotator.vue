<script setup lang="ts">
import auth from '@/auth';
import type { CodeMapping, Predicate } from '@/types/types';
import getEnv from '@/utils/env';
import { mdiPencil } from '@mdi/js';
import { ref, type Ref } from 'vue';

const { code, dataElementId } = defineProps<{
  code: CodeMapping,
  dataElementId: number,
}>();

defineEmits<{
  (e: "edit"): void,
  (e: "focus", v: CodeMapping): void,
}>();

// creation request
const loadingCreate = ref(false);
const response: Ref<Response | undefined> = ref(undefined);

// SSSOM metadata
const confidence: Ref<number> = ref(100);
const predicate: Ref<Predicate> = ref("skos:exactMatch");
const comment = ref("");
const predicates = ["owl:sameAs", "owl:equivalentClass", "owl:equivalentProperty", "rdfs:subClassOf", "rdfs:subPropertyOf", "skos:relatedMatch", "skos:closeMatch", "skos:exactMatch", "skos:narrowMatch", "skos:broadMatch", "oboInOwl:hasDbXref", "rdfs:seeAlso"];

const submitAnnotation = async () => {
  const u = await auth.getUser();
  if (!u?.access_token) return;

  loadingCreate.value = true;
  try {
    response.value = await fetch(`${getEnv("VITE_API_BASE")}/dataElement/${dataElementId}/annotation`, {
      method: "POST",
      body: JSON.stringify({
        authorName: u.profile?.preferred_username,
        code,
        confidence: confidence.value,
        comment: comment.value,
        predicate: predicate.value,
      }),
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${u.access_token}`,
      },
    });
  } catch (e) {
    console.error(e);
  } finally {
    loadingCreate.value = false;
  }
}

</script>

<template>
  <div class="code-annotator-container">
    <v-list lines="one">
      <v-list-item :key="code.system + code.code">
        <template #title>{{ code.display }}</template>
        <template #subtitle>
          <span color="blue">Ausgewählter Code:</span> {{ code.code }} in {{ code.system }}
        </template>
        <template #append>
          <v-btn :disabled="loadingCreate" :icon="mdiPencil" @click="$emit('edit')" variant="text" />
        </template>
      </v-list-item>
    </v-list>
    <br />
    <!-- form to add further SSSOM metadata -->
    <v-form @submit.prevent="submitAnnotation">
      <v-slider v-model="confidence" :step="1" :min="0" :max="100" label="Konfidenz" thumb-label="hover">
        <template #thumb-label="{ modelValue }">{{ modelValue }}%</template>
      </v-slider>
      <v-select label="Prädikat" v-model="predicate" :items="predicates" />
      <v-textarea v-model="comment" label="Kommentar" />
      <v-btn :disabled="loadingCreate" :loading="loadingCreate" size="x-large" type="submit" block>Erstellen</v-btn>
    </v-form>
  </div>
</template>

<style>
.code-annotator-container {
  max-width: 800px;
}
</style>
