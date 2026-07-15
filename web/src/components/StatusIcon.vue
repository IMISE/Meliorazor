<script setup lang="ts">
import { Status } from "@/types/enums";
import {
  mdiCheckboxBlankCircleOutline,
  mdiCheckCircleOutline,
  mdiCloseCircleOutline,
} from "@mdi/js";

defineProps<{
  status: Status;
}>();

const icon = (status: Status) => {
  switch (status) {
    case Status.ACCEPTED:
      return mdiCheckCircleOutline;
    case Status.OPEN:
      return mdiCheckboxBlankCircleOutline;
    case Status.REJECTED:
      return mdiCloseCircleOutline;
  }
};
</script>

<template>
  <v-icon :key="status" :icon="icon(status)" :class="status" />
</template>

<style scoped>
.ACCEPTED.v-icon {
  color: white;
  background-color: #028a0f;
  border-radius: 100%;
}
.OPEN.v-icon {
  background-color: lightgray;
  border-radius: 50%;
}
.REJECTED.v-icon {
  color: white;
  background-color: #8a0202;
  border-radius: 50%;
}
</style>
