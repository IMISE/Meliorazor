<script setup lang="ts">
import { RouterView, useRouter } from "vue-router";
import auth from "./auth";
import { computed, onMounted, onUpdated, provide, ref, type Ref } from "vue";
import type { User } from "oidc-client-ts";
import { watch } from "vue";
import { mdiLogin, mdiLogout } from "@mdi/js";

const login = () => auth.signinRedirect();
const logout = () => auth.signoutRedirect();

const user: Ref<User | undefined> = ref();
const profile = computed(() => user.value?.profile);
const router = useRouter();

provide("user", profile);

async function getUser() {
  user.value = (await auth.getUser()) ?? undefined;
}

watch(router.currentRoute, getUser);

onMounted(() => getUser());
onUpdated(() => getUser());
</script>

<template>
  <v-layout>
    <v-app-bar>
      <v-app-bar-title>Meliorazor</v-app-bar-title>
      <v-btn href="/">Home</v-btn>
      <v-btn href="/groups">Gruppen</v-btn>
      <v-btn href="/projects">Projekte</v-btn>
      <v-btn v-if="user?.access_token" :append-icon="mdiLogout" @click="logout()">{{
        user.profile.preferred_username ?? "Logout"
      }}</v-btn>
      <v-btn v-else :append-icon="mdiLogin" @click="login()">Login</v-btn>
    </v-app-bar>
    <v-main>
      <RouterView />
    </v-main>
  </v-layout>
</template>

<style scoped>
header {
  line-height: 1.5;
  max-height: 100vh;
}

.logo {
  display: block;
  margin: 0 auto 2rem;
}
.v-app-bar {
  padding-inline: 10%;
}

nav {
  width: 100%;
  font-size: 12px;
  text-align: center;
  margin-top: 2rem;
}

nav a.router-link-exact-active {
  color: var(--color-text);
}

nav a.router-link-exact-active:hover {
  background-color: transparent;
}

nav a {
  display: inline-block;
  padding: 0 1rem;
  border-left: 1px solid var(--color-border);
}

nav a:first-of-type {
  border: 0;
}

@media (min-width: 1024px) {
  header {
    display: flex;
    place-items: center;
    padding-right: calc(var(--section-gap) / 2);
  }

  .logo {
    margin: 0 2rem 0 0;
  }

  header .wrapper {
    display: flex;
    place-items: flex-start;
    flex-wrap: wrap;
  }

  nav {
    text-align: left;
    margin-left: -1rem;
    font-size: 1rem;

    padding: 1rem 0;
    margin-top: 1rem;
  }
}
</style>
