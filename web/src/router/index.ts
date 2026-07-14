import { createRouter, createWebHistory } from "vue-router";
import HomeView from "../views/HomeView.vue";
import { mdiBookAlphabet, mdiHistory, mdiPen } from "@mdi/js";

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: "/",
      name: "home",
      component: HomeView,
      meta: {
        disableBackButton: true,
      },
    },
    {
      path: "/about",
      name: "about",
      // route level code-splitting
      // this generates a separate chunk (About.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: () => import("../views/AboutView.vue"),
    },
    {
      path: "/users/:name",
      name: "user",
      component: () => import("../views/SingleUserView.vue"),
    },
    {
      path: "/projects",
      name: "projects",
      component: () => import("../views/QuestionnaireOverview.vue"),
      meta: {
        disableBackButton: true,
      },
    },
    {
      path: "/projects/create",
      name: "create-project",
      component: () => import("../views/QuestionnaireCreateView.vue"),
    },
    {
      path: "/projects/:pid-:title",
      name: "single-project",
      component: () => import("../views/QuestionnaireView.vue"),
    },
    {
      path: "/projects/:pid-:title/elements/:eid-:name",
      name: "data-element",
      component: () => import("../views/DataElementView.vue"),
      children: [
        {
          path: "create-project-annotate",
          name: "data-element-annotation",
          component: () => import("../views/AnnotationCreateView.vue"),
          meta: { tabLabel: "Annotate", tabIcon: mdiPen },
        },
        {
          path: "annotations",
          name: "data-element-annotations",
          component: () => import("../views/DataElementView.vue"),
          meta: { tabLabel: "Annotations", tabIcon: mdiBookAlphabet },
        },
        {
          path: "history",
          name: "data-element-history",
          component: HomeView,
          meta: { tabLabel: "History", tabIcon: mdiHistory },
        },
      ],
    },
    {
      path: "/projects/:pid-:title/version/create",
      name: "create-version",
      component: () => import("../views/VersionCreateView.vue"),
    },
    {
      path: "/projects/:pid-:title/version/:key",
      name: "version",
      component: () => import("../views/VersionView.vue"),
    },
    {
      path: "/projects/:pid-:title/elements/:eid-:name/annotations/:aid-:subject",
      name: "focus-annotation",
      component: () => import("../views/AnnotationEditView.vue"),
    },
    {
      path: "/annotate/:projectId-:elementIds",
      name: "create-annotation",
      component: () => import("../views/AnnotationCreateView.vue"),
    },
    {
      path: "/annotations/:projectId-:annotationIds/correct",
      name: "correct-annotation",
      component: () => import("../views/AnnotationCreateView.vue"),
    },
    {
      path: "/afterauth",
      name: "post-login",
      component: () => import("../views/AuthView.vue"),
      meta: {
        disableBackButton: true,
      },
    },
  ],
});

export default router;
