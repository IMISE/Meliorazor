<script setup lang="ts">
import type { FhirTerminologyCode } from "@/types/fhir";
import type { CodeInfo, CodeInfoMap } from "@/types/types";
import { getCode } from "@/utils/fhirApi";
import { toFhirCode } from "@/utils/fhirUtils";
import { ref } from "vue";
import { computed, type Ref } from "vue";

defineEmits<{
  (e: "focus", v: FhirTerminologyCode): void;
}>();

const props = defineProps<{ depth: number; maxCols: number; info: CodeInfo; server: string }>();

const allChildren = computed(() => props.info.children);
const children: Ref<CodeInfoMap> = ref({});
const loaded = ref(false);
const loading = ref(false);

const load = async () => {
  const system = props.info.self.system;
  const server = props.server;
  if (!system || !server) {
    return;
  }

  await Promise.allSettled(
    allChildren.value.map(async (code) => {
      const response = await getCode(code, system, props.info.self.systemVersion, server);
      if (!response) return;
      children.value[code] = response;
    }),
  );
  loaded.value = true;
};
</script>

<template>
  <div @click="depth === 0 ? undefined : $emit('focus', toFhirCode(info))">
    <b v-if="depth === 0">{{ info.self.display }}</b
    ><span v-else>{{ info.self.display }}</span> (<var>{{ info.self.code }}</var
    >)<br />
  </div>
  <!-- TODO: value set display? only if depth > 0?-->
  <!-- only display children if we have enough space, requires 1col for new spacer and 1col for content at least -->
  <!-- depth is 0-based, maxCols is 1-based -->
  <div v-if="depth + 2 < maxCols" style="padding-left: 20px">
    <code-children
      v-for="child in children"
      :key="`${info.self.system}-${info.self.code}-${child.self.system}-${child.self.code}`"
      :depth="depth + 1"
      :max-cols="maxCols"
      :info="child"
      :server="server"
      @focus="(v) => $emit('focus', v)"
    />
    <v-btn
      v-if="!loaded && allChildren?.length"
      @click="load"
      variant="text"
      :disabled="loading"
      :loading="loading"
      block
    >
      Kinder laden
    </v-btn>
  </div>
</template>
