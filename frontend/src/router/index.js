import { createRouter, createWebHistory } from 'vue-router'

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

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'main',
      component: MainLayoutView,
    },
    {
      path: '/goods',
      name: 'goods',
      component: GoodsView,
    },
    {
      path: '/services',
      name: 'services',
      component: ServicesView,
    },
    {
      path: '/constructions',
      name: 'constructions',
      component: ConstructionsView,
    },
    {
      path: '/top-contracts',
      name: 'top-contracts',
      component: TopContractsView,
    },
    {
      path: '/shopping-mall',
      name: 'shopping-mall',
      component: ShoppingMallView,
    },
    {
      path: '/target-projects',
      name: 'target-projects',
      component: TargetProjectsView,
    },
    {
      path: '/work-in-progress',
      name: 'work-in-progress',
      component: WorkInProgressView,
    },
    {
      path: '/settings',
      name: 'settings',
      component: SettingsView,
    },
    {
      path: '/bids',
      name: 'bids',
      component: BidList,
    },
    {
      path: '/home',
      name: 'home',
      component: HomeView,
    },
    {
      path: '/about',
      name: 'about',
      component: AboutView,
    },
  ],
})

export default router
