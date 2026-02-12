<template>
  <LegacySidebarLayout>
    <h1 class="admin-title">관리자 - 계정 관리</h1>
    <div class="admin-card">
      <div v-if="errorMsg" class="error-msg">{{ errorMsg }}</div>
      <div v-if="successMsg" class="success-msg">{{ successMsg }}</div>
      <div v-if="loading" class="loading-text">목록을 불러오는 중...</div>
      <div v-else class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>아이디</th>
              <th>이메일</th>
              <th>승인</th>
              <th>역할</th>
              <th v-if="authStore.isSuperAdmin">역할 변경</th>
              <th v-if="authStore.isSuperAdmin">승인하기</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="users.length === 0">
              <td :colspan="authStore.isSuperAdmin ? 6 : 4" class="no-data">등록된 사용자가 없습니다.</td>
            </tr>
            <tr v-for="u in users" :key="u.id">
              <td>{{ u.username }}</td>
              <td>{{ u.email }}</td>
              <td>{{ u.approved ? '승인됨' : '대기' }}</td>
              <td>{{ roleLabel(u.role) }}</td>
              <td v-if="authStore.isSuperAdmin">
                <select
                  :value="u.role"
                  class="role-select"
                  @change="handleRoleChange(u.id, $event.target.value)"
                >
                  <option value="ROLE_USER">일반 사용자</option>
                  <option value="ROLE_ADMIN">관리자</option>
                  <option value="ROLE_SUPER_ADMIN">슈퍼관리자</option>
                </select>
              </td>
              <td v-if="authStore.isSuperAdmin">
                <button
                  v-if="!u.approved"
                  type="button"
                  class="approve-btn"
                  @click="handleApprove(u.id)"
                >
                  승인
                </button>
                <span v-else class="approved-text">-</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </LegacySidebarLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import LegacySidebarLayout from '../components/LegacySidebarLayout.vue'
import { useAuthStore } from '@/stores/auth'
import axios from 'axios'

const authStore = useAuthStore()
const users = ref([])
const loading = ref(false)
const errorMsg = ref('')
const successMsg = ref('')

function roleLabel(role) {
  if (role === 'ROLE_SUPER_ADMIN') return '슈퍼관리자'
  if (role === 'ROLE_ADMIN') return '관리자'
  return '일반 사용자'
}

async function loadUsers() {
  loading.value = true
  errorMsg.value = ''
  try {
    const { data } = await axios.get('/api/admin/users')
    users.value = data || []
  } catch (e) {
    if (e.response?.status === 403) {
      errorMsg.value = '관리자 권한이 필요합니다.'
    } else {
      errorMsg.value = e.response?.data?.message || '목록을 불러오지 못했습니다.'
    }
    users.value = []
  } finally {
    loading.value = false
  }
}

async function handleRoleChange(userId, role) {
  successMsg.value = ''
  errorMsg.value = ''
  try {
    await axios.patch(`/api/admin/users/${userId}/role`, { role })
    successMsg.value = '역할이 변경되었습니다.'
    loadUsers()
  } catch (e) {
    errorMsg.value = e.response?.data?.message || '역할 변경에 실패했습니다.'
  }
}

async function handleApprove(userId) {
  successMsg.value = ''
  errorMsg.value = ''
  try {
    await axios.patch(`/api/admin/users/${userId}/approve`)
    successMsg.value = '승인되었습니다.'
    loadUsers()
  } catch (e) {
    errorMsg.value = e.response?.data?.message || '승인 처리에 실패했습니다.'
  }
}

onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
.admin-title {
  margin-bottom: 20px;
  font-size: 1.4em;
  color: #2c3e50;
}
.admin-card {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  border: 1px solid #e0e0e0;
}
.table-wrap {
  overflow-x: auto;
}
.data-table {
  width: 100%;
  border-collapse: collapse;
}
.data-table th,
.data-table td {
  padding: 10px 12px;
  border: 1px solid #ddd;
  text-align: left;
}
.data-table th {
  background: #34495e;
  color: #ecf0f1;
}
.data-table tbody tr:nth-child(even) {
  background: #fff;
}
.no-data {
  text-align: center;
  color: #7f8c8d;
}
.role-select {
  padding: 6px 10px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 0.95em;
}
.approve-btn {
  padding: 6px 12px;
  background: #27ae60;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9em;
}
.approve-btn:hover {
  background: #219a52;
}
.approved-text {
  color: #7f8c8d;
}
.error-msg {
  margin-bottom: 12px;
  color: #e74c3c;
  font-size: 0.95em;
}
.success-msg {
  margin-bottom: 12px;
  color: #27ae60;
  font-size: 0.95em;
}
.loading-text {
  color: #7f8c8d;
  padding: 20px;
}
</style>
