<script setup lang="ts">
import type { Participant } from "@/types/types";
import UserInfo from "./UserInfo.vue";

defineProps<{
  users: Participant[];
}>();
</script>

<template>
  <v-list>
    <v-list-item v-for="p in users" :key="p.username" :subtitle="p.roles.join(', ')">
      <user-info :username="p.username" />
    </v-list-item>
  </v-list>
</template>
