<script setup lang="ts">
import type { AnnotationComment, AnnotationVersioning, RichAction } from "@/types/types";
import { clean } from "@/utils/utils";
import { mdiArrowRight, mdiComment, mdiPencil, mdiTagMultiple } from "@mdi/js";
import { computed } from "vue";
import { useRouter } from "vue-router";

const props = defineProps<{
  richAction: RichAction;
}>();

const router = useRouter();

const linkToAnnotation = computed(() => ({
  name: "focus-annotation",
  params: {
    pid: props.richAction.questionnaireId,
    title: clean(props.richAction.questionnaireTitle),
    eid: props.richAction.dataElementId,
    name: clean(props.richAction.dataElementLabel),
    aid: props.richAction.annotationId,
    subject: clean(props.richAction.annotationLabel),
  },
}));
const display = computed(() => {
  const type = props.richAction.action.actionType;
  if (type === "submission") {
    return {
      title: "Annotation erstellt",
      subtitle: `"${props.richAction.annotatableLabel} ${props.richAction.annotationLabel}" an ${props.richAction.dataElementLabel} in ${props.richAction.questionnaireTitle}`,
      link: linkToAnnotation.value,
      icon: mdiPencil,
    };
  } else if (type === "comment") {
    const ctype = (props.richAction.action as AnnotationComment).commentType;
    const ctypeDisplay =
      ctype === "ACCEPT" ? "Akzeptiert" : ctype === "REJECT" ? "Abgelehnt" : "Kommentiert";
    return {
      title: "Feedback gegeben",
      subtitle: `${ctypeDisplay}: "${props.richAction.annotatableLabel} ${props.richAction.annotationLabel}" an ${props.richAction.dataElementLabel} in ${props.richAction.questionnaireTitle}`,
      link: linkToAnnotation.value,
      icon: mdiComment,
    };
  } else {
    // type === "versioning"
    const versionKey = (props.richAction.action as AnnotationVersioning).versionKey;
    return {
      title: "Version erstellt",
      subtitle: versionKey,
      link: {
        name: "single-version",
        params: {
          pid: props.richAction.questionnaireId,
          title: clean(props.richAction.questionnaireTitle),
          key: versionKey,
        },
      },
      icon: mdiTagMultiple,
    };
  }
});
</script>

<template>
  <v-list-item :title="display.title" :subtitle="display.subtitle" :prepend-icon="display.icon">
    <template #append>
      <v-icon-btn
        hide-overlay
        variant="plain"
        :icon="mdiArrowRight"
        @click="() => router.push(display.link)"
      />
    </template>
  </v-list-item>
</template>
