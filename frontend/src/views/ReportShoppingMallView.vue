<template>
  <LegacySidebarLayout>
    <h1 class="page-title">보고서 데이터 - 쇼핑몰(제3자단가)</h1>
    <p class="page-desc">
      G2B 특정품목조달 중 계약유형이 제3자단가계약인 납품 실적입니다. 기간은 납품요구결재일(ref_date) 기준입니다.
    </p>

    <div class="search-container">
      <div class="search-filter-row">
        <label class="inline-label">기간</label>
        <input v-model="filters.dateFrom" type="date" class="date-input" />
        <span class="sep">~</span>
        <input v-model="filters.dateTo" type="date" class="date-input" />
        <input type="text" v-model="filters.demandAgencyName" placeholder="수요기관명" />
        <input type="text" v-model="filters.demandAgencyRegion" placeholder="수요기관지역" />
        <input type="text" v-model="filters.itemCategoryNo" placeholder="물품분류번호" />
        <input type="text" v-model="filters.detailItemNo" placeholder="세부품명번호" />
        <input type="text" v-model="filters.vendorBizRegNo" placeholder="업체사업자번호" />
        <select v-model="filters.isMas" class="date-select">
          <option value="">MAS (전체)</option>
          <option value="Y">Y</option>
          <option value="N">N</option>
        </select>
        <select v-model="filters.isExcellentProduct" class="date-select">
          <option value="">우수제품 (전체)</option>
          <option value="Y">Y</option>
          <option value="N">N</option>
        </select>
        <button type="button" class="search-btn" @click="handleSearch">검색</button>
        <button
          type="button"
          class="excel-btn"
          :disabled="isLoadingExcel"
          @click="handleDownloadExcel"
        >
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
            데이터가 많을 경우 시간이 걸릴 수 있습니다. 조건을 좁히면 더 빠릅니다.
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
              <th>납품요구결재일</th>
              <th>수요기관명</th>
              <th>수요기관지역</th>
              <th>계약명(요청명)</th>
              <th>물품분류명</th>
              <th>세부품명</th>
              <th>물품식별명</th>
              <th>단가</th>
              <th>수량</th>
              <th>공급금액</th>
              <th>MAS</th>
              <th>우수제품</th>
              <th>직접구매</th>
              <th>업체명</th>
              <th>납품장소명</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="isLoading">
              <td colspan="15" class="loading-text">데이터를 불러오는 중입니다...</td>
            </tr>
            <tr v-else-if="items.length === 0">
              <td colspan="15" class="no-data">데이터가 없습니다.</td>
            </tr>
            <tr v-else v-for="(row, idx) in items" :key="rowKey(row, idx)">
              <td>{{ formatDate(row.refDate) }}</td>
              <td>{{ row.demandAgencyName }}</td>
              <td>{{ row.demandAgencyRegion }}</td>
              <td>{{ row.contractTitle }}</td>
              <td>{{ row.itemCategoryName }}</td>
              <td>{{ row.detailItemName }}</td>
              <td>{{ row.itemIdentifierName }}</td>
              <td>{{ formatNumber(row.unitPrice) }}</td>
              <td>{{ formatNumber(row.quantity) }}</td>
              <td>{{ formatNumber(row.supplyAmount) }}</td>
              <td>{{ row.isMas }}</td>
              <td>{{ row.isExcellentProduct }}</td>
              <td>{{ row.isDirectPurchaseTarget }}</td>
              <td>{{ row.vendorName }}</td>
              <td>{{ row.deliveryPlaceName }}</td>
            </tr>
          </tbody>
        </table>
      </div>
      <div v-if="totalCount > 0" class="pagination">
        <span class="pagination-info">
          {{ formatNumber(startDisplay) }}-{{ formatNumber(endDisplay) }} / {{ formatNumber(totalCount) }}건
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
        <button class="page-btn" :disabled="currentPage >= totalPages" @click="goPage(currentPage + 1)">
          다음
        </button>
      </div>
    </div>
  </LegacySidebarLayout>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import axios from 'axios'
import LegacySidebarLayout from './components/LegacySidebarLayout.vue'

const API_BASE = '/api/report/shopping-mall'
const PAGE_SIZE = 50

const isLoading = ref(false)
const isLoadingExcel = ref(false)
const items = ref([])
const totalCount = ref(0)
const currentPage = ref(1)

const filters = reactive({
  dateFrom: '',
  dateTo: '',
  demandAgencyName: '',
  demandAgencyRegion: '',
  itemCategoryNo: '',
  detailItemNo: '',
  vendorBizRegNo: '',
  isMas: '',
  isExcellentProduct: '',
})

function rowKey(row, idx) {
  return `${row.deliveryContractNo ?? ''}_${row.deliveryContractChangeSeq ?? ''}_${row.deliveryItemSeq ?? ''}_${idx}`
}

function buildParams() {
  const p = {
    dateFrom: filters.dateFrom || undefined,
    dateTo: filters.dateTo || undefined,
    demandAgencyName: filters.demandAgencyName || undefined,
    demandAgencyRegion: filters.demandAgencyRegion || undefined,
    itemCategoryNo: filters.itemCategoryNo || undefined,
    detailItemNo: filters.detailItemNo || undefined,
    vendorBizRegNo: filters.vendorBizRegNo || undefined,
    isMas: filters.isMas || undefined,
    isExcellentProduct: filters.isExcellentProduct || undefined,
    page: currentPage.value,
    size: PAGE_SIZE,
  }
  return p
}

function buildExcelQueryParams() {
  const params = new URLSearchParams()
  const q = {
    dateFrom: filters.dateFrom || undefined,
    dateTo: filters.dateTo || undefined,
    demandAgencyName: filters.demandAgencyName || undefined,
    demandAgencyRegion: filters.demandAgencyRegion || undefined,
    itemCategoryNo: filters.itemCategoryNo || undefined,
    detailItemNo: filters.detailItemNo || undefined,
    vendorBizRegNo: filters.vendorBizRegNo || undefined,
    isMas: filters.isMas || undefined,
    isExcellentProduct: filters.isExcellentProduct || undefined,
  }
  Object.entries(q).forEach(([k, v]) => {
    if (v !== undefined && v !== '') params.append(k, v)
  })
  return params
}

const totalPages = computed(() => Math.max(1, Math.ceil(totalCount.value / PAGE_SIZE)))
const startDisplay = computed(() =>
  totalCount.value === 0 ? 0 : (currentPage.value - 1) * PAGE_SIZE + 1,
)
const endDisplay = computed(() => Math.min(currentPage.value * PAGE_SIZE, totalCount.value))
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
    const { data } = await axios.get(API_BASE, { params: buildParams() })
    items.value = Array.isArray(data.content) ? data.content : []
    totalCount.value = typeof data.totalCount === 'number' ? data.totalCount : items.value.length
  } catch (e) {
    console.error('쇼핑몰 납품 실적 조회 실패', e)
    items.value = []
    totalCount.value = 0
  } finally {
    isLoading.value = false
  }
}

const goPage = (page) => {
  const total = Math.max(1, Math.ceil(totalCount.value / PAGE_SIZE))
  if (page < 1 || page > total) return
  currentPage.value = page
  fetchData()
}

const handleSearch = () => fetchData(true)

const handleDownloadExcel = async () => {
  isLoadingExcel.value = true
  try {
    const qs = buildExcelQueryParams().toString()
    const url = qs ? `${API_BASE}/excel?${qs}` : `${API_BASE}/excel`
    const { data } = await axios.get(url, {
      responseType: 'blob',
      timeout: 1800000,
    })
    const blobUrl = URL.createObjectURL(new Blob([data]))
    const a = document.createElement('a')
    a.href = blobUrl
    a.download = `report_shopping_mall_${Date.now()}.xlsx`
    a.click()
    URL.revokeObjectURL(blobUrl)
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

const formatNumber = (num) => {
  if (num == null || num === '') return ''
  const n = typeof num === 'number' ? num : Number(num)
  return isNaN(n) ? String(num) : n.toLocaleString()
}

function formatDate(v) {
  if (v == null || v === '') return ''
  if (typeof v === 'string' && /^\d{4}-\d{2}-\d{2}/.test(v)) return v.slice(0, 10)
  return String(v)
}

onMounted(() => fetchData())
</script>

<style scoped>
.page-title {
  color: #ecf0f1;
  margin-bottom: 8px;
}
.page-desc {
  font-size: 0.9em;
  color: #94a3b8;
  margin-bottom: 20px;
  max-width: 720px;
  line-height: 1.5;
}
.search-container {
  margin-bottom: 24px;
  padding: 18px 20px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
}
.search-filter-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px 12px;
  justify-content: flex-end;
}
.inline-label {
  font-size: 0.875em;
  color: #475569;
  margin-right: 4px;
}
.sep {
  color: #64748b;
}
.date-input {
  padding: 8px 10px;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  font-size: 0.9em;
}
input[type='text'],
select {
  padding: 10px 12px;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  font-size: 0.9375em;
  min-width: 120px;
}
.search-btn {
  padding: 10px 18px;
  background: linear-gradient(180deg, #3d5a73 0%, #34495e 100%);
  color: #f0f4f8;
  border: none;
  cursor: pointer;
  border-radius: 8px;
  font-weight: 500;
}

.excel-btn {
  padding: 10px 18px;
  background: linear-gradient(180deg, #1e8449 0%, #27ae60 100%);
  color: #fff;
  border: none;
  cursor: pointer;
  border-radius: 8px;
  font-weight: 500;
}

.excel-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.excel-download-overlay {
  position: fixed;
  inset: 0;
  z-index: 10000;
  background: rgba(15, 23, 42, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}

.excel-download-progress {
  background: #fff;
  border-radius: 16px;
  padding: 28px 32px;
  max-width: 400px;
  text-align: center;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.15);
}

.excel-spinner {
  width: 40px;
  height: 40px;
  margin: 0 auto 16px;
  border: 4px solid #e2e8f0;
  border-top-color: #27ae60;
  border-radius: 50%;
  animation: spin 0.9s linear infinite;
}

.excel-progress-title {
  margin: 0 0 8px;
  font-size: 1.1rem;
  font-weight: 600;
  color: #0f172a;
}

.excel-progress-desc,
.excel-progress-hint {
  margin: 0;
  font-size: 0.875rem;
  color: #64748b;
  line-height: 1.5;
}

.excel-progress-hint {
  margin-top: 12px;
  font-weight: 500;
  color: #334155;
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
  to {
    transform: rotate(360deg);
  }
}
.table-container {
  overflow-x: auto;
}
.table-wrapper {
  max-height: 70vh;
  overflow: auto;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
}
.data-table {
  width: 100%;
  border-collapse: collapse;
}
.data-table th,
.data-table td {
  padding: 10px 8px;
  border: 1px solid #e2e8f0;
  text-align: center;
  font-size: 0.85em;
}
.data-table th {
  background: #f1f5f9;
  font-weight: 600;
  color: #334155;
  position: sticky;
  top: 0;
  z-index: 1;
}
.data-table th,
.data-table td {
  white-space: nowrap;
}
.loading-text,
.no-data {
  text-align: center;
  padding: 24px;
  color: #64748b;
}
.pagination {
  margin-top: 16px;
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
.page-btn,
.page-num-btn {
  padding: 8px 14px;
  border: 1px solid #e2e8f0;
  background: #fff;
  cursor: pointer;
  border-radius: 8px;
  font-size: 0.9em;
}
.page-num-btn.active {
  background: linear-gradient(180deg, #3d5a73 0%, #34495e 100%);
  color: #fff;
  border-color: #34495e;
}
.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
