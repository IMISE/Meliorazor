<script setup lang="ts">
import type { Questionnaire, RichAction } from "@/types/types";
import { computed, inject, ref, watch, type Ref } from "vue";
import QuestionnaireList from "./QuestionnaireList.vue";
import { getProjects, getRecentActions } from "@/utils/api.ts";
import type { IdTokenClaims } from "oidc-client-ts";
import SingleRichAction from "./SingleRichAction.vue";

const user = inject("user") as Ref<IdTokenClaims | undefined>;
const props = defineProps<{ username: string | undefined; isHome: boolean }>();

const questionnaires: Ref<Questionnaire[]> = ref([]);
const userQuestionnaires = computed(() =>
  questionnaires.value.filter((q) => q.participants.some((p) => p.username === props.username)),
);
const recentActions: Ref<RichAction[]> = ref([]);
const loading = ref(false);

watch(
  props,
  async (newVal) => {
    if (newVal.username) {
      loading.value = true;
      questionnaires.value = (await getProjects()) ?? [];
      recentActions.value = (await getRecentActions(newVal.username)) ?? [];
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
  <template v-if="username">
    <v-row fluid>
      <v-col :cols="myProfile ? 6 : 12">
        <h1>Letzte Aktivitäten</h1>
        <v-list v-if="recentActions.length">
          <single-rich-action
            v-for="action in recentActions"
            :key="action.action.id"
            :rich-action="action"
          />
        </v-list>
        <i v-else>Keine Aktivitäten in den letzten 7 Tagen gefunden</i>
      </v-col>
      <v-col v-if="myProfile" cols="6">
        <h1>Handlungsbedarf</h1>
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
    <h1><template v-if="myProfile">Meine </template>Annotationen und Kommentare</h1>
    <h1 v-if="false"><template v-if="myProfile">Meine </template>Statistik</h1>
  </template>
  <template v-else-if="isHome">
    Bitte loggen Sie sich ein, um auf diese Seite zugreifen zu können.
  </template>
  <template v-else>Name nicht gefunden.</template>
</template>
