<script setup lang="ts">
import { computed } from 'vue';
import { useRoute } from 'vue-router';

const route = useRoute();
const annotationIds = computed(() => route.query.annotations ?? []);
const currentIndex = computed(() => route.query.i);

//const props = defineProps<{
//  dataElements: DataElement[];
//  /** current index of the dataElements array */
//  current: number;
//}>();

</script>

<template>
  <template v-if="annotationIds.length">
    <!-- TODO - we have existing annotations, edit them one by one -->
  </template>
  <template v-else>
    <!-- TODO - we don't have any provided - redirect and provide some (so the link remains shareable)-->
  </template>
</template>
