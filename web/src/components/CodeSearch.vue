<script setup lang="ts">
import type { FhirValueSetDefinition, InformedCodeMapping } from '@/stores/types';
import { expandValueSet } from '@/utils/fhirApi';
import { mdiMagnify } from '@mdi/js';
import { ref, type Ref } from 'vue';

const { initialSearch, valueSets } = defineProps<{
  initialSearch: string,
  valueSets: FhirValueSetDefinition[],
}>();

defineEmits<{
  (e: "focus", v: InformedCodeMapping): void,
}>();

const search = ref(initialSearch ?? "");
const loading = ref(false);
const searchResults: Ref<InformedCodeMapping[] | undefined> = ref(undefined);

const searchResultHeaders = [{
  title: "Name",
  key: "display",
}, {
  title: "Code",
  key: "code",
}, {
  title: "Codesystem",
  key: "system",
}];
const searchResultsGroupBy: import('vuetify/lib/components/VDataTable/composables/sort.mjs').SortItem[] | undefined = valueSets.length > 1 ? [{
  key: "valueSetDisplayName",
  order: "asc",
}] : undefined;

const go = async (text: string) => {
  loading.value = true;
  const fetches = valueSets.map(async (set) => {
      const response = await expandValueSet(text, set);
      return response;
  });
  // no try/catch - this never fails
  const answers = await Promise.allSettled(fetches);
  searchResults.value = answers.filter((answer) => answer.status === "fulfilled").map((answer) => answer.value).flat(1);
  loading.value = false;
}

</script>

<template>
  <v-row fluid>
    <v-text-field v-model="search" hint="Code suchen …">
      <template #append-inner>
        <v-btn :loading="loading" :disabled="loading" :icon="mdiMagnify" variant="text" @click="go(search)" />
      </template>
    </v-text-field>
  </v-row>
  <v-row fluid>
    <!-- TODO group by terminology/source, or display it & filter out duplicates? -->
    <v-data-table
      v-if="searchResults?.length"
      :item-value="(item) => item"
      :items="searchResults"
      :headers="searchResultHeaders"
      :group-by="searchResultsGroupBy"
      @click:row="(event: any, v: { item: InformedCodeMapping }) => $emit('focus', v.item)"
    />
  </v-row>
</template>
