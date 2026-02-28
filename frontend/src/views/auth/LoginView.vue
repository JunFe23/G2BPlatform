<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="branding">
        <h1 class="service-name">G2B Project</h1>
        <p class="service-sub">조달 데이터 분석 플랫폼</p>
      </div>
      <h2 class="auth-title">로그인</h2>
      <form @submit.prevent="handleLogin" class="auth-form">
        <div class="form-group">
          <label for="username">아이디</label>
          <input
            id="username"
            v-model="username"
            type="text"
            required
            placeholder="아이디 입력"
            autocomplete="username"
          />
        </div>
        <div class="form-group">
          <label for="password">비밀번호</label>
          <input
            id="password"
            v-model="password"
            type="password"
            required
            placeholder="비밀번호 입력"
            autocomplete="current-password"
          />
        </div>
        <p v-if="errorMsg" class="error-msg">{{ errorMsg }}</p>
        <button type="submit" class="auth-btn" :disabled="loading">로그인</button>
      </form>
      <div class="auth-links">
        <router-link to="/signup">회원가입</router-link>
        <router-link to="/account-recovery">계정찾기</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import client from '@/api/client'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const username = ref('')
const password = ref('')
const loading = ref(false)
const errorMsg = ref('')

const handleLogin = async () => {
  errorMsg.value = ''
  loading.value = true
  try {
    await authStore.login(username.value, password.value)
    await authStore.fetchMe()
    const redirect = route.query.redirect || '/report-goods'
    router.push(redirect)
  } catch (e) {
    const code = e.response?.data?.code
    errorMsg.value = code === 'NOT_APPROVED'
      ? (e.response?.data?.message || '승인 대기 중입니다. 관리자 승인 후 이용 가능합니다.')
      : (e.response?.data?.message || '아이디 또는 비밀번호가 올바르지 않습니다.')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(180deg, #223247 0%, #1f2a37 100%);
}

.auth-card {
  background: #fff;
  padding: 36px 44px;
  border-radius: 14px;
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.2);
  width: 100%;
  max-width: 400px;
}

.branding {
  margin-bottom: 26px;
  padding-bottom: 22px;
  border-bottom: 1px solid #e8ecf0;
  text-align: center;
}
.service-name {
  margin: 0 0 6px;
  font-size: 1.5em;
  font-weight: 700;
  color: #223247;
  letter-spacing: -0.02em;
}
.service-sub {
  margin: 0;
  font-size: 0.875em;
  color: #64748b;
}
.auth-title {
  margin: 0 0 22px;
  font-size: 1.15em;
  font-weight: 600;
  color: #334155;
  text-align: center;
}
.auth-form {
  display: flex;
  flex-direction: column;
  gap: 18px;
}
.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.form-group label {
  font-size: 0.875em;
  font-weight: 500;
  color: #475569;
}
.form-group input {
  padding: 12px 14px;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  font-size: 1em;
  transition: border-color 0.2s, box-shadow 0.2s;
}
.form-group input::placeholder {
  color: #94a3b8;
}
.form-group input:focus {
  outline: none;
  border-color: #3498db;
  box-shadow: 0 0 0 3px rgba(52, 152, 219, 0.15);
}
.error-msg {
  margin: 0;
  font-size: 0.875em;
  color: #dc2626;
}
.auth-btn {
  padding: 12px 20px;
  margin-top: 4px;
  background: linear-gradient(180deg, #3d5a73 0%, #34495e 100%);
  color: #f0f4f8;
  border: none;
  border-radius: 8px;
  font-size: 1em;
  font-weight: 500;
  cursor: pointer;
  transition: opacity 0.2s, transform 0.15s;
}
.auth-btn:hover:not(:disabled) {
  opacity: 0.95;
  transform: translateY(-1px);
}
.auth-btn:disabled {
  opacity: 0.65;
  cursor: not-allowed;
  transform: none;
}
.auth-links {
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #e8ecf0;
  display: flex;
  justify-content: center;
  gap: 24px;
  font-size: 0.9em;
}
.auth-links a {
  color: #3498db;
  text-decoration: none;
  font-weight: 500;
}
.auth-links a:hover {
  text-decoration: underline;
}
</style>
