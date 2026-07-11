<script setup lang="ts">
import SingleUserPage from "@/components/SingleUserPage.vue";
import type { IdTokenClaims } from "oidc-client-ts";
import { inject, type Ref } from "vue";

const user = inject<Ref<IdTokenClaims | undefined>>("user");
</script>

<template>
  <single-user-page :username="user?.preferred_username" is-home />
</template>
