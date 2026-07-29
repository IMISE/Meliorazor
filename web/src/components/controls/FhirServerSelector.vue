<script setup lang="ts">
import { isFhirServer, isUrl } from "@/utils/utils";
import { mdiLocationEnter, mdiMinus, mdiPencil } from "@mdi/js";
import { ref } from "vue";

const model = defineModel<string>();

defineProps<{
  allowRemove?: boolean;
}>();

defineEmits(["remove"]);

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

// if we started with a model, select it immediately
selectServer();

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
    :append-icon="allowRemove ? mdiMinus : undefined"
    @click:prepend="selectServer"
    @keydown.enter="loading ? undefined : selectServer"
    @click:append-inner="selected = false"
    @click:append="$emit('remove')"
  />
</template>
