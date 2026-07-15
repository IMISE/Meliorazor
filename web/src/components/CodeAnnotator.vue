<script setup lang="ts">
import auth from "@/auth";
import type { Annotatable, Choice, CodeMapping, Predicate, Variable } from "@/types/types";
import { createAnnotation } from "@/utils/api";
import { mdiCheck, mdiClose, mdiPencil } from "@mdi/js";
import { computed } from "vue";
import { ref, type Ref } from "vue";
import type { IconValue } from "vuetify/lib/composables/icons.mjs";

const annotatable = defineModel<Annotatable>();
const { code, dataElement } = defineProps<{
  code: CodeMapping;
  dataElement: Variable;
}>();

const emit = defineEmits<{
  (e: "success"): void;
  (e: "edit"): void;
  (e: "focus", v: CodeMapping): void;
}>();

// creation request
const loadingCreate = ref(false);
const loadingFeedbackIcon: Ref<IconValue | undefined> = ref();

// SSSOM metadata
const confidence: Ref<number> = ref(100);
const predicate: Ref<Predicate> = ref("skos:exactMatch");
const comment = ref("");
const predicates = [
  "owl:sameAs",
  "owl:equivalentClass",
  "owl:equivalentProperty",
  "rdfs:subClassOf",
  "rdfs:subPropertyOf",
  "skos:relatedMatch",
  "skos:closeMatch",
  "skos:exactMatch",
  "skos:narrowMatch",
  "skos:broadMatch",
  "oboInOwl:hasDbXref",
  "rdfs:seeAlso",
];

const allAnnotatables = computed(() => [dataElement, ...(dataElement.choices ?? [])]);

const submitAnnotation = async () => {
  const u = await auth.getUser();
  if (!u?.access_token) return;

  loadingCreate.value = true;
  try {
    const response = await createAnnotation(
      {
        authorName: u.profile!.preferred_username!,
        code,
        confidence: confidence.value,
        comment: comment.value,
        predicate: predicate.value,
      },
      dataElement.id!,
      (annotatable.value as Choice & Variable)?.choices?.length ? undefined : annotatable.value!.id,
    );
    // keep errror state for longer so user has time to think
    if (response?.ok) {
      loadingFeedbackIcon.value = mdiCheck;
      setTimeout(() => (loadingFeedbackIcon.value = undefined), 2_000);
      setTimeout(() => emit("success"), 500);
    } else {
      loadingFeedbackIcon.value = mdiClose;
      setTimeout(() => (loadingFeedbackIcon.value = undefined), 5_000);
    }
  } catch (e) {
    console.error(e);
  } finally {
    loadingCreate.value = false;
  }
};
</script>

<template>
  <div class="code-annotator-container">
    <v-list lines="one">
      <v-list-item :key="modelValue?.id">
        <template #title>{{ modelValue?.label }}</template>
        <template #subtitle>
          <span color="primary">Ausgewähltes Element</span>
        </template>
        <template #append>
          <v-menu>
            <template #activator="{ props }">
              <v-btn v-bind="props" :disabled="loadingCreate" :icon="mdiPencil" variant="plain" />
            </template>
            <v-list>
              <v-list-item
                v-for="(item, i) in allAnnotatables"
                :key="item.id"
                link
                @click="() => $emit('update:modelValue', item)"
              >
                <v-list-item-title>{{ item.label }}</v-list-item-title>
                <v-list-item-subtitle v-if="i === 0">Variable selbst</v-list-item-subtitle>
                <template #append>
                  <v-chip v-if="item.annotations?.length">
                    {{ item.annotations.length }} vorgeschlagene Annotation<template
                      v-if="item.annotations.length > 1"
                      >en
                    </template>
                  </v-chip>
                </template>
              </v-list-item>
            </v-list>
          </v-menu>
        </template>
      </v-list-item>
      <v-list-item :key="code.system + code.code">
        <template #title>{{ code.display }}</template>
        <template #subtitle>
          <span color="primary">Ausgewählter Code:</span> {{ code.code }} in {{ code.system }}
        </template>
        <template #append>
          <v-btn
            :disabled="loadingCreate"
            :icon="mdiPencil"
            @click="$emit('edit')"
            variant="plain"
          />
        </template>
      </v-list-item>
    </v-list>
    <br />
    <!-- form to add further SSSOM metadata -->
    <v-form @submit.prevent="submitAnnotation">
      <v-slider
        v-model="confidence"
        :step="1"
        :min="0"
        :max="100"
        label="Konfidenz"
        thumb-label="hover"
        color="primary"
      >
        <template #thumb-label="{ modelValue: sliderModelValue }">{{ sliderModelValue }}%</template>
      </v-slider>
      <v-select label="Prädikat" v-model="predicate" :items="predicates" />
      <v-textarea v-model="comment" label="Kommentar" />
      <v-btn
        :prepend-icon="loadingFeedbackIcon"
        :disabled="!!loadingFeedbackIcon || loadingCreate"
        :loading="loadingCreate"
        size="x-large"
        type="submit"
        color="primary"
        block
      >
        Erstellen
      </v-btn>
    </v-form>
  </div>
</template>

<style>
.code-annotator-container {
  max-width: 800px;
}
</style>
