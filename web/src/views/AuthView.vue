<script setup lang="ts">
import auth from "@/auth";
import router from "@/router";
import { onMounted, ref } from "vue";

const message = ref("Authentifiziere ...");
onMounted(async () => {
  try {
    await auth.signinCallback();
    router.push("/");
  } catch (e) {
    console.error(e);
    message.value = "Ein Fehler ist aufgetreten!";
  }
});
</script>

<template>
  <div>{{ message }}</div>
</template>
