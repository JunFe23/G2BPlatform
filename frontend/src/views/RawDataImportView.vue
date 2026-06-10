<template>
  <LegacySidebarLayout>
    <div class="raw-import-page">
      <h1 class="page-title">Raw 데이터 적재</h1>
      <p class="page-desc">
        조달데이터허브에서 다운로드한 CSV 파일을 DB에 적재합니다.<br>
        동일 파일을 재업로드해도 중복 행은 자동으로 건너뜁니다.
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

      <!-- 특정품목 조달 내역 탭 -->
      <section v-if="activeTab === 'specific'" class="section">
        <h2 class="section-title">특정품목 조달 내역 CSV 적재</h2>
        <p class="tab-desc">
          조달데이터허브 &gt; 특정품목 조달 내역 CSV (UTF-16, 탭 구분).<br>
          물품 계약과 쇼핑몰(제3자단가) 계약이 모두 포함된 통합 파일입니다.
        </p>

        <div
          class="upload-zone"
          :class="{ dragging: isDragging }"
          @dragover.prevent="isDragging = true"
          @dragleave.prevent="isDragging = false"
          @drop.prevent="onDrop"
        >
          <input
            ref="fileInputRef"
            type="file"
            accept=".csv,.tsv"
            class="file-input"
            @change="onFileSelect"
          />
          <div class="upload-placeholder">
            <span class="upload-icon">📄</span>
            <p v-if="!selectedFile">CSV 파일을 선택하거나 여기에 끌어다 놓으세요.</p>
            <p v-else class="selected-name">{{ selectedFile.name }} ({{ formatBytes(selectedFile.size) }})</p>
          </div>
        </div>

        <!-- 결과 카드 -->
        <div v-if="result" class="result-card" :class="result.errorMessage ? 'result-error' : 'result-ok'">
          <template v-if="result.errorMessage">
            <p class="result-title">적재 실패</p>
            <p class="result-msg">{{ result.errorMessage }}</p>
          </template>
          <template v-else>
            <p class="result-title">적재 완료</p>
            <div class="result-grid">
              <div class="result-item">
                <span class="result-label">전체 행</span>
                <span class="result-value">{{ result.totalRows.toLocaleString() }}건</span>
              </div>
              <div class="result-item">
                <span class="result-label">신규 적재</span>
                <span class="result-value green">{{ result.insertedCount.toLocaleString() }}건</span>
              </div>
              <div class="result-item">
                <span class="result-label">중복 스킵</span>
                <span class="result-value gray">{{ result.skippedCount.toLocaleString() }}건</span>
              </div>
              <div class="result-item">
                <span class="result-label">소요 시간</span>
                <span class="result-value">{{ (result.elapsedMs / 1000).toFixed(1) }}초</span>
              </div>
            </div>
          </template>
        </div>

        <!-- 진행률 표시 -->
        <div v-if="uploading || activeJobId" class="progress-card">
          <div class="progress-head">
            <span class="progress-title">{{ progressPhaseLabel }}</span>
            <span class="progress-pct">{{ progressPct }}%</span>
          </div>
          <div class="progress-bar" :class="{ processing: progressPhase === 'processing' }">
            <div class="progress-fill" :style="{ width: progressPct + '%' }"></div>
          </div>
          <p class="progress-hint">
            <template v-if="progressPhase === 'uploading'">
              파일 업로드 중입니다. (브라우저 → 서버)
              <span v-if="uploadEtaText"> · 남은시간 {{ uploadEtaText }}</span>
            </template>
            <template v-else-if="progressPhase === 'processing'">
              서버에서 CSV를 파싱하고 DB에 적재하는 중입니다.
              <span v-if="serverEtaText"> · 남은시간 {{ serverEtaText }}</span>
              <span v-if="jobMessage" class="job-msg"> · {{ jobMessage }}</span>
            </template>
            <template v-else>
              진행 상태를 불러오는 중입니다.
            </template>
          </p>
        </div>

        <div class="action-row">
          <button
            type="button"
            class="btn-import"
            :disabled="!selectedFile || uploading"
            @click="doUpload"
          >
            <span v-if="uploading" class="spinner"></span>
            {{ uploading ? '적재 중...' : '적재 시작' }}
          </button>
        </div>
      </section>

      <!-- 업무별 구성원별 계약내역 탭 -->
      <section v-if="activeTab === 'task'" class="section">
        <h2 class="section-title">업무별 구성원별 계약내역 CSV 적재</h2>
        <p class="tab-desc">
          조달데이터허브 &gt; 업무별 구성원별 계약내역 CSV (UTF-16, 탭 구분).<br>
          파일명에 <strong>공사</strong>가 포함되면 공사(construction), 그 외는 기술용역(engineering)으로 자동 분류됩니다.
        </p>

        <div
          class="upload-zone"
          :class="{ dragging: taskIsDragging }"
          @dragover.prevent="taskIsDragging = true"
          @dragleave.prevent="taskIsDragging = false"
          @drop.prevent="onTaskDrop"
        >
          <input
            ref="taskFileInputRef"
            type="file"
            accept=".csv,.tsv"
            class="file-input"
            @change="onTaskFileSelect"
          />
          <div class="upload-placeholder">
            <span class="upload-icon">📄</span>
            <p v-if="!taskSelectedFile">CSV 파일을 선택하거나 여기에 끌어다 놓으세요.</p>
            <p v-else class="selected-name">{{ taskSelectedFile.name }} ({{ formatBytes(taskSelectedFile.size) }})</p>
          </div>
        </div>

        <!-- 결과 카드 -->
        <div v-if="taskResult" class="result-card" :class="taskResult.errorMessage ? 'result-error' : 'result-ok'">
          <template v-if="taskResult.errorMessage">
            <p class="result-title">적재 실패</p>
            <p class="result-msg">{{ taskResult.errorMessage }}</p>
          </template>
          <template v-else>
            <p class="result-title">적재 완료</p>
            <div class="result-grid">
              <div class="result-item">
                <span class="result-label">전체 행</span>
                <span class="result-value">{{ taskResult.totalRows.toLocaleString() }}건</span>
              </div>
              <div class="result-item">
                <span class="result-label">신규 적재</span>
                <span class="result-value green">{{ taskResult.insertedCount.toLocaleString() }}건</span>
              </div>
              <div class="result-item">
                <span class="result-label">중복 스킵</span>
                <span class="result-value gray">{{ taskResult.skippedCount.toLocaleString() }}건</span>
              </div>
              <div class="result-item">
                <span class="result-label">소요 시간</span>
                <span class="result-value">{{ (taskResult.elapsedMs / 1000).toFixed(1) }}초</span>
              </div>
            </div>
          </template>
        </div>

        <!-- 진행률 표시 -->
        <div v-if="taskUploading || taskActiveJobId" class="progress-card">
          <div class="progress-head">
            <span class="progress-title">{{ taskProgressPhaseLabel }}</span>
            <span class="progress-pct">{{ taskProgressPct }}%</span>
          </div>
          <div class="progress-bar" :class="{ processing: taskProgressPhase === 'processing' }">
            <div class="progress-fill" :style="{ width: taskProgressPct + '%' }"></div>
          </div>
          <p class="progress-hint">
            <template v-if="taskProgressPhase === 'uploading'">
              파일 업로드 중입니다. (브라우저 → 서버)
              <span v-if="taskUploadEtaText"> · 남은시간 {{ taskUploadEtaText }}</span>
            </template>
            <template v-else-if="taskProgressPhase === 'processing'">
              서버에서 CSV를 파싱하고 DB에 적재하는 중입니다.
              <span v-if="taskServerEtaText"> · 남은시간 {{ taskServerEtaText }}</span>
              <span v-if="taskJobMessage" class="job-msg"> · {{ taskJobMessage }}</span>
            </template>
            <template v-else>
              진행 상태를 불러오는 중입니다.
            </template>
          </p>
        </div>

        <div class="action-row">
          <button
            type="button"
            class="btn-import"
            :disabled="!taskSelectedFile || taskUploading"
            @click="doTaskUpload"
          >
            <span v-if="taskUploading" class="spinner"></span>
            {{ taskUploading ? '적재 중...' : '적재 시작' }}
          </button>
        </div>
      </section>
    </div>
  </LegacySidebarLayout>
</template>

<script setup>
import { computed, ref } from 'vue'
import axios from 'axios'
import LegacySidebarLayout from './components/LegacySidebarLayout.vue'

const tabs = [
  { key: 'specific', label: '특정품목 조달 내역' },
  { key: 'task',     label: '업무별 구성원별 계약내역' },
]

const activeTab   = ref('specific')

// ── 특정품목 탭 상태 ──
const selectedFile = ref(null)
const fileInputRef = ref(null)
const uploading   = ref(false)
const isDragging  = ref(false)
const result      = ref(null)
const progressPct = ref(0)
const progressPhase = ref('idle') // idle | uploading | processing
const uploadEtaText = ref('')
const serverEtaText = ref('')
const activeJobId = ref(localStorage.getItem('specificItemJobId') || '')
const jobMessage = ref('')

let uploadStartedAtMs = 0
let lastProgressAtMs = 0
let lastLoaded = 0
let emaBps = 0
let pollTimer = null
let lastJobSampleAt = 0
let lastJobProcessed = 0
let jobRateEma = 0

const progressPhaseLabel = computed(() => {
  if (progressPhase.value === 'uploading') return '업로드 중'
  if (progressPhase.value === 'processing') return '적재 처리 중'
  if (activeJobId.value) return '적재 처리 중'
  return ''
})

// ── 업무별 구성원별 계약내역 탭 상태 ──
const taskSelectedFile = ref(null)
const taskFileInputRef = ref(null)
const taskUploading   = ref(false)
const taskIsDragging  = ref(false)
const taskResult      = ref(null)
const taskProgressPct = ref(0)
const taskProgressPhase = ref('idle')
const taskUploadEtaText = ref('')
const taskServerEtaText = ref('')
const taskActiveJobId = ref(localStorage.getItem('taskContractJobId') || '')
const taskJobMessage = ref('')

let taskUploadStartedAtMs = 0
let taskLastProgressAtMs = 0
let taskLastLoaded = 0
let taskEmaBps = 0
let taskPollTimer = null
let taskLastJobSampleAt = 0
let taskLastJobProcessed = 0
let taskJobRateEma = 0

const taskProgressPhaseLabel = computed(() => {
  if (taskProgressPhase.value === 'uploading') return '업로드 중'
  if (taskProgressPhase.value === 'processing') return '적재 처리 중'
  if (taskActiveJobId.value) return '적재 처리 중'
  return ''
})

function onFileSelect(event) {
  const file = event.target.files?.[0]
  if (file) setFile(file)
}

function onDrop(event) {
  isDragging.value = false
  const file = event.dataTransfer.files?.[0]
  if (file) setFile(file)
}

function setFile(file) {
  selectedFile.value = file
  result.value = null
  progressPct.value = 0
  progressPhase.value = 'idle'
  uploadEtaText.value = ''
  serverEtaText.value = ''
  uploadStartedAtMs = 0
  lastProgressAtMs = 0
  lastLoaded = 0
  emaBps = 0
}

async function doUpload() {
  if (!selectedFile.value || uploading.value) return

  uploading.value = true
  result.value    = null
  progressPct.value = 0
  progressPhase.value = 'uploading'
  uploadEtaText.value = ''
  serverEtaText.value = ''
  uploadStartedAtMs = Date.now()
  lastProgressAtMs = uploadStartedAtMs
  lastLoaded = 0
  emaBps = 0

  const formData = new FormData()
  formData.append('file', selectedFile.value)

  try {
    const res = await axios.post('/api/admin/upload/specific-item', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      timeout: 300_000, // 5분 — 대용량 파일 대응
      onUploadProgress: (evt) => {
        const total = evt.total ?? selectedFile.value?.size ?? 0
        if (!total) return

        // progress 이벤트가 너무 자주 오면(또는 너무 드물면) UI가 튀는 느낌이 나서
        // 100ms 단위로 속도/ETA를 갱신하고, 속도는 EMA로 완화한다.
        const now = Date.now()
        const dt = Math.max(1, now - lastProgressAtMs)
        const dLoaded = Math.max(0, evt.loaded - lastLoaded)
        if (dLoaded > 0) {
          // 1) 순간 속도(증분 기반) + EMA
          if (dt >= 100) {
            const bps = (dLoaded * 1000) / dt
            emaBps = emaBps ? (emaBps * 0.8 + bps * 0.2) : bps
            lastProgressAtMs = now
            lastLoaded = evt.loaded
          }
          // 2) 업로드가 너무 빨리 끝나 progress 이벤트가 거의 안 오는 경우 대비:
          //    시작 시점 기준 평균 속도로 ETA를 계산한다.
          const elapsed = Math.max(1, now - uploadStartedAtMs)
          const avgBps = (evt.loaded * 1000) / elapsed
          const effBps = emaBps || avgBps
          const remainingBytes = Math.max(0, total - evt.loaded)
          if (effBps > 0 && remainingBytes > 0) {
            uploadEtaText.value = formatEta(remainingBytes / effBps)
          } else {
            uploadEtaText.value = ''
          }
        }

        // 업로드가 완료되면 즉시 '처리 중'으로 전환해서 99%에 멈춘 느낌을 없앤다.
        if (evt.loaded >= total) {
          progressPhase.value = 'processing'
          progressPct.value = 100
          uploadEtaText.value = ''
          return
        }

        progressPct.value = Math.round((evt.loaded / total) * 100)
      },
    })
    // 서버는 jobId를 반환하고, 실제 적재는 비동기 처리됨
    const jobId = res.data?.jobId
    if (jobId) {
      activeJobId.value = jobId
      localStorage.setItem('specificItemJobId', jobId)
      progressPhase.value = 'processing'
      progressPct.value = 0
      startPolling()
    } else {
      result.value = { errorMessage: 'jobId를 받지 못했습니다.' }
    }
  } catch (err) {
    const msg = err.response?.data?.errorMessage
      || err.response?.data?.message
      || err.message
      || '서버 오류가 발생했습니다.'
    result.value = { errorMessage: msg }
  } finally {
    uploading.value = false
    if (!activeJobId.value) progressPhase.value = 'idle'
  }
}

function startPolling() {
  if (!activeJobId.value) return
  stopPolling()
  lastJobSampleAt = Date.now()
  lastJobProcessed = 0
  jobRateEma = 0
  pollTimer = setInterval(pollJob, 1000)
  pollJob()
}

function stopPolling() {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

async function pollJob() {
  if (!activeJobId.value) return
  try {
    const res = await axios.get(`/api/admin/upload/jobs/${activeJobId.value}`)
    const job = res.data

    const total = job.totalRows ?? 0
    const processed = job.processedRows ?? 0
    const inserted = job.insertedCount ?? 0
    const skipped = job.skippedCount ?? 0
    jobMessage.value = job.message || ''

    if (total > 0) {
      progressPct.value = Math.min(100, Math.round((processed / total) * 100))
    } else {
      progressPct.value = 0
    }

    // ETA: 처리율(rows/sec) 기반
    const now = Date.now()
    const dt = Math.max(1, (now - lastJobSampleAt) / 1000)
    const dProc = Math.max(0, processed - lastJobProcessed)
    const rate = dProc / dt
    jobRateEma = jobRateEma ? (jobRateEma * 0.8 + rate * 0.2) : rate
    lastJobSampleAt = now
    lastJobProcessed = processed

    if (total > 0 && jobRateEma > 0 && processed < total) {
      serverEtaText.value = formatEta((total - processed) / jobRateEma)
    } else {
      serverEtaText.value = ''
    }

    if (job.status === 'SUCCESS' || job.status === 'FAILED' || job.status === 'CANCELLED') {
      stopPolling()
      // 완료 결과 카드 표시용
      if (job.status === 'SUCCESS') {
        result.value = {
          totalRows: total,
          insertedCount: inserted,
          skippedCount: skipped,
          elapsedMs: job.finishedAt && job.startedAt
            ? (new Date(job.finishedAt).getTime() - new Date(job.startedAt).getTime())
            : 0,
        }
      } else {
        result.value = { errorMessage: job.errorMessage || '적재 실패' }
      }
      localStorage.removeItem('specificItemJobId')
      activeJobId.value = ''
      jobMessage.value = ''
      serverEtaText.value = ''
      progressPhase.value = 'idle'
    } else {
      progressPhase.value = 'processing'
    }
  } catch (e) {
    // 일시 오류 시에도 폴링 지속
  }
}

// ── 업무별 구성원별 계약내역 탭 핸들러 ──
function onTaskFileSelect(event) {
  const file = event.target.files?.[0]
  if (file) setTaskFile(file)
}

function onTaskDrop(event) {
  taskIsDragging.value = false
  const file = event.dataTransfer.files?.[0]
  if (file) setTaskFile(file)
}

function setTaskFile(file) {
  taskSelectedFile.value = file
  taskResult.value = null
  taskProgressPct.value = 0
  taskProgressPhase.value = 'idle'
  taskUploadEtaText.value = ''
  taskServerEtaText.value = ''
  taskUploadStartedAtMs = 0
  taskLastProgressAtMs = 0
  taskLastLoaded = 0
  taskEmaBps = 0
}

async function doTaskUpload() {
  if (!taskSelectedFile.value || taskUploading.value) return

  taskUploading.value = true
  taskResult.value    = null
  taskProgressPct.value = 0
  taskProgressPhase.value = 'uploading'
  taskUploadEtaText.value = ''
  taskServerEtaText.value = ''
  taskUploadStartedAtMs = Date.now()
  taskLastProgressAtMs = taskUploadStartedAtMs
  taskLastLoaded = 0
  taskEmaBps = 0

  const formData = new FormData()
  formData.append('file', taskSelectedFile.value)

  try {
    const res = await axios.post('/api/admin/upload/task-member-contract', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      timeout: 300_000,
      onUploadProgress: (evt) => {
        const total = evt.total ?? taskSelectedFile.value?.size ?? 0
        if (!total) return

        const now = Date.now()
        const dt = Math.max(1, now - taskLastProgressAtMs)
        const dLoaded = Math.max(0, evt.loaded - taskLastLoaded)
        if (dLoaded > 0) {
          if (dt >= 100) {
            const bps = (dLoaded * 1000) / dt
            taskEmaBps = taskEmaBps ? (taskEmaBps * 0.8 + bps * 0.2) : bps
            taskLastProgressAtMs = now
            taskLastLoaded = evt.loaded
          }
          const elapsed = Math.max(1, now - taskUploadStartedAtMs)
          const avgBps = (evt.loaded * 1000) / elapsed
          const effBps = taskEmaBps || avgBps
          const remainingBytes = Math.max(0, total - evt.loaded)
          if (effBps > 0 && remainingBytes > 0) {
            taskUploadEtaText.value = formatEta(remainingBytes / effBps)
          } else {
            taskUploadEtaText.value = ''
          }
        }

        if (evt.loaded >= total) {
          taskProgressPhase.value = 'processing'
          taskProgressPct.value = 100
          taskUploadEtaText.value = ''
          return
        }

        taskProgressPct.value = Math.round((evt.loaded / total) * 100)
      },
    })

    const jobId = res.data?.jobId
    if (jobId) {
      taskActiveJobId.value = jobId
      localStorage.setItem('taskContractJobId', jobId)
      taskProgressPhase.value = 'processing'
      taskProgressPct.value = 0
      startTaskPolling()
    } else {
      taskResult.value = { errorMessage: 'jobId를 받지 못했습니다.' }
    }
  } catch (err) {
    const msg = err.response?.data?.errorMessage
      || err.response?.data?.message
      || err.message
      || '서버 오류가 발생했습니다.'
    taskResult.value = { errorMessage: msg }
  } finally {
    taskUploading.value = false
    if (!taskActiveJobId.value) taskProgressPhase.value = 'idle'
  }
}

function startTaskPolling() {
  if (!taskActiveJobId.value) return
  stopTaskPolling()
  taskLastJobSampleAt = Date.now()
  taskLastJobProcessed = 0
  taskJobRateEma = 0
  taskPollTimer = setInterval(pollTaskJob, 1000)
  pollTaskJob()
}

function stopTaskPolling() {
  if (taskPollTimer) {
    clearInterval(taskPollTimer)
    taskPollTimer = null
  }
}

async function pollTaskJob() {
  if (!taskActiveJobId.value) return
  try {
    const res = await axios.get(`/api/admin/upload/jobs/${taskActiveJobId.value}`)
    const job = res.data

    const total = job.totalRows ?? 0
    const processed = job.processedRows ?? 0
    const inserted = job.insertedCount ?? 0
    const skipped = job.skippedCount ?? 0
    taskJobMessage.value = job.message || ''

    if (total > 0) {
      taskProgressPct.value = Math.min(100, Math.round((processed / total) * 100))
    } else {
      taskProgressPct.value = 0
    }

    const now = Date.now()
    const dt = Math.max(1, (now - taskLastJobSampleAt) / 1000)
    const dProc = Math.max(0, processed - taskLastJobProcessed)
    const rate = dProc / dt
    taskJobRateEma = taskJobRateEma ? (taskJobRateEma * 0.8 + rate * 0.2) : rate
    taskLastJobSampleAt = now
    taskLastJobProcessed = processed

    if (total > 0 && taskJobRateEma > 0 && processed < total) {
      taskServerEtaText.value = formatEta((total - processed) / taskJobRateEma)
    } else {
      taskServerEtaText.value = ''
    }

    if (job.status === 'SUCCESS' || job.status === 'FAILED' || job.status === 'CANCELLED') {
      stopTaskPolling()
      if (job.status === 'SUCCESS') {
        taskResult.value = {
          totalRows: total,
          insertedCount: inserted,
          skippedCount: skipped,
          elapsedMs: job.finishedAt && job.startedAt
            ? (new Date(job.finishedAt).getTime() - new Date(job.startedAt).getTime())
            : 0,
        }
      } else {
        taskResult.value = { errorMessage: job.errorMessage || '적재 실패' }
      }
      localStorage.removeItem('taskContractJobId')
      taskActiveJobId.value = ''
      taskJobMessage.value = ''
      taskServerEtaText.value = ''
      taskProgressPhase.value = 'idle'
    } else {
      taskProgressPhase.value = 'processing'
    }
  } catch (e) {
    // 일시 오류 시에도 폴링 지속
  }
}

function formatEta(sec) {
  if (!Number.isFinite(sec) || sec < 0) return ''
  const s = Math.ceil(sec)
  if (s < 60) return `${s}초`
  const m = Math.floor(s / 60)
  const r = s % 60
  if (m < 60) return `${m}분 ${r}초`
  const h = Math.floor(m / 60)
  const mm = m % 60
  return `${h}시간 ${mm}분`
}

function formatBytes(bytes) {
  if (bytes < 1024)        return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

// 새로고침 복구: localStorage에 jobId가 있으면 자동 폴링
if (activeJobId.value) {
  progressPhase.value = 'processing'
  startPolling()
}
if (taskActiveJobId.value) {
  taskProgressPhase.value = 'processing'
  startTaskPolling()
}
</script>

<style scoped>
.raw-import-page {
  padding: 0 4px;
  max-width: 900px;
}

.page-title {
  margin: 0 0 8px;
}

.page-desc {
  margin: 0 0 24px;
  font-size: 14px;
  color: #6b7280;
  line-height: 1.6;
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
  transition: color 0.15s;
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
  margin: 0 0 6px;
  font-size: 1.1em;
  color: #374151;
}

.tab-desc {
  margin: 0 0 20px;
  font-size: 13px;
  color: #6b7280;
  line-height: 1.6;
}

.upload-zone {
  position: relative;
  border: 2px dashed #d1d5db;
  border-radius: 10px;
  padding: 32px;
  text-align: center;
  background: #fff;
  margin-bottom: 16px;
  transition: border-color 0.2s, background 0.2s;
}

.upload-zone.dragging {
  border-color: #111827;
  background: #f0f0f0;
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
  color: #111827 !important;
  font-weight: 500;
}

/* 결과 카드 */
.result-card {
  border-radius: 10px;
  padding: 16px 20px;
  margin-bottom: 16px;
  font-size: 14px;
}

.result-ok {
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
}

.result-error {
  background: #fef2f2;
  border: 1px solid #fecaca;
}

.result-title {
  margin: 0 0 10px;
  font-weight: 600;
  font-size: 15px;
  color: #111827;
}

.result-msg {
  margin: 0;
  color: #dc2626;
}

.result-grid {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
}

.result-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.result-label {
  font-size: 12px;
  color: #6b7280;
}

.result-value {
  font-size: 18px;
  font-weight: 600;
  color: #111827;
}

.result-value.green { color: #16a34a; }
.result-value.gray  { color: #9ca3af; }

/* 진행률 카드 */
.progress-card {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 14px 16px;
  margin-bottom: 16px;
}

.progress-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 8px;
}

.progress-title {
  font-weight: 600;
  color: #111827;
  font-size: 14px;
}

.progress-pct {
  font-weight: 600;
  color: #111827;
  font-size: 14px;
}

.progress-bar {
  width: 100%;
  height: 10px;
  border-radius: 999px;
  background: #f3f4f6;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  width: 0%;
  background: #111827;
  transition: width 0.15s ease;
}

.progress-bar.processing .progress-fill {
  background: linear-gradient(90deg, #111827 0%, #374151 40%, #111827 80%);
  background-size: 200% 100%;
  animation: shimmer 1.1s linear infinite;
}

.progress-hint {
  margin: 8px 0 0;
  font-size: 12px;
  color: #6b7280;
  line-height: 1.4;
}

.job-msg {
  color: #374151;
}

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: 0 0; }
}

/* 안내 박스 */
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
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 9px 22px;
  background: #111827;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.15s;
}

.btn-import:hover:not(:disabled) {
  background: #1f2937;
}

.btn-import:disabled {
  background: #9ca3af;
  cursor: not-allowed;
}

/* 스피너 */
.spinner {
  display: inline-block;
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255, 255, 255, 0.4);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
