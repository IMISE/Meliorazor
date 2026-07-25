<script setup lang="ts">
import auth from "@/auth";
import type { Annotatable, CodeMapping, Predicate, Variable } from "@/types/types";
import { createAnnotation } from "@/utils/api";
import { mdiCheck, mdiClose, mdiEye, mdiPencil } from "@mdi/js";
import { computed } from "vue";
import { ref, type Ref } from "vue";
import type { IconValue } from "vuetify/lib/composables/icons.mjs";
import ConfidenceSlider from "./ConfidenceSlider.vue";

const annotatable = defineModel<Annotatable>();
const { code, dataElement, knownPredicates } = defineProps<{
  code: CodeMapping | null;
  dataElement: Variable | undefined;
  knownPredicates: string[];
  editable: boolean;
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

const forbiddenPredicateSelected = computed(() => knownPredicates.includes(predicate.value));

const allAnnotatables = computed(() =>
  dataElement
    ? [dataElement, ...(dataElement.unit ? [dataElement.unit] : []), ...(dataElement.choices ?? [])]
    : annotatable.value
      ? [annotatable.value]
      : [],
);

const submitAnnotation = async () => {
  const u = await auth.getUser();
  if (!u?.access_token) return;

  loadingCreate.value = true;
  try {
    const response = await createAnnotation(
      {
        // TODO: give user option to negate normal mapping?
        // backend supports it theoretically, but do we want it to happen?
        negate: false,
        code,
        predicate: code === null ? null : predicate.value,
        actions: [
          {
            authorName: u.profile!.preferred_username!,
            description: comment.value,
            confidence: confidence.value,
            actionType: "submission",
          },
        ],
      },
      annotatable.value!.id!,
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
    <v-row v-if="code !== null">
      <v-col cols="4">
        <v-list lines="one">
          <v-list-item :key="modelValue?.id">
            <template #title>{{ modelValue?.label }}</template>
            <template #subtitle>
              <span color="primary">Ausgewähltes Element</span>
            </template>
            <template v-if="editable" #append>
              <v-menu>
                <template #activator="{ props }">
                  <v-btn
                    v-bind="props"
                    :disabled="loadingCreate"
                    :icon="mdiPencil"
                    variant="plain"
                  />
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
        </v-list>
      </v-col>
      <v-col cols="4">
        <v-select v-if="editable" label="Prädikat" v-model="predicate" :items="predicates" />
        <p v-else style="text-align: center; align-content: center">{{ predicate }}</p>
      </v-col>
      <v-col cols="4">
        <v-list lines="one">
          <v-list-item :key="code.system + code.code">
            <template #title>{{ code.display }}</template>
            <template #subtitle>
              <v-tooltip :text="`${code.code} in ${code.system}`">
                <template #activator="{ props }">
                  <span v-bind="props">
                    <span color="primary">Ausgewählter Code:</span>
                    {{ code.code }} in {{ code.system }}
                  </span>
                </template>
              </v-tooltip>
            </template>
            <template v-if="editable || code.code !== null" #append>
              <v-btn
                :disabled="loadingCreate"
                :icon="editable ? mdiPencil : mdiEye"
                @click="$emit('edit')"
                variant="plain"
              />
            </template>
          </v-list-item>
        </v-list>
      </v-col>
    </v-row>
    <v-row v-else>
      Es existiert kein zufriedenstellender Term oder es ist keiner auffindbar.
    </v-row>
    <v-alert
      v-if="knownPredicates.length"
      :type="forbiddenPredicateSelected ? 'error' : 'warning'"
      title="Schon verwendet"
    >
      Das ausgewählte Element haben Sie schon mit diesem Code verbunden.
      <template v-if="code !== null">
        Konkret wurden folgende Prädikate verwendet:
        <ul>
          <li v-for="predicate in knownPredicates" :key="predicate">
            <code>{{ predicate }}</code>
          </li>
        </ul>
      </template>
    </v-alert>
    <br />
    <!-- form to add further SSSOM metadata -->
    <v-form v-if="editable" @submit.prevent="submitAnnotation">
      <confidence-slider v-model="confidence" editable />
      <v-textarea v-model="comment" label="Kommentar" />
      <v-btn
        :prepend-icon="loadingFeedbackIcon"
        :disabled="!!loadingFeedbackIcon || loadingCreate || forbiddenPredicateSelected"
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
  min-width: 700px;
  width: 80%;
  max-width: 1000px;
}
</style>
