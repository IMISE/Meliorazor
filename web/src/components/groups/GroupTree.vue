<script setup lang="ts">
import { Role } from "@/types/enums";
import type { Group } from "@/types/types";
import { clean } from "@/utils/utils";
import {
  mdiAccountGroup,
  mdiAccountMultiplePlus,
  mdiBook,
  mdiBookMultiple,
  mdiBookPlus,
  mdiChevronRight,
} from "@mdi/js";
import type { IdTokenClaims } from "oidc-client-ts";
import { computed, inject, type Ref } from "vue";
import DeleteButton from "../controls/DeleteButton.vue";

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
  icon: string;
  type: "single-group" | "single-project" | "create-group-sub" | "create-project-sub" | "nolink";
}

const groupToTree = (group: TreeGroupHelper): TreeGroup => {
  const node: TreeGroup = {
    title: group.name,
    children: undefined,
    id: group.id,
    roles:
      group.participants.find((p) => p.username == user.value?.preferred_username)?.roles ?? [],
    orderedParents: [],
    icon: mdiAccountGroup,
    type: "single-group",
  };
  const isAdmin = node.roles.includes(Role.ADMIN);
  if (group.subGroups.length === 1 && !isAdmin) {
    return groupToTree({
      ...group.subGroups[0]!,
      orderedParents: [...(group.orderedParents ?? []), node],
    });
  } else {
    const kids = group.subGroups.map(groupToTree);
    const projectKid = {
      title: "Projekte",
      children: [],
      id: group.id,
      roles: [],
      orderedParents: [],
      icon: mdiBookMultiple,
      type: "create-project-sub",
    } as TreeGroup;

    if (group.projects.length) {
      projectKid.children!.push.apply(
        group.projects.map((project) => ({
          title: project.title,
          children: undefined,
          id: project.id,
          roles: node.roles,
          orderedParents: [],
          icon: mdiBook,
          type: "single-project",
        })),
      );
    }

    if (isAdmin) {
      kids.push({
        title: "Untergruppe erstellen",
        children: undefined,
        id: group.id,
        roles: [],
        orderedParents: [],
        icon: mdiAccountMultiplePlus,
        type: "create-group-sub",
      });
      projectKid.children!.push({
        title: "Projekt erstellen",
        children: undefined,
        id: group.id,
        roles: [],
        orderedParents: [],
        icon: mdiBookPlus,
        type: "create-project-sub",
      });
    }
    if (projectKid.children!.length) {
      kids.push(projectKid.children!.length === 1 ? projectKid.children![0]! : projectKid);
    }

    return {
      ...node,
      orderedParents: group.orderedParents ?? [],
      children: kids.length ? kids : undefined,
    };
  }
};
</script>

<template>
  <v-treeview
    v-if="groupsTree.length"
    :items="groupsTree"
    indent-lines
    open-on-click
    separate-roots
  >
    <template #prepend="{ item }">
      <v-icon :icon="item.icon" />
    </template>
    <template #title="{ item }">
      <template v-if="item.type === 'nolink'">
        {{ item.title }}
      </template>
      <template v-else-if="item.type === 'single-project'">
        <router-link
          :to="{
            name: item.type,
            params: { pid: item.id, title: clean(item.title) },
          }"
          class="link-in-tree"
        >
          {{ item.title }}
        </router-link>
      </template>
      <template v-else>
        <template v-for="parent in item.orderedParents" :key="parent.id">
          <router-link
            :to="{ name: 'single-group', params: { gid: parent.id, name: clean(parent.title) } }"
            class="link-in-tree"
          >
            {{ parent.title }}
          </router-link>
          <v-icon :icon="mdiChevronRight" color="primary" />
        </template>
        <router-link
          :to="{
            name: item.type,
            params: { gid: item.id, name: clean(item.title) },
          }"
          class="link-in-tree"
        >
          {{ item.title }}
        </router-link>
      </template>
    </template>
    <template #append="{ item }">
      <delete-button v-if="item.type === 'single-group' && item.roles.includes(Role.ADMIN)" />
    </template>
  </v-treeview>
  <p v-else><i>Keine Gruppen gefunden.</i></p>
</template>

<style scoped>
.link-in-tree:hover {
  font-style: italic;
}
</style>
