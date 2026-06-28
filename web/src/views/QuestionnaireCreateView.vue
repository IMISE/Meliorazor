<script setup lang="ts">
import { computed, ref, watch, type Ref } from 'vue';
import Papa from 'papaparse';
import { mdiAccountMinus, mdiAccountPlus, mdiAlertCircleOutline, mdiCheck, mdiMinus, mdiPlus, mdiTestTube } from '@mdi/js';
import type { FhirValueSetDefinition, Participant, Questionnaire, QuestionnaireOptions, Variable } from '@/types/types';
import auth from '@/auth';
import { roles } from '@/types/enums';
import { useRouter } from 'vue-router';
import { read, utils } from 'xlsx';

const title = ref("");
const description = ref("");
const file = ref();
const fileType: Ref<"RedCap" | "OpenClinica"> = ref("RedCap");

type ValueSetDefinition = FhirValueSetDefinition & { testResult: { ok: boolean, title: string, status: string, codes: number } | undefined, loading: boolean };
const emptyValueSet: () => ValueSetDefinition = () => ({ display: "", server: "", valueSet: "", testResult: undefined, loading: false });
const valueSets: Ref<ValueSetDefinition[]> = ref([emptyValueSet()]);
const participants: Ref<Participant[]> = ref([{ username: "", roles: [] }]);
const contents: Ref<{ data: { [index: string]: string }[], errors: any[] } | undefined> = ref(undefined);
const options = ref({
  posCount: {
    use: true,
    val: 1,
    fraction: false,
    label: "Mindestanzahl positiver Bewertungen einer Annotation zur automatischen Akzeptanz",
  },
  posRatio: {
    use: false,
    val: 50,
    fraction: true,
    label: "Mindestanteil positiver Bewertungen an allen Bewertungen einer Annotation zur automatischen Akzeptanz",
  },
  negCount: {
    use: true,
    val: 1,
    fraction: false,
    label: "Mindestanzahl negativer Bewertungen einer Annotation zur automatischen Ablehnung",
  },
  negRatio: {
    use: false,
    val: 70,
    fraction: true,
    label: "Mindestanteil negativer Bewertungen an allen Bewertungen einer Annotation zur automatischen Ablehnung",
  },
});
watch(file, () => {
  if(!file.value) contents.value = undefined;

  switch (fileType.value) {
    case "RedCap": {
      Papa.parse(file.value, {
        complete: (result) => { contents.value = { data: result.data as { [index: string]: string }[], errors: result.errors } },
        header: true,
        worker: true,
      });
      break;
    }
    case "OpenClinica": {
      const reader = new FileReader();
      reader.onload = ((data) => {
        try {
          const wb = read(data, { type: "buffer" });
          const ws = wb.Sheets["Items"]!;
          const items: { [index: string]: string }[] = utils.sheet_to_json(ws, { header: "A", skipHidden: true });
          contents.value = { data: items, errors: [] };
        } catch (e) {
          console.error(e);
          contents.value = { data: [], errors: [e] };
        }
      });
      reader.onerror = console.error;
      reader.readAsText(file.value);
      break;
    }
    default: {
      console.error("Unknown type of input file");
    }
  }

});

const parsedDataREDCap = () => contents.value?.data.map((datum) => ({
  id: undefined,
  variableName: datum["Variable / Field Name"] as string,
  label: datum["Field Label"] as string,
  type: datum["Field Type"] as string,
  choices: ["dropdown", "checkbox", "radio"].includes(datum["Field Type"] as string) ?
      (datum["Choices, Calculations, OR Slider Labels"] as string)
        .split(" | ")
        .map((choice) => choice.split(", ", 2))
        .map((choice) => ({
          id: undefined,
          code: choice[0],
          name: choice[1],
          annotations: [],
        }))
    : null,
  metadata: Object.keys(datum)
    .filter((key) =>
      !["Variable / Field Name", "Field Label", "Field Type"].includes(key)
      && (key !== "Choices, Calculations, OR Slider Labels" || !["dropdown", "checkbox", "radio"].includes(datum["Field Type"] as string)))
    .filter((key) => datum[key])
    .map((key) => ({ key, value: datum[key] })),
  annotations: [],
} as Variable));

const parsedDataOpenClinica = () => contents.value?.data.map((datum) => ({
  id: undefined,
  variableName: datum["ITEM_NAME*"] as string,
  label: datum["DESCRIPTION_LABEL*"] as string,
  type: datum["DATA_TYPE*"] as string,
  choices: datum["RESPONSE_OPTIONS_TEXT"] ?
      ((datum["RESPONSE_OPTIONS_TEXT"] as string))
        .split(",")
        .filter((choice) => choice)
        .map((choice, index) => ({
          id: undefined,
          code: "" + index,
          name: choice,
          annotations: [],
        }))
    : null,
  metadata: Object.keys(datum)
    .filter((key) => !["ITEM_NAME*", "DESCRIPTION_LABEL*", "DATA_TYPE*", "RESPONSE_OPTIONS_TEXT"].includes(key))
    .filter((key) => datum[key])
    .map((key) => ({ key, value: datum[key] })),
  annotations: [],
} as Variable));

const parsedData = computed(() => {
  switch (fileType.value) {
    case "RedCap": return parsedDataREDCap();
    case "OpenClinica": return parsedDataOpenClinica();
    default: return undefined;
  }
})

const headers = [{
  title: "Variablenname",
  key: "variableName",
}, {
  title: "Beschreibung",
  key: "label",
}, {
  title: "Feld-/Datentyp",
  key: "type",
}, {
  title: "Optionen",
  key: "choices",
  value: (item: Variable) => (item.choices?.map((choice) => `${choice.code}. ${choice.name}`).join("; ") ?? "-") || "(keine)",
}, {
  title: "Weitere Informationen",
  key: "metadata",
  value: (item: Variable) => item.metadata.map((pair) => `${pair.key}: ${pair.value}`).join(";") || "-",
}];

const loading: Ref<boolean> = ref(false);
const response: Ref<Response | undefined> = ref(undefined);
const create = async () => {
  if (!parsedData.value) return;
  const u = await auth.getUser();
  if (!u?.access_token) return;
  loading.value = true;
  response.value = await fetch(`${(window as any).config?.VITE_API_BASE}/questionnaire`, {
    method: "POST",
    body: JSON.stringify({
      title: title.value,
      description: description.value,
      dataElements: parsedData.value as Variable[],
      options: {
        reqPosReviews: options.value.posCount.use ? options.value.posCount.val : null,
        reqNegReviews: options.value.negCount.use ? options.value.negCount.val : null,
        reqPosReviewRatio: options.value.posRatio.use ? options.value.posRatio.val : null,
        reqNegReviewRatio: options.value.negRatio.use ? options.value.negRatio.val : null,
      } as QuestionnaireOptions,
      participants: participants.value as Participant[],
      valueSets: valueSets.value.map((v) => ({ display: v.display, server: v.server, valueSet: v.valueSet })) as FhirValueSetDefinition[],
    }),
    headers: {
      "Content-Type": "application/json",
      "Authorization": `Bearer ${u.access_token}`,
    },
  });
  loading.value = false;
}

const clean = (input: string | null) =>
  (input || "undef").toLowerCase()
    .replaceAll("ä", "ae")
    .replaceAll("ö", "oe")
    .replaceAll("ü", "ue")
    .replaceAll("ß", "ss")
    .replace(/[^a-z0-9-]/gi, "_")
    .replace(/_{2,}/g, "_")
    .replace(/(^_|_$)/g, "");
const isUrl = (input: string) => {
  try {
    new URL(input);
    return true;
  } catch {
    return false;
  }
}

const isValidUrlConf = (serverUrl: string, valueSetUrl: string) => {
  return serverRules.value.every((test) => test(serverUrl) === true && test(valueSetUrl) === true);
}
const testConnectionLimit = ref(100);
const testConnection = async (input: ValueSetDefinition) => {
  input.loading = true;
  try {
    const response = await fetch(`${input.server}/ValueSet/$expand?url=${input.valueSet}&count=${testConnectionLimit.value}`);
    const json = await response.json();
    input.testResult = {
      ok: response.ok && json.resourceType === "ValueSet",
      title: json.title ?? json.name ?? "unbekannt",
      status: json.status,
      codes: json.expansion?.contains?.length ?? json.expansion?.parameter?.find((e: any) => e.name === "count")?.valueInteger ?? "unknown",
    };
  } catch (e) {
    console.error(e);
    input.testResult = {
      ok: false, title: "", status: "", codes: 0,
    };
  } finally {
    input.loading = false;
  }
}

watch(response, async () => {
  if (response.value?.ok) {
    const body = await response.value.text();
    const id = Number(body);
    if (!body || !Number.isInteger(id)) return;
    useRouter().push({ name: "single-project", query: {
      pid: id,
      title: clean(title.value),
    }});
  }
});

const notEmpty = ref([
  (v: string | any[]) => !!v.length || "darf nicht leer sein",
]);
const atLeastOneDE = ref([
  (_: any) => !!parsedData.value?.length || "darf nicht leer sein",
]);
const participantUser = ref([
  (v: string) => !!v.length || "darf nicht leer sein",
  (v: string) => participants.value.reduce((acc, w) => Number(v === w.username) + acc, 0) === 1 || "darf nur 1x vorkommen",
]);

const serverDisplayRules = ref([
  (v: string) => !!v.length || valueSets.value.length === 1 || "darf nicht leer sein",
  (v: string) => valueSets.value.reduce((acc, w) => Number(v === w.display) + acc, 0) === 1 || "darf nur 1x vorkommen",
])
const serverRules = ref([
  (v: string) => !!v.length || "darf nicht leer sein",
  (v: string) => isUrl(v) || "muss valide URL sein",
]);
const valueSetRules = ref([
  (v: string) => !!v.length || "darf nicht leer sein",
  (v: string) => isUrl(v) || "muss valide URL sein",
  (v: string) => valueSets.value.reduce((acc, w) => Number(v === w.valueSet) + acc, 0) === 1 || "darf nur 1x vorkommen",
]);

const sliderRules = ref([
  (v: number) => 0 <= v || "zu klein",
  (v: string) => /^[0-9]+$/.test(v) || "invalider Wert",
  (v: number) => v <= 100 || "zu groß",
]);
const fracSliderRules = ref([
  ...sliderRules.value,
  (_: number) => (options.value.posRatio.val + options.value.negRatio.val) >= 100 || !options.value.posRatio.use || !options.value.negRatio.use || "müssen zusammen mind. 100% sein",
]);

</script>

<template>
  <v-sheet width="1000" class="sheet">
    <v-form :disabled="loading" @submit.prevent="create">
      <h1>Projekt erstellen</h1>
      <v-text-field v-model="title" :rules="notEmpty" label="Titel" required></v-text-field>
      <v-textarea v-model="description" label="Beschreibung"></v-textarea>
      <v-select label="Typ vom Data Dictionary" :rules="notEmpty" :items="['RedCap', 'OpenClinica']" v-model="fileType"></v-select>
      <v-file-input v-if="fileType === 'RedCap'" v-model="file" accept=".csv" :rules="atLeastOneDE" label="Data Dictionary eingeben"></v-file-input>
      <v-file-input v-if="fileType === 'OpenClinica'" v-model="file" accept=".xls" :rules="atLeastOneDE" label="Data Dictionary eingeben"></v-file-input>
      <br />

      <h2>FHIR value sets</h2>
      <p>In diesen Value Sets wird nach Termen gesucht.</p>
      <v-col fluid>
        <v-row v-for="(row, i) in valueSets" :key="valueSets.length + i" gap="10" ref="row.ref">
          <v-col v-if="valueSets.length > 1" cols="3">
            <v-text-field v-model="row.display" :rules="serverDisplayRules" label="Name"></v-text-field>
          </v-col>
          <v-col :cols="valueSets.length > 1 ? 4 : 6">
            <v-text-field v-model="row.server" :rules="serverRules" label="FHIR Server-URL"></v-text-field>
          </v-col>
          <v-col :cols="valueSets.length > 1 ? 4 : 6">
            <v-text-field v-model="row.valueSet" :rules="valueSetRules" label="Value Set-URL"></v-text-field>
          </v-col>
          <v-col v-if="valueSets.length > 1" cols="1">
            <v-icon-btn :icon="mdiMinus" @click="valueSets.splice(i, 1)"></v-icon-btn>
          </v-col>
          <v-col v-if="isValidUrlConf(row.server, row.valueSet)" cols="2">
            <v-btn :disabled="loading || row.loading" :loading="row.loading" :prepend-icon="mdiTestTube" @click="testConnection(row)">Verbindung testen</v-btn>
          </v-col>
          <v-col v-if="row.testResult" cols="10">
            <template v-if="row.testResult.ok">
              <v-icon :icon="mdiCheck" aria-hidden="true" /> Funktioniert!
              {{ row.testResult.title }} <template v-if="row.testResult.status">({{ row.testResult.status }})</template>
              enthält <template v-if="row.testResult.codes === testConnectionLimit">&ge;</template>{{ row.testResult.codes }} Codes.
            </template>
            <template v-else>
              <v-icon :icon="mdiAlertCircleOutline" />Test fehlgeschlagen
            </template>
          </v-col>
        </v-row>
        <v-row>
          <v-btn variant="text" :disabled="loading" :prepend-icon="mdiPlus" @click="valueSets.push(emptyValueSet())">Hinzufügen</v-btn>
          <v-btn variant="text" :disabled="loading" :prepend-icon="mdiMinus" @click="valueSets.pop()" v-if="valueSets.length > 1">Entfernen</v-btn>
        </v-row>
      </v-col>
      <br />

      <h2>Mitwirkende</h2>
      <p>Wer hat welche Rechte?</p>
      <v-col fluid>
        <v-row v-for="(row, i) in participants" :key="participants.length + i" gap="10">
          <v-col cols="6">
            <v-text-field v-model="row.username" :rules="participantUser" label="Nutzername"></v-text-field>
          </v-col>
          <v-col :cols="participants.length > 1 ? 5 : 6">
            <v-select label="Rolle" :rules="notEmpty" :items="roles" v-model="row.roles" multiple></v-select>
          </v-col>
          <v-col v-if="participants.length > 1" cols="1">
            <v-icon-btn :icon="mdiMinus" @click="participants.splice(i, 1)"></v-icon-btn>
          </v-col>
        </v-row>
        <v-row>
          <v-btn variant="text" :disabled="loading" :prepend-icon="mdiAccountPlus" @click="participants.push({ username: '', roles: [] })">Hinzufügen</v-btn>
          <v-btn variant="text" :disabled="loading" :prepend-icon="mdiAccountMinus" @click="participants.pop()" v-if="participants.length > 1">Entfernen</v-btn>
        </v-row>
      </v-col>
      <br />

      <h2>Akzeptanzbedingungen</h2>
      <p>
        Wenn die Bedingungen erfüllt sind, kann eine Annotation automatisch als akzeptiert oder abgelehnt markiert werden.
        Wenn nicht, geht das natürlich immer noch manuell.
        Die Bedingungen hängen von der Anzahl der positiver bzw. negativer Bewertungen oder vom Anteil an allen Bewertungen des Annotationskandidaten ab.
        Diese anders zu konfigurieren ergibt nur Sinn, wenn auch regelmäßig mehrere Bewertungen pro Annotation erwaret sind.
        Bei nur einer erwarteten Bewertung reichen die Standardwerte.
        Um eine Bedingung außen vor zu lassen, einfach deaktivieren.
      </p><br />
      <v-col fluid>
        <v-row v-for="row in options" fluid>
          <v-col cols="12"><i>{{ row.label }}</i></v-col>
          <v-col cols="2">
            <v-checkbox v-model="row.use" label="Aktivieren"></v-checkbox>
          </v-col>
          <v-col cols="10">
            <v-slider v-model="row.val" :disabled="!row.use" :step="1" :min="0" :max="100" :rules="row.fraction ? fracSliderRules : sliderRules" thumb-label="always">
              <template #thumb-label="{ modelValue }">{{ modelValue }}<template v-if="row.fraction">%</template></template>
              <template #append>
                <v-text-field
                    v-model="row.val"
                    density="compact"
                    style="width: 90px; max-width: fit-content; margin-left: 20px;"
                    type="number"
                    hide-details
                    single-line
                  >
                  <template v-if="row.fraction" #append-inner>%</template>
                </v-text-field>
              </template>
            </v-slider>
          </v-col>
        </v-row>
      </v-col>

      <v-btn :disabled="loading" :loading="loading" size="x-large" type="submit" block>Erstellen</v-btn>
    </v-form>
  </v-sheet>

  <v-sheet width="1000" v-if="contents" class="sheet">
    <h1>Daten</h1>
    <v-data-table v-if="parsedData?.length" :headers="headers" :items="parsedData"></v-data-table>
    <p v-else><i>Keine Daten gefunden.</i></p>
    <template v-if="contents.errors?.length">
      <h1>Fehler</h1>
      <v-list>
        <v-list-item v-for="error in contents.errors" :title="`${error.type}: ${error.message}`" color="danger">
          <template #prepend>
            <v-icon :icon="mdiAlertCircleOutline" />
          </template>
        </v-list-item>
      </v-list>
    </template>
  </v-sheet>
</template>

<style scoped>
.sheet {
  padding: 20px;
  margin: 50px;
}
</style>
