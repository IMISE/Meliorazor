<script setup lang="ts" generic="T extends { id: number | undefined }">
import { ref, watch } from "vue";
import { computed } from "vue";
import { useRouter } from "vue-router";

const { data } = defineProps<{
  data: T[];
  title: (e: T) => string;
}>();

const route = useRouter().currentRoute;
const index = computed(() => Number(route.value.hash.substring(1)));
const tab = ref();
watch(index, (val) => (isNaN(index.value) ? data[0]?.id : (tab.value = val)), { immediate: true });
</script>

<template>
  <div v-if="data.length > 1" class="d-flex flex-row">
    <v-tabs v-model="tab" color="primary" direction="vertical" style="width: 20%">
      <!-- v-for + v-if instead of computed + v-for so that index stays the same -->
      <template v-for="e in data" :key="e.id">
        <v-tab
          v-if="e"
          :value="e.id"
          :text="title(e)"
          class="overflow-x-scroll"
          :href="`#${e.id}`"
        />
        <v-divider></v-divider>
      </template>
    </v-tabs>
    <v-divider vertical></v-divider>
    <v-tabs-window v-model="tab" style="width: 100%; padding: 20px">
      <template v-for="(e, i) in data" :key="e.id">
        <v-tabs-window-item v-if="e" :value="e.id">
          <slot :element="e" :index="i" :input="data"></slot>
        </v-tabs-window-item>
      </template>
    </v-tabs-window>
  </div>
  <div v-else-if="data?.[0]">
    <slot :element="data[0]" :index="0" :input="data"></slot>
  </div>
</template>
