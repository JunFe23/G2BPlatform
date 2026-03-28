<template>
  <LegacySidebarLayout>
    <h1 class="construction">탑인더스트리 & 탑정보통신 수주현황DB - 보고서 데이터</h1>

    <!-- 검색 필드 -->
    <div class="search-container">
      <select v-model="filters.type" class="type-select">
        <option value="">선택</option>
        <option value="물품">물품</option>
        <option value="용역">용역</option>
        <option value="공사">공사</option>
      </select>

      <input type="text" v-model="filters.dminsttNm" placeholder="수요기관명 검색" />
      <input type="text" v-model="filters.dminsttNmDetail" placeholder="수요기관지역명 검색" />
      <input type="text" v-model="filters.prdctClsfcNo" placeholder="품명내용 검색" />
      <input type="text" v-model="filters.cntctCnclsMthdNm" placeholder="입찰계약방법 검색" />
      <input type="text" v-model="filters.firstCntrctDate" placeholder="최초계약일자 검색" />

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

      <button @click="handleSearch" class="search-btn">검색</button>

      <label class="checkbox-label">
        <input type="checkbox" v-model="filters.showSavedOnly" />
        저장된 데이터만 보기
      </label>

      <button @click="handleDownloadExcel" class="excel-btn">엑셀 다운로드</button>

      <div v-if="isLoading" class="loading-spinner-container">
        <div class="loading-spinner"></div>
      </div>
    </div>

    <!-- WIP 배너 -->
    <div class="wip-banner">
      <span class="wip-icon">🚧</span>
      <span>보고서 데이터 화면은 현재 개발 중입니다.</span>
    </div>

    <!-- 데이터 테이블 -->
    <div class="table-container">
      <div class="table-wrapper">
        <table class="data-table">
          <thead>
            <tr>
              <th>분류</th>
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
              <td colspan="14" class="loading-text">데이터를 불러오는 중입니다...</td>
            </tr>
            <tr v-else-if="items.length === 0">
              <td colspan="14" class="no-data">데이터가 없습니다.</td>
            </tr>
            <tr v-else v-for="item in items" :key="item.id">
              <td>{{ item.type }}</td>
              <td>{{ item.cmpNm }}</td>
              <td>{{ item.cntrctNm }}</td>
              <td>{{ item.dminsttNm }}</td>
              <td>{{ item.dminsttNmDetail }}</td>
              <td>{{ item.prdctClsfcNo }}</td>
              <td>{{ item.cntctCnclsMthdNm }}</td>
              <td>{{ item.ntceNo }}</td>
              <td>{{ item.firstCntrctDate }}</td>
              <td>{{ formatNumber(item.firstCntrctAmt) }}</td>
              <td>{{ item.cntrctDate }}</td>
              <td>{{ formatNumber(item.thtmCntrctAmt) }}</td>
              <td>{{ item.cntrctCnt }}</td>
              <td>
                <input type="checkbox" :checked="item.checked" @change="toggleSave(item)" />
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div v-if="recordsFiltered > 0" class="pagination">
        <span class="pagination-info">
          {{ formatNumber(startDisplay) }}-{{ formatNumber(endDisplay) }} / {{ formatNumber(recordsFiltered) }}건
        </span>
        <button class="page-btn" :disabled="currentPage <= 1" @click="goPage(currentPage - 1)">이전</button>
        <button
          v-for="p in pageNumbers"
          :key="p"
          class="page-num-btn"
          :class="{ active: p === currentPage }"
          @click="goPage(p)"
        >
          {{ p }}
        </button>
        <button class="page-btn" :disabled="currentPage >= totalPages" @click="goPage(currentPage + 1)">다음</button>
      </div>
    </div>
  </LegacySidebarLayout>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import LegacySidebarLayout from './components/LegacySidebarLayout.vue'

const PAGE_SIZE = 100

const isLoading = ref(false)
const items = ref([])
const recordsFiltered = ref(0)
const currentPage = ref(1)

const currentYear = new Date().getFullYear()
const years = Array.from({ length: currentYear - 2014 }, (_, i) => currentYear - i)

const filters = reactive({
  type: '',
  dminsttNm: '',
  dminsttNmDetail: '',
  prdctClsfcNo: '',
  cntctCnclsMthdNm: '',
  firstCntrctDate: '',
  dateType: 'year',
  year: String(currentYear),
  month: '',
  rangeStart: '',
  rangeEnd: '',
  showSavedOnly: false,
})

const totalPages = computed(() => Math.ceil(recordsFiltered.value / PAGE_SIZE) || 1)
const startDisplay = computed(() => (currentPage.value - 1) * PAGE_SIZE + 1)
const endDisplay = computed(() => Math.min(currentPage.value * PAGE_SIZE, recordsFiltered.value))
const pageNumbers = computed(() => {
  const total = totalPages.value
  const cur = currentPage.value
  const delta = 2
  const pages = []
  for (let i = Math.max(1, cur - delta); i <= Math.min(total, cur + delta); i++) pages.push(i)
  return pages
})

function formatNumber(val) {
  if (val === null || val === undefined || val === '') return ''
  const num = Number(val)
  return isNaN(num) ? val : num.toLocaleString('ko-KR')
}

function handleSearch() {
  // 추후 API 연동
}

function handleDownloadExcel() {
  // 추후 API 연동
}

function toggleSave(_item) {
  // 추후 API 연동
}

function goPage(page) {
  if (page < 1 || page > totalPages.value) return
  currentPage.value = page
}
</script>

<style scoped>
.search-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
  align-items: center;
}

.type-select,
.date-select {
  padding: 8px 10px;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  font-size: 14px;
  background: #fff;
  color: #2c3e50;
  cursor: pointer;
}

.search-container input[type='text'],
.search-container input[type='month'] {
  padding: 8px 10px;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  font-size: 14px;
  color: #2c3e50;
  min-width: 140px;
}

.search-btn {
  padding: 8px 18px;
  background: linear-gradient(135deg, #3d5a73, #34495e);
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
}

.search-btn:hover {
  opacity: 0.9;
}

.excel-btn {
  padding: 8px 16px;
  background: #27ae60;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
}

.excel-btn:hover {
  background: #229954;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #475569;
  cursor: pointer;
}

.loading-spinner-container {
  display: flex;
  align-items: center;
}

.loading-spinner {
  width: 20px;
  height: 20px;
  border: 3px solid #e2e8f0;
  border-top-color: #3d5a73;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.wip-banner {
  display: flex;
  align-items: center;
  gap: 10px;
  background: #fffbeb;
  border: 1px solid #fde68a;
  border-radius: 10px;
  padding: 14px 18px;
  margin-bottom: 16px;
  font-size: 14px;
  color: #92400e;
  font-weight: 500;
}

.wip-icon {
  font-size: 18px;
}

.table-container {
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.07);
  padding: 16px;
  overflow: hidden;
}

.table-wrapper {
  overflow-x: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}

.data-table th {
  background: #f1f5f9;
  color: #334155;
  font-weight: 600;
  padding: 10px 12px;
  text-align: left;
  border-bottom: 2px solid #e2e8f0;
  white-space: nowrap;
}

.data-table td {
  padding: 9px 12px;
  border-bottom: 1px solid #f1f5f9;
  color: #475569;
  vertical-align: middle;
}

.data-table tbody tr:hover {
  background: #f8fafc;
}

.loading-text,
.no-data {
  text-align: center;
  padding: 40px;
  color: #94a3b8;
  font-size: 14px;
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  margin-top: 16px;
  flex-wrap: wrap;
}

.pagination-info {
  font-size: 13px;
  color: #64748b;
  margin-right: 8px;
}

.page-btn,
.page-num-btn {
  padding: 6px 14px;
  border: 1px solid #cbd5e1;
  border-radius: 7px;
  background: #fff;
  color: #475569;
  font-size: 13px;
  cursor: pointer;
}

.page-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.page-num-btn.active {
  background: linear-gradient(135deg, #3d5a73, #34495e);
  color: #fff;
  border-color: transparent;
}

.page-btn:hover:not(:disabled),
.page-num-btn:hover:not(.active) {
  background: #f1f5f9;
}
</style>
