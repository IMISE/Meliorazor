<script setup lang="ts">
import SingleUserPage from "@/components/SingleUserPage.vue";
import { computed, type ComputedRef } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();

const username: ComputedRef<string | undefined> = computed(() =>
  // only true if name is a string[] (=> name contains a comma)
  // shouldn't happen, but maybe somebody did something weird
  typeof router.currentRoute.value.params.name === "object"
    ? router.currentRoute.value.params.name.join(",")
    : router.currentRoute.value.params.name,
);
</script>

<template>
  <single-user-page :username="username" :is-home="false" />
</template>
