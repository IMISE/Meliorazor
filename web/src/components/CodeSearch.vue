<script setup lang="ts">
import type { FhirValueSetDefinition } from "@/types/types";
import type { CodeMapping, InformedCodeMapping } from "@/types/types";
import { expandValueSet } from "@/utils/fhirApi";
import { codeEquals } from "@/utils/fhirUtils";
import { mdiCheck, mdiClose, mdiMagnify, mdiWeb } from "@mdi/js";
import { computed, ref, watch, type Ref } from "vue";

const { valueSets, previouslyAnnotated, focus } = defineProps<{
  valueSets: FhirValueSetDefinition[];
  previouslyAnnotated: (CodeMapping | null)[];
  focus: InformedCodeMapping | undefined;
}>();

defineEmits<{
  (e: "focus", v: InformedCodeMapping | null): void;
}>();

const search = defineModel<string>();

const selectedLang: Ref<string | undefined> = ref();
const loading = ref(false);
const searchedOnce = ref(false);
const searchResults: Ref<InformedCodeMapping[] | undefined> = ref();
watch(search, () => (searchedOnce.value = false));

const langs = [
  {
    shortCode: "DE",
    longCode: "de-DE",
    display: "Deutsch (Deutschland)",
  },
  {
    shortCode: "EN",
    longCode: "en-US",
    display: "English (USA)",
  },
];

const searchResultHeaders = [
  {
    title: "Name",
    key: "display",
  },
  {
    title: "Code",
    key: "code",
  },
  {
    title: "Codesystem",
    key: "system",
    value: (item: InformedCodeMapping) => item,
  },
  {
    title: "Aktiv?",
    key: "inactive",
  },
];

const go = async (text: string) => {
  loading.value = true;
  const fetches = valueSets.map(async (set) => {
    const response = await expandValueSet(
      text,
      set,
      selectedLang.value ?? "en-US",
      20_000 /* TODO: pagination */,
    );
    return response;
  });
  // no try/catch - this never fails
  const answers = await Promise.allSettled(fetches);
  searchResults.value = answers
    .filter((answer) => answer.status === "fulfilled")
    .map((answer) => answer.value)
    .filter((val) => !!val)
    .flat(1);
  if (text === search.value) {
    searchedOnce.value = true;
  }
  loading.value = false;
};

const colourSpecialCodes = ({ item }: { item: InformedCodeMapping }) => {
  if (focus && item.code === focus.code && item.system === focus.system) {
    return { class: "focus-code" };
  } else {
    const classes = [];
    if (previouslyAnnotated.some((code) => codeEquals(item, code ?? undefined))) {
      classes.push("known-code bg-warning");
    }
    if (item.inactive) {
      classes.push("inactive-code");
    }
    return { class: classes.join(" ") };
  }
};

const hasNullAnnotation = computed(() => previouslyAnnotated.some((mapping) => mapping === null));

// PAGINATION (todo?)
const itemsPerPage = ref(15);
</script>

<template>
  <v-alert v-if="hasNullAnnotation" type="warning" title="Schon negativ annotiert">
    Laut einer vorherigen Annotation scheint es hier keinen passenden Code zu geben. Eine neue
    Annotation kann trotzdem immer erstellt werden.
  </v-alert>
  <br />
  <v-row fluid>
    <v-text-field
      v-model="search"
      label="Codesuche"
      hint="Label oder Code eingeben"
      @keydown.enter="go(modelValue!)"
    >
      <template v-if="langs.length && false" #prepend-inner>
        <v-menu>
          <template #activator="{ props }">
            <v-btn v-if="selectedLang" v-bind="props" variant="plain" hide-overlay>
              {{ selectedLang }}
            </v-btn>
            <v-icon-btn v-else v-bind="props" :icon="mdiWeb" variant="plain" hide-overlay />
          </template>
          <v-list>
            <v-list-item
              v-for="lang of langs"
              :key="lang.longCode"
              :title="lang.display"
              :subtitle="lang.longCode"
              link
              @click="() => (selectedLang = lang.shortCode)"
            />
          </v-list>
        </v-menu>
      </template>
      <template #append-inner>
        <v-btn
          :loading="loading"
          :disabled="loading"
          :icon="mdiMagnify"
          variant="plain"
          hide-overlay
          @click="go(modelValue!)"
        />
      </template>
    </v-text-field>
  </v-row>
  <v-row fluid>
    <!-- TODO group by terminology/source, or display it & filter out duplicates? -->
    <template v-if="loading">
      <v-progress-circular indeterminate />
      <span>Suche lädt &hellip;</span>
    </template>
    <v-data-table
      v-if="searchResults?.length"
      :item-value="(item) => item"
      :items="searchResults"
      :headers="searchResultHeaders"
      :row-props="colourSpecialCodes"
      density="compact"
      :items-per-page="itemsPerPage"
      @click:row="(event: any, v: { item: InformedCodeMapping }) => $emit('focus', v.item)"
    >
      <template #[`item.system`]="{ value }">
        <v-tooltip :text="`${value.system}, Version ${value.systemVersion}`">
          <template #activator="{ props: tooltipProps }">
            <v-chip v-bind="tooltipProps" inline>
              {{ value.codeSystemName }}
            </v-chip>
          </template>
        </v-tooltip>
      </template>
      <template #[`item.inactive`]="{ value }">
        <v-icon v-if="!value" :icon="mdiCheck" />
        <v-icon v-else :icon="mdiClose" />
      </template>
    </v-data-table>
    <i v-else-if="searchedOnce">Keine Ergebnisse gefunden</i>
    <i v-else-if="!loading">Noch nicht gesucht</i>
  </v-row>
  <template v-if="searchedOnce && !hasNullAnnotation">
    <v-divider thickness="3">oder</v-divider>
    <v-btn block color="error" variant="outlined" @click="() => $emit('focus', null)">
      Ich glaube, es gibt keinen passenden Code.
    </v-btn>
  </template>
</template>

<style>
.focus-code {
  background-color: skyblue;
}
.inactive-code {
  opacity: 0.5;
}
.known-code {
  font-style: italic;
}
</style>
