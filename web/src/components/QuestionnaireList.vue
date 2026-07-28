<script setup lang="ts">
import type { Questionnaire } from "@/types/types";
import { inject, type Ref } from "vue";
import type { IdTokenClaims } from "oidc-client-ts";
import { Role } from "@/types/enums.ts";
import { mdiArrowRight } from "@mdi/js";
import { clean, shorten } from "@/utils/utils";
import DeleteButton from "./controls/DeleteButton.vue";
import { deleteProject } from "@/utils/api.ts";

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
  const status = await deleteProject(q.id);
  if (status) {
    emit("delete", q);
  } else {
    // TODO: feedback
  }
};
</script>

<template>
  <v-list v-if="questionnaires.length" max-width="480px">
    <v-list-item
      v-for="(q, i) in questionnaires"
      :key="`${questionnaires.length}-${i}-${q.id}`"
      :href="displayLink ? `/projects/${q.id}-${clean(q.title.substring(0, 20))}/` : undefined"
      :append-icon="
        displayLink ? mdiArrowRight : undefined /* only displayed if displayControls is false */
      "
    >
      <v-list-item-title>{{ q.title }}</v-list-item-title>
      <v-list-item-subtitle v-if="q.description">
        {{ shorten(q.description, 50) }}
      </v-list-item-subtitle>
      <template v-if="displayControls" #append>
        <delete-button
          v-if="
            q.participants
              .find((p) => p.username === user?.preferred_username)
              ?.roles.includes(Role.ADMIN)
          "
          @delete="deleteQuestionnaire(q)"
        />
      </template>
    </v-list-item>
  </v-list>
  <p v-else><i>Keine Projekte gefunden.</i></p>
</template>
