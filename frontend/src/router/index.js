import { createRouter, createWebHistory } from 'vue-router'
import BidList from '../views/BidList.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/goods'
    },
    {
      path: '/goods', // 물품
      name: 'goods',
      component: BidList,
      props: { category: 'goods' } // 컴포넌트에 카테고리 전달
    },
    {
      path: '/services', // 용역 (예시)
      name: 'services',
      component: BidList,
      props: { category: 'services' }
    },
    {
      path: '/construction',
      name: 'construction',
      component: BidList,
      props: { category: 'construction' }
    }
  ]
})

export default router
