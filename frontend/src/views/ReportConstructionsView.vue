<template>
  <LegacySidebarLayout>
    <div class="work-in-progress-container">
      <div class="work-in-progress">
        <div class="construction-icon">🚧</div>
        <h1>준비 중입니다.</h1>
        <p>보고서 데이터 - 공사 페이지는 준비 중입니다. <br>빠른 시일 내에 업데이트하겠습니다.</p>
      </div>
    </div>
  </LegacySidebarLayout>
</template>

<script setup>
import LegacySidebarLayout from './components/LegacySidebarLayout.vue';
</script>

<style scoped>
.work-in-progress-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 80vh;
  text-align: center;
}

.work-in-progress {
  background-color: #34495e;
  color: #ecf0f1;
  padding: 30px;
  border-radius: 12px;
  border: 2px solid #2c3e50;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  max-width: 600px;
}

.work-in-progress h1 {
  font-size: 2.2em;
  margin-bottom: 10px;
}

.work-in-progress p {
  font-size: 1.1em;
}

.construction-icon {
  font-size: 4em;
  margin-bottom: 20px;
  color: #e67e22;
}
</style>
