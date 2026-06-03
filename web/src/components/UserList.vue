<script setup lang="ts">
import type { Participant } from '@/types/types';
import { ref } from 'vue';
import UserInfo from './UserInfo.vue';

const props = defineProps<{
  users: Participant[];
}>();

const selection = ref({});

</script>

<template>
  <v-list>
    <v-list-item v-for="p in users" :subtitle="p.roles.join(', ')">
      <user-info :username="p.username" />
    </v-list-item>
  </v-list>
</template>
