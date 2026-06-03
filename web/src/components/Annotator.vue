<script setup lang="ts">
import auth from '@/auth';
import type { CodeInfo, CodeMapping, FhirValueSetDefinition, InformedCodeMapping, Predicate, Variable } from '@/stores/types';
import { mdiArrowLeft, mdiArrowRight, mdiMagnify, mdiPencil } from '@mdi/js';
import { ref, watch, type Ref } from 'vue';
import { expandValueSet, getCode } from '@/utils/fhirApi.ts';

const { valueSets, dataElement } = defineProps<{
  valueSets: FhirValueSetDefinition[],
  dataElement: Variable,
  /** url to previous data element */ prev: string,
  /** url to next data elemnt */ next: string,
  showPrev: boolean,
  showNext: boolean,
}>();

const search = ref(dataElement.label ?? "");
const loadingSearch = ref(false);
const loadingFocus = ref(false);
const loadingCreate = ref(false);

const searchResults: Ref<InformedCodeMapping[] | undefined> = ref(undefined);

// SSSOM metadata
const confidence: Ref<number> = ref(100);
const predicate: Ref<Predicate> = ref("skos:exactMatch");
const comment = ref("");
const predicates = ["owl:sameAs", "owl:equivalentClass", "owl:equivalentProperty", "rdfs:subClassOf", "rdfs:subPropertyOf", "skos:relatedMatch", "skos:closeMatch", "skos:exactMatch", "skos:narrowMatch", "skos:broadMatch", "oboInOwl:hasDbXref", "rdfs:seeAlso"];

const go = async (text: string) => {
  loadingSearch.value = true;
  const fetches = valueSets.map(async (set) => {
      const response = await expandValueSet(text, set);
      return response;
  });
  // no try/catch - this never fails
  const answers = await Promise.allSettled(fetches);
  searchResults.value = answers.filter((answer) => answer.status === "fulfilled").map((answer) => answer.value).flat(1);
  loadingSearch.value = false;
}

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

// further information
const focusCode: Ref<InformedCodeMapping | undefined> = ref(undefined);
const focusCodeInfo: Ref<CodeInfo | undefined> = ref(undefined);
const selectedCode: Ref<CodeMapping | undefined> = ref(undefined);

const response: Ref<Response | undefined> = ref(undefined);

const focus = (event: any, { item }: { item: InformedCodeMapping }) => {
  focusCode.value = item;
};
const updateFocus = (value: CodeMapping) => {
  focusCode.value = {
    ...value,
    valueSet: focusCode.value!.valueSet,
  };
}
const unselect = () => {
  selectedCode.value = undefined;
};

const submitAnnotation = async () => {
  const u = await auth.getUser();
  if (!u?.access_token) return;

  loadingCreate.value = true;
  try {
    response.value = await fetch(`${import.meta.env.VITE_API_BASE}/dataElement/${dataElement.id}/annotation`, {
      method: "POST",
      body: JSON.stringify({
        authorName: u.profile?.preferred_username,
        code: focusCode.value!,
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


// populate focusCodeInfo
watch(focusCode, async (newCode, oldCode) => {
  if (!newCode?.valueSet || newCode === oldCode) return;
  focusCodeInfo.value = undefined;

  try {
    const response = await getCode(newCode.code, newCode.system, newCode.valueSet.server);
    if (!response) return;
    focusCodeInfo.value = response;
  } catch (e) {
    console.error(e);
  }
});

</script>

<template>
  <v-row size="12" cols="12" class="full-width">
    <v-btn v-if="showPrev" :href="prev" class="navigate" :prepend-icon="mdiArrowLeft">Zurück</v-btn>
    <v-btn v-if="showNext" :href="next" class="navigate" :append-icon="mdiArrowRight">Weiter</v-btn>
  </v-row>
  <v-row v-if="!selectedCode" size="12" class="full-width">
    <v-col :cols="focusCode ? 6 : 12">
      <v-row fluid>
        <h3>{{ dataElement.label }}</h3><br />
        <h6 v-if="dataElement.variableName">{{ dataElement.variableName }}</h6>
        <v-expansion-panels v-if="dataElement.metadata.length">
          <v-expansion-panel v-if="dataElement.choices?.length" title="Auswahlmöglichkeiten">
            <v-expansion-panel-text>
              <template v-for="choice in dataElement.choices">
                {{ choice.name }} (<var>{{ choice.code }}</var>)<br />
              </template>
            </v-expansion-panel-text>
          </v-expansion-panel>
          <v-expansion-panel v-if="dataElement.metadata.length" title="Weitere Informationen">
            <v-expansion-panel-text>
              <template v-for="m in dataElement.metadata">
                {{ m.key }}: {{ m.value }}<br />
              </template>
            </v-expansion-panel-text>
          </v-expansion-panel>
        </v-expansion-panels>
      </v-row>
      <v-row fluid>
        <v-text-field v-model="search" hint="Code suchen …">
          <template #append-inner>
            <v-btn :loading="loadingSearch" :disabled="loadingSearch" :icon="mdiMagnify" variant="text" @click="go(search)" />
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
          @click:row="focus"
        />
      </v-row>
    </v-col>
    <v-col v-if="focusCode && focusCodeInfo" cols="6">
      <code-display :code="focusCodeInfo" :server="focusCode.valueSet.server" show-select-button @focus="updateFocus" @selection="(v: CodeMapping) => selectedCode = v" />
    </v-col>
  </v-row>
  <v-col v-else cols="12">
    <v-row fluid>
      <v-list lines="one">
        <v-list-item :key="selectedCode.system + selectedCode.code">
          <template #title>{{ selectedCode.display }}</template>
          <template #subtitle>
            <span color="blue">Ausgewählter Code:</span> {{ selectedCode.code }} in {{ selectedCode.system }}
          </template>
          <template #append>
            <v-btn :disabled="loadingCreate" :icon="mdiPencil" @click="unselect()" variant="text" />
          </template>
        </v-list-item>
      </v-list>
    </v-row>
    <!-- form to add further SSSOM metadata -->
    <v-row fluid>
      <v-form @submit.prevent="submitAnnotation">
        <v-slider v-model="confidence" :step="1" :min="0" :max="100" label="Konfidenz" thumb-label="always">
          <template #thumb-label="{ modelValue }">{{ modelValue }}%</template>
        </v-slider>
        <v-select label="Prädikat" v-model="predicate" :items="predicates" />
        <v-textarea v-model="comment" label="Kommentar" />
        <v-btn :disabled="loadingCreate" :loading="loadingCreate" size="x-large" type="submit" block>Erstellen</v-btn>
      </v-form>
    </v-row>
  </v-col>
</template>

<style scoped>
.navigate {
  background-color: lightblue;
}
.save {
  background-color: greenyellow;
}
.close {
  background-color: red;
}
.wrap-text {
   white-space: normal;
}
.full-width {
  width: 100%;
}
</style>
