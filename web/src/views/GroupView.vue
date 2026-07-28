<script setup lang="ts">
import FhirServerSelector from "@/components/controls/FhirServerSelector.vue";
import TooltipIcon from "@/components/icons/TooltipIcon.vue";
import { Role, roles } from "@/types/enums";
import type { Group, Participant, ServerDefinition } from "@/types/types";
import { editGroupParticipants, editGroupServers, getGroup } from "@/utils/api";
import { isUrl } from "@/utils/utils";
import { mdiAccount, mdiEmailOpenHeartOutline, mdiHexagram, mdiMinus, mdiPlus } from "@mdi/js";
import type { IdTokenClaims } from "oidc-client-ts";
import { ref, watch } from "vue";
import { computed, inject, type Ref } from "vue";
import { useRouter } from "vue-router";

const user = inject<Ref<IdTokenClaims | undefined>>("user");

const router = useRouter();
const groupId = computed(() => Number(router.currentRoute.value.params.gid));
// const groupName = computed(() => String(router.currentRoute.value.params.name));

const group: Ref<Group | undefined> = ref();
const loading = ref(false);

const tab = ref("projects");

watch(groupId, getGroup, { immediate: true });

// EDITING STUFF
/** editing */
const participantEdit: Ref<{ id: number; participant: Participant; original: boolean }[]> = ref([]);
const participantKey = ref(0);
const serverEdit: Ref<{ id: number; server: ServerDefinition | undefined }[]> = ref([]);
const serverKey = ref(0);
const loadingEdits = ref(false);
watch(
  group,
  (newVal) => {
    participantEdit.value = [
      ...(newVal?.participants?.map((p) => ({ ...p, invited: false })) ?? []),
      ...(newVal?.invitees?.map((p) => ({ ...p, invited: true })) ?? []),
    ].map((participant, id) => ({
      id,
      participant: { ...participant, invited: undefined } as Participant,
      invited: participant.invited,
      original: true,
    }));
    participantKey.value = participantEdit.value.length ?? 0;

    serverEdit.value = (newVal?.allowedServers ?? []).map((server, id) => ({ id, server }));
    serverKey.value = serverEdit.value.length ?? 0;
  },
  { deep: true, immediate: true },
);

const myRoles = computed(
  () =>
    group.value?.participants.find(
      (participant) => participant.username === user?.value?.preferred_username,
    )?.roles ?? [],
);
const isAdmin = computed(() => myRoles.value.includes(Role.ADMIN));

const editedParticipants = computed(
  () =>
    group.value?.participants.length === participantEdit.value.length &&
    participantEdit.value.every((p1) =>
      group.value?.participants.some(
        (p2) =>
          p1.participant.roles.length === p2.roles.length &&
          p1.participant.roles.every((role) => p2.roles.includes(role)),
      ),
    ),
);

const notEmpty = ref([(v: string | unknown[]) => !!v.length || "darf nicht leer sein"]);
const participantUser = ref([
  (v: string) => !!v.length || "darf nicht leer sein",
  (v: string) =>
    participantEdit.value.reduce((acc, w) => Number(v === w.participant.username) + acc, 0) === 1 ||
    "darf nur 1x vorkommen",
]);

const editParticipants = () =>
  editGroupParticipants(
    group.value!.id,
    participantEdit.value.map((row) => row.participant),
  );

const editServers = () =>
  editGroupServers(
    group.value!.id,
    serverEdit.value.map((row) => row.server).filter(isUrl) as string[],
  );
</script>

<template>
  <base-component :back-button-link="{ name: 'single-group' }">
    <v-sheet :min-height="200" class="sheet">
      <template v-if="group">
        <h1>{{ group.name }}</h1>
        <a v-if="group.website && isUrl(group.website)" :href="group.website">{{
          group.website
        }}</a>
        <p>{{ group.description }}</p>
        <v-tabs v-model="tab" color="primary">
          <v-tab value="single-group" text="Projekte" />
          <v-tab value="group-members" text="Mitglieder" />
          <v-tab value="group-servers" text="Freigeschaltene Server" />
        </v-tabs>
        <v-tabs-window v-model="tab">
          <v-tabs-window-item value="single-group">
            <h2>Projekte</h2>
            <questionnaire-list
              :questionnaires="group.projects"
              display-controls
              display-link
              @delete="getGroup(groupId)"
            />
          </v-tabs-window-item>
          <v-tabs-window-item value="group-members">
            <h2>Mitwirkende</h2>
            <p>Wer hat welche Rechte?</p>
            <v-form @submit.prevent="editParticipants">
              <h3>Mitglieder</h3>
              <v-row v-for="(row, i) in participantEdit" :key="row.id" class="participant-row">
                <div v-if="!isAdmin" style="align-self: stretch; text-align: right">
                  {{ row.participant.username }}
                </div>
                <v-text-field
                  v-else
                  v-model="row.participant.username"
                  :rules="participantUser"
                  label="Nutzername"
                  :disabled="row.original"
                >
                  <template v-if="row.participant.username" #prepend>
                    <tooltip-icon
                      v-if="
                        group?.participants.some((p) => p.username === row.participant.username)
                      "
                      text="Bestehendes Mitglied"
                      :icon="mdiAccount"
                    />
                    <tooltip-icon
                      v-else-if="
                        group?.invitees.some((p) => p.username === row.participant.username)
                      "
                      text="Eingeladen"
                      :icon="mdiEmailOpenHeartOutline"
                    />
                    <tooltip-icon v-else text="Neu" :icon="mdiHexagram" />
                  </template>
                </v-text-field>
                <v-select
                  v-model="row.participant.roles"
                  label="Rolle"
                  :rules="notEmpty"
                  :items="roles"
                  multiple
                  :readonly="!isAdmin"
                >
                  <template #item="{ props, item }">
                    <v-list-item
                      v-bind="props"
                      :disabled="
                        (row.original &&
                          (i === 0 || user?.preferred_username === row.participant.username) &&
                          item.value === 'ADMIN') ||
                        item.value === 'INFO'
                      "
                    >
                      <template #prepend>
                        <v-checkbox
                          hide-details
                          single-line
                          :model-value="row.participant.roles.includes(item.value as Role)"
                        />
                      </template>
                    </v-list-item>
                  </template>
                </v-select>
                <v-icon-btn
                  v-if="
                    i !== 0 &&
                    (user?.preferred_username !== row.participant.username || !row.original) &&
                    isAdmin
                  "
                  :icon="mdiMinus"
                  @click.prevent="participantEdit.splice(i, 1)"
                />
              </v-row>
              <v-btn
                v-if="isAdmin"
                variant="text"
                :disabled="loading"
                :prepend-icon="mdiPlus"
                @click="
                  participantEdit.push({
                    id: participantKey++,
                    participant: { username: '', roles: [Role.INFO] },
                    original: false,
                  })
                "
              >
                Hinzufügen
              </v-btn>
              <v-btn
                block
                type="submit"
                :disabled="loadingEdits || editedParticipants"
                :loading="loadingEdits"
              >
                Änderungen abschicken
              </v-btn>
            </v-form>
          </v-tabs-window-item>
          <v-tabs-window-item value="group-servers">
            <h2>Freigeschaltene FHIR-Server</h2>
            <v-form @submit.prevent="editServers">
              <fhir-server-selector
                v-for="(row, i) in serverEdit"
                :key="row.id"
                v-model="row.server"
                :disabled="false"
                :display-controls="serverEdit.length > 1"
                @remove="serverEdit.splice(i, 1)"
              />
              <v-btn
                variant="text"
                :disabled="loading"
                :prepend-icon="mdiPlus"
                @click="serverEdit.push({ id: serverKey++, server: undefined })"
              >
                Hinzufügen
              </v-btn>
              <v-btn
                block
                type="submit"
                :disabled="loadingEdits || editedParticipants"
                :loading="loadingEdits"
              >
                Änderungen abschicken
              </v-btn>
            </v-form>
          </v-tabs-window-item>
        </v-tabs-window>
      </template>
      <h3 v-else-if="loading">Lädt &hellip;</h3>
      <template v-else>
        <h3>Fehler</h3>
        <p>Fehler beim Abrufen des Projekts.</p>
      </template>
    </v-sheet>
  </base-component>
</template>

<style scoped>
.sheet {
  padding-left: 20%;
  padding-right: 20%;
}
</style>
