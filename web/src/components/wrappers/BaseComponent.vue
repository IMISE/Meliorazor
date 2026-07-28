<script setup lang="ts">
import { mdiArrowLeft } from "@mdi/js";
import { useRouter, type RouteLocationRaw } from "vue-router";

defineEmits(["clicked"]);
defineProps<{
  backButtonLink: RouteLocationRaw | undefined | -1;
}>();

const router = useRouter();
const history = window.history.length;
</script>

<template>
  <v-icon-btn
    v-if="backButtonLink || history"
    :icon="mdiArrowLeft"
    variant="plain"
    style="position: sticky; top: 70px"
    @click="
      backButtonLink === undefined
        ? $emit('clicked')
        : backButtonLink === -1
          ? router.go(-1)
          : router.push(backButtonLink as RouteLocationRaw)
    "
  />
  <slot></slot>
</template>
