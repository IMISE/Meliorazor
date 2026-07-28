<script setup lang="ts">
import { Role } from "@/types/enums";
import type { Group } from "@/types/types";
import { clean } from "@/utils/utils";
import { mdiChevronRight } from "@mdi/js";
import type { IdTokenClaims } from "oidc-client-ts";
import { computed, inject, type Ref } from "vue";

const user = inject("user") as Ref<IdTokenClaims | undefined>;

const props = defineProps<{
  groups: Group[];
}>();
const groupsTree = computed(() => props.groups.map(groupToTree));

interface TreeGroupHelper extends Group {
  orderedParents?: TreeGroup[] | undefined;
}
interface TreeGroup {
  // used by vuetify
  title: string;
  children: TreeGroup[] | undefined;
  // used by us
  id: number;
  roles: Role[];
  /**
   * Parents with exactly one child are grouped to not have any unnecessarily wide trees.
   * Order is most general at the start of the array to most specific at the end of the array.
   */
  orderedParents: TreeGroup[];
}

const groupToTree = (group: TreeGroupHelper): TreeGroup => {
  const node: TreeGroup = {
    title: group.name,
    children: [],
    id: group.id,
    roles:
      group.participants.find((p) => p.username == user.value?.preferred_username)?.roles ?? [],
    orderedParents: [],
  };
  if (group.subGroups.length === 1) {
    return groupToTree({
      ...group.subGroups[0]!,
      orderedParents: [...(group.orderedParents ?? []), node],
    });
  } else {
    return {
      ...node,
      orderedParents: group.orderedParents ?? [],
      children: group.subGroups.map(groupToTree),
    };
  }
};
</script>

<template>
  <v-treeview v-model="groupsTree" indent-lines open-on-click separate-roots>
    <template #title="{ item }">
      <template v-for="parent in (item as TreeGroup).orderedParents" :key="parent.id">
        <router-link
          :to="{ name: 'single-group', params: { gid: parent.id, name: clean(parent.title) } }"
        >
          {{ parent.title }}
        </router-link>
        <v-icon :icon="mdiChevronRight" />
      </template>
      <router-link
        :to="{
          name: 'single-group',
          params: { gid: (item as TreeGroup).id, name: clean((item as TreeGroup).title) },
        }"
      >
        {{ (item as TreeGroup).title }}
      </router-link>
    </template>
    <template #append="{ item }">
      <delete-button v-if="(item as TreeGroup).roles.includes(Role.ADMIN)" />
    </template>
  </v-treeview>
</template>
