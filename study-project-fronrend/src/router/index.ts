import { useStore } from '@/stores'
import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'


const routes: Readonly<RouteRecordRaw[]> = [{
  path: '/', name: 'welcome', component: () => import('@/views/WelcomeView.vue'),
  children: [
    {
      path: '',
      name: 'welcome-login',
      component: () => import('@/components/welcome/LoginPage.vue')
    },
    {
      path: 'register',
      name: 'welcome-register',
      component: () => import('@/components/welcome/RegisterPage.vue')
    },
    {
      path: 'forget',
      name: 'welcome-forget',
      component: () => import('@/components/welcome/ForgetPage.vue')
    },
  ]
}, {
  path: '/index', name: 'index', component: () => import('@/views/IndexView.vue'),
}]



const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: routes,
})

router.beforeEach((to, from) => {
  const store = useStore()
  if (store.auth.user !== null && typeof to.name === 'string' && to.name.startsWith('welcome')) {
    return { name: 'index' }
  } else if (store.auth.user === null && to.fullPath.startsWith('/index')) {
    return { name: 'welcome-login' }
  } else if (to.matched.length === 0) {
    return { name: 'index' }
  }
})


export default router
