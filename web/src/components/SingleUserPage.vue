<script setup lang="ts">
defineProps<{ username: string | undefined; isHome: boolean }>();
</script>
<!--
- "my (or their) annotations"
- "my (or their) comments"
- "my (or their) projects"
- "my (or their) data"
- stats?
- latest actions? (history)
=> acts as homepage
-->

<template>
  <template v-if="username">
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
  <template v-else-if="isHome">
    Bitte loggen Sie sich ein, um auf diese Seite zugreifen zu können.
  </template>
  <template v-else>Name nicht gefunden.</template>
</template>
