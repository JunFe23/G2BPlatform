<template>
  <div class="auth-page">
    <div class="auth-card">
      <h1 class="auth-title">계정찾기</h1>
      <p class="info-msg">
        아이디 찾기는 추후 이메일 연동 후 제공 예정입니다. 비밀번호 재설정만 가능합니다.
      </p>

      <!-- 1) 재설정 요청 -->
      <div v-if="step === 'request'" class="step-section">
        <p class="step-desc">비밀번호 재설정을 위해 가입 이메일을 입력하세요.</p>
        <form @submit.prevent="handleRequest" class="auth-form">
          <div class="form-group">
            <label for="email">이메일</label>
            <input id="email" v-model="email" type="email" required placeholder="이메일 입력" />
          </div>
          <p v-if="errorMsg" class="error-msg">{{ errorMsg }}</p>
          <p v-if="successMsg" class="success-msg">{{ successMsg }}</p>
          <div v-if="devResetToken" class="dev-token-box">
            <p class="dev-label">[DEV] 재설정 토큰 (아래 재설정 완료에서 사용)</p>
            <div class="token-row">
              <input type="text" :value="devResetToken" readonly class="token-input" />
              <button type="button" class="copy-btn" @click="copyToken">복사</button>
            </div>
          </div>
          <button type="submit" class="auth-btn" :disabled="loading">재설정 요청</button>
        </form>
      </div>

      <!-- 2) 재설정 완료 -->
      <div v-else class="step-section">
        <p class="step-desc">토큰과 새 비밀번호를 입력하세요.</p>
        <form @submit.prevent="handleReset" class="auth-form">
          <div class="form-group">
            <label for="token">재설정 토큰</label>
            <input
              id="token"
              v-model="token"
              type="text"
              required
              placeholder="이메일로 받은 토큰 입력"
            />
          </div>
          <div class="form-group">
            <label for="newPassword">새 비밀번호</label>
            <input
              id="newPassword"
              v-model="newPassword"
              type="password"
              required
              minlength="8"
              placeholder="8자 이상"
            />
          </div>
          <p v-if="errorMsg" class="error-msg">{{ errorMsg }}</p>
          <p v-if="resetSuccess" class="success-msg">비밀번호가 변경되었습니다. 로그인해 주세요.</p>
          <button type="submit" class="auth-btn" :disabled="loading">비밀번호 변경</button>
        </form>
      </div>

      <div class="auth-links">
        <button v-if="step === 'reset'" type="button" class="link-btn" @click="step = 'request'">
          재설정 요청으로 돌아가기
        </button>
        <router-link to="/login">로그인</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import client from '@/api/client'

const step = ref('request')
const email = ref('')
const token = ref('')
const newPassword = ref('')
const loading = ref(false)
const errorMsg = ref('')
const successMsg = ref('')
const devResetToken = ref('')
const resetSuccess = ref(false)

const handleRequest = async () => {
  errorMsg.value = ''
  successMsg.value = ''
  devResetToken.value = ''
  loading.value = true
  try {
    const { data } = await client.post('/api/auth/recovery/request', { email: email.value })
    successMsg.value = data.message
    if (data.devResetToken) {
      devResetToken.value = data.devResetToken
    }
  } catch (e) {
    errorMsg.value = e.response?.data?.message || '요청에 실패했습니다.'
  } finally {
    loading.value = false
  }
}

const handleReset = async () => {
  errorMsg.value = ''
  resetSuccess.value = false
  if (newPassword.value.length < 8) {
    errorMsg.value = '비밀번호는 8자 이상이어야 합니다.'
    return
  }
  loading.value = true
  try {
    await client.post('/api/auth/recovery/reset', {
      token: token.value,
      newPassword: newPassword.value,
    })
    resetSuccess.value = true
  } catch (e) {
    errorMsg.value = e.response?.data?.message || '비밀번호 변경에 실패했습니다.'
  } finally {
    loading.value = false
  }
}

const copyToken = async () => {
  try {
    await navigator.clipboard.writeText(devResetToken.value)
    alert('토큰이 복사되었습니다.')
  } catch {
    alert('복사에 실패했습니다.')
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
  max-width: 420px;
}
.auth-title {
  margin: 0 0 8px;
  font-size: 1.5em;
  color: #2c3e50;
  text-align: center;
}
.info-msg {
  margin: 0 0 20px;
  font-size: 0.85em;
  color: #95a5a6;
  text-align: center;
}
.step-desc {
  margin: 0 0 16px;
  font-size: 0.95em;
  color: #7f8c8d;
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
.success-msg {
  margin: 0;
  font-size: 0.9em;
  color: #27ae60;
}
.dev-token-box {
  padding: 12px;
  background: #f8f9fa;
  border: 1px dashed #95a5a6;
  border-radius: 6px;
}
.dev-label {
  margin: 0 0 8px;
  font-size: 0.85em;
  color: #7f8c8d;
}
.token-row {
  display: flex;
  gap: 8px;
}
.token-input {
  flex: 1;
  font-size: 0.85em;
}
.copy-btn {
  padding: 8px 12px;
  background: #3498db;
  color: #fff;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9em;
}
.copy-btn:hover {
  background: #2980b9;
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
  align-items: center;
  gap: 16px;
  font-size: 0.9em;
}
.auth-links a,
.link-btn {
  color: #3498db;
  text-decoration: none;
  background: none;
  border: none;
  cursor: pointer;
  font-size: inherit;
}
.auth-links a:hover,
.link-btn:hover {
  text-decoration: underline;
}
</style>
