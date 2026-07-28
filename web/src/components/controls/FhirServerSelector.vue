<script setup lang="ts">
import { isFhirServer, isUrl } from "@/utils/utils";
import { mdiLocationEnter, mdiPencil } from "@mdi/js";
import { ref } from "vue";

const model = defineModel<string>();

const server = ref(model.value ?? "");
const loading = ref(false);
const selected = ref(false);

const selectServer = async () => {
  if (selected.value) return;

  loading.value = true;
  const isValid = await isFhirServer(server.value);
  if (isValid) {
    // TODO: error handling
    selected.value = true;
    model.value = server.value;
  }
  loading.value = false;
};

const serverRules = ref([
  (v: string) => !!v.length || "darf nicht leer sein",
  (v: string) => isUrl(v) || "muss valide URL sein",
]);
</script>

<template>
  <v-text-field
    v-model="server"
    :readonly="selected || loading"
    :loading="loading"
    :rules="serverRules"
    label="FHIR Server-URL"
    :prepend-icon="isUrl(server) && !selected ? mdiLocationEnter : undefined"
    :append-inner-icon="selected ? mdiPencil : undefined"
    @click:prepend="selectServer"
    @keydown.enter="loading ? undefined : selectServer"
    @click:append-inner="selected = false"
  />
</template>
