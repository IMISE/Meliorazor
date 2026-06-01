<script setup lang="ts">
import type { User } from "@/stores/user";

const props = defineProps<{
  user: User;
}>();
</script>

<template>
  <div class="container">
    <div class="name">{{ user.lastname }}</div>
    <div class="buttons">
      <!-- TDDO button to add user to project -->
      <!-- TODO stats, as modal maybe? -->
      <v-icon-btn icon="mdi-counter" href="example.com" />
      <v-icon-btn icon="mdi-email-fast" :href="`mailto:${props.user.email}`" />
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
