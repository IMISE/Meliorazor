<script setup lang="ts">
import { mdiDelete, mdiDeleteOutline } from "@mdi/js";
import { mergeProps, onMounted, ref } from "vue";

defineEmits(["delete"]);

const disabled = ref(true);

onMounted(() => {
  setTimeout(() => (disabled.value = false), 2_000);
});
</script>

<template>
  <v-dialog max-width="600px">
    <template #activator="{ props: dialogProps }">
      <v-hover>
        <template #default="{ isHovering, props: hoverProps }">
          <v-icon-btn
            v-bind="mergeProps(dialogProps, hoverProps)"
            :icon="isHovering ? mdiDelete : mdiDeleteOutline"
            :color="isHovering ? 'error' : undefined"
            variant="plain"
            hide-overlay
            @click.prevent
          />
        </template>
      </v-hover>
    </template>
    <template #default="{ isActive }">
      <v-card title="Wirklich löschen?">
        <v-card-text>
          Soll die Ressource wirklich gelöscht werden? Bestehende Annotationen bleiben erhalten, um
          in zukünftigen Projekten Vorschläge zu machen. Alle anderen untergeordneten Informationen
          werden unwiderruflich entfernt. Dieser Schritt kann nicht rückgängig gemacht werden.
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="primary" @click="isActive.value = false">Abbrechen</v-btn>
          <v-btn
            color="error"
            variant="outlined"
            :disabled="disabled"
            @click="
              () => {
                isActive.value = false;
                $emit('delete');
              }
            "
          >
            Löschen
          </v-btn>
        </v-card-actions>
      </v-card>
    </template>
  </v-dialog>
</template>
