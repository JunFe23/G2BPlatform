import axios from 'axios'
import { useAuthStore } from '@/stores/auth'

axios.interceptors.request.use((config) => {
  const store = useAuthStore()
  if (store?.token) {
    config.headers.Authorization = `Bearer ${store.token}`
  }
  return config
})

axios.interceptors.response.use(
  (res) => res,
  (err) => {
    const url = err.config?.url || ''
    if (
      err.response?.status === 401 &&
      !url.includes('/auth/login') &&
      !url.includes('/auth/signup')
    ) {
      const store = useAuthStore()
      store?.logout?.()
      window.location.href = '/login'
    }
    return Promise.reject(err)
  },
)
