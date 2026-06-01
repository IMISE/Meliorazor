<script setup lang="ts">
import { CommentType } from '@/stores/enums';
import type { AnnotationComment } from '@/stores/types';
import { mdiCheckboxBlankCircleOutline, mdiCheckCircleOutline, mdiCloseCircleOutline } from '@mdi/js';

const { comment } = defineProps<{
  comment: AnnotationComment,
}>();

</script>

<template>
  <div class="comment">
    <div v-if="comment.type === CommentType.ACCEPT">
      <v-icon :icon="mdiCheckCircleOutline" color="green"></v-icon>
      <h3>Angenommen</h3>
    </div>
    <div v-else-if="comment.type === CommentType.NOTE">
      <v-icon :icon="mdiCheckboxBlankCircleOutline" color="grey"></v-icon>
      <h3>Bemerkung</h3>
    </div>
    <div v-else>
      <v-icon :icon="mdiCloseCircleOutline" color="red"></v-icon>
      <h3>Abgelehnt</h3>
    </div>
    <div class="comment-contents">{{ comment.contents }}</div>
    <div class="comment-footer">
      <span class="comment-author">{{ comment.authorName }}</span> am
      <v-tooltip :text="new Date(comment.submittedAt).toTimeString()">
        <template #activator="props">
          <span v-bind="props" class="comment-date">{{ new Date(comment.submittedAt).toDateString() }}</span>
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
    background-color: #FFD0D0;
  }
  .comment-footer {
    font-size: 10pt;
  }
  .comment-date {
    color: grey;
  }
</style>
