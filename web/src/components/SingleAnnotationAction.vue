<script setup lang="ts">
import { CommentType } from "@/types/enums";
import type {
  AnnotationAction,
  AnnotationComment,
  AnnotationSubmission,
  AnnotationVersioning,
} from "@/types/types";
import { clean } from "@/utils/utils";
import {
  mdiArrowRight,
  mdiCheckboxBlankCircleOutline,
  mdiCheckCircleOutline,
  mdiCloseCircleOutline,
  mdiPencilPlus,
  mdiTag,
} from "@mdi/js";

defineProps<{
  action: AnnotationAction;
  projectId: number;
  projectTitle: string;
}>();
</script>

<template>
  <div class="comment">
    <div v-if="action.actionType === 'versioning'">
      <h3>
        <v-icon :icon="mdiTag" />
        Zu Version <var>{{ (action as AnnotationVersioning).versionKey }}</var> hinzugefügt
      </h3>
      <router-link
        :to="{
          name: 'single-version',
          params: {
            pid: projectId,
            title: clean(projectTitle),
            key: (action as AnnotationVersioning).versionKey,
          },
        }"
      >
        Version ansehen <v-icon :icon="mdiArrowRight" />
      </router-link>
    </div>
    <div v-else-if="action.actionType === 'submission'">
      <h3>
        <v-icon :icon="mdiPencilPlus" />
        {{ action.authorName }} hat annotiert
      </h3>
      <small>{{ (action as AnnotationSubmission).confidence }}% Konfidenz</small>
    </div>
    <div v-else-if="(action as AnnotationComment).commentType === CommentType.ACCEPT">
      <h3><v-icon :icon="mdiCheckCircleOutline" color="green" /> Angenommen</h3>
    </div>
    <div v-else-if="(action as AnnotationComment).commentType === CommentType.REJECT">
      <h3><v-icon :icon="mdiCloseCircleOutline" color="red"></v-icon> Abgelehnt</h3>
    </div>
    <div v-else>
      <h3><v-icon :icon="mdiCheckboxBlankCircleOutline" color="grey"></v-icon> Bemerkung</h3>
    </div>
    <div v-if="action.description" class="comment-contents">{{ action.description }}</div>
    <div class="comment-footer">
      <span class="comment-author">{{ action.authorName }}</span> am
      <v-tooltip :text="new Date(action.submittedAt).toTimeString()">
        <template #activator="props">
          <span v-bind="props" class="comment-date">{{
            new Date(action.submittedAt).toLocaleDateString("de-DE")
          }}</span>
        </template>
      </v-tooltip>
    </div>
  </div>
</template>

<style scoped>
.comment {
  border-left: 1px solid black;
  overflow-wrap: break-word;
  margin: 20px;
  padding-left: 20px;
}
.comment-footer {
  font-size: 10pt;
}
.comment-date {
  color: grey;
}
</style>
