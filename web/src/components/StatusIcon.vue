<script setup lang="ts">
import { Status } from "@/types/enums";
import {
  mdiCheckboxBlankCircleOutline,
  mdiCheckCircleOutline,
  mdiCloseCircleOutline,
} from "@mdi/js";
import { computed } from "vue";

const props = defineProps<{
  status: Status;
  includeText: boolean;
}>();

const icon = computed(() => {
  switch (props.status) {
    case Status.ACCEPTED:
      return mdiCheckCircleOutline;
    case Status.REJECTED:
      return mdiCloseCircleOutline;
    case Status.OPEN:
    default:
      return mdiCheckboxBlankCircleOutline;
  }
});

const text = computed(() => {
  switch (props.status) {
    case Status.ACCEPTED:
      return "Akzeptiert";
    case Status.REJECTED:
      return "Abgelehnt";
    case Status.OPEN:
    default:
      return "Offen";
  }
});
</script>

<template>
  <v-icon :key="status" :icon="icon" :class="status" />
  <span v-if="includeText">{{ text }}</span>
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
