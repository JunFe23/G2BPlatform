<template>
  <LegacySidebarLayout>
    <template v-if="isReportMode">
      <h1 class="servicesSelected">사업탐색(with 설계&amp;감리)</h1>

      <div class="mock-banner">
        <strong>화면 초안</strong>
        <span>실데이터 연동 전 확인용 목업입니다. 흐름 확정 후 API와 저장 구조를 연결합니다.</span>
      </div>

      <div class="search-container report-search">
        <div class="search-filter-row">
          <input type="text" v-model="reportFilters.dminsttNm" placeholder="수요기관명 검색" />
          <input type="text" v-model="reportFilters.dminsttNmDetail" placeholder="수요기관지역명 검색" />
          <input type="text" v-model="reportFilters.contractName" placeholder="계약명 검색" />
          <select v-model="reportFilters.categoryName" class="date-select">
            <option value="">공공조달분류명 (전체)</option>
            <option value="토목설계용역">토목설계용역</option>
            <option value="상하수도설계용역">상하수도설계용역</option>
            <option value="건축설계용역">건축설계용역</option>
            <option value="토목감리용역">토목감리용역</option>
            <option value="건축감리용역">건축감리용역</option>
          </select>
          <select v-model="reportFilters.cntctCnclsMthdNm" class="date-select">
            <option value="">입찰계약방법 (전체)</option>
            <option value="제한경쟁">제한경쟁</option>
            <option value="일반경쟁">일반경쟁</option>
            <option value="수의계약">수의계약</option>
          </select>
          <input type="text" v-model="reportFilters.firstCntrctDate" placeholder="최초계약일자(YYYY-MM-DD)" />
        </div>

        <div class="search-filter-row search-date-row">
          <select v-model="reportFilters.dateBasis" class="date-select">
            <option value="first">최초계약일자 기준</option>
            <option value="completion">완수일자 기준</option>
          </select>
          <select v-model="reportFilters.dateType" class="date-select">
            <option value="year">연도 검색</option>
            <option value="month">특정 월 검색</option>
            <option value="range">기간 검색</option>
          </select>
          <select v-if="reportFilters.dateType === 'year'" v-model="reportFilters.year" class="date-select">
            <option value="">선택</option>
            <option v-for="y in years" :key="y" :value="y">{{ y }}</option>
          </select>
          <input v-if="reportFilters.dateType === 'month'" type="month" v-model="reportFilters.month" />
          <template v-if="reportFilters.dateType === 'range'">
            <input type="month" v-model="reportFilters.rangeStart" placeholder="시작월" />
            <input type="month" v-model="reportFilters.rangeEnd" placeholder="종료월" />
          </template>
        </div>

        <div class="search-filter-row keyword-row">
          <input type="text" v-model="reportFilters.includeText" placeholder="포함 텍스트 예: 하수도, 상수도" />
          <input type="text" v-model="reportFilters.excludeText" placeholder="제외 텍스트 예: 송전탑" />
        </div>

        <div class="search-actions-row">
          <button @click="handleReportSearch" class="search-btn">검색</button>
          <button @click="handleReportExcel" class="excel-btn">엑셀 다운로드</button>
          <button @click="openReportModal" class="modal-btn">수주대상 확정하기</button>
        </div>
      </div>

      <div class="table-container">
        <div class="table-wrapper">
          <table class="data-table report-table">
            <thead>
              <tr>
                <th>업체명</th>
                <th>계약건명</th>
                <th>수요기관명</th>
                <th>입찰공고번호</th>
                <th>최초계약일자</th>
                <th>최초계약금액</th>
                <th>완수일자</th>
                <th>영업대상(O/X)</th>
                <th>선택해제</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="filteredReportItems.length === 0">
                <td colspan="9" class="no-data">데이터가 없습니다.</td>
              </tr>
              <tr v-else v-for="item in filteredReportItems" :key="item.contractNo">
                <td>{{ item.vendorName }}</td>
                <td>{{ item.contractName }}</td>
                <td>{{ item.demandAgencyName }}</td>
                <td>{{ item.bidNoticeNo }}</td>
                <td>{{ item.firstContractDate }}</td>
                <td>{{ formatNumber(item.firstContractAmount) }}</td>
                <td>{{ item.completionDate }}</td>
                <td>
                  <span class="target-badge" :class="{ off: item.businessTarget !== 'O' }">
                    {{ item.businessTarget }}
                  </span>
                </td>
                <td>
                  <button class="unselect-btn" @click="mockUnselect(item)">선택 해제</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <div class="pagination mock-pagination">
          <span class="pagination-info">
            1-{{ filteredReportItems.length }} / {{ filteredReportItems.length }}건
          </span>
        </div>
      </div>

      <div v-if="isReportModalOpen" class="modal-overlay">
        <div class="modal-content report-modal">
          <div class="modal-header">
            <strong>수주대상 확정하기</strong>
            <button @click="closeReportModal" class="close-btn">&times;</button>
          </div>

          <div class="modal-tabs">
            <button
              v-for="tab in reportTabs"
              :key="tab.value"
              type="button"
              class="tab-btn"
              :class="{ active: activeReportTab === tab.value }"
              @click="activeReportTab = tab.value"
            >
              {{ tab.label }}
            </button>
          </div>

          <div class="notice">
            <div v-if="activeReportTab === 'select'">
              조건에 맞는 설계/감리 용역 후보를 체크박스로 선택하고 확정하는 화면입니다. 전체 선택이 가능합니다.
            </div>
            <div v-else-if="activeReportTab === 'reselect'">
              확정되지 않은 계약을 다시 검색해서 추가 선택하는 화면입니다.
            </div>
            <div v-else>
              이미 확정된 계약의 영업대상 상태를 바꾸거나 목록에서 삭제하는 화면입니다.
            </div>
          </div>

          <div v-if="activeReportTab !== 'change'" class="modal-body">
            <div class="modal-pane left-pane">
              <div class="modal-tool-row">
                <input
                  type="text"
                  v-model="reportModalSearch"
                  placeholder="계약명, 수요기관, 공공조달분류 검색"
                  class="modal-search-input"
                />
                <label class="select-all-label">
                  <input type="checkbox" :checked="isAllVisibleChecked" @change="toggleVisibleCandidates" />
                  전체 선택
                </label>
              </div>
              <ul class="modal-list">
                <li
                  v-for="item in visibleCandidates"
                  :key="item.contractNo"
                  class="modal-list-item candidate-item"
                >
                  <label>
                    <input
                      type="checkbox"
                      :checked="isCandidateChecked(item)"
                      @change="toggleCandidate(item)"
                    />
                    <span>
                      {{ item.firstContractDate }} | {{ item.contractName }} | {{ item.demandAgencyName }} |
                      {{ formatNumber(item.firstContractAmount) }}
                    </span>
                  </label>
                </li>
                <li v-if="visibleCandidates.length === 0" class="modal-loading">검색 결과가 없습니다.</li>
              </ul>
            </div>

            <div class="modal-pane right-pane">
              <h4>선택된 항목</h4>
              <ul class="modal-list">
                <li v-for="item in reportSelectedCandidates" :key="item.contractNo" class="modal-list-item">
                  {{ item.firstContractDate }} | {{ item.contractName }} | {{ item.demandAgencyName }}
                  <button class="remove-btn" @click="removeReportCandidate(item)">제거</button>
                </li>
                <li v-if="reportSelectedCandidates.length === 0" class="modal-loading">
                  선택된 항목이 없습니다.
                </li>
              </ul>
            </div>
          </div>

          <div v-else class="modal-body change-body">
            <div class="modal-pane change-pane">
              <table class="data-table change-table">
                <thead>
                  <tr>
                    <th>계약건명</th>
                    <th>수요기관명</th>
                    <th>완수일자</th>
                    <th>영업대상</th>
                    <th>관리</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="item in reportItems" :key="item.contractNo">
                    <td>{{ item.contractName }}</td>
                    <td>{{ item.demandAgencyName }}</td>
                    <td>{{ item.completionDate }}</td>
                    <td>
                      <select v-model="item.businessTarget" class="target-select">
                        <option value="O">O</option>
                        <option value="X">X</option>
                      </select>
                    </td>
                    <td>
                      <button class="remove-btn" @click="mockUnselect(item)">삭제</button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <div class="modal-footer">
            <button v-if="activeReportTab !== 'change'" @click="saveReportSelected" class="save-btn">
              선택 항목 확정
            </button>
            <button v-else @click="closeReportModal" class="save-btn">변경 내용 확인</button>
          </div>
        </div>
      </div>
    </template>

    <template v-else>
      <h1 class="servicesSelected">{{ pageTitle }}</h1>

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
          <span class="pagination-info">
            {{ formatNumber(startDisplay) }}-{{ formatNumber(endDisplay) }} / {{ formatNumber(recordsFiltered) }}건
          </span>
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

      <div v-if="isModalOpen" class="modal-overlay">
        <div class="modal-content">
          <div class="modal-header">
            <strong>수주대상 사업 탐색</strong>
            <button @click="closeModal" class="close-btn">&times;</button>
          </div>

          <div class="notice">
            <div>
              <b>안내</b> - 이미 <b>수주대상 사업 선택하여 저장된 계약</b>은 좌측 목록에 표시되지
              않습니다.
            </div>
          </div>

          <div class="modal-body">
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
    </template>
  </LegacySidebarLayout>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import axios from 'axios'
import LegacySidebarLayout from './components/LegacySidebarLayout.vue'

const route = useRoute()
const isReportMode = computed(() => route.meta.targetProjectsSource === 'report')
const pageTitle = computed(() => '수주대상 사업탐색')

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

const years = ['2026', '2025', '2024', '2023', '2022', '2021', '2020']

const reportFilters = reactive({
  dminsttNm: '',
  dminsttNmDetail: '',
  contractName: '',
  categoryName: '',
  cntctCnclsMthdNm: '',
  firstCntrctDate: '',
  dateBasis: 'completion',
  dateType: 'year',
  year: '',
  month: '',
  rangeStart: '',
  rangeEnd: '',
  includeText: '',
  excludeText: '',
})

const reportItems = ref([
  {
    contractNo: 'R26TA01910358',
    vendorName: '탑정보통신 주식회사',
    contractName: '칠곡군 노후관로 정비공사 건설사업관리용역(총괄 및 1차분)',
    demandAgencyName: '경상북도 칠곡군 수도사업소',
    demandAgencyRegion: '경상북도 칠곡군',
    categoryName: '토목감리용역',
    contractMethod: '제한경쟁',
    bidNoticeNo: '20260528112',
    firstContractDate: '2026-05-28',
    firstContractAmount: 1574000000,
    completionDate: '2028-07-06',
    businessTarget: 'O',
  },
  {
    contractNo: 'R26TA01905487',
    vendorName: '탑인더스트리(주)',
    contractName: '하수관로 신설사업 건설사업관리용역(2차수)',
    demandAgencyName: '부산광역시 강서구',
    demandAgencyRegion: '부산광역시 강서구',
    categoryName: '상하수도설계용역',
    contractMethod: '제한경쟁',
    bidNoticeNo: '20260528074',
    firstContractDate: '2026-05-28',
    firstContractAmount: 640600000,
    completionDate: '2029-03-29',
    businessTarget: 'O',
  },
  {
    contractNo: 'R26TA01903743',
    vendorName: '우리기술사사무소',
    contractName: '26년 정기 위험성평가 및 근골격계부담작업 유해요인조사 용역',
    demandAgencyName: '서울특별시교육청 서울다솜관광고등학교',
    demandAgencyRegion: '서울특별시',
    categoryName: '안전진단용역',
    contractMethod: '수의계약',
    bidNoticeNo: '20260529031',
    firstContractDate: '2026-05-29',
    firstContractAmount: 2860000,
    completionDate: '2026-09-30',
    businessTarget: 'X',
  },
])

const reportCandidateItems = ref([
  {
    contractNo: 'R26TA01916361',
    contractName: '삼척시 성장관리계획구역 지정 및 성장관리계획 수립 용역',
    demandAgencyName: '강원특별자치도 삼척시',
    categoryName: '도시계획용역',
    firstContractDate: '2026-05-29',
    firstContractAmount: 99935000,
    completionDate: '2027-11-25',
  },
  {
    contractNo: 'R26TA01914917',
    contractName: '2030년 통영 도시관리계획 수립 용역(총괄분 및 1차분)',
    demandAgencyName: '경상남도 통영시',
    categoryName: '도시계획용역',
    firstContractDate: '2026-05-29',
    firstContractAmount: 300000000,
    completionDate: '2028-11-20',
  },
  {
    contractNo: 'R26TA01906263',
    contractName: '서성지구 농촌공간정비사업 석면감리용역',
    demandAgencyName: '한국농어촌공사 전남지역본부 화순지사',
    categoryName: '건축감리용역',
    firstContractDate: '2026-05-28',
    firstContractAmount: 45000000,
    completionDate: '2027-12-20',
  },
])

const isReportModalOpen = ref(false)
const activeReportTab = ref('select')
const reportModalSearch = ref('')
const reportSelectedCandidates = ref([])
const reportTabs = [
  { value: 'select', label: '선택하기' },
  { value: 'reselect', label: '재선택하기' },
  { value: 'change', label: '변경하기' },
]

const isModalOpen = ref(false)
const modalSearch = ref('')
const modalItems = ref([])
const selectedModalItems = ref([])
const isModalLoading = ref(false)
const modalOffset = ref(0)
const modalAllLoaded = ref(false)

const filteredReportItems = computed(() => {
  return reportItems.value.filter((item) => {
    if (reportFilters.dminsttNm && !item.demandAgencyName.includes(reportFilters.dminsttNm)) return false
    if (reportFilters.dminsttNmDetail && !item.demandAgencyRegion.includes(reportFilters.dminsttNmDetail)) return false
    if (reportFilters.contractName && !item.contractName.includes(reportFilters.contractName)) return false
    if (reportFilters.categoryName && item.categoryName !== reportFilters.categoryName) return false
    if (reportFilters.cntctCnclsMthdNm && item.contractMethod !== reportFilters.cntctCnclsMthdNm) return false
    if (reportFilters.firstCntrctDate && item.firstContractDate !== reportFilters.firstCntrctDate) return false
    return matchesKeywordRule(item)
  })
})

const visibleCandidates = computed(() => {
  const keyword = reportModalSearch.value.trim()
  const existing = new Set(reportItems.value.map((item) => item.contractNo))
  return reportCandidateItems.value.filter((item) => {
    if (activeReportTab.value === 'select' && existing.has(item.contractNo)) return false
    if (!keyword) return true
    return [item.contractName, item.demandAgencyName, item.categoryName].some((value) =>
      value.includes(keyword),
    )
  })
})

const isAllVisibleChecked = computed(() => {
  if (visibleCandidates.value.length === 0) return false
  return visibleCandidates.value.every((item) => isCandidateChecked(item))
})

function splitWords(value) {
  return value
    .split(',')
    .map((word) => word.trim())
    .filter(Boolean)
}

function matchesKeywordRule(item) {
  const target = `${item.contractName} ${item.demandAgencyName} ${item.categoryName}`
  const includeWords = splitWords(reportFilters.includeText)
  const excludeWords = splitWords(reportFilters.excludeText)
  if (includeWords.length > 0 && !includeWords.some((word) => target.includes(word))) return false
  if (excludeWords.some((word) => target.includes(word))) return false
  return true
}

function handleReportSearch() {
  currentPage.value = 1
}

function handleReportExcel() {
  alert('목업 화면입니다. 실제 엑셀 다운로드는 데이터 연동 후 연결됩니다.')
}

function openReportModal() {
  isReportModalOpen.value = true
  activeReportTab.value = 'select'
  reportModalSearch.value = ''
  reportSelectedCandidates.value = []
}

function closeReportModal() {
  isReportModalOpen.value = false
}

function isCandidateChecked(item) {
  return reportSelectedCandidates.value.some((selected) => selected.contractNo === item.contractNo)
}

function toggleCandidate(item) {
  if (isCandidateChecked(item)) {
    removeReportCandidate(item)
    return
  }
  reportSelectedCandidates.value.push({ ...item, businessTarget: 'O' })
}

function toggleVisibleCandidates() {
  if (isAllVisibleChecked.value) {
    const visible = new Set(visibleCandidates.value.map((item) => item.contractNo))
    reportSelectedCandidates.value = reportSelectedCandidates.value.filter(
      (item) => !visible.has(item.contractNo),
    )
    return
  }
  for (const item of visibleCandidates.value) {
    if (!isCandidateChecked(item)) reportSelectedCandidates.value.push({ ...item, businessTarget: 'O' })
  }
}

function removeReportCandidate(item) {
  reportSelectedCandidates.value = reportSelectedCandidates.value.filter(
    (selected) => selected.contractNo !== item.contractNo,
  )
}

function saveReportSelected() {
  if (reportSelectedCandidates.value.length === 0) {
    alert('선택된 항목이 없습니다.')
    return
  }
  const existing = new Set(reportItems.value.map((item) => item.contractNo))
  const additions = reportSelectedCandidates.value.filter((item) => !existing.has(item.contractNo))
  reportItems.value = [...reportItems.value, ...additions]
  reportSelectedCandidates.value = []
  closeReportModal()
}

function mockUnselect(item) {
  reportItems.value = reportItems.value.filter((row) => row.contractNo !== item.contractNo)
}

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
  if (isReportMode.value) return
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
  if (isReportMode.value) return
  const total = Math.max(1, Math.ceil(recordsFiltered.value / PAGE_SIZE))
  if (page < 1 || page > total) return
  currentPage.value = page
  fetchData()
}

const handleSearch = () => fetchData(true)

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
  if (isReportMode.value) return
  modalOffset.value = 0
  modalAllLoaded.value = false
  modalItems.value = []
  fetchModalData()
})

watch(
  () => route.fullPath,
  () => {
    if (!isReportMode.value) {
      fetchData(true)
    }
  },
)

onMounted(() => {
  if (!isReportMode.value) fetchData()
})
</script>

<style scoped>
.servicesSelected {
  color: #ecf0f1;
}

.mock-banner {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  padding: 12px 16px;
  border: 1px solid #bfdbfe;
  border-left: 4px solid #2563eb;
  border-radius: 8px;
  background: #eff6ff;
  color: #1e3a8a;
  font-size: 14px;
}

.search-container {
  margin-bottom: 20px;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.report-search {
  padding: 18px 20px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  align-items: flex-start;
}

.search-filter-row,
.search-actions-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: flex-end;
  gap: 10px 12px;
  flex: 1 1 100%;
  min-width: 0;
}

.search-date-row,
.keyword-row,
.search-actions-row {
  padding-top: 10px;
  border-top: 1px dashed #e2e8f0;
}

input[type='text'],
select,
input[type='month'] {
  padding: 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
}

.report-search input[type='text'],
.report-search select,
.report-search input[type='month'] {
  min-width: 160px;
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

.table-wrapper {
  max-height: 70vh;
  overflow: auto;
  border: 1px solid #ccc;
}

.data-table {
  width: 100%;
  margin-top: 0;
  border-collapse: collapse;
}

.data-table th,
.data-table td {
  padding: 10px;
  border: 1px solid #ddd;
  text-align: center;
  white-space: nowrap;
}

.data-table th {
  position: sticky;
  top: 0;
  background-color: #f1f1f1;
  font-weight: bold;
  z-index: 1;
}

.data-table tbody tr:nth-child(even) {
  background-color: #f9f9f9;
}

.data-table tbody tr:hover {
  background-color: #f1f1f1;
}

.report-table td:nth-child(2) {
  max-width: 420px;
  overflow: hidden;
  text-overflow: ellipsis;
}

.loading-text,
.no-data,
.modal-loading {
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

.target-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 24px;
  border-radius: 999px;
  background: #dcfce7;
  color: #166534;
  font-weight: 700;
}

.target-badge.off {
  background: #fee2e2;
  color: #991b1b;
}

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
  border-radius: 8px;
  display: flex;
  flex-direction: column;
}

.report-modal {
  width: min(1180px, 94vw);
  height: 84vh;
}

.modal-header {
  padding: 10px 14px;
  background: #34495e;
  color: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top-left-radius: 8px;
  border-top-right-radius: 8px;
}

.close-btn {
  background: transparent;
  border: none;
  color: #fff;
  font-size: 20px;
  cursor: pointer;
}

.modal-tabs {
  display: flex;
  gap: 8px;
  padding: 12px 14px 0;
}

.tab-btn {
  padding: 8px 14px;
  border: 1px solid #cbd5e1;
  background: #fff;
  color: #334155;
  border-radius: 6px;
  cursor: pointer;
}

.tab-btn.active {
  background: #34495e;
  border-color: #34495e;
  color: #fff;
}

.notice {
  margin: 8px 14px 0;
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
  padding: 10px 14px;
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

.modal-tool-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
}

.modal-search-input {
  width: 100%;
  padding: 8px;
  box-sizing: border-box;
}

.select-all-label {
  flex: 0 0 auto;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
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
}

.candidate-item label {
  display: flex;
  align-items: center;
  gap: 8px;
}

.change-pane {
  flex: 1 1 100%;
}

.target-select {
  min-width: 64px;
}

.modal-footer {
  padding: 10px 14px;
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

.page-num-btn {
  min-width: 36px;
  padding: 6px 10px;
  border: 1px solid #ddd;
  background: #fff;
  cursor: pointer;
  border-radius: 4px;
}

.page-num-btn.active {
  background: #34495e;
  color: #fff;
  border-color: #34495e;
}
</style>
