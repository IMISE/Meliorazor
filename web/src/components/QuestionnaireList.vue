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
  displayLink: boolean;
}>();

const user = inject<Ref<IdTokenClaims | undefined>>("user");

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
  <v-list v-if="questionnaires.length" max-width="480px">
    <v-list-item
      v-for="(q, i) in questionnaires"
      :key="`${questionnaires.length}-${i}-${q.id}`"
      :href="displayLink ? `/projects/${q.id}-${clean(q.title.substring(0, 20))}/` : undefined"
    >
      <v-list-item-title>{{ q.title }}</v-list-item-title>
      <v-list-item-subtitle v-if="q.description">
        {{ q.description.length > 50 ? q.description.substring(0, 47) + "…" : q.description }}
      </v-list-item-subtitle>
      <template v-if="displayControls" #append>
        <v-dialog max-width="600px">
          <template #activator="{ props }">
            <v-icon-btn
              v-if="
                q.participants
                  .find((p) => p.username === user?.preferred_username)
                  ?.roles.includes(Role.ADMIN)
              "
              v-bind="props"
              :icon="mdiDeleteOutline"
              @click.prevent
            />
          </template>
          <template #default="{ isActive }">
            <v-card title="Wirklich löschen?">
              <v-card-text>Soll das Projekt wirklich gelöscht werden?</v-card-text>
              <v-card-actions>
                <v-spacer></v-spacer>
                <v-btn color="primary" @click="isActive.value = false">Abbrechen</v-btn>
                <v-btn
                  color="error"
                  variant="outlined"
                  @click="
                    () => {
                      isActive.value = false;
                      deleteQuestionnaire(q);
                    }
                  "
                >
                  Löschen
                </v-btn>
              </v-card-actions>
            </v-card>
          </template>
        </v-dialog>
      </template>
    </v-list-item>
  </v-list>
  <p v-else><i>Keine Projekte gefunden.</i></p>
</template>
