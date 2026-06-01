import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    {
      path: '/about',
      name: 'about',
      // route level code-splitting
      // this generates a separate chunk (About.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: () => import('../views/AboutView.vue'),
    },
    {
      path: '/users/:id-:name',
      name: 'user',
      component: () => import('../views/SingleUserView.vue'),
    },
    {
      path: '/projects',
      name: 'projects',
      component: () => import('../views/QuestionnaireOverview.vue'),
    },
    {
      path: '/projects/create',
      name: 'create-project',
      component: () => import('../views/QuestionnaireCreateView.vue'),
    },
    {
      path: '/projects/:pid-:title',
      name: 'single-project',
      component: () => import('../views/QuestionnaireView.vue'),
    },
    {
      path: '/projects/:pid-:title/elements/:eid-:name',
      name: 'data-element',
      component: () => import('../views/DataElementView.vue'),
      children: [{
        path: 'create-project-annotate',
        name: 'data-element-annotation',
        component: () => import('../views/AnnotationCreateView.vue'),
        meta: { tabLabel: 'Annotate', tabIcon: 'draw-pen' },
      }, {
        path: 'annotations',
        name: 'data-element-annotations',
        component: () => import('../views/DataElementView.vue'),
        meta: { tabLabel: 'Annotations', tabIcon: 'book-alphabet' },
        children: [{
          path: ':aid-:subject',
          name: 'focus-annotation',
          component: () => import('../views/AnnotationEditView.vue'),
        }]
      }, {
        path: 'history',
        name: 'data-element-history',
        component: HomeView,
        meta: { tabLabel: 'History', tabIcon: 'history' },
      }],
    },
    {
      path: '/annotate/:projectId-:elementIds',
      name: 'create-annotation',
      component: () => import('../views/AnnotationCreateView.vue'),
    },
    {
      path: '/annotations/:projectId-:annotationIds',
      name: 'correct-annotation',
      component: () => import('../views/AnnotationCreateView.vue'),
    },
    {
      path: '/auth',
      name: 'post-login',
      component: () => import('../views/AuthView.vue'),
    },
  ],
})

export default router
