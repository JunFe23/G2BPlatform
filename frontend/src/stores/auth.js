import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import client from '@/api/client'

const TOKEN_KEY = 'g2b_token'
const USER_KEY = 'g2b_user'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem(TOKEN_KEY) || '')
  const user = ref(
    (() => {
      try {
        const s = localStorage.getItem(USER_KEY)
        return s ? JSON.parse(s) : null
      } catch {
        return null
      }
    })(),
  )

  const isLoggedIn = computed(() => !!token.value)

  function setAuth(accessToken, userData) {
    token.value = accessToken
    user.value = userData
    localStorage.setItem(TOKEN_KEY, accessToken)
    localStorage.setItem(USER_KEY, JSON.stringify(userData || {}))
  }

  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
  }

  async function login(username, password) {
    const { data } = await client.post('/api/auth/login', { username, password })
    setAuth(data.accessToken, data.user)
    return data
  }

  async function signup(payload) {
    await client.post('/api/auth/signup', payload)
  }

  async function fetchMe() {
    const { data } = await client.get('/api/auth/me')
    user.value = { username: data.username, role: data.role }
    localStorage.setItem(USER_KEY, JSON.stringify(user.value))
    return data
  }

  return { token, user, isLoggedIn, setAuth, logout, login, signup, fetchMe }
})
