<script setup lang="ts">
import type { FhirKeyValue, FhirValueSetDefinition } from "@/types/fhir";
import { allValueSets } from "@/utils/fhirApi";
import { isFhirServer, isUrl } from "@/utils/utils";
import {
  mdiAlertCircleOutline,
  mdiCheck,
  mdiLocationEnter,
  mdiMinus,
  mdiPencil,
  mdiTestTube,
} from "@mdi/js";
import { computed, watch } from "vue";
import { ref, type Ref } from "vue";

const model = defineModel<FhirValueSetDefinition | undefined>();
defineProps<{
  displayControls: boolean;
  disabled: boolean;
}>();

defineEmits<{
  (e: "remove"): void;
}>();

type TestResults = { ok: boolean; title: string; status: string; codes: number };

const server = ref(model.value?.server ?? "");
const selectedSet: Ref<FhirValueSetDefinition | string | undefined> = ref(model.value /* ?? undefined (implicit) */);
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
      server: server.value,
      display: val,
      valueSet: val,
    };
  } else {
    model.value = val;
  }
});

/** value sets on `server` */
const valueSets: Ref<FhirValueSetDefinition[]> = ref([]);
const loading = ref(false);
// if we were passed a server via a model, count that as selected
// (we do that further below, because we need to load the valuesets still)
const serverSelected = ref(false);
const loadingSelection = ref(false);
const loadingValueSets = ref(false);

const serverRules = ref([
  (v: string) => !!v.length || "darf nicht leer sein",
  (v: string) => isUrl(v) || "muss valide URL sein",
]);

const testConnectionLimit = ref(100);
const testConnection = async () => {
  loading.value = true;
  try {
    const response = await fetch(
      `${server.value}/ValueSet/$expand?url=${model.value?.valueSet}&count=${testConnectionLimit.value}`,
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
  } catch (e) {
    console.error(e);
  } finally {
    loadingValueSets.value = false;
  }
};

const selectServer = async () => {
  loadingSelection.value = true;
  const isValid = await isFhirServer(server.value);
  if (isValid) {
    // TODO: error handling
    await getValueSets();
    serverSelected.value = true;
  }
  loadingSelection.value = false;
};

const unselectServer = () => {
  serverSelected.value = false;
  selectedSet.value = undefined;
};

// if we started with a server in our model, select it
selectServer();

watch(loadingSelection, (val) => {
  if (val) return;
});

const valueSetProps = (item: FhirValueSetDefinition | string) =>
  typeof item === "string"
    ? item
    : {
        title: item.display,
        subtitle: item.valueSet,
      };
</script>

<template>
  <v-row gap="10" ref="row.ref">
    <v-col :cols="(displayControls ? 5 : 6) + (serverSelected ? 0 : 6)">
      <v-text-field
        v-model="server"
        :readonly="serverSelected || loadingSelection"
        :loading="loadingSelection"
        :rules="serverRules"
        label="FHIR Server-URL"
        :prepend-icon="isUrl(server) && !serverSelected ? mdiLocationEnter : undefined"
        :append-inner-icon="serverSelected ? mdiPencil : undefined"
        @click:prepend="selectServer"
        @click:append-inner="unselectServer"
      />
    </v-col>
    <v-col v-if="serverSelected" :cols="6">
      <v-combobox
        v-model="selectedSet"
        auto-select-first="exact"
        :items="valueSets"
        :item-props="valueSetProps"
        label="Value Set"
      >
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
              />
            </template>
          </v-tooltip>
        </template>
      </v-combobox>
    </v-col>
    <v-col v-if="displayControls" :cols="1">
      <v-icon-btn :icon="mdiMinus" @click="$emit('remove')"></v-icon-btn>
    </v-col>
  </v-row>
</template>
