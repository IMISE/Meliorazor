import { createRouter, createWebHistory } from "vue-router";
import HomeView from "../views/HomeView.vue";

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: "/",
      name: "home",
      component: HomeView,
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
      path: "/groups",
      name: "groups",
      component: () => import("../views/GroupOverview.vue"),
    },
    {
      path: "/groups/:gid-:name?",
      name: "single-group",
      component: () => import("../views/GroupView.vue"),
    },
    {
      path: "/groups/:gid-:name?/members",
      name: "group-members",
      component: () => import("../views/GroupView.vue"),
    },
    {
      path: "/groups/:gid-:name?/servers",
      name: "group-servers",
      component: () => import("../views/GroupView.vue"),
    },
    {
      path: "/groups/create",
      name: "create-group",
      component: () => import("../views/GroupCreateView.vue"),
    },
    {
      path: "/groups/:gid-:name?/create",
      name: "create-group-sub",
      component: () => import("../views/GroupCreateView.vue"),
    },
    {
      path: "/projects",
      name: "projects",
      component: () => import("../views/QuestionnaireOverview.vue"),
    },
    {
      path: "/projects/create",
      name: "create-project",
      component: () => import("../views/QuestionnaireCreateView.vue"),
    },
    {
      path: "/grooups/:gid-:name?/projects/create",
      name: "create-project-sub",
      component: () => import("../views/QuestionnaireCreateView.vue"),
    },
    {
      path: "/projects/:pid-:title?",
      name: "single-project",
      component: () => import("../views/QuestionnaireView.vue"),
    },
    {
      path: "/projects/:pid-:title?/version",
      name: "project-version-index",
      component: () => import("../views/QuestionnaireView.vue"),
    },
    {
      path: "/projects/:pid-:title?/admin",
      name: "project-value-sets",
      component: () => import("../views/QuestionnaireView.vue"),
    },
    {
      path: "/projects/:pid-:title?/elements/:eid-:name?",
      name: "data-element",
      component: () => import("../views/DataElementView.vue"),
      children: [
        {
          path: "create-project-annotate",
          name: "data-element-annotation",
          component: () => import("../views/AnnotationCreateView.vue"),
        },
        {
          path: "annotations",
          name: "data-element-annotations",
          component: () => import("../views/DataElementView.vue"),
        },
      ],
    },
    {
      path: "/projects/:pid-:title?/version/create",
      name: "create-version",
      component: () => import("../views/VersionCreateView.vue"),
    },
    {
      path: "/projects/:pid-:title?/version/:key",
      name: "single-version",
      component: () => import("../views/VersionView.vue"),
    },
    {
      path: "/projects/:pid-:title?/elements/:eid-:name?/annotations/:aid-:subjec?t",
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
    },
  ],
});

export default router;
