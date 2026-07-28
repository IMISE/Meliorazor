<script setup lang="ts">
import { computed, inject, ref, watch, type Ref } from "vue";
import { mdiAccountMinus, mdiAccountPlus, mdiMinus } from "@mdi/js";
import type { Group, Participant } from "@/types/types";
import { Role, roles } from "@/types/enums";
import { useRouter } from "vue-router";
import type { IdTokenClaims } from "oidc-client-ts";
import { clean } from "@/utils/utils";
import BaseComponent from "@/components/wrappers/BaseComponent.vue";
import { createGroup, getGroup } from "@/utils/api";
import GroupSelector from "@/components/groups/GroupSelector.vue";

const user = inject<Ref<IdTokenClaims | undefined>>("user");
const router = useRouter();

const parentGroupId = computed(() =>
  router.currentRoute.value.params.gid ? Number(router.currentRoute.value.params.gid) : undefined,
);
const parentGroup: Ref<Group | undefined> = ref();

watch(parentGroupId, async (newId, oldId) => {
  parentGroup.value = await getGroup(newId, oldId);
});

const name = ref("");
const description = ref("");
const website = ref();

// default set of participants: 1) ourselves and 2) everyone above us
const myParticipant = computed(() => ({
  username: user?.value?.preferred_username ?? "",
  roles: [Role.ADMIN, Role.INFO],
}));
const parentParticipants = computed(
  () => JSON.parse(JSON.stringify(parentGroup.value?.participants ?? [])) as Participant[],
);
const defaultParticipants = computed(() => [
  myParticipant.value,
  ...parentParticipants.value.filter((p) => p.username !== myParticipant.value.username),
]);
const participants: Ref<Participant[]> = ref([]);
watch(
  defaultParticipants,
  () => (participants.value = JSON.parse(JSON.stringify(defaultParticipants.value))),
);

const loading: Ref<boolean> = ref(false);
const response: Ref<Response | undefined> = ref();
const create = async () => {
  loading.value = true;
  await createGroup({
    name: name.value,
    description: description.value,
    website: website.value || null,
    participants: participants.value,
    parentGroupId: parentGroupId.value ?? null,
  });
  loading.value = false;
};

watch(response, async (val) => {
  if (val?.ok) {
    const body = await val.text();
    const id = Number(body);
    if (!body || !Number.isInteger(id)) return;
    return router.push({
      name: "single-group",
      params: {
        gid: id,
        name: clean(name.value),
      },
    });
  }
});

const notEmpty = ref([(v: string | unknown[]) => !!v.length || "darf nicht leer sein"]);
const participantUser = ref([
  (v: string) => !!v.length || "darf nicht leer sein",
  (v: string) =>
    participants.value.reduce((acc, w) => Number(v === w.username) + acc, 0) === 1 ||
    "darf nur 1x vorkommen",
]);
</script>

<template>
  <base-component :back-button-link="{ name: 'projects' }">
    <v-sheet width="1000" class="sheet">
      <v-form :disabled="loading" @submit.prevent="create">
        <h1>Gruppe erstellen</h1>
        <template v-if="parentGroup">
          <h3>Übergeordnete Gruppe</h3>
          <group-selector v-model="parentGroup" :options="[parentGroup]" :editable="false" />
        </template>
        <h3>Allgemeine Informationen</h3>
        <v-text-field v-model="name" :rules="notEmpty" label="Titel" required></v-text-field>
        <v-textarea v-model="description" label="Beschreibung"></v-textarea>
        <v-text-field v-model="website" label="Website (optional)"></v-text-field>
        <h3>Mitwirkende</h3>
        <p>Wer hat welche Rechte in der Gruppe und den zugehörigen Projekten?</p>
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
              <v-select v-model="row.roles" label="Rolle" :rules="notEmpty" :items="roles" multiple>
                <template #item="{ props, item }">
                  <v-list-item
                    v-bind="props"
                    :disabled="(i === 0 && item.value === 'ADMIN') || item.value === 'INFO'"
                  >
                    <template #prepend>
                      <v-checkbox
                        hide-details
                        single-line
                        :model-value="row.roles.includes(item.value as Role)"
                      />
                    </template>
                  </v-list-item>
                </template>
              </v-select>
            </v-col>
            <v-col
              v-if="
                participants.length > 1 &&
                // can't remove ourselves
                i !== 0 &&
                !(
                  // parent admins can't be removed ...
                  (
                    parentGroup?.participants.some(
                      (p) => p.username === row.username && row.roles.includes(Role.ADMIN),
                    ) &&
                    // ... unless someone put them there twice
                    !participants.some((p, j) => p.username === row.username && i !== j)
                  )
                )
              "
              cols="1"
            >
              <v-icon-btn :icon="mdiMinus" @click="participants.splice(i, 1)"></v-icon-btn>
            </v-col>
          </v-row>
          <v-row>
            <v-btn
              variant="text"
              :disabled="loading"
              :prepend-icon="mdiAccountPlus"
              @click="participants.push({ username: '', roles: [Role.INFO] })"
            >
              Hinzufügen
            </v-btn>
            <v-btn
              variant="text"
              :disabled="loading"
              :prepend-icon="mdiAccountMinus"
              @click="participants.pop()"
              v-if="participants.length > 1"
            >
              Entfernen
            </v-btn>
          </v-row>
        </v-col>
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
  </base-component>
</template>

<style scoped>
.sheet {
  padding: 20px;
  margin: 50px;
  padding-top: 0;
  margin-top: 20px;
}
</style>
