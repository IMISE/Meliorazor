<script setup lang="ts">
import type { GroupInvitation, Questionnaire, RichAction } from "@/types/types";
import { computed, inject, ref, watch, type Ref } from "vue";
import QuestionnaireList from "./QuestionnaireList.vue";
import {
  acceptInvitation,
  declineInvitation,
  getInvitations,
  getProjects,
  getRecentActions,
} from "@/utils/api.ts";
import type { IdTokenClaims } from "oidc-client-ts";
import SingleRichAction from "./SingleRichAction.vue";
import { mdiCheck, mdiChevronDown, mdiChevronRight, mdiClose } from "@mdi/js";
import { shorten } from "@/utils/utils.ts";

const user = inject("user") as Ref<IdTokenClaims | undefined>;
const props = defineProps<{ username: string | undefined; isHome: boolean }>();

const questionnaires: Ref<Questionnaire[]> = ref([]);
const userQuestionnaires = computed(() =>
  questionnaires.value.filter((q) => q.participants.some((p) => p.username === props.username)),
);
const recentActions: Ref<RichAction[]> = ref([]);
const loading = ref(false);

const recentActionsLimit = ref(5);
const recentActionsLimited = computed(() =>
  [...recentActions.value].reverse().slice(0, recentActionsLimit.value),
);

const invitations: Ref<{ invite: GroupInvitation; loading: boolean }[]> = ref([]);

const fetchInvitations = async () => {
  if (user.value?.preferred_username === props.username) {
    const _invitations = (await getInvitations(user.value!.preferred_username!)) ?? [];
    invitations.value = _invitations.map((invite) => ({ invite, loading: false }));
  }
};

watch(
  props,
  async (newVal) => {
    if (newVal.username) {
      loading.value = true;
      questionnaires.value = (await getProjects()) ?? [];
      recentActions.value = (await getRecentActions(newVal.username)) ?? [];
      await fetchInvitations();
      loading.value = false;
    } else {
      questionnaires.value = [];
      recentActions.value = [];
    }
  },
  { immediate: true },
);

const myProfile = computed(
  () => (!user.value && props.isHome) || props.username === user.value?.preferred_username,
);
</script>
<!--
- "my (or their) annotations"
- "my (or their) comments"
- "my (or their) projects"
- "my (or their) data"
- stats?
- latest actions? (history)
=> acts as homepage
-->

<template>
  <template v-if="username && !loading">
    <v-row fluid>
      <v-col :cols="myProfile ? 6 : 12">
        <h1>Letzte Aktivitäten</h1>
        <v-list v-if="recentActions.length">
          <single-rich-action
            v-for="action in recentActionsLimited"
            :key="action.action.id"
            :rich-action="action"
          />
          <v-list-item
            v-if="recentActionsLimit < recentActions.length"
            :prepend-icon="mdiChevronDown"
            :title="
              Math.min(5, recentActions.length - recentActionsLimit) + ' weitere Aktionen anzeigen'
            "
            link
            @click="recentActionsLimit += 5"
          />
        </v-list>
        <i v-else>Keine Aktivitäten in den letzten 7 Tagen gefunden.</i>
      </v-col>
      <v-col v-if="myProfile" cols="6">
        <h1 v-if="false">Handlungsbedarf</h1>
        <template v-if="invitations.length">
          <h1>Einladungen</h1>
          <v-list lines="one">
            <v-list-item v-for="row in invitations" :key="row.invite.id">
              <v-list-item-title>
                <template v-for="(name, i) of row.invite.names" :key="{ name, i }">
                  {{ name }}
                  <v-icon
                    v-if="i !== row.invite.names.length - 1"
                    color="primary"
                    :icon="mdiChevronRight"
                  />
                </template>
              </v-list-item-title>
              <v-list-item-subtitle>
                {{ shorten(row.invite.description, 50) }}
                <template v-if="row.invite.website">
                  <br />
                  <a :href="row.invite.website">{{ row.invite.website }}</a>
                </template>
              </v-list-item-subtitle>
              <template #append>
                <v-icon-btn
                  @click="
                    async () => {
                      row.loading = true;
                      await acceptInvitation(row.invite.id);
                      await fetchInvitations();
                      row.loading = false;
                    }
                  "
                  :icon="mdiCheck"
                  color="success"
                  :loading="row.loading"
                  :disabled="row.loading"
                />
                <v-icon-btn
                  @click="
                    async () => {
                      row.loading = true;
                      await declineInvitation(row.invite.id);
                      await fetchInvitations();
                      row.loading = false;
                    }
                  "
                  :icon="mdiClose"
                  color="error"
                  :loading="row.loading"
                  :disabled="row.loading"
                />
              </template>
            </v-list-item>
          </v-list>
        </template>
      </v-col>
    </v-row>
    <h1><template v-if="myProfile">Meine </template>Projekte</h1>
    <h3 v-if="loading">Lädt &hellip;</h3>
    <questionnaire-list
      v-else
      :display-controls="false"
      display-link
      :questionnaires="userQuestionnaires"
    />
    <h1 v-if="false"><template v-if="myProfile">Meine </template>Annotationen und Kommentare</h1>
    <h1 v-if="false"><template v-if="myProfile">Meine </template>Statistik</h1>
  </template>
  <template v-else-if="loading"> Lädt &hellip; </template>
  <template v-else-if="isHome">
    Bitte loggen Sie sich ein, um auf diese Seite zugreifen zu können.
  </template>
  <template v-else>Name nicht gefunden.</template>
</template>
