<script setup lang="ts">
import GroupTree from "@/components/groups/GroupTree.vue";
import type { Group } from "@/types/types";
import { getGroups } from "@/utils/api";
import { mdiPlus, mdiRefresh } from "@mdi/js";
import { onMounted, ref, type Ref } from "vue";

const groups: Ref<Group[]> = ref([]);
const loading = ref(false);

const fetchGroups = async () => {
  loading.value = true;
  groups.value = (await getGroups()) ?? [];
  loading.value = false;
};

onMounted(fetchGroups);
</script>

<template>
  <base-component :back-button-link="{ name: 'home' }">
    <h1>Gruppen</h1>
    <br />
    <h3 v-if="loading">Lädt &hellip;</h3>
    <group-tree v-else :groups="groups" />
    <v-row>
      <v-btn
        :prepend-icon="mdiPlus"
        color="primary"
        @click="$router.push({ name: 'create-group' })"
      >
        Neu
      </v-btn>
      <v-btn @click="fetchGroups" :prepend-icon="mdiRefresh" :disabled="loading" :loading="loading">
        Aktualisieren
      </v-btn>
    </v-row>
  </base-component>
</template>
