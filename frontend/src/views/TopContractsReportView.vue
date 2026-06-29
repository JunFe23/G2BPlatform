<template>
  <LegacySidebarLayout>
    <h1 class="top-title">탑 수주 현황</h1>

    <!-- 검색 필드 -->
    <div class="search-container">
      <div class="search-filter-row">
        <select v-model="filters.type" class="date-select">
          <option value="">분류 (전체)</option>
          <option value="물품">물품</option>
          <option value="쇼핑몰">쇼핑몰</option>
          <option value="공사">공사</option>
          <option value="용역">용역</option>
        </select>
        <select v-model="filters.dataOrigin" class="date-select">
          <option value="">데이터구분 (전체)</option>
          <option value="관급">관급</option>
          <option value="민수">민수</option>
        </select>
        <input type="text" v-model="filters.dminsttNm" placeholder="수요기관명 검색" />
        <input type="text" v-model="filters.dminsttNmDetail" placeholder="수요기관지역명 검색" />
        <input type="text" v-model="filters.contractName" placeholder="계약명 검색" />
        <select v-model="filters.cntctCnclsMthdNm" class="date-select">
          <option value="">입찰계약방법 (전체)</option>
          <option v-for="m in contractMethodOptions" :key="m" :value="m">{{ m }}</option>
        </select>
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

      <div class="search-filter-row search-category-row">
        <span class="category-row-label">공공조달분류</span>
        <CategoryTreeSelect
          v-model="filters.categoryNames"
          :category-map="categoryMap"
          placeholder="공공조달분류 선택"
        />
      </div>

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
            :aria-checked="grouped"
            class="long-term-toggle-switch"
            :class="{ on: grouped }"
            @click="onGroupedToggleClick"
          >
            <span class="long-term-toggle-slider"></span>
          </button>
          <span class="long-term-toggle-caption">{{ grouped ? '합쳐서 보기' : '풀어서 보기' }}</span>
        </div>
        <button @click="handleSearch" class="search-btn">검색</button>
        <button @click="handleDownloadExcel" class="excel-btn" :disabled="isLoadingExcel">
          {{ isLoadingExcel ? '다운로드 중...' : '엑셀 다운로드' }}
        </button>
        <div v-if="isLoading" class="loading-spinner-container"><div class="loading-spinner"></div></div>
      </div>
    </div>

    <!-- 민수 데이터 관리 (관리자 전용) -->
    <div v-if="isAdmin" class="manual-admin">
      <div class="manual-admin-head">
        <span class="manual-admin-title">민수 데이터 입력 · 관리</span>
        <button type="button" class="manual-toggle-btn" @click="toggleManualPanel">
          {{ showManualPanel ? '닫기' : '열기' }}
        </button>
      </div>

      <div v-if="showManualPanel" class="manual-admin-body">
        <!-- 입력/수정 폼 -->
        <div class="manual-form">
          <div class="manual-form-grid">
            <label>분류
              <select v-model="manualForm.type">
                <option value="물품">물품</option>
                <option value="쇼핑몰">쇼핑몰</option>
                <option value="공사">공사</option>
                <option value="용역">용역</option>
              </select>
            </label>
            <label>업체
              <select v-model="manualForm.vendorBizRegNo" @change="onVendorChange">
                <option v-for="v in vendorOptions" :key="v.no" :value="v.no">{{ v.name }}</option>
              </select>
            </label>
            <label>업체명<input type="text" v-model="manualForm.vendorName" /></label>
            <label>계약건명<input type="text" v-model="manualForm.contractName" /></label>
            <label>수요기관명<input type="text" v-model="manualForm.demandAgencyName" /></label>
            <label>수요기관지역명<input type="text" v-model="manualForm.demandAgencyRegion" /></label>
            <label>중분류<input type="text" v-model="manualForm.midCategory" /></label>
            <label>품명내용<input type="text" v-model="manualForm.productClassification" /></label>
            <label>입찰계약방법<input type="text" v-model="manualForm.contractMethod" /></label>
            <label>입찰공고번호<input type="text" v-model="manualForm.bidNoticeNo" /></label>
            <label>최초계약일자<input type="date" v-model="manualForm.firstContractDate" /></label>
            <label>최초계약금액<input type="number" v-model="manualForm.firstContractAmount" /></label>
            <label>최종계약일자<input type="date" v-model="manualForm.lastContractDate" /></label>
            <label>최종계약금액<input type="number" v-model="manualForm.lastContractAmount" /></label>
            <label>계약변경차수<input type="number" v-model="manualForm.contractChangeSeq" /></label>
          </div>
          <div class="manual-form-actions">
            <button type="button" class="search-btn" @click="saveManual" :disabled="isSavingManual">
              {{ manualForm.id ? '수정 저장' : '추가' }}
            </button>
            <button v-if="manualForm.id" type="button" class="manual-cancel-btn" @click="resetManualForm">취소</button>
          </div>
        </div>

        <!-- 기존 민수 목록 -->
        <div class="manual-list-wrap">
          <table class="data-table manual-list-table">
            <thead>
              <tr>
                <th>분류</th><th>업체명</th><th>계약건명</th><th>수요기관명</th>
                <th>품명내용</th><th>최초계약일자</th><th>최종계약금액</th><th>관리</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="manualList.length === 0"><td colspan="8" class="no-data">민수 데이터가 없습니다.</td></tr>
              <tr v-for="row in manualList" :key="row.id">
                <td>{{ row.type }}</td>
                <td>{{ row.vendorName }}</td>
                <td>{{ row.contractName }}</td>
                <td>{{ row.demandAgencyName }}</td>
                <td>{{ row.productClassification }}</td>
                <td>{{ row.firstContractDate }}</td>
                <td>{{ formatNumber(row.lastContractAmount) }}</td>
                <td class="manual-row-actions">
                  <button type="button" @click="editManual(row)">수정</button>
                  <button type="button" class="manual-del-btn" @click="removeManual(row)">삭제</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- 상단 합계 -->
    <div v-if="totals" class="grouped-totals">
      <span class="total-item">최초계약금액 합계 <strong>{{ formatNumber(totals.firstAmountTotal) }}원</strong></span>
      <span class="total-sep">|</span>
      <span class="total-item">최종계약금액 합계 <strong>{{ formatNumber(totals.finalAmountTotal) }}원</strong></span>
    </div>

    <div class="table-container">
      <div class="table-wrapper" ref="tableRef">
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
              <th>{{ grouped ? '최초계약금액(합계)' : '최초계약금액' }}</th>
              <th>최종계약일자</th>
              <th>{{ grouped ? '최종계약금액(합계)' : '최종계약금액' }}</th>
              <th>{{ grouped ? '계약건수' : '계약변경차수' }}</th>
              <th>저장</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="isLoading"><td colspan="14" class="loading-text">데이터를 불러오는 중입니다...</td></tr>
            <tr v-else-if="items.length === 0"><td colspan="14" class="no-data">데이터가 없습니다.</td></tr>
            <tr v-else v-for="item in items" :key="rowKey(item)">
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
              <td><input type="checkbox" :checked="item.saved === 'Y'" @change="toggleSave(item)" /></td>
            </tr>
          </tbody>
        </table>
      </div>
      <div v-if="recordsFiltered > 0" class="pagination">
        <span class="pagination-info">
          {{ formatNumber(startDisplay) }}-{{ formatNumber(endDisplay) }} / {{ formatNumber(recordsFiltered) }}건
        </span>
        <button class="page-btn" :disabled="currentPage <= 1" @click="goPage(currentPage - 1)">이전</button>
        <button v-for="p in pageNumbers" :key="p" class="page-num-btn" :class="{ active: p === currentPage }" @click="goPage(p)">{{ p }}</button>
        <button class="page-btn" :disabled="currentPage >= totalPages" @click="goPage(currentPage + 1)">다음</button>
      </div>
    </div>
  </LegacySidebarLayout>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch, nextTick } from 'vue'
import axios from 'axios'
import { useAuthStore } from '@/stores/auth'
import LegacySidebarLayout from './components/LegacySidebarLayout.vue'
import CategoryTreeSelect from './components/CategoryTreeSelect.vue'

const API_BASE = '/api/report/top-companies'
const PAGE_SIZE = 100

const authStore = useAuthStore()
const isAdmin = computed(() => authStore.isAdmin)

const vendorOptions = [
  { no: '1188117437', name: '탑인더스트리' },
  { no: '1188119624', name: '탑정보통신' },
]

const isLoading = ref(false)
const isLoadingExcel = ref(false)
const items = ref([])
const recordsFiltered = ref(0)
const currentPage = ref(1)
const grouped = ref(false)
const totals = ref(null)
const tableRef = ref(null)
const contractMethodOptions = ref([])
const categoryMap = ref({})

const currentYear = new Date().getFullYear()
const years = Array.from({ length: currentYear - 2014 }, (_, i) => currentYear - i)

const filters = reactive({
  type: '',
  dataOrigin: '',
  dminsttNm: '',
  dminsttNmDetail: '',
  contractName: '',
  categoryNames: [],
  cntctCnclsMthdNm: '',
  firstCntrctDate: '',
  dateType: 'year',
  year: '',
  month: '',
  rangeStart: '',
  rangeEnd: '',
  showSavedOnly: false,
})

let applied = { ...filters, categoryNames: [] }

const totalPages = computed(() => Math.ceil(recordsFiltered.value / PAGE_SIZE) || 1)
const startDisplay = computed(() => (recordsFiltered.value === 0 ? 0 : (currentPage.value - 1) * PAGE_SIZE + 1))
const endDisplay = computed(() => Math.min(currentPage.value * PAGE_SIZE, recordsFiltered.value))
const pageNumbers = computed(() => {
  const total = totalPages.value
  const cur = currentPage.value
  const pages = []
  for (let i = Math.max(1, cur - 2); i <= Math.min(total, cur + 2); i++) pages.push(i)
  return pages
})

function rowKey(item) {
  return [item.type, item.pk1, item.pk2, item.pk3].join('_')
}

function buildParams(page) {
  const f = applied
  const p = {
    type: f.type || '',
    dataOrigin: f.dataOrigin || undefined,
    grouped: grouped.value,
    dminsttNm: f.dminsttNm || undefined,
    dminsttNmDetail: f.dminsttNmDetail || undefined,
    contractName: f.contractName || undefined,
    categoryNames: f.categoryNames && f.categoryNames.length ? f.categoryNames.join(',') : undefined,
    cntctCnclsMthdNm: f.cntctCnclsMthdNm || undefined,
    firstCntrctDate: f.firstCntrctDate || undefined,
    showSavedOnly: f.showSavedOnly,
  }
  if (page != null) {
    p.start = (page - 1) * PAGE_SIZE
    p.length = PAGE_SIZE
  }
  if (f.dateType === 'year' && f.year) p.year = f.year
  if (f.dateType === 'month' && f.month) p.month = f.month
  if (f.dateType === 'range' && f.rangeStart && f.rangeEnd) {
    p.rangeStart = f.rangeStart
    p.rangeEnd = f.rangeEnd
  }
  return p
}

async function fetchPage(page) {
  isLoading.value = true
  try {
    const res = await axios.get(API_BASE, { params: buildParams(page) })
    items.value = res.data.data || []
    recordsFiltered.value = res.data.recordsFiltered || 0
    currentPage.value = page
    if (res.data.totals != null) totals.value = res.data.totals
  } catch (e) {
    console.error('탑 수주현황 조회 오류', e)
    items.value = []
    recordsFiltered.value = 0
    totals.value = null
  } finally {
    isLoading.value = false
  }
}

function handleSearch() {
  applied = { ...filters, categoryNames: [...filters.categoryNames] }
  fetchPage(1)
}

function onGroupedToggleClick() {
  grouped.value = !grouped.value
  handleSearch()
}

async function handleDownloadExcel() {
  isLoadingExcel.value = true
  try {
    const p = buildParams(null)
    const res = await axios.get(API_BASE + '/excel', { params: p, responseType: 'blob', timeout: 1800000 })
    const url = window.URL.createObjectURL(new Blob([res.data]))
    const link = document.createElement('a')
    link.href = url
    link.download = `탑수주현황_${Date.now()}.xlsx`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
  } catch (e) {
    console.error('엑셀 다운로드 오류', e)
    alert('엑셀 다운로드에 실패했습니다.')
  } finally {
    isLoadingExcel.value = false
  }
}

async function toggleSave(item) {
  const nextSaved = item.saved === 'Y' ? 'N' : 'Y'
  try {
    if (item.type === '물품' || item.type === '쇼핑몰') {
      const body = { grouped: grouped.value, saved: nextSaved }
      if (grouped.value) {
        body.dataType = item.type === '쇼핑몰' ? 'shopping_mall' : 'general'
        body.groupKey = item.pk1
        body.vendorBizRegNo = item.pk2
      } else {
        body.contractNo = item.pk1
        body.itemSeq = item.pk2
        body.changeSeq = item.pk3
      }
      await axios.patch('/api/specific-item/saved', body)
    } else {
      const contractType = item.type === '공사' ? 'construction' : 'service'
      const body = { contractType, grouped: grouped.value, saved: nextSaved }
      if (grouped.value) {
        body.groupKey = item.pk1
        body.vendorBizRegNo = item.pk2
      } else {
        body.contractNo = item.pk1
      }
      await axios.patch('/api/report/market-contracts/saved', body)
    }
    item.saved = nextSaved
  } catch (e) {
    console.error('저장 상태 변경 오류', e)
    alert('저장 상태 변경에 실패했습니다.')
  }
}

function goPage(page) {
  if (page < 1 || page > totalPages.value) return
  fetchPage(page)
}

function formatNumber(val) {
  if (val === null || val === undefined || val === '') return ''
  const num = Number(val)
  return isNaN(num) ? val : num.toLocaleString('ko-KR')
}

// 표 셀 말줄임(...) 시 hover로 전체값 — 다른 페이지와 동일
function refreshCellTitles() {
  const root = tableRef.value
  if (!root) return
  root.querySelectorAll('tbody td').forEach((td) => {
    td.title = td.scrollWidth > td.clientWidth ? td.textContent.trim() : ''
  })
}
watch(items, () => nextTick(refreshCellTitles))

async function loadFilterOptions() {
  try {
    const { data } = await axios.get(API_BASE + '/filter-options')
    contractMethodOptions.value = Array.isArray(data.contractMethods) ? data.contractMethods : []
    const map = {}
    if (Array.isArray(data.categories)) {
      for (const row of data.categories) {
        if (!row.mid || !row.name) continue
        if (!map[row.mid]) map[row.mid] = []
        if (!map[row.mid].includes(row.name)) map[row.mid].push(row.name)
      }
    }
    categoryMap.value = map
  } catch (e) {
    console.error('필터 옵션 조회 실패', e)
    contractMethodOptions.value = []
    categoryMap.value = {}
  }
}

// ===== 민수(직접입력) 데이터 관리 (관리자) =====
const MANUAL_API = API_BASE + '/manual'
const showManualPanel = ref(false)
const isSavingManual = ref(false)
const manualList = ref([])

function emptyManualForm() {
  return {
    id: null,
    type: '물품',
    vendorBizRegNo: vendorOptions[0].no,
    vendorName: vendorOptions[0].name,
    contractName: '',
    demandAgencyName: '',
    demandAgencyRegion: '',
    midCategory: '',
    productClassification: '',
    contractMethod: '',
    bidNoticeNo: '',
    firstContractDate: '',
    firstContractAmount: '',
    lastContractDate: '',
    lastContractAmount: '',
    contractChangeSeq: 0,
  }
}
const manualForm = reactive(emptyManualForm())

function resetManualForm() {
  Object.assign(manualForm, emptyManualForm())
}

function onVendorChange() {
  const v = vendorOptions.find((o) => o.no === manualForm.vendorBizRegNo)
  if (v && !manualForm.vendorName) manualForm.vendorName = v.name
}

async function toggleManualPanel() {
  showManualPanel.value = !showManualPanel.value
  if (showManualPanel.value) await loadManualList()
}

async function loadManualList() {
  try {
    const { data } = await axios.get(MANUAL_API)
    manualList.value = Array.isArray(data.data) ? data.data : []
  } catch (e) {
    console.error('민수 목록 조회 실패', e)
    manualList.value = []
  }
}

function toPayload() {
  return {
    type: manualForm.type,
    vendorBizRegNo: manualForm.vendorBizRegNo,
    vendorName: manualForm.vendorName || null,
    contractName: manualForm.contractName || null,
    demandAgencyName: manualForm.demandAgencyName || null,
    demandAgencyRegion: manualForm.demandAgencyRegion || null,
    midCategory: manualForm.midCategory || null,
    productClassification: manualForm.productClassification || null,
    contractMethod: manualForm.contractMethod || null,
    bidNoticeNo: manualForm.bidNoticeNo || null,
    firstContractDate: manualForm.firstContractDate || null,
    firstContractAmount: manualForm.firstContractAmount === '' ? null : Number(manualForm.firstContractAmount),
    lastContractDate: manualForm.lastContractDate || null,
    lastContractAmount: manualForm.lastContractAmount === '' ? null : Number(manualForm.lastContractAmount),
    contractChangeSeq: manualForm.contractChangeSeq === '' ? 0 : Number(manualForm.contractChangeSeq),
  }
}

async function saveManual() {
  isSavingManual.value = true
  try {
    if (manualForm.id) {
      await axios.put(`${MANUAL_API}/${manualForm.id}`, toPayload())
    } else {
      await axios.post(MANUAL_API, toPayload())
    }
    resetManualForm()
    await loadManualList()
    handleSearch()
  } catch (e) {
    console.error('민수 저장 실패', e)
    alert('민수 데이터 저장에 실패했습니다.')
  } finally {
    isSavingManual.value = false
  }
}

function editManual(row) {
  Object.assign(manualForm, {
    id: row.id,
    type: row.type,
    vendorBizRegNo: row.vendorBizRegNo,
    vendorName: row.vendorName || '',
    contractName: row.contractName || '',
    demandAgencyName: row.demandAgencyName || '',
    demandAgencyRegion: row.demandAgencyRegion || '',
    midCategory: row.midCategory || '',
    productClassification: row.productClassification || '',
    contractMethod: row.contractMethod || '',
    bidNoticeNo: row.bidNoticeNo || '',
    firstContractDate: row.firstContractDate || '',
    firstContractAmount: row.firstContractAmount ?? '',
    lastContractDate: row.lastContractDate || '',
    lastContractAmount: row.lastContractAmount ?? '',
    contractChangeSeq: row.contractChangeSeq ?? 0,
  })
}

async function removeManual(row) {
  if (!confirm('이 민수 데이터를 삭제하시겠습니까?')) return
  try {
    await axios.delete(`${MANUAL_API}/${row.id}`)
    if (manualForm.id === row.id) resetManualForm()
    await loadManualList()
    handleSearch()
  } catch (e) {
    console.error('민수 삭제 실패', e)
    alert('민수 데이터 삭제에 실패했습니다.')
  }
}

onMounted(() => {
  handleSearch()
  loadFilterOptions()
})
</script>

<style scoped>
.top-title {
  color: #ecf0f1;
}
.search-container {
  margin-bottom: 16px;
  padding: 18px 20px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
}
.search-filter-row {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
  gap: 10px 12px;
}
.search-filter-row input[type='text'],
.search-filter-row input[type='month'],
.date-select {
  padding: 8px 10px;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  font-size: 14px;
  background: #fff;
  color: #2c3e50;
  min-width: 120px;
  box-sizing: border-box;
}
.search-category-row {
  padding-top: 8px;
  margin-top: 8px;
  border-top: 1px dashed #e2e8f0;
}
.category-row-label {
  font-size: 0.875em;
  font-weight: 500;
  color: #475569;
  align-self: center;
}
.search-actions-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
  margin-top: 10px;
}
.actions-sep {
  flex: 1;
}
.checkbox-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #475569;
  cursor: pointer;
}
.long-term-toggle-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
}
.long-term-toggle-label,
.long-term-toggle-caption {
  font-size: 13px;
  color: #475569;
}
.long-term-toggle-switch {
  width: 44px;
  height: 24px;
  border-radius: 12px;
  border: none;
  background: #cbd5e1;
  position: relative;
  cursor: pointer;
  transition: background 0.2s;
}
.long-term-toggle-switch.on {
  background: #3d5a73;
}
.long-term-toggle-slider {
  position: absolute;
  top: 2px;
  left: 2px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: #fff;
  transition: left 0.2s;
}
.long-term-toggle-switch.on .long-term-toggle-slider {
  left: 22px;
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
.excel-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
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
.grouped-totals {
  display: flex;
  align-items: center;
  gap: 14px;
  margin: 8px 0 4px;
  padding: 10px 14px;
  background: #eef2f7;
  border: 1px solid #d6dee8;
  border-radius: 8px;
  font-size: 0.92em;
  color: #334155;
}
.grouped-totals strong { color: #1d4ed8; margin-left: 4px; }
.grouped-totals .total-sep { color: #94a3b8; }
.table-container {
  margin-top: 4px;
}
.table-wrapper {
  max-height: 70vh;
  overflow: scroll;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  scrollbar-width: auto;
}
.table-wrapper::-webkit-scrollbar {
  -webkit-appearance: none;
  width: 12px;
  height: 12px;
}
.table-wrapper::-webkit-scrollbar-thumb {
  background: #b0b8c4;
  border-radius: 6px;
  border: 2px solid #f1f5f9;
}
.table-wrapper::-webkit-scrollbar-track {
  background: #f1f5f9;
}
.data-table {
  width: 100%;
  border-collapse: collapse;
}
.data-table th,
.data-table td {
  padding: 6px 8px;
  border: 1px solid #e2e8f0;
  text-align: center;
  font-size: 0.82em;
}
.data-table td {
  max-width: 180px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.data-table th {
  background: #f1f5f9;
  font-weight: 600;
  color: #334155;
  white-space: nowrap;
  position: sticky;
  top: 0;
  z-index: 1;
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

/* 민수 데이터 관리 (관리자) */
.manual-admin {
  margin-bottom: 14px;
  border: 1px solid #d6dee8;
  border-radius: 10px;
  background: #fff;
  overflow: hidden;
}
.manual-admin-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 14px;
  background: #eef2f7;
}
.manual-admin-title {
  font-weight: 600;
  color: #334155;
  font-size: 0.92em;
}
.manual-toggle-btn {
  padding: 5px 14px;
  border: 1px solid #cbd5e1;
  border-radius: 7px;
  background: #fff;
  color: #475569;
  font-size: 13px;
  cursor: pointer;
}
.manual-admin-body {
  padding: 14px;
}
.manual-form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 10px 12px;
}
.manual-form-grid label {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 12px;
  color: #475569;
}
.manual-form-grid input,
.manual-form-grid select {
  padding: 7px 9px;
  border: 1px solid #cbd5e1;
  border-radius: 7px;
  font-size: 13px;
  background: #fff;
  color: #2c3e50;
  box-sizing: border-box;
}
.manual-form-actions {
  display: flex;
  gap: 10px;
  margin-top: 12px;
}
.manual-cancel-btn {
  padding: 8px 16px;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  background: #fff;
  color: #475569;
  font-size: 14px;
  cursor: pointer;
}
.manual-list-wrap {
  margin-top: 16px;
  max-height: 320px;
  overflow: auto;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
}
.manual-list-table {
  margin: 0;
}
.manual-row-actions {
  white-space: nowrap;
}
.manual-row-actions button {
  padding: 4px 10px;
  margin: 0 2px;
  border: 1px solid #cbd5e1;
  border-radius: 6px;
  background: #fff;
  color: #475569;
  font-size: 12px;
  cursor: pointer;
}
.manual-del-btn {
  color: #dc2626 !important;
  border-color: #fca5a5 !important;
}
</style>
