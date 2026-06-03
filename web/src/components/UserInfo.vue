<script setup lang="ts">
import type { User } from "@/types/types";
import { ref, type Ref } from "vue";

defineProps<{
  username: string;
}>();

// TODO fetch
const user: Ref<User | undefined> = ref(undefined);

</script>

<template>
  <div class="container" v-if="user">
    <div class="name">{{ user.displayName }}</div>
    <div class="buttons">
      <!-- TDDO button to add user to project -->
      <!-- TODO stats, as modal maybe? -->
      <v-icon-btn icon="mdi-counter" href="example.com" />
      <v-icon-btn icon="mdi-email-fast" :href="`mailto:${user.email}`" />
    </div>
  </div>
</template>

<style scoped>
.container {
  width: 100%;
}
.container div {
  display: inline-block;
  height: 50px;
  align-items: center;
}
.name {
  width: 60%;
  height: 100%;
  text-align: left;
}
.buttons {
  width: 40%;
  justify-content: right;
}
</style>
