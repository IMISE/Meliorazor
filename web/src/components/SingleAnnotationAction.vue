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
  mdiCheckboxBlankCircleOutline,
  mdiCheckCircleOutline,
  mdiCloseCircleOutline,
  mdiFloppy,
  mdiPencilPlus,
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
      <v-icon :icon="mdiFloppy"></v-icon>
      <h3>Zu Version hinzugefügt</h3>
      <router-link
        :to="{
          name: 'single-version',
          params: {
            pid: projectId,
            title: clean(projectTitle),
            key: (action as AnnotationVersioning).versionKey,
          },
        }"
      >{{ (action as AnnotationVersioning).versionKey }}</router-link>
    </div>
    <div v-else-if="action.actionType === 'submission'">
      <v-icon :icon="mdiPencilPlus"></v-icon>
      <h3>{{ action.authorName }} hat annotiert</h3>
      <ul>
        <li>Konfidenz: {{ (action as AnnotationSubmission).confidence }}</li>
        <li v.if="action.description">Kommentar: {{ action.description }}</li>
      </ul>
    </div>
    <div v-else-if="(action as AnnotationComment).commentType === CommentType.ACCEPT">
      <v-icon :icon="mdiCheckCircleOutline" color="green"></v-icon>
      <h3>Angenommen</h3>
    </div>
    <div v-else-if="(action as AnnotationComment).commentType === CommentType.REJECT">
      <v-icon :icon="mdiCloseCircleOutline" color="red"></v-icon>
      <h3>Abgelehnt</h3>
    </div>
    <div v-else>
      <v-icon :icon="mdiCheckboxBlankCircleOutline" color="grey"></v-icon>
      <h3>Bemerkung</h3>
    </div>
    <div class="comment-contents">{{ action.description }}</div>
    <div class="comment-footer">
      <span class="comment-author">{{ action.authorName }}</span> am
      <v-tooltip :text="new Date(action.submittedAt).toTimeString()">
        <template #activator="props">
          <span v-bind="props" class="comment-date">{{
            new Date(action.submittedAt).toDateString()
          }}</span>
        </template>
      </v-tooltip>
    </div>
  </div>
</template>

<style scoped>
.comment {
  border-left: 1px solid black;
  width: 240px;
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
