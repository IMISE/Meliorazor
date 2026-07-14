<script setup lang="ts">
import { computed, inject, ref, shallowRef, watch, type Ref } from "vue";
import Papa from "papaparse";
import { mdiAccountMinus, mdiAccountPlus, mdiAlertCircleOutline, mdiMinus, mdiPlus } from "@mdi/js";
import type { Participant, QuestionnaireOptions, Variable } from "@/types/types";
import auth from "@/auth";
import { roles } from "@/types/enums";
import { useRouter } from "vue-router";
import { read, utils } from "xlsx";
import getEnv from "@/utils/env";
import type { IdTokenClaims } from "oidc-client-ts";
import ValueSetSelector from "@/components/ValueSetSelector.vue";
import { clean } from "@/utils/utils";
import type { FhirValueSetDefinition } from "@/types/fhir";

const step = shallowRef([1]);
const itemsPerPage = ref(25);

const user = inject<Ref<IdTokenClaims | undefined>>("user");
const router = useRouter();

const title = ref("");
const description = ref("");

const fileType: Ref<"REDCap" | "OpenClinica"> = ref("REDCap");
/** REDCap */
const rcFile = ref();
/** OpenClinica */
const ocFile = ref();
const file = computed(() => (fileType.value === "REDCap" ? rcFile.value : ocFile.value));

const valueSets: Ref<{ id: number; set: FhirValueSetDefinition | undefined }[]> = ref([
  { id: 0, set: undefined },
]);
const valueSetKey = ref(0);
const setKey = () => {
  valueSetKey.value++;
  return valueSetKey.value;
};
const participants: Ref<Participant[]> = ref([
  { username: user?.value?.preferred_username ?? "", roles: [] },
]);
const contents: Ref<
  | {
      data: { [index: string]: string }[];
      errors: (Papa.ParseError | { type: string; message: string })[];
    }
  | undefined
> = ref();
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
    label:
      "Mindestanteil positiver Bewertungen an allen Bewertungen einer Annotation zur automatischen Akzeptanz",
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
    label:
      "Mindestanteil negativer Bewertungen an allen Bewertungen einer Annotation zur automatischen Ablehnung",
  },
});
watch(file, () => {
  if (!file.value) contents.value = undefined;

  switch (fileType.value) {
    case "REDCap": {
      Papa.parse(file.value, {
        complete: (result) => {
          contents.value = {
            data: result.data as { [index: string]: string }[],
            errors: result.errors,
          };
        },
        header: true,
        worker: true,
        skipEmptyLines: true,
      });
      break;
    }
    case "OpenClinica": {
      const reader = new FileReader();
      reader.onload = (data) => {
        try {
          const wb = read(data, { type: "buffer" });
          const ws = wb.Sheets["Items"]!;
          const items: { [index: string]: string }[] = utils.sheet_to_json(ws, {
            header: "A",
            skipHidden: true,
          });
          contents.value = { data: items, errors: [] };
        } catch (e) {
          console.error(e);
          contents.value = {
            data: [],
            errors: [
              {
                type: "Fehler!",
                message: "Unbekannter Fehler beim Parsen der XLS-Datei aufgetreten.",
              },
            ],
          };
        }
      };
      reader.onerror = console.error;
      reader.readAsText(file.value);
      break;
    }
    default: {
      console.error("Unknown type of input file");
    }
  }
});

const redcapFieldType = (datum: { [index: string]: string }) => {
  const t = datum?.["Field Type"] as string;
  switch (t) {
    case "yesno":
      return "radio";
    default:
      return t;
  }
};

const parsedDataREDCap = () =>
  contents.value?.data.map(
    (datum) =>
      ({
        id: undefined,
        variableName: datum["Variable / Field Name"] as string,
        label: datum["Field Label"] as string,
        type: redcapFieldType(datum),
        choices:
          datum["Choices, Calculations, OR Slider Labels"] &&
          ["dropdown", "checkbox", "radio", "slider"].includes(datum["Field Type"] as string)
            ? (datum["Choices, Calculations, OR Slider Labels"] as string)
                .split(" | ")
                .map((choice) => choice.split(", ", 2))
                .map((choice) => ({
                  id: undefined,
                  code: choice[0],
                  name: choice[1] ?? choice[0],
                  annotations: [],
                }))
            : (datum["Field Type"] as string) === "yesno"
              ? ["yes", "no"].map((choice, i) => ({
                  id: undefined,
                  code: i,
                  name: choice,
                  annotations: [],
                }))
              : undefined,
        metadata: Object.keys(datum)
          .filter(
            (key) =>
              !["Variable / Field Name", "Field Label", "Field Type"].includes(key) &&
              (key !== "Choices, Calculations, OR Slider Labels" ||
                !["dropdown", "checkbox", "radio", "slider"].includes(
                  datum["Field Type"] as string,
                )),
          )
          .filter((key) => datum[key])
          .map((key) => ({ key, value: datum[key] })),
        annotations: [],
      }) as Variable,
  );

const parsedDataOpenClinica = () =>
  contents.value?.data.map(
    (datum) =>
      ({
        id: undefined,
        variableName: datum["ITEM_NAME*"] as string,
        label: datum["DESCRIPTION_LABEL*"] as string,
        type: datum["DATA_TYPE*"] as string,
        choices: datum["RESPONSE_OPTIONS_TEXT"]
          ? (datum["RESPONSE_OPTIONS_TEXT"] as string)
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
          .filter(
            (key) =>
              !["ITEM_NAME*", "DESCRIPTION_LABEL*", "DATA_TYPE*", "RESPONSE_OPTIONS_TEXT"].includes(
                key,
              ),
          )
          .filter((key) => datum[key])
          .map((key) => ({ key, value: datum[key] })),
        annotations: [],
      }) as Variable,
  );

const parsedData = computed(() => {
  switch (fileType.value) {
    case "REDCap":
      return parsedDataREDCap();
    case "OpenClinica":
      return parsedDataOpenClinica();
    default:
      return undefined;
  }
});

const headers = [
  {
    title: "Variablenname",
    key: "variableName",
  },
  {
    title: "Beschreibung",
    key: "label",
  },
  {
    title: "Feld-/Datentyp",
    key: "type",
  },
  {
    title: "Optionen",
    key: "choices",
    value: (item: Variable) =>
      (item.choices?.map((choice) => `${choice.code}. ${choice.name}`).join("; ") ?? "-") ||
      "(keine)",
  },
  {
    title: "Weitere Informationen",
    key: "metadata",
    value: (item: Variable) =>
      item.metadata.map((pair) => `${pair.key}: ${pair.value}`).join(";") || "-",
  },
];

const loading: Ref<boolean> = ref(false);
const response: Ref<Response | undefined> = ref();
const create = async () => {
  if (!parsedData.value) return;
  const u = await auth.getUser();
  if (!u?.access_token) return;
  loading.value = true;
  response.value = await fetch(`${getEnv("VITE_API_BASE")}/questionnaire`, {
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
      valueSets: valueSets.value
        .map((v) => v.set)
        .filter((v) => !!v)
        .map((v) => ({
          display: v.display,
          server: v.server,
          valueSet: v.valueSet,
        })) as FhirValueSetDefinition[],
    }),
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${u.access_token}`,
    },
  });
  loading.value = false;
};

watch(response, async (val) => {
  if (val?.ok) {
    const body = await val.text();
    const id = Number(body);
    if (!body || !Number.isInteger(id)) return;
    return router.push({
      name: "single-project",
      params: {
        pid: id,
        title: clean(title.value),
      },
    });
  }
});

const notEmpty = ref([(v: string | unknown[]) => !!v.length || "darf nicht leer sein"]);
const atLeastOneDE = ref([
  () => !!parsedData.value?.length || "darf nicht leer sein; muss mind. ein Datenelement enthalten",
]);
const participantUser = ref([
  (v: string) => !!v.length || "darf nicht leer sein",
  (v: string) =>
    participants.value.reduce((acc, w) => Number(v === w.username) + acc, 0) === 1 ||
    "darf nur 1x vorkommen",
]);

const sliderRules = ref([
  (v: number) => 0 <= v || "zu klein",
  (v: string) => /^[0-9]+$/.test(v) || "invalider Wert",
  (v: number) => v <= 100 || "zu groß",
]);
const fracSliderRules = ref([
  ...sliderRules.value,
  (/* _: number */) =>
    options.value.posRatio.val + options.value.negRatio.val >= 100 ||
    !options.value.posRatio.use ||
    !options.value.negRatio.use ||
    "müssen zusammen mind. 100% sein",
]);
</script>

<template>
  <v-sheet width="1000" class="sheet">
    <v-form :disabled="loading" @submit.prevent="create">
      <h1>Projekt erstellen</h1>
      <v-stepper-vertical v-model="step" :mandatory="false" multiple non-linear hide-actions>
        <v-stepper-vertical-item title="Allgemeine Informationen" value="1" editable>
          <v-text-field v-model="title" :rules="notEmpty" label="Titel" required></v-text-field>
          <v-textarea v-model="description" label="Beschreibung"></v-textarea>
        </v-stepper-vertical-item>
        <v-stepper-vertical-item
          title="Datenelemente"
          subtitle="Import des Data Dictionaries."
          value="2"
          editable
        >
          <v-select
            label="Typ vom Data Dictionary"
            :rules="notEmpty"
            :items="['REDCap', 'OpenClinica']"
            v-model="fileType"
          ></v-select>
          <v-file-input
            v-if="fileType === 'REDCap'"
            v-model="rcFile"
            accept=".csv"
            :rules="atLeastOneDE"
            label="Data Dictionary eingeben"
          ></v-file-input>
          <v-file-input
            v-else-if="fileType === 'OpenClinica'"
            v-model="ocFile"
            accept=".xls"
            :rules="atLeastOneDE"
            label="Data Dictionary eingeben"
          ></v-file-input>
        </v-stepper-vertical-item>
        <v-stepper-vertical-item
          title="FHIR value sets"
          subtitle="In diesen Value Sets wird nach Termen gesucht."
          value="3"
          editable
        >
          <v-col fluid>
            <value-set-selector
              v-for="(row, i) in valueSets"
              :key="row.id"
              v-model="row.set"
              :disabled="loading"
              :display-controls="valueSets.length > 1"
              @remove="valueSets.splice(i, 1)"
            />
            <v-btn
              variant="text"
              :disabled="loading"
              :prepend-icon="mdiPlus"
              @click="valueSets.push({ id: setKey(), set: undefined })"
            >
              Hinzufügen
            </v-btn>
          </v-col>
        </v-stepper-vertical-item>
        <v-stepper-vertical-item
          title="Mitwirkende"
          subtitle="Wer hat welche Rechte?"
          value="4"
          editable
        >
          <v-col fluid>
            <v-row v-for="(row, i) in participants" :key="participants.length + i" gap="10">
              <v-col cols="6">
                <v-text-field
                  v-model="row.username"
                  :rules="participantUser"
                  label="Nutzername"
                  :disabled="i === 0 && !!user?.preferred_username"
                ></v-text-field>
              </v-col>
              <v-col :cols="participants.length > 1 ? 5 : 6">
                <v-select
                  label="Rolle"
                  :rules="notEmpty"
                  :items="roles"
                  v-model="row.roles"
                  multiple
                ></v-select>
              </v-col>
              <v-col v-if="participants.length > 1" cols="1">
                <v-icon-btn :icon="mdiMinus" @click="participants.splice(i, 1)"></v-icon-btn>
              </v-col>
            </v-row>
            <v-row>
              <v-btn
                variant="text"
                :disabled="loading"
                :prepend-icon="mdiAccountPlus"
                @click="participants.push({ username: '', roles: [] })"
                >Hinzufügen</v-btn
              >
              <v-btn
                variant="text"
                :disabled="loading"
                :prepend-icon="mdiAccountMinus"
                @click="participants.pop()"
                v-if="participants.length > 1"
                >Entfernen</v-btn
              >
            </v-row>
          </v-col>
        </v-stepper-vertical-item>
        <v-stepper-vertical-item
          value="5"
          title="Akzeptanzbedingungen"
          subtitle="(optional)"
          editable
        >
          <p>
            Wenn die Bedingungen erfüllt sind, kann eine Annotation automatisch als akzeptiert oder
            abgelehnt markiert werden. Wenn nicht, geht das natürlich immer noch manuell. Die
            Bedingungen hängen von der Anzahl der positiver bzw. negativer Bewertungen oder vom
            Anteil an allen Bewertungen des Annotationskandidaten ab. Diese anders zu konfigurieren
            ergibt nur Sinn, wenn auch regelmäßig mehrere Bewertungen pro Annotation erwaret sind.
            Bei nur einer erwarteten Bewertung reichen die Standardwerte. Um eine Bedingung außen
            vor zu lassen, einfach deaktivieren.
          </p>
          <br />
          <v-col fluid>
            <v-row v-for="row in options" :key="row.label" fluid>
              <v-col cols="12">
                <i>{{ row.label }}</i>
              </v-col>
              <v-col cols="2">
                <v-checkbox v-model="row.use" label="Aktivieren"></v-checkbox>
              </v-col>
              <v-col cols="10">
                <v-slider
                  v-model="row.val"
                  :disabled="!row.use"
                  :step="1"
                  :min="0"
                  :max="100"
                  :rules="row.fraction ? fracSliderRules : sliderRules"
                  thumb-label="always"
                >
                  <template #thumb-label="{ modelValue }">
                    {{ modelValue }}<template v-if="row.fraction">%</template>
                  </template>
                  <template #append>
                    <v-text-field
                      v-model="row.val"
                      density="compact"
                      style="width: 90px; max-width: fit-content; margin-left: 20px"
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
        </v-stepper-vertical-item>
      </v-stepper-vertical>
      <v-btn
        :disabled="loading"
        :loading="loading"
        size="x-large"
        type="submit"
        color="primary"
        block
      >
        Erstellen
      </v-btn>
    </v-form>
  </v-sheet>
  <v-sheet width="1000" v-if="contents" class="sheet">
    <h1>Daten</h1>
    <v-data-table
      v-if="parsedData?.length"
      :headers="headers"
      :items="parsedData"
      density="compact"
      :items-per-page="itemsPerPage"
    ></v-data-table>
    <p v-else><i>Keine Daten gefunden.</i></p>
    <template v-if="contents.errors?.length">
      <h1>Fehler</h1>
      <v-list>
        <v-list-item
          v-for="(error, i) in contents.errors"
          :key="i"
          :title="`${error.type}: ${error.message}`"
          color="danger"
        >
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
  padding-top: 0;
  margin-top: 20px;
}
</style>
