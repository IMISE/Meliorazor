<script setup lang="ts">
import type { FhirKeyValue } from "@/types/fhir";
import type { FhirValueSetDefinition, ServerDefinition } from "@/types/types";
import {
  allCodeSystemsAsImplicitValueSets,
  allValueSets,
  expandValueSetRaw,
} from "@/utils/fhirApi";
import { mdiAlertCircleOutline, mdiCheck, mdiMinus, mdiTestTube } from "@mdi/js";
import { computed, watch } from "vue";
import { ref, type Ref } from "vue";
import type { InternalItem } from "vuetify";

const model = defineModel<FhirValueSetDefinition | undefined>();
defineProps<{
  displayControls: boolean;
  disabled: boolean;
  allowedServers: ServerDefinition[];
}>();

defineEmits<{
  (e: "remove"): void;
}>();

type TestResults = { ok: boolean; title: string; status: string; codes: number };

const server = ref(model.value?.server ?? "");
const implicit = ref(model.value?.implicit ?? true);
const selectedSet: Ref<FhirValueSetDefinition | string | undefined> = ref(model.value);
const testResult: Ref<TestResults | undefined> = ref();
const testTooltip = computed(() =>
  testResult.value
    ? testResult.value.ok
      ? `Funktioniert! ${testResult.value.title}` +
        (testResult.value.status ? ` (${testResult.value.status})` : "") +
        ` enthält ${testResult.value.codes === testConnectionLimit.value ? "≥" : ""}${testResult.value.codes} Codes.`
      : "Test fehlgeschlagen."
    : "Verbindung testen",
);

watch(selectedSet, (val) => {
  testResult.value = undefined;
  if (!val) {
    model.value = undefined;
  } else if (typeof val === "string") {
    model.value = {
      implicit: false,
      server: server.value,
      display: val,
      url: val,
      version: "",
    };
  } else {
    model.value = val;
  }
});

/** value sets on `server` */
const valueSets: Ref<FhirValueSetDefinition[]> = ref([]);
/** code systems on `server`, saved as implicit value sets */
const codeSystems: Ref<
  (FhirValueSetDefinition | (FhirValueSetDefinition & { type: "subheader" }))[]
> = ref([]);
const loading = ref(false);
const loadingValueSets = ref(false);

const testConnectionLimit = ref(100);
const testConnection = async () => {
  loading.value = true;
  if (!model.value) return;
  try {
    // limit of 0 should return just the count
    // for some servers, this is not actually done though
    // so we have testConnectionLimit
    const response = await expandValueSetRaw(
      undefined,
      model.value,
      false,
      undefined,
      testConnectionLimit.value,
    );

    const json = await response.json();
    testResult.value = {
      ok: response.ok && json.resourceType === "ValueSet",
      title: json.title ?? json.name ?? "unbekannt",
      status: json.status,
      codes:
        json.expansion?.contains?.length ??
        json.expansion?.parameter?.find((e: FhirKeyValue) => e.name === "count")?.valueInteger ??
        "unbekannt",
    };
  } catch (e) {
    console.error(e);
    testResult.value = {
      ok: false,
      title: "",
      status: "",
      codes: 0,
    };
  } finally {
    loading.value = false;
  }
};

const getValueSets = async () => {
  loadingValueSets.value = true;
  try {
    valueSets.value = await allValueSets(server.value);
    const groupedCodeSystems = await allCodeSystemsAsImplicitValueSets(server.value);
    codeSystems.value = Object.keys(groupedCodeSystems).flatMap((key) => [
      {
        // everything except type and display is unimportant
        type: "subheader",
        server: "subheader",
        display: key,
        url: groupedCodeSystems[key]![0]?.url ?? "",
        version: "",
        implicit: true,
      },
      ...groupedCodeSystems[key]!,
    ]);
    // TODO: chips that show version for combobox
  } catch (e) {
    console.error(e);
  } finally {
    loadingValueSets.value = false;
  }
};

watch(server, getValueSets, { immediate: true });

const valueSetProps = (
  item: FhirValueSetDefinition | (FhirValueSetDefinition & { type: "subheader" }),
) =>
  item.server === "subheader"
    ? { type: "subheader", title: `${item.display} (${item.url})` }
    : {
        type: "",
        title: item.display,
        subtitle: item.implicit ? item.version : item.url,
      };

/** Filter for ValueSet name, url and version. */
const customFilter = (
  itemTitle: string,
  queryText: string,
  item: InternalItem<FhirValueSetDefinition> | undefined,
) => {
  return (
    itemTitle.indexOf(queryText) > -1 ||
    (item?.raw.url.indexOf(queryText) ?? -1) > -1 ||
    (item?.raw.version.indexOf(queryText) ?? -1) > -1
  );
};
</script>

<template>
  <v-row gap="10" ref="row.ref">
    <v-col :cols="(displayControls ? 5 : 6) + (server !== undefined ? 0 : 6)">
      <v-select v-model="server" :items="allowedServers" label="FHIR Server" />
    </v-col>
    <v-col v-if="server !== undefined" :cols="6">
      <v-autocomplete
        v-model="selectedSet"
        :items="implicit ? codeSystems : valueSets"
        :item-props="valueSetProps"
        :custom-filter="customFilter"
        label="Codequelle"
      >
        <template #menu-footer>
          <v-btn-toggle v-model="implicit" mandatory>
            <v-btn :value="true">CodeSystem</v-btn>
            <v-btn :value="false">ValueSet</v-btn>
          </v-btn-toggle>
        </template>
        <template #append-inner>
          <v-tooltip v-if="model" :text="testTooltip">
            <template #activator="{ props }">
              <v-icon-btn
                v-bind="props"
                :icon="
                  testResult ? (testResult.ok ? mdiCheck : mdiAlertCircleOutline) : mdiTestTube
                "
                @click="testConnection"
                :disabled="loading"
                :loading="loading"
                variant="plain"
                hide-overlay
              />
            </template>
          </v-tooltip>
        </template>
      </v-autocomplete>
    </v-col>
    <v-col v-if="displayControls" :cols="1">
      <v-icon-btn :icon="mdiMinus" @click="$emit('remove')"></v-icon-btn>
    </v-col>
  </v-row>
</template>
