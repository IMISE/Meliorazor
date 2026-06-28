<script setup lang="ts">
import auth from '@/auth';
import { CommentType } from '@/types/enums';
import { mdiCancel, mdiCheck, mdiCircleOutline } from '@mdi/js';
import { ref, watch, type Ref } from 'vue';

const emit = defineEmits(["reload"])

const loading = ref(false);
const response: Ref<Response | undefined> = ref(undefined);

const type: Ref<CommentType> = ref(CommentType.NOTE);
const comment = ref("");

const { annotationId, dataElementId } = defineProps<{
  annotationId: string | number,
  dataElementId: string | number,
}>();

const giveFeedback = async () => {
  if (loading.value) return;
  loading.value = true;
  try {
    const u = await auth.getUser();
    if (!u?.access_token) return;

    response.value = await fetch(`${(window as any).config?.VITE_API_BASE}/dataElement/${dataElementId}/annotation/${annotationId}/comment`, {
      method: "POST",
      body: JSON.stringify({
        authorName: u.profile.preferred_username,
        type: type.value,
        contents: comment.value,
      }),
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${u.access_token}`,
      },
    });
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
}

watch(response, async (res) => {
  if (!res?.ok) return;
  const answer = await res.text();
  if (!Number.isNaN(Number(answer))) {
    emit("reload");
  }
});


</script>

<template>
  <v-form :disabled="loading" @submit.prevent="giveFeedback">
    <v-textarea v-model="comment" label="Kommentar" />
    <v-row cols="12">
      <v-col cols="4">
        <v-btn
          :prepend-icon="mdiCheck"
          @click="type = CommentType.ACCEPT"
          type="submit"
          :disabled="loading"
          :loading="loading"
          block
        >Akzeptieren</v-btn>
      </v-col>
      <v-col cols="4">
        <v-btn
          :prepend-icon="mdiCircleOutline"
          @click="type = CommentType.NOTE"
          type="submit"
          :disabled="loading"
          :loading="loading"
          block
        >Kommentieren</v-btn>
      </v-col>
      <v-col cols="4">
        <v-btn
          :prepend-icon="mdiCancel"
          @click="type = CommentType.REJECT"
          type="submit"
          :disabled="loading"
          :loading="loading"
          block
        >Ablehnen</v-btn>
      </v-col>
    </v-row>
  </v-form>
</template>
