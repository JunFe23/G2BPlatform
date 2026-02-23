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
  padding: 32px 40px;
  border-radius: 12px;
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.3);
  width: 100%;
  max-width: 380px;
}

.branding {
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ecf0f1;
  text-align: center;
}
.service-name {
  margin: 0 0 6px;
  font-size: 1.6em;
  font-weight: 700;
  color: #223247;
}
.service-sub {
  margin: 0;
  font-size: 0.9em;
  color: #7f8c8d;
}
.auth-title {
  margin: 0 0 20px;
  font-size: 1.2em;
  font-weight: 600;
  color: #2c3e50;
  text-align: center;
}
.auth-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.form-group label {
  font-size: 0.9em;
  color: #34495e;
}
.form-group input {
  padding: 10px 12px;
  border: 1px solid #ccc;
  border-radius: 6px;
  font-size: 1em;
}
.form-group input:focus {
  outline: none;
  border-color: #3498db;
}
.error-msg {
  margin: 0;
  font-size: 0.9em;
  color: #e74c3c;
}
.auth-btn {
  padding: 12px;
  background-color: #34495e;
  color: #ecf0f1;
  border: none;
  border-radius: 6px;
  font-size: 1em;
  cursor: pointer;
  margin-top: 8px;
}
.auth-btn:hover:not(:disabled) {
  background-color: #2c3e50;
}
.auth-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}
.auth-links {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  gap: 20px;
  font-size: 0.9em;
}
.auth-links a {
  color: #3498db;
  text-decoration: none;
}
.auth-links a:hover {
  text-decoration: underline;
}
</style>
