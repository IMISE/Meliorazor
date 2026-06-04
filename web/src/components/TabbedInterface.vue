<script setup lang="ts" generic="T extends { id: number | undefined }">
import { computed } from 'vue';
import { useRouter } from 'vue-router';

const props = defineProps<{
  data: T[],
  title: (e: T) => string,
}>();

const route = useRouter().currentRoute;
const index = computed(() => Number(route.value.hash.substring(1)));
const tab = computed(() => isNaN(index.value) ? 0 : index.value);
//const tab = computed(() => props.data.findIndex((e) => e.id === index_clean.value));
</script>

<template>
  <div v-if="data.length > 1" class="d-flex flex-row">
    <v-tabs v-model="tab" color="primary" direction="vertical" class="fit">
      <!-- v-for + v-if instead of computed + v-for so that index stays the same -->
      <template v-for="e in data">
        <v-tab v-if="e" :value="e.id" :text="title(e)" :href="`#${e.id}`"/>
      </template>
    </v-tabs>
    <v-divider vertical></v-divider>
    <v-tabs-window v-model="tab" style="width: 100%" class="tabs-window">
      <template v-for="(e, i) in data">
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

<style scoped>
.fit {
  max-width: 200px;
  overflow-x: scroll;
}
.tabs-window {
  padding: 20px;
}
</style>
