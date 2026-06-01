<script setup lang="ts">
import type { User } from 'oidc-client-ts';
import { inject, type Ref } from 'vue';

const user = inject("user") as Ref<User | undefined>
</script>

<template>
  <template v-if="user">
    <v-row fluid>
      <v-col cols="6">
        <h1>Letzte Aktivitäten</h1>
      </v-col>
      <v-col cols="6">
        <h1>Handlungsbedarf</h1>
      </v-col>
    </v-row>
    <h1>Meine Projekte</h1>
    <h1>Meine Annotationen und Kommentare</h1>
    <h1 v-if="false">Meine Statistik</h1>
  </template>
  <template v-else>
    Bitte loggen Sie sich ein, um auf diese Seite zugreifen zu können.
  </template>
</template>
