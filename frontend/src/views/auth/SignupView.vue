<template>
  <div class="auth-page">
    <div class="auth-card">
      <h1 class="auth-title">회원가입</h1>
      <form @submit.prevent="handleSignup" class="auth-form">
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
          <p v-if="usernameError" class="field-error">{{ usernameError }}</p>
        </div>
        <div class="form-group">
          <label for="password">비밀번호</label>
          <input
            id="password"
            v-model="password"
            type="password"
            required
            minlength="8"
            placeholder="8자 이상"
            autocomplete="new-password"
          />
        </div>
        <div class="form-group">
          <label for="email">이메일</label>
          <input
            id="email"
            v-model="email"
            type="email"
            required
            placeholder="이메일 입력"
            autocomplete="email"
          />
          <p v-if="emailError" class="field-error">{{ emailError }}</p>
        </div>
        <p v-if="errorMsg" class="error-msg">{{ errorMsg }}</p>
        <button type="submit" class="auth-btn" :disabled="loading">가입하기</button>
      </form>
      <div class="auth-links">
        <router-link to="/login">로그인</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const username = ref('')
const password = ref('')
const email = ref('')
const loading = ref(false)
const errorMsg = ref('')
const usernameError = ref('')
const emailError = ref('')

const handleSignup = async () => {
  errorMsg.value = ''
  usernameError.value = ''
  emailError.value = ''
  if (password.value.length < 8) {
    errorMsg.value = '비밀번호는 8자 이상이어야 합니다.'
    return
  }
  loading.value = true
  try {
    await authStore.signup({
      username: username.value,
      password: password.value,
      email: email.value,
    })
    router.push('/signup/success')
  } catch (e) {
    const code = e.response?.data?.code
    const msg = e.response?.data?.message || '가입에 실패했습니다.'
    if (code === 'EMAIL_CONFLICT') {
      emailError.value = msg
    } else if (code === 'USERNAME_CONFLICT') {
      usernameError.value = msg
    } else {
      errorMsg.value = msg
    }
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
.auth-title {
  margin: 0 0 24px;
  font-size: 1.5em;
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
.field-error {
  margin: 6px 0 0;
  font-size: 0.85em;
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
