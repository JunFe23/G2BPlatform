<template>
  <div class="auth-page">
    <div class="auth-card success-card">
      <h1 class="success-title">회원가입이 완료되었습니다</h1>
      <p class="success-desc">로그인 후 서비스를 이용할 수 있습니다</p>
      <router-link to="/login" class="auth-btn">로그인하러 가기</router-link>
    </div>
  </div>
</template>

<script setup></script>

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
  text-align: center;
}
.success-title {
  margin: 0 0 16px;
  font-size: 1.4em;
  font-weight: 700;
  color: #2c3e50;
}
.success-desc {
  margin: 0 0 28px;
  font-size: 1em;
  color: #7f8c8d;
}
.auth-btn {
  display: inline-block;
  padding: 12px 24px;
  background-color: #34495e;
  color: #ecf0f1;
  border: none;
  border-radius: 6px;
  font-size: 1em;
  text-decoration: none;
  cursor: pointer;
}
.auth-btn:hover {
  background-color: #2c3e50;
}
</style>
