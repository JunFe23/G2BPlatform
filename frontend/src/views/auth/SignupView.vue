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
  padding: 36px 44px;
  border-radius: 14px;
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.2);
  width: 100%;
  max-width: 400px;
}
.auth-title {
  margin: 0 0 26px;
  font-size: 1.35em;
  font-weight: 600;
  color: #334155;
  text-align: center;
  letter-spacing: -0.02em;
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
.field-error {
  margin: 6px 0 0;
  font-size: 0.825em;
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
