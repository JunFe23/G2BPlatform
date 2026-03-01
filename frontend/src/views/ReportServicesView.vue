<template>
  <LegacySidebarLayout>
    <h1 class="services">보고서 데이터 - 용역</h1>

    <!-- 검색 필드 -->
    <div class="search-container">
      <!-- 1줄: 필터만 -->
      <div class="search-filter-row">
        <input type="text" v-model="filters.dminsttNm" placeholder="수요기관명 검색" />
        <input type="text" v-model="filters.dminsttNmDetail" placeholder="수요기관지역명 검색" />
        <input type="text" v-model="filters.prdctClsfcNo" placeholder="품명내용 검색" />
        <input type="text" v-model="filters.cntctCnclsMthdNm" placeholder="입찰계약방법 검색" />
        <input type="text" v-model="filters.firstCntrctDate" placeholder="최초계약일자(YYYY-MM-DD)" />

        <select v-model="filters.dateType" class="date-select">
          <option value="year">연도 검색</option>
          <option value="month">특정 월 검색</option>
          <option value="range">기간 검색</option>
        </select>

        <select v-if="filters.dateType === 'year'" v-model="filters.year" class="date-select">
          <option value="">선택</option>
          <option v-for="y in years" :key="y" :value="y">{{ y }}</option>
        </select>

        <input v-if="filters.dateType === 'month'" type="month" v-model="filters.month" />

        <template v-if="filters.dateType === 'range'">
          <input type="month" v-model="filters.rangeStart" placeholder="시작월" />
          <input type="month" v-model="filters.rangeEnd" placeholder="종료월" />
        </template>
      </div>

      <!-- 2줄: 저장된 데이터만 보기, 장기계약 토글, 검색, 엑셀 (오른쪽 정렬) -->
      <div class="search-actions-row">
        <label class="checkbox-label">
          <input type="checkbox" v-model="filters.showSavedOnly" />
          저장된 데이터만 보기
        </label>
        <span class="actions-sep" aria-hidden="true"></span>
        <div class="long-term-toggle-wrap">
        <span class="long-term-toggle-label">장기계약 건</span>
        <button
          type="button"
          role="switch"
          :aria-checked="longTermViewMerged"
          :title="longTermViewMerged ? '합쳐서 보기' : '풀어서 보기'"
          class="long-term-toggle-switch"
          :class="{ on: longTermViewMerged }"
          @click="onLongTermToggleClick"
        >
          <span class="long-term-toggle-slider"></span>
        </button>
        <span class="long-term-toggle-caption">{{ longTermViewMerged ? '합쳐서 보기' : '풀어서 보기' }}</span>
        </div>

        <button @click="handleSearch" class="search-btn">검색</button>
        <button @click="handleDownloadExcel" class="excel-btn" :disabled="isLoadingExcel">
          {{ isLoadingExcel ? '다운로드 중...' : '엑셀 다운로드' }}
        </button>

        <div v-if="isLoading" class="loading-spinner-container">
          <div class="loading-spinner"></div>
        </div>
      </div>

      <div v-if="isLoadingExcel" class="excel-download-overlay">
        <div class="excel-download-progress">
          <div class="excel-spinner"></div>
          <p class="excel-progress-title">엑셀 생성 중</p>
          <p class="excel-progress-desc">
            데이터가 많을 경우 10~30분 정도 걸릴 수 있습니다. 조건을 좁히면 더 빠릅니다.
          </p>
          <p class="excel-progress-hint">창을 닫지 마세요.</p>
        </div>
      </div>
    </div>

    <div class="table-container">
      <div class="table-wrapper">
        <table class="data-table">
          <thead>
            <tr>
              <th>업체명</th>
              <th>계약건명</th>
              <th>수요기관명</th>
              <th>수요기관지역명</th>
              <th>품명내용</th>
              <th>입찰계약방법</th>
              <th>입찰공고번호</th>
              <th>최초계약일자</th>
              <th>최초계약금액</th>
              <th>최종계약일자</th>
              <th>최종계약금액</th>
              <th>계약변경차수</th>
              <th>저장</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="isLoading">
              <td :colspan="COL_SPAN" class="loading-text">데이터를 불러오는 중입니다...</td>
            </tr>
            <tr v-else-if="items.length === 0">
              <td :colspan="COL_SPAN" class="no-data">데이터가 없습니다.</td>
            </tr>
            <tr v-else v-for="item in items" :key="rowKey(item)">
              <td>{{ item.vendorName }}</td>
              <td>{{ item.contractTitle }}</td>
              <td>{{ item.demandAgencyName }}</td>
              <td>{{ item.demandAgencyRegion }}</td>
              <td>{{ item.detailItemName }}</td>
              <td>{{ item.contractMethod }}</td>
              <td>{{ item.bidNoticeNo }}</td>
              <td>{{ item.firstContractDate }}</td>
              <td>{{ formatNumber(item.firstContractAmount) }}</td>
              <td>{{ item.finalContractDate }}</td>
              <td>{{ formatNumber(item.finalContractAmount) }}</td>
              <td>{{ item.contractCount ?? item.contractChangeCount ?? '-' }}</td>
              <td>
                <input type="checkbox" :checked="item.saved === 'Y'" @change="toggleSave(item)" />
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div v-if="recordsFiltered > 0" class="pagination">
        <span class="pagination-info"
          >{{ formatNumber(startDisplay) }}-{{ formatNumber(endDisplay) }} / {{ formatNumber(recordsFiltered) }}건</span
        >
        <button class="page-btn" :disabled="currentPage <= 1" @click="goPage(currentPage - 1)">
          이전
        </button>
        <button
          v-for="p in pageNumbers"
          :key="p"
          class="page-num-btn"
          :class="{ active: p === currentPage }"
          @click="goPage(p)"
        >
          {{ p }}
        </button>
        <button
          class="page-btn"
          :disabled="currentPage >= totalPages"
          @click="goPage(currentPage + 1)"
        >
          다음
        </button>
      </div>
    </div>
  </LegacySidebarLayout>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import axios from 'axios'
import LegacySidebarLayout from './components/LegacySidebarLayout.vue'

const API_BASE = '/api/report/services'
const COL_SPAN = 13
const PAGE_SIZE = 100

const isLoading = ref(false)
const isLoadingExcel = ref(false)
const items = ref([])
const recordsFiltered = ref(0)
const currentPage = ref(1)

/** true: 장기계약 합쳐서 보기, false: 풀어서 보기 (기능 연동 예정) */
const longTermViewMerged = ref(true)

function onLongTermToggleClick() {
  alert('해당 기능은 개발 중입니다.')
  longTermViewMerged.value = !longTermViewMerged.value
}

const filters = reactive({
  dminsttNm: '',
  dminsttNmDetail: '',
  prdctClsfcNo: '',
  cntctCnclsMthdNm: '',
  firstCntrctDate: '',
  dateType: 'year',
  year: '',
  month: '',
  rangeStart: '',
  rangeEnd: '',
  showSavedOnly: false,
})

const years = ['2025', '2024', '2023', '2022', '2021', '2020']

function rowKey(item) {
  return (item.bidNoticeNo || '') + '_' + (item.vendorBizRegNo || '')
}

function buildParams(includePaging = true) {
  const p = {
    dminsttNm: filters.dminsttNm || undefined,
    dminsttNmDetail: filters.dminsttNmDetail || undefined,
    prdctClsfcNo: filters.prdctClsfcNo || undefined,
    cntctCnclsMthdNm: filters.cntctCnclsMthdNm || undefined,
    firstCntrctDate: filters.firstCntrctDate || undefined,
    year: filters.dateType === 'year' && filters.year ? parseInt(filters.year, 10) : undefined,
    month: filters.dateType === 'month' ? filters.month || undefined : undefined,
    rangeStart: filters.dateType === 'range' ? filters.rangeStart || undefined : undefined,
    rangeEnd: filters.dateType === 'range' ? filters.rangeEnd || undefined : undefined,
    showSavedOnly: filters.showSavedOnly,
  }
  if (includePaging) {
    p.start = (currentPage.value - 1) * PAGE_SIZE
    p.length = PAGE_SIZE
  }
  return p
}

const totalPages = computed(() => Math.max(1, Math.ceil(recordsFiltered.value / PAGE_SIZE)))
const startDisplay = computed(() =>
  recordsFiltered.value === 0 ? 0 : (currentPage.value - 1) * PAGE_SIZE + 1,
)
const endDisplay = computed(() => Math.min(currentPage.value * PAGE_SIZE, recordsFiltered.value))
const pageNumbers = computed(() => {
  const total = totalPages.value
  if (total <= 10) return Array.from({ length: total }, (_, i) => i + 1)
  const cur = currentPage.value
  let start = Math.max(1, cur - 4)
  let end = Math.min(total, start + 9)
  if (end - start < 9) start = Math.max(1, end - 9)
  return Array.from({ length: end - start + 1 }, (_, i) => start + i)
})

const fetchData = async (resetPage = false) => {
  if (resetPage) currentPage.value = 1
  isLoading.value = true
  try {
    const { data } = await axios.get(API_BASE, { params: buildParams(true) })
    items.value = Array.isArray(data.data) ? data.data : []
    recordsFiltered.value = data.recordsFiltered ?? items.value.length
  } catch (e) {
    console.error('보고서 용역 조회 실패', e)
    items.value = []
    recordsFiltered.value = 0
  } finally {
    isLoading.value = false
  }
}

const goPage = (page) => {
  const total = Math.max(1, Math.ceil(recordsFiltered.value / PAGE_SIZE))
  if (page < 1 || page > total) return
  currentPage.value = page
  fetchData()
}

const handleSearch = () => fetchData(true)

const handleDownloadExcel = async () => {
  isLoadingExcel.value = true
  try {
    const params = new URLSearchParams()
    Object.entries(buildParams(false)).forEach(([k, v]) => {
      if (v !== undefined && v !== '') params.append(k, v)
    })
    const { data } = await axios.get(API_BASE + '/excel?' + params.toString(), {
      responseType: 'blob',
      timeout: 1800000,
    })
    const url = URL.createObjectURL(new Blob([data]))
    const a = document.createElement('a')
    a.href = url
    a.download = `report_services_${Date.now()}.xlsx`
    a.click()
    URL.revokeObjectURL(url)
  } catch (e) {
    console.error('엑셀 다운로드 실패', e)
    alert(
      e?.code === 'ECONNABORTED'
        ? '요청 시간이 초과되었습니다. 조건을 줄이거나 다시 시도해 주세요.'
        : '엑셀 다운로드에 실패했습니다.',
    )
  } finally {
    isLoadingExcel.value = false
  }
}

const toggleSave = async (item) => {
  const nextSaved = item.saved === 'Y' ? 'N' : 'Y'
  try {
    await axios.patch(API_BASE + '/saved', {
      bidNoticeNo: item.bidNoticeNo,
      vendorBizRegNo: item.vendorBizRegNo,
      saved: nextSaved,
    })
    item.saved = nextSaved
  } catch (e) {
    console.error('저장 여부 업데이트 실패', e)
    alert('저장 상태 변경에 실패했습니다.')
  }
}

const formatNumber = (num) => {
  if (num == null) return ''
  const n = typeof num === 'number' ? num : Number(num)
  return isNaN(n) ? String(num) : n.toLocaleString()
}

watch(
  () => filters.showSavedOnly,
  () => fetchData(true),
)

onMounted(() => fetchData())
</script>

<style scoped>
.services {
  color: #ecf0f1;
}
.search-container {
  margin-bottom: 24px;
  padding: 18px 20px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: flex-start;
}

.search-filter-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px 12px;
  flex: 1 1 100%;
  min-width: 0;
}
.search-filter-row input[type='text'],
.search-filter-row select,
.search-filter-row input[type='month'] {
  min-width: 100px;
}
.search-actions-row {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
  flex: 1 1 100%;
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #e2e8f0;
}
input[type='text'],
select,
input[type='month'] {
  padding: 10px 12px;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  font-size: 0.9375em;
  min-width: 120px;
  transition: border-color 0.2s;
}
input[type='text']:focus,
select:focus,
input[type='month']:focus {
  outline: none;
  border-color: #3498db;
}
.search-btn {
  padding: 10px 18px;
  background: linear-gradient(180deg, #3d5a73 0%, #34495e 100%);
  color: #f0f4f8;
  border: none;
  cursor: pointer;
  border-radius: 8px;
  font-weight: 500;
  font-size: 0.9375em;
}
.search-btn:hover {
  opacity: 0.95;
}
.excel-btn {
  padding: 10px 18px;
  background: linear-gradient(180deg, #2e8b5e 0%, #27ae60 100%);
  color: #fff;
  border: none;
  cursor: pointer;
  border-radius: 8px;
  font-weight: 500;
  font-size: 0.9375em;
}
.excel-btn:hover:not(:disabled) {
  opacity: 0.95;
}
.excel-btn:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}
.excel-download-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}
.excel-download-progress {
  background: #fff;
  padding: 28px 36px;
  border-radius: 12px;
  text-align: center;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
}
.excel-spinner {
  width: 48px;
  height: 48px;
  margin: 0 auto 16px;
  border: 4px solid #ecf0f1;
  border-top: 4px solid #27ae60;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}
.excel-progress-title {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 8px;
  color: #2c3e50;
}
.excel-progress-desc {
  font-size: 14px;
  color: #7f8c8d;
  margin: 0 0 4px;
}
.excel-progress-hint {
  font-size: 12px;
  color: #95a5a6;
  margin: 0;
}
.checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-size: 0.9em;
  color: #475569;
}

.actions-sep {
  display: inline-block;
  width: 1px;
  height: 18px;
  margin: 0 4px;
  background: #cbd5e1;
  opacity: 0.7;
  border-radius: 1px;
  flex-shrink: 0;
}

.long-term-toggle-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
}

.long-term-toggle-label {
  font-size: 0.875em;
  font-weight: 500;
  color: #475569;
}

.long-term-toggle-switch {
  position: relative;
  width: 44px;
  height: 24px;
  border: 1px solid #cbd5e1;
  border-radius: 24px;
  background: #e2e8f0;
  cursor: pointer;
  padding: 0;
  transition: background 0.2s, border-color 0.2s;
}

.long-term-toggle-switch:hover {
  border-color: #94a3b8;
}

.long-term-toggle-switch.on {
  background: #3498db;
  border-color: #3498db;
}

.long-term-toggle-slider {
  position: absolute;
  top: 2px;
  left: 2px;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: #fff;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.15);
  transition: transform 0.2s;
}

.long-term-toggle-switch.on .long-term-toggle-slider {
  transform: translateX(20px);
}

.long-term-toggle-caption {
  font-size: 0.8125em;
  color: #64748b;
}

.loading-spinner-container {
  margin-left: 10px;
}
.loading-spinner {
  width: 24px;
  height: 24px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #3498db;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}
@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}
.table-container {
  overflow-x: auto;
  margin-top: 4px;
}
.data-table {
  width: 100%;
  border-collapse: collapse;
}
.data-table th,
.data-table td {
  padding: 12px 10px;
  border: 1px solid #e2e8f0;
  text-align: center;
  font-size: 0.9em;
}
.data-table th {
  background: #f1f5f9;
  font-weight: 600;
  color: #334155;
}
.data-table tbody tr:nth-child(even) {
  background-color: #fafbfc;
}
.data-table tbody tr:hover {
  background-color: #f1f5f9;
}
.loading-text,
.no-data {
  text-align: center;
  padding: 24px;
  color: #64748b;
  font-size: 0.95em;
}
.table-wrapper {
  max-height: 70vh;
  overflow: auto;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
}
.data-table th {
  position: sticky;
  top: 0;
  background: #f1f5f9;
  z-index: 1;
}
.data-table th,
.data-table td {
  white-space: nowrap;
}
.pagination {
  margin-top: 16px;
  padding: 14px 0;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.pagination-info {
  margin-right: 14px;
  font-size: 0.9em;
  color: #64748b;
}
.page-btn {
  padding: 8px 14px;
  border: 1px solid #e2e8f0;
  background: #fff;
  cursor: pointer;
  border-radius: 8px;
  font-size: 0.9em;
  transition: background 0.2s, border-color 0.2s;
}
.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.page-btn:not(:disabled):hover {
  background: #f1f5f9;
  border-color: #cbd5e1;
}
.page-num-btn {
  min-width: 38px;
  padding: 8px 10px;
  border: 1px solid #e2e8f0;
  background: #fff;
  cursor: pointer;
  border-radius: 8px;
  font-size: 0.9em;
  transition: background 0.2s, border-color 0.2s;
}
.page-num-btn:hover {
  background: #f1f5f9;
  border-color: #cbd5e1;
}
.page-num-btn.active {
  background: linear-gradient(180deg, #3d5a73 0%, #34495e 100%);
  color: #fff;
  border-color: #34495e;
}
</style>
