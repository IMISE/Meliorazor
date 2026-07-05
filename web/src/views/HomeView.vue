<script setup lang="ts">
import SingleUserPage from "@/components/SingleUserPage.vue";
import type { User } from "oidc-client-ts";
import { inject, type Ref } from "vue";

const user = inject("user") as Ref<User | undefined>;
</script>

<template>
  <single-user-page :username="user?.profile.preferred_username" is-home />
</template>
