<template>
  <LegacySidebarLayout>
    <div class="raw-import-page">
      <h1 class="page-title">Raw 데이터 적재 (CSV)</h1>
      <p class="page-desc">
        CSV 파일을 수동 첨부하여 DB에 넣습니다. 물품·공사·용역별 대상 테이블 및 컬럼 형식은 추후 정리 후 연동 예정입니다.
      </p>

      <div class="tab-bar">
        <button
          v-for="tab in tabs"
          :key="tab.key"
          type="button"
          class="tab-btn"
          :class="{ active: activeTab === tab.key }"
          @click="activeTab = tab.key"
        >
          {{ tab.label }}
        </button>
      </div>

      <section v-for="tab in tabs" :key="tab.key" v-show="activeTab === tab.key" class="section">
        <h2 class="section-title">{{ tab.label }} CSV 적재</h2>
        <div class="upload-zone">
          <input
            :ref="(el) => setFileInputRef(tab.key, el)"
            type="file"
            accept=".csv"
            class="file-input"
            @change="onFileSelect(tab.key, $event)"
          />
          <div class="upload-placeholder">
            <span class="upload-icon">📄</span>
            <p>CSV 파일을 선택하거나 여기에 끌어다 놓으세요.</p>
            <p v-if="selectedFile[tab.key]" class="selected-name">{{ selectedFile[tab.key].name }}</p>
          </div>
        </div>
        <div class="notice-box">
          <strong>예정 작업</strong>
          <p>대상 테이블 및 컬럼 매핑이 정리되면 이 화면에서 업로드 → DB 적재 기능을 연동할 예정입니다.</p>
        </div>
        <div class="action-row">
          <button type="button" class="btn-import" disabled>적재 (연동 예정)</button>
        </div>
      </section>
    </div>
  </LegacySidebarLayout>
</template>

<script setup>
import { ref, reactive } from 'vue'
import LegacySidebarLayout from './components/LegacySidebarLayout.vue'

const tabs = [
  { key: 'goods', label: '물품' },
  { key: 'construction', label: '공사' },
  { key: 'service', label: '용역' },
]

const activeTab = ref('goods')
const selectedFile = reactive({
  goods: null,
  construction: null,
  service: null,
})
const fileInputRefs = reactive({ goods: null, construction: null, service: null })

function setFileInputRef(key, el) {
  if (el) fileInputRefs[key] = el
}

function onFileSelect(key, event) {
  const file = event.target.files?.[0]
  selectedFile[key] = file || null
}
</script>

<style scoped>
.raw-import-page {
  padding: 0 4px;
  max-width: 900px;
}

/* 타이틀은 LegacySidebarLayout :deep(h1) 스타일과 동일하게 */
.page-title {
  margin: 0 0 8px;
}

.page-desc {
  margin: 0 0 24px;
  font-size: 14px;
  color: #6b7280;
  line-height: 1.5;
}

.tab-bar {
  display: flex;
  gap: 4px;
  margin-bottom: 20px;
  border-bottom: 1px solid #e5e7eb;
}

.tab-btn {
  padding: 10px 20px;
  background: none;
  border: none;
  border-bottom: 2px solid transparent;
  font-size: 14px;
  color: #6b7280;
  cursor: pointer;
  margin-bottom: -1px;
}

.tab-btn:hover {
  color: #111827;
}

.tab-btn.active {
  color: #111827;
  font-weight: 600;
  border-bottom-color: #111827;
}

.section {
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 24px;
}

.section-title {
  margin: 0 0 16px;
  font-size: 1.1em;
  color: #374151;
}

.upload-zone {
  position: relative;
  border: 2px dashed #d1d5db;
  border-radius: 10px;
  padding: 32px;
  text-align: center;
  background: #fff;
  margin-bottom: 16px;
}

.file-input {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  opacity: 0;
  cursor: pointer;
}

.upload-placeholder {
  pointer-events: none;
}

.upload-icon {
  font-size: 2em;
  display: block;
  margin-bottom: 8px;
}

.upload-placeholder p {
  margin: 0;
  font-size: 14px;
  color: #6b7280;
}

.selected-name {
  margin-top: 8px !important;
  color: #111827;
  font-weight: 500;
}

.notice-box {
  background: #eff6ff;
  border: 1px solid #bfdbfe;
  border-radius: 8px;
  padding: 12px 16px;
  margin-bottom: 16px;
  font-size: 13px;
  color: #1e40af;
}

.notice-box strong {
  display: block;
  margin-bottom: 4px;
}

.notice-box p {
  margin: 0;
  line-height: 1.5;
}

.action-row {
  display: flex;
  justify-content: flex-end;
}

.btn-import {
  padding: 8px 20px;
  background: #9ca3af;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  cursor: not-allowed;
}

.btn-import:not(:disabled) {
  background: #111827;
  cursor: pointer;
}

.btn-import:not(:disabled):hover {
  background: #1f2937;
}
</style>
