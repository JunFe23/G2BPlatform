<template>
  <LegacySidebarLayout>
    <div class="settings-container">
      <h1>설정</h1>
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
/* 타이틀은 LegacySidebarLayout :deep(h1) 스타일 사용 */
.settings-content {
  margin-top: 20px;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}
</style>