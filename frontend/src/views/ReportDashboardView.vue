<template>
  <LegacySidebarLayout>
    <div class="dashboard-container">
      <div class="dashboard-placeholder">
        <div class="icon">📊</div>
        <h1>보고서 대시보드</h1>
        <p>대시보드 페이지는 곧 추가될 예정입니다.</p>
        <p class="sub-text">데이터 시각화 및 통계 정보가 여기에 표시됩니다.</p>
      </div>
    </div>
  </LegacySidebarLayout>
</template>

<script setup>
import LegacySidebarLayout from './components/LegacySidebarLayout.vue';
</script>

<style scoped>
.dashboard-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 80vh;
  text-align: center;
}

.dashboard-placeholder {
  background-color: #34495e;
  color: #ecf0f1;
  padding: 50px;
  border-radius: 8px;
  border: 2px solid #2c3e50;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
  max-width: 700px;
}

.dashboard-placeholder h1 {
  font-size: 2.5em;
  margin-bottom: 20px;
  background: none;
  color: #ecf0f1;
  padding: 0;
  border: none;
  box-shadow: none;
}

.dashboard-placeholder p {
  font-size: 1.3em;
  margin: 15px 0;
}

.dashboard-placeholder .sub-text {
  font-size: 1.1em;
  color: #bdc3c7;
  margin-top: 10px;
}

.icon {
  font-size: 5em;
  margin-bottom: 20px;
  color: #3498db;
}
</style>
