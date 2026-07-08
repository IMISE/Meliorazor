<script setup lang="ts">
import type { Questionnaire } from "@/types/types";
import { inject, type Ref } from "vue";
import type { IdTokenClaims } from "oidc-client-ts";
import auth from "@/auth.ts";
import getEnv from "@/utils/env.ts";
import { Role } from "@/types/enums.ts";
import { mdiDeleteOutline } from "@mdi/js";
import { clean } from "@/utils/utils";

const emit = defineEmits<{
  (e: "delete", v: Questionnaire): void;
}>();

defineProps<{
  questionnaires: Questionnaire[];
  displayControls: boolean;
}>();

const user = inject("user") as Ref<IdTokenClaims | undefined>;

const deleteQuestionnaire = async (q: Questionnaire) => {
  const u = await auth.getUser();
  if (!u?.access_token) return;
  const options = {
    method: "DELETE",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${u.access_token}`,
    },
  };

  try {
    const response = await fetch(`${getEnv("VITE_API_BASE")}/questionnaire/${q.id}`, options);
    if (response.ok) {
      emit("delete", q);
    }
  } catch (e) {
    console.error(e);
  }
};
</script>

<template>
  <v-list v-if="questionnaires.length">
    <v-list-item
      v-for="(q, i) in questionnaires"
      :key="`${questionnaires.length}-${i}-${q.id}`"
      :href="`/projects/${q.id}-${clean(q.title.substring(0, 20))}/`"
    >
      <v-list-item-title>{{ q.title }}</v-list-item-title>
      <v-list-item-subtitle v-if="q.description">
        {{ q.description.length > 50 ? q.description.substring(0, 47) + "…" : q.description }}
      </v-list-item-subtitle>
      <template #append>
        <v-icon-btn
          v-if="
            q.participants
              .find((p) => p.username === user?.preferred_username)
              ?.roles.includes(Role.ADMIN)
          "
          :icon="mdiDeleteOutline"
          @click.prevent="deleteQuestionnaire(q)"
        />
      </template>
    </v-list-item>
  </v-list>
  <p v-else>Keine Elemente.</p>
</template>
