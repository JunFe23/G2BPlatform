import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

import MainLayoutView from '../views/MainLayoutView.vue'
import GoodsView from '../views/GoodsView.vue'
import ServicesView from '../views/ServicesView.vue'
import ConstructionsView from '../views/ConstructionsView.vue'
import TopContractsView from '../views/TopContractsView.vue'
import ShoppingMallView from '../views/ShoppingMallView.vue'
import TargetProjectsView from '../views/TargetProjectsView.vue'
import WorkInProgressView from '../views/WorkInProgressView.vue'
import SettingsView from '../views/SettingsView.vue'
import BidList from '../views/BidList.vue'
import HomeView from '../views/HomeView.vue'
import AboutView from '../views/AboutView.vue'
import ReportGoodsView from '../views/ReportGoodsView.vue'
import ReportDashboardView from '../views/ReportDashboardView.vue'
import ReportServicesView from '../views/ReportServicesView.vue'
import ReportConstructionsView from '../views/ReportConstructionsView.vue'
import LoginView from '../views/auth/LoginView.vue'
import SignupView from '../views/auth/SignupView.vue'
import SignupSuccessView from '../views/auth/SignupSuccessView.vue'
import AccountRecoveryView from '../views/auth/AccountRecoveryView.vue'
import AccountView from '../views/auth/AccountView.vue'
import AdminUsersView from '../views/admin/AdminUsersView.vue'
import RawDataImportView from '../views/RawDataImportView.vue'

const PUBLIC_PATHS = ['/login', '/signup', '/signup/success', '/account-recovery']

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: LoginView,
      meta: { guestOnly: true, public: true },
    },
    {
      path: '/signup',
      name: 'signup',
      component: SignupView,
      meta: { guestOnly: true, public: true },
    },
    {
      path: '/signup/success',
      name: 'signup-success',
      component: SignupSuccessView,
      meta: { guestOnly: true, public: true },
    },
    {
      path: '/account-recovery',
      name: 'account-recovery',
      component: AccountRecoveryView,
      meta: { public: true },
    },
    {
      path: '/account',
      name: 'account',
      component: AccountView,
      meta: { requiresAuth: true },
    },
    {
      path: '/admin/users',
      name: 'admin-users',
      component: AdminUsersView,
      meta: { requiresAuth: true, requiresAdmin: true },
    },
    {
      path: '/',
      name: 'main',
      redirect: () => {
        const store = useAuthStore()
        return store.isLoggedIn ? '/report-goods' : '/login'
      },
    },
    {
      path: '/report-goods',
      name: 'report-goods',
      component: ReportGoodsView,
      meta: { requiresAuth: true },
    },
    {
      path: '/report-services',
      name: 'report-services',
      component: ReportServicesView,
      meta: { requiresAuth: true },
    },
    {
      path: '/report-constructions',
      name: 'report-constructions',
      component: ReportConstructionsView,
      meta: { requiresAuth: true },
    },
    {
      path: '/report-dashboard',
      name: 'report-dashboard',
      component: ReportDashboardView,
      meta: { requiresAuth: true },
    },
    {
      path: '/raw-data-import',
      name: 'raw-data-import',
      component: RawDataImportView,
      meta: { requiresAuth: true, requiresAdmin: true },
    },
    {
      path: '/goods',
      name: 'goods',
      component: GoodsView,
      meta: { requiresAuth: true },
    },
    {
      path: '/services',
      name: 'services',
      component: ServicesView,
      meta: { requiresAuth: true },
    },
    {
      path: '/constructions',
      name: 'constructions',
      component: ConstructionsView,
      meta: { requiresAuth: true },
    },
    {
      path: '/top-contracts',
      name: 'top-contracts',
      component: TopContractsView,
      meta: { requiresAuth: true },
    },
    {
      path: '/shopping-mall',
      name: 'shopping-mall',
      component: ShoppingMallView,
      meta: { requiresAuth: true },
    },
    {
      path: '/target-projects',
      name: 'target-projects',
      component: TargetProjectsView,
      meta: { requiresAuth: true },
    },
    {
      path: '/work-in-progress',
      name: 'work-in-progress',
      component: WorkInProgressView,
      meta: { requiresAuth: true },
    },
    {
      path: '/settings',
      name: 'settings',
      component: SettingsView,
      meta: { requiresAuth: true },
    },
    {
      path: '/bids',
      name: 'bids',
      component: BidList,
      meta: { requiresAuth: true },
    },
    {
      path: '/home',
      name: 'home',
      component: HomeView,
      meta: { requiresAuth: true },
    },
    {
      path: '/about',
      name: 'about',
      component: AboutView,
      meta: { requiresAuth: true },
    },
  ],
})

router.beforeEach((to, _from, next) => {
  const authStore = useAuthStore()
  const isLoggedIn = authStore.isLoggedIn
  const isPublic = to.meta.public || PUBLIC_PATHS.includes(to.path)

  if (isPublic) {
    if (to.meta.guestOnly && isLoggedIn) {
      next({ name: 'report-goods' })
      return
    }
    next()
    return
  }
  if (!isLoggedIn) {
    next({ name: 'login', query: { redirect: to.fullPath } })
    return
  }
  if (to.meta.requiresAdmin && !authStore.isAdmin) {
    next({ name: 'report-goods' })
    return
  }
  next()
})

export default router
