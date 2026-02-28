<template>
  <LegacySidebarLayout>
    <h1 class="servicesSelected">수주대상 사업탐색</h1>

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

      <button @click="handleDownloadExcel" class="excel-btn">엑셀 다운로드</button>

      <div v-if="isLoading" class="loading-spinner-container">
        <div class="loading-spinner"></div>
      </div>

      <button @click="openModal" class="modal-btn">수주대상 탐색하기</button>
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
              <th>입찰공고번호</th>
              <th>최초계약일자</th>
              <th>최초계약금액</th>
              <th>계약변경차수</th>
              <th>금차완수일자</th>
              <th>총완수일자</th>
              <th>선택해제</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="isLoading">
              <td colspan="12" class="loading-text">데이터를 불러오는 중입니다...</td>
            </tr>
            <tr v-else-if="items.length === 0">
              <td colspan="12" class="no-data">데이터가 없습니다.</td>
            </tr>
            <tr v-else v-for="item in items" :key="rowKey(item)">
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
              <td>{{ item.ntceNo }}</td>
              <td>{{ item.firstCntrctDate }}</td>
              <td>{{ formatNumber(item.firstCntrctAmt) }}</td>
              <td>{{ item.cntrctCnt }}</td>
              <td>{{ item.thtmScmpltDate }}</td>
              <td>{{ item.ttalScmpltDate }}</td>
              <td>
                <button class="unselect-btn" @click="unselectItem(item)">선택 해제</button>
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

    <!-- 모달창 -->
    <div v-if="isModalOpen" class="modal-overlay">
      <div class="modal-content">
        <div class="modal-header">
          <strong>수주대상 사업 탐색</strong>
          <button @click="closeModal" class="close-btn">&times;</button>
        </div>

        <div class="notice">
          <div>
            <b>안내</b> — 이미 <b>수주대상 사업 선택하여 저장된 계약</b>은 좌측 목록에 표시되지
            않습니다.
          </div>
        </div>

        <div class="modal-body">
          <!-- 좌측: 기존 데이터 리스트 -->
          <div class="modal-pane left-pane" @scroll="handleModalScroll">
            <input
              type="text"
              v-model="modalSearch"
              placeholder="검색어 입력..."
              class="modal-search-input"
            />
            <ul class="modal-list">
              <li
                v-for="item in modalItems"
                :key="item.untyCntrctNo"
                class="modal-list-item"
                @click="selectModalItem(item)"
              >
                {{ item.firstCntrctDate }} | {{ item.cntrctNm }} | {{ item.dminsttNm }} |
                {{ formatNumber(item.firstCntrctAmt) }}
              </li>
              <li v-if="isModalLoading" class="modal-loading">로딩 중...</li>
            </ul>
          </div>

          <!-- 우측: 선택된 항목 리스트 -->
          <div class="modal-pane right-pane">
            <h4>선택된 항목</h4>
            <ul class="modal-list">
              <li
                v-for="item in selectedModalItems"
                :key="item.untyCntrctNo"
                class="modal-list-item"
              >
                {{ item.firstCntrctDate }} | {{ item.cntrctNm }} | {{ item.dminsttNm }} |
                {{ formatNumber(item.firstCntrctAmt) }}
                <button class="remove-btn" @click="removeSelectedModalItem(item)">제거</button>
              </li>
            </ul>
          </div>
        </div>

        <div class="modal-footer">
          <button @click="saveSelected" class="save-btn">선택 항목 저장</button>
        </div>
      </div>
    </div>
  </LegacySidebarLayout>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import axios from 'axios'
import LegacySidebarLayout from './components/LegacySidebarLayout.vue'

const PAGE_SIZE = 100
const CATEGORY = 'servicesSelected'
const MODAL_LIMIT = 50

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

const isModalOpen = ref(false)
const modalSearch = ref('')
const modalItems = ref([])
const selectedModalItems = ref([])
const isModalLoading = ref(false)
const modalOffset = ref(0)
const modalAllLoaded = ref(false)

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

const unselectItem = async (item) => {
  if (!confirm('정말 선택을 취소하시겠습니까?')) return
  try {
    await axios.post('/api/unselect', {
      tableName: 'daily_contracts_services',
      untyCntrctNo: item.untyCntrctNo,
      selectType: 2,
    })
    items.value = items.value.filter(
      (i) => (i.untyCntrctNo || i.id) !== (item.untyCntrctNo || item.id),
    )
  } catch (e) {
    console.error('선택 해제 실패', e)
    alert('선택 해제에 실패했습니다.')
  }
}

const openModal = () => {
  isModalOpen.value = true
  resetModal()
  fetchModalData()
}

const closeModal = () => {
  isModalOpen.value = false
}

const resetModal = () => {
  modalItems.value = []
  selectedModalItems.value = []
  modalOffset.value = 0
  modalAllLoaded.value = false
}

const fetchModalData = async () => {
  if (isModalLoading.value || modalAllLoaded.value) return
  isModalLoading.value = true
  try {
    const { data } = await axios.get('/api/modal-service-data', {
      params: {
        category: 'services',
        keyword: modalSearch.value,
        offset: modalOffset.value,
        limit: MODAL_LIMIT,
      },
    })
    const list = Array.isArray(data) ? data : []
    if (list.length < MODAL_LIMIT) modalAllLoaded.value = true
    modalItems.value = modalOffset.value === 0 ? list : [...modalItems.value, ...list]
    modalOffset.value += MODAL_LIMIT
  } catch (e) {
    console.error('모달 데이터 조회 실패', e)
    modalAllLoaded.value = true
  } finally {
    isModalLoading.value = false
  }
}

const handleModalScroll = (e) => {
  const { scrollTop, scrollHeight, clientHeight } = e.target
  if (scrollTop + clientHeight >= scrollHeight - 10) fetchModalData()
}

const selectModalItem = (item) => {
  if (selectedModalItems.value.find((i) => i.untyCntrctNo === item.untyCntrctNo)) return
  selectedModalItems.value.push(item)
}

const removeSelectedModalItem = (item) => {
  selectedModalItems.value = selectedModalItems.value.filter(
    (i) => i.untyCntrctNo !== item.untyCntrctNo,
  )
}

const saveSelected = async () => {
  if (selectedModalItems.value.length === 0) return alert('선택된 항목이 없습니다.')
  try {
    await axios.post('/api/update-is-selected', {
      tableName: 'daily_contracts_services',
      untyCntrctNos: selectedModalItems.value.map((i) => i.untyCntrctNo),
    })
    alert('선택 항목 저장 완료')
    closeModal()
    fetchData()
  } catch (e) {
    console.error('저장 실패', e)
    alert('저장 중 오류가 발생했습니다.')
  }
}

const formatNumber = (num) =>
  num != null ? (isNaN(Number(num)) ? String(num) : Number(num).toLocaleString()) : ''

const rowKey = (item) => item.untyCntrctNo || item.id || Math.random()

watch(modalSearch, () => {
  modalOffset.value = 0
  modalAllLoaded.value = false
  modalItems.value = []
  fetchModalData()
})

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

.modal-btn {
  padding: 8px 15px;
  background-color: #e84393;
  color: white;
  border: none;
  border-radius: 5px;
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

.unselect-btn {
  padding: 5px 10px;
  background-color: #95a5a6;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

/* Modal Styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.6);
  z-index: 9999;
  display: flex;
  justify-content: center;
  align-items: center;
}

.modal-content {
  width: 90vw;
  height: 80vh;
  background: #fff;
  border-radius: 10px;
  display: flex;
  flex-direction: column;
}

.modal-header {
  padding: 10px;
  background: #34495e;
  color: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top-left-radius: 10px;
  border-top-right-radius: 10px;
}

.close-btn {
  background: transparent;
  border: none;
  color: #fff;
  font-size: 20px;
  cursor: pointer;
}

.notice {
  margin: 8px 10px 0 10px;
  padding: 10px 12px;
  border-radius: 6px;
  border: 1px solid #cfe0ff;
  border-left: 4px solid #4c78ff;
  background: #f0f5ff;
  color: #1f3b7d;
  font-size: 13px;
  line-height: 1.4;
}

.modal-body {
  flex: 1;
  display: flex;
  overflow: hidden;
  padding: 10px;
  gap: 10px;
}

.modal-pane {
  flex: 1;
  display: flex;
  flex-direction: column;
  border: 1px solid #eee;
  border-radius: 5px;
  padding: 10px;
  overflow-y: auto;
}

.modal-search-input {
  width: 100%;
  padding: 8px;
  margin-bottom: 10px;
  box-sizing: border-box;
}

.modal-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.modal-list-item {
  white-space: nowrap;
  overflow-x: auto;
  border: 1px solid #ccc;
  border-radius: 5px;
  padding: 10px;
  margin-bottom: 10px;
  background-color: #f9f9f9;
  font-size: 14px;
  cursor: pointer;
}

.modal-list-item:hover {
  background-color: #f1f1f1;
}

.modal-footer {
  padding: 10px;
  text-align: right;
  border-top: 1px solid #ccc;
}

.save-btn {
  padding: 10px 20px;
  background-color: #27ae60;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
}

.remove-btn {
  margin-left: 10px;
  background-color: #e74c3c;
  color: white;
  border: none;
  padding: 5px 10px;
  border-radius: 3px;
  cursor: pointer;
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
