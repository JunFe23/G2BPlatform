<template>
  <LegacySidebarLayout>
    <div class="settings-container">
      <h1 class="settings-title">설정</h1>
      <div class="settings-content">
        <p>설정 페이지입니다.</p>
        <!-- Add settings controls here later -->
      </div>
    </div>
  </LegacySidebarLayout>
</template>

<script setup>
import LegacySidebarLayout from './components/LegacySidebarLayout.vue';
</script>

<style scoped>
.settings-title {
  background-color: #34495e;
  color: #ecf0f1;
  padding: 15px;
  border-radius: 8px;
  border: 2px solid #2c3e50;
  text-align: center;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
}

.settings-content {
  margin-top: 20px;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}
</style>