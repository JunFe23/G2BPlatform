import axios from 'axios'
import { useAuthStore } from '@/stores/auth'

const client = axios.create({
  baseURL: '',
  timeout: 30000,
  headers: { 'Content-Type': 'application/json' },
})

client.interceptors.request.use((config) => {
  const token = useAuthStore().token
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

client.interceptors.response.use(
  (res) => res,
  (err) => {
    const url = err.config?.url || ''
    if (
      err.response?.status === 401 &&
      !url.includes('/auth/login') &&
      !url.includes('/auth/signup')
    ) {
      useAuthStore().logout()
      window.location.href = '/login'
    }
    return Promise.reject(err)
  },
)

export default client
