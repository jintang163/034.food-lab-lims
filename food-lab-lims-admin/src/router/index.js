import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/layout/index.vue'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '仪表盘', icon: 'Odometer', requiresAuth: true }
      }
    ]
  },
  {
    path: '/sample',
    component: Layout,
    redirect: '/sample/list',
    meta: { title: '样品管理', icon: 'Box', requiresAuth: true },
    children: [
      {
        path: 'list',
        name: 'SampleList',
        component: () => import('@/views/sample/index.vue'),
        meta: { title: '样品列表', icon: 'List', requiresAuth: true }
      },
      {
        path: 'register',
        name: 'SampleRegister',
        component: () => import('@/views/sample/register.vue'),
        meta: { title: '样品登记', icon: 'Plus', requiresAuth: true }
      }
    ]
  },
  {
    path: '/task',
    component: Layout,
    redirect: '/task/list',
    meta: { title: '任务管理', icon: 'Tickets', requiresAuth: true },
    children: [
      {
        path: 'list',
        name: 'TaskList',
        component: () => import('@/views/task/index.vue'),
        meta: { title: '任务列表', icon: 'List', requiresAuth: true }
      }
    ]
  },
  {
    path: '/detect',
    component: Layout,
    redirect: '/detect/item',
    meta: { title: '检测项目管理', icon: 'DataAnalysis', requiresAuth: true },
    children: [
      {
        path: 'item',
        name: 'DetectItem',
        component: () => import('@/views/detect/index.vue'),
        meta: { title: '检测项目', icon: 'Operation', requiresAuth: true }
      },
      {
        path: 'standard',
        name: 'LimitStandard',
        component: () => import('@/views/detect/standard.vue'),
        meta: { title: '限量标准', icon: 'Document', requiresAuth: true }
      }
    ]
  },
  {
    path: '/audit',
    component: Layout,
    redirect: '/audit/pending',
    meta: { title: '审核管理', icon: 'CircleCheck', requiresAuth: true },
    children: [
      {
        path: 'pending',
        name: 'AuditPending',
        component: () => import('@/views/audit/index.vue'),
        meta: { title: '待审列表', icon: 'Clock', requiresAuth: true }
      },
      {
        path: 'history',
        name: 'AuditHistory',
        component: () => import('@/views/audit/history.vue'),
        meta: { title: '审核历史', icon: 'History', requiresAuth: true }
      }
    ]
  },
  {
    path: '/report',
    component: Layout,
    redirect: '/report/list',
    meta: { title: '报告管理', icon: 'Document', requiresAuth: true },
    children: [
      {
        path: 'list',
        name: 'ReportList',
        component: () => import('@/views/report/index.vue'),
        meta: { title: '报告列表', icon: 'List', requiresAuth: true }
      }
    ]
  },
  {
    path: '/system',
    component: Layout,
    redirect: '/system/user',
    meta: { title: '系统管理', icon: 'Setting', requiresAuth: true },
    children: [
      {
        path: 'user',
        name: 'UserManagement',
        component: () => import('@/views/system/user/index.vue'),
        meta: { title: '用户管理', icon: 'User', requiresAuth: true }
      },
      {
        path: 'role',
        name: 'RoleManagement',
        component: () => import('@/views/system/role/index.vue'),
        meta: { title: '角色管理', icon: 'UserFilled', requiresAuth: true }
      },
      {
        path: 'menu',
        name: 'MenuManagement',
        component: () => import('@/views/system/menu/index.vue'),
        meta: { title: '菜单管理', icon: 'Menu', requiresAuth: true }
      }
    ]
  },
  {
    path: '/404',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '404', requiresAuth: false }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

const whiteList = ['/login', '/404']

router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 食品检测 LIMS 管理系统` : '食品检测 LIMS 管理系统'
  
  const userStore = useUserStore()
  const hasToken = userStore.token

  if (hasToken) {
    if (to.path === '/login') {
      next({ path: '/' })
    } else {
      if (userStore.userInfo && Object.keys(userStore.userInfo).length > 0) {
        next()
      } else {
        userStore.getInfo().then(() => {
          next()
        }).catch(() => {
          userStore.logout()
          next('/login')
        })
      }
    }
  } else {
    if (whiteList.indexOf(to.path) !== -1) {
      next()
    } else {
      next(`/login?redirect=${to.path}`)
    }
  }
})

export default router
