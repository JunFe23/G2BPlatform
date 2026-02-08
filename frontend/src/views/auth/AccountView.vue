<template>
  <LegacySidebarLayout>
    <h1 class="account">계정정보</h1>
    <div class="account-card">
      <div v-if="me" class="info-section">
        <p><strong>아이디:</strong> {{ me.username }}</p>
        <p v-if="me.emailMasked"><strong>이메일:</strong> {{ me.emailMasked }}</p>
      </div>
      <hr />
      <h2>비밀번호 변경</h2>
      <form @submit.prevent="handleChangePassword" class="auth-form">
        <div class="form-group">
          <label for="currentPassword">현재 비밀번호</label>
          <input
            id="currentPassword"
            v-model="currentPassword"
            type="password"
            required
            placeholder="현재 비밀번호"
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
        <p v-if="successMsg" class="success-msg">{{ successMsg }}</p>
        <button type="submit" class="auth-btn" :disabled="loading">비밀번호 변경</button>
      </form>
    </div>
  </LegacySidebarLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import LegacySidebarLayout from '../components/LegacySidebarLayout.vue'
import client from '@/api/client'

const me = ref(null)
const currentPassword = ref('')
const newPassword = ref('')
const loading = ref(false)
const errorMsg = ref('')
const successMsg = ref('')

onMounted(async () => {
  try {
    const { data } = await client.get('/api/auth/me')
    me.value = data
  } catch {
    me.value = null
  }
})

const handleChangePassword = async () => {
  errorMsg.value = ''
  successMsg.value = ''
  if (newPassword.value.length < 8) {
    errorMsg.value = '비밀번호는 8자 이상이어야 합니다.'
    return
  }
  loading.value = true
  try {
    await client.patch('/api/users/password', {
      currentPassword: currentPassword.value,
      newPassword: newPassword.value,
    })
    successMsg.value = '비밀번호가 변경되었습니다.'
    currentPassword.value = ''
    newPassword.value = ''
  } catch (e) {
    errorMsg.value = e.response?.data?.message || '비밀번호 변경에 실패했습니다.'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.account-card {
  max-width: 480px;
  padding: 24px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e0e0e0;
}
.info-section p {
  margin: 8px 0;
}
hr {
  margin: 20px 0;
  border: none;
  border-top: 1px solid #ddd;
}
h2 {
  margin: 0 0 16px;
  font-size: 1.1em;
  color: #2c3e50;
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
.auth-btn {
  padding: 12px;
  background-color: #34495e;
  color: #ecf0f1;
  border: none;
  border-radius: 6px;
  font-size: 1em;
  cursor: pointer;
  width: fit-content;
}
.auth-btn:hover:not(:disabled) {
  background-color: #2c3e50;
}
.auth-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}
</style>
