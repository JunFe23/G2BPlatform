<template>
  <LegacySidebarLayout>
    <h1 class="construction">관련업계 전체시장 DB - 3자단가</h1>

    <!-- 검색 필드 -->
    <div class="search-container">
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

    <!-- 데이터 테이블 -->
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
              <td colspan="13" class="loading-text">데이터를 불러오는 중입니다...</td>
            </tr>
            <tr v-else-if="items.length === 0">
              <td colspan="13" class="no-data">데이터가 없습니다.</td>
            </tr>
            <tr v-else v-for="item in items" :key="item.id">
              <td>{{ item.cmpNm }}</td>
              <td>
                <a
                  v-if="item.cntrctDtlInfoUrl"
                  :href="item.cntrctDtlInfoUrl"
                  target="_blank"
                  rel="noopener noreferrer"
                  >{{ item.cntrctNm }}</a
                >
                <span v-else>{{ item.cntrctNm }}</span>
              </td>
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

const PAGE_SIZE = 100
const CATEGORY = 'shoppingmall'

const isLoading = ref(false)
const items = ref([])
const recordsFiltered = ref(0)
const currentPage = ref(1)
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

function buildParams() {
  const start = (currentPage.value - 1) * PAGE_SIZE
  return {
    draw: 1,
    start,
    length: PAGE_SIZE,
    'search[value]': '',
    category: CATEGORY,
    dminsttNm: filters.dminsttNm || undefined,
    dminsttNmDetail: filters.dminsttNmDetail || undefined,
    prdctClsfcNo: filters.prdctClsfcNo || undefined,
    cntctCnclsMthdNm: filters.cntctCnclsMthdNm || undefined,
    firstCntrctDate: filters.firstCntrctDate || undefined,
    year: filters.dateType === 'year' && filters.year ? parseInt(filters.year, 10) : undefined,
    month: filters.dateType === 'month' ? filters.month || undefined : undefined,
    rangeStart: filters.dateType === 'range' ? filters.rangeStart || undefined : undefined,
    rangeEnd: filters.dateType === 'range' ? filters.rangeEnd || undefined : undefined,
    showSavedOnly: filters.showSavedOnly ? 1 : 0,
  }
}

function buildExcelRequest() {
  const p = buildParams()
  return {
    category: CATEGORY,
    dminsttNm: p.dminsttNm ?? null,
    dminsttNmDetail: p.dminsttNmDetail ?? null,
    prdctClsfcNo: p.prdctClsfcNo ?? null,
    cntctCnclsMthdNm: p.cntctCnclsMthdNm ?? null,
    firstCntrctDate: p.firstCntrctDate ?? null,
    year: p.year ?? null,
    month: p.month ?? null,
    rangeStart: p.rangeStart ?? null,
    rangeEnd: p.rangeEnd ?? null,
    showSavedOnly: p.showSavedOnly,
  }
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
    const { data } = await axios.get('/api/data', { params: buildParams() })
    items.value = Array.isArray(data.data) ? data.data : []
    recordsFiltered.value = data.recordsFiltered ?? 0
  } catch (e) {
    console.error('데이터 조회 실패', e)
    items.value = []
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

const handleSearch = () => {
  fetchData(true)
}

const handleDownloadExcel = async () => {
  try {
    const res = await axios.post('/api/data/excel', buildExcelRequest(), { responseType: 'blob' })
    const url = URL.createObjectURL(new Blob([res.data]))
    const a = document.createElement('a')
    a.href = url
    a.download = '조회결과.xlsx'
    a.click()
    URL.revokeObjectURL(url)
  } catch (e) {
    console.error('엑셀 다운로드 실패', e)
    alert('엑셀 다운로드에 실패했습니다.')
  }
}

const toggleSave = async (item) => {
  const nextChecked = !item.checked
  try {
    await axios.post('/api/update-checked', {
      category: CATEGORY,
      id: item.id,
      checked: nextChecked,
    })
    item.checked = nextChecked
  } catch (e) {
    console.error('저장 상태 업데이트 실패', e)
    alert('저장 상태 변경에 실패했습니다.')
  }
}

const formatNumber = (num) =>
  num != null ? (isNaN(Number(num)) ? String(num) : Number(num).toLocaleString()) : ''

watch(
  () => filters.showSavedOnly,
  () => {
    fetchData(true)
  },
)
onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.search-container {
  margin-bottom: 20px;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

input[type='text'],
select,
input[type='month'] {
  padding: 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
}

.search-btn {
  padding: 8px 15px;
  background-color: #34495e;
  color: #ecf0f1;
  border: none;
  cursor: pointer;
  border-radius: 4px;
}

.excel-btn {
  padding: 8px 15px;
  background-color: #27ae60;
  color: #ecf0f1;
  border: none;
  cursor: pointer;
  margin-left: 10px;
  border-radius: 4px;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 5px;
  cursor: pointer;
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
}

.data-table {
  width: 100%;
  margin-top: 20px;
  border-collapse: collapse;
}

.data-table th,
.data-table td {
  padding: 10px;
  border: 1px solid #ddd;
  text-align: center;
}

.data-table th {
  background-color: #f8f9fa;
  font-weight: bold;
}

.data-table tbody tr:nth-child(even) {
  background-color: #f9f9f9;
}

.data-table tbody tr:hover {
  background-color: #f1f1f1;
}

.loading-text,
.no-data {
  text-align: center;
  padding: 20px;
  color: #666;
}

a {
  color: #3498db;
  text-decoration: none;
}

a:hover {
  text-decoration: underline;
}
.table-wrapper {
  max-height: 70vh;
  overflow: auto;
  border: 1px solid #ccc;
}

.data-table th {
  position: sticky;
  top: 0;
  background-color: #f1f1f1;
  z-index: 1;
}

.data-table th,
.data-table td {
  white-space: nowrap;
}

.pagination {
  margin-top: 12px;
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}
.pagination-info {
  margin-right: 12px;
}
.page-btn {
  padding: 6px 12px;
  border: 1px solid #ddd;
  background: #fff;
  cursor: pointer;
  border-radius: 4px;
}
.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.page-btn:not(:disabled):hover {
  background: #f5f5f5;
}
.page-num-btn {
  min-width: 36px;
  padding: 6px 10px;
  border: 1px solid #ddd;
  background: #fff;
  cursor: pointer;
  border-radius: 4px;
}
.page-num-btn:hover {
  background: #f5f5f5;
}
.page-num-btn.active {
  background: #34495e;
  color: #fff;
  border-color: #34495e;
}
</style>
