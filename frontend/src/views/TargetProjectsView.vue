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
          <select v-model="reportFilters.viewMode" class="date-select">
            <option value="flat">풀어서 보기</option>
            <option value="grouped">합쳐서 보기</option>
          </select>
          <select v-model="reportFilters.designSupervisionType" class="date-select">
            <option value="">설계/감리 (전체)</option>
            <option value="설계">설계</option>
            <option value="감리">감리</option>
          </select>
          <select v-model="reportFilters.categoryName" class="date-select">
            <option value="">공공조달분류명 (전체)</option>
            <option value="토목설계용역">토목설계용역</option>
            <option value="상하수도설계용역">상하수도설계용역</option>
            <option value="건축설계용역">건축설계용역</option>
            <option value="토목감리용역">토목감리용역</option>
            <option value="건축감리용역">건축감리용역</option>
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
                <th>설계/감리</th>
                <th>공공조달분류명</th>
                <th>입찰공고번호</th>
                <th>최초계약일자</th>
                <th>최초계약금액</th>
                <th>완수일자</th>
                <th>수기입력</th>
                <th>선택해제</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="filteredReportItems.length === 0">
                <td colspan="11" class="no-data">데이터가 없습니다.</td>
              </tr>
              <tr v-else v-for="item in filteredReportItems" :key="item.contractNo">
                <td>{{ item.vendorName }}</td>
                <td>{{ item.contractName }}</td>
                <td>{{ item.demandAgencyName }}</td>
                <td>
                  <span class="type-badge" :class="{ supervision: item.designSupervisionType === '감리' }">
                    {{ item.designSupervisionType }}
                  </span>
                </td>
                <td>{{ item.categoryName }}</td>
                <td>{{ item.bidNoticeNo }}</td>
                <td>{{ item.firstContractDate }}</td>
                <td>{{ formatNumber(item.firstContractAmount) }}</td>
                <td>{{ item.completionDate }}</td>
                <td>
                  <button class="manual-btn" @click="openManualInput(item)">입력</button>
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
            <div v-if="activeReportTab === 'keyword'">
              포함/제외 키워드를 먼저 저장해두고, 수주대상 확정 화면에서 추천 리스트 분류 기준으로 사용합니다.
            </div>
            <div v-else-if="activeReportTab === 'select'">
              기간과 설계/감리 조건으로 조회한 뒤 추천/제외 추천/미분류를 나눠 보여줍니다. 각 리스트에서 체크한 항목은 확정 예정 항목에 포함됩니다.
            </div>
            <div v-else-if="activeReportTab === 'reselect'">
              아직 확정되지 않은 계약만 다시 조회해서 추가 선택하는 화면입니다.
            </div>
            <div v-else>
              이미 확정된 계약만 조회하고, 선택 목록에서 삭제하거나 수기 입력 값을 관리하는 화면입니다.
            </div>
          </div>

          <div v-if="activeReportTab === 'keyword'" class="keyword-config">
            <div class="keyword-card include">
              <h4>대상 추천 포함 키워드</h4>
              <div class="keyword-chip-row">
                <span v-for="word in keywordDictionary.include" :key="word" class="keyword-chip include">
                  {{ word }}
                </span>
              </div>
              <input
                type="text"
                v-model="keywordDraft.include"
                placeholder="예: 하수도, 상수도, 배수지"
                class="modal-search-input"
              />
            </div>
            <div class="keyword-card exclude">
              <h4>대상 제외 추천 키워드</h4>
              <div class="keyword-chip-row">
                <span v-for="word in keywordDictionary.exclude" :key="word" class="keyword-chip exclude">
                  {{ word }}
                </span>
              </div>
              <input
                type="text"
                v-model="keywordDraft.exclude"
                placeholder="예: 송전탑, 조경, 석면"
                class="modal-search-input"
              />
            </div>
          </div>

          <div v-else-if="activeReportTab !== 'change'" class="modal-body report-selection-body">
            <div class="modal-filter-band">
              <select v-model="reportModalFilters.dateBasis" class="date-select">
                <option value="first">최초계약일자 기준</option>
                <option value="completion">완수일자 기준</option>
              </select>
              <select v-model="reportModalFilters.dateType" class="date-select">
                <option value="year">연도 검색</option>
                <option value="month">특정 월 검색</option>
                <option value="range">기간 검색</option>
              </select>
              <select v-if="reportModalFilters.dateType === 'year'" v-model="reportModalFilters.year" class="date-select">
                <option value="">선택</option>
                <option v-for="y in years" :key="y" :value="y">{{ y }}</option>
              </select>
              <input v-if="reportModalFilters.dateType === 'month'" type="month" v-model="reportModalFilters.month" />
              <template v-if="reportModalFilters.dateType === 'range'">
                <input type="month" v-model="reportModalFilters.rangeStart" placeholder="시작월" />
                <input type="month" v-model="reportModalFilters.rangeEnd" placeholder="종료월" />
              </template>
              <select v-model="reportModalFilters.designSupervisionType" class="date-select">
                <option value="">설계/감리 전체</option>
                <option value="설계">설계</option>
                <option value="감리">감리</option>
              </select>
              <input
                type="text"
                v-model="reportModalSearch"
                placeholder="계약명, 수요기관, 공공조달분류 검색"
                class="modal-search-input"
              />
            </div>

            <div class="recommendation-grid">
              <div class="modal-pane recommendation-pane exclude">
                <h4>대상 제외 추천리스트</h4>
                <ul class="modal-list">
                  <li
                    v-for="item in excludedCandidates"
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
                        <span class="recommendation-reason">제외 키워드: {{ matchedKeyword(item, 'exclude') }}</span>
                        {{ item.firstContractDate }} | {{ item.contractName }} | {{ item.demandAgencyName }}
                      </span>
                    </label>
                  </li>
                  <li v-if="excludedCandidates.length === 0" class="modal-loading">조회 결과가 없습니다.</li>
                </ul>
              </div>

              <div class="modal-pane recommendation-pane neutral">
                <h4>미분류</h4>
                <ul class="modal-list">
                  <li
                    v-for="item in neutralCandidates"
                    :key="item.contractNo"
                    class="modal-list-item candidate-item"
                  >
                    <label>
                      <input
                        type="checkbox"
                        :checked="isCandidateChecked(item)"
                        @change="toggleCandidate(item)"
                      />
                      <span>{{ item.firstContractDate }} | {{ item.contractName }} | {{ item.categoryName }}</span>
                    </label>
                  </li>
                  <li v-if="neutralCandidates.length === 0" class="modal-loading">조회 결과가 없습니다.</li>
                </ul>
              </div>

              <div class="modal-pane recommendation-pane include">
                <div class="recommendation-heading">
                  <h4>대상 추천리스트</h4>
                  <label class="select-all-label">
                    <input type="checkbox" :checked="isAllRecommendedChecked" @change="toggleRecommendedCandidates" />
                    전체 선택
                  </label>
                </div>
                <ul class="modal-list">
                  <li
                    v-for="item in recommendedCandidates"
                    :key="item.contractNo"
                    class="modal-list-item candidate-item"
                  >
                    <label>
                      <input
                        type="checkbox"
                        :checked="isCandidateChecked(item)"
                        @change="toggleCandidate(item)"
                      />
                      <span class="recommendation-reason">포함 키워드: {{ matchedKeyword(item, 'include') }}</span>
                      <span>{{ item.firstContractDate }} | {{ item.contractName }} | {{ item.demandAgencyName }}</span>
                    </label>
                  </li>
                  <li v-if="recommendedCandidates.length === 0" class="modal-loading">조회 결과가 없습니다.</li>
                </ul>
              </div>
            </div>

            <div class="modal-pane selected-pane">
              <h4>확정 예정 항목</h4>
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
                    <th>수기입력</th>
                    <th>관리</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="item in reportItems" :key="item.contractNo">
                    <td>{{ item.contractName }}</td>
                    <td>{{ item.demandAgencyName }}</td>
                    <td>{{ item.completionDate }}</td>
                    <td>
                      <button class="manual-btn" @click="openManualInput(item)">입력</button>
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
            <button v-if="activeReportTab === 'keyword'" @click="saveKeywordMock" class="save-btn">
              키워드 사전 저장
            </button>
            <button v-else-if="activeReportTab !== 'change'" @click="saveReportSelected" class="save-btn">
              선택 항목 확정
            </button>
            <button v-else @click="closeReportModal" class="save-btn">변경 내용 확인</button>
          </div>
        </div>
      </div>

      <div v-if="manualInputItem" class="modal-overlay">
        <div class="modal-content manual-modal">
          <div class="modal-header">
            <strong>수기 입력 - {{ manualInputItem.contractName }}</strong>
            <button @click="manualInputItem = null" class="close-btn">&times;</button>
          </div>
          <div class="manual-form">
            <label>
              착공일자
              <input type="text" v-model="manualInputItem.manual.startDate" placeholder="YYYY-MM-DD" />
            </label>
            <label>
              발주일자
              <input type="text" v-model="manualInputItem.manual.orderDate" placeholder="YYYY-MM-DD" />
            </label>
            <div
              v-for="field in manualAmountFields"
              :key="field.key"
              class="manual-amount-row"
            >
              <label>
                {{ field.label }}
                <input type="text" v-model="manualInputItem.manual[field.key]" placeholder="금액 입력" />
              </label>
              <select v-model="manualInputItem.manual[`${field.key}Status`]">
                <option value="예상">예상금액</option>
                <option value="확인">확인금액</option>
              </select>
            </div>
            <label class="manual-note-field">
              비고
              <textarea v-model="manualInputItem.manual.note" placeholder="비고 입력"></textarea>
            </label>
          </div>
          <div class="modal-footer">
            <button @click="manualInputItem = null" class="save-btn">저장</button>
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
  viewMode: 'flat',
  designSupervisionType: '',
  categoryName: '',
  firstCntrctDate: '',
  dateBasis: 'completion',
  dateType: 'year',
  year: '',
  month: '',
  rangeStart: '',
  rangeEnd: '',
})

const reportItems = ref([
  {
    contractNo: 'R26TA01910358',
    vendorName: '탑정보통신 주식회사',
    contractName: '칠곡군 노후관로 정비공사 건설사업관리용역(총괄 및 1차분)',
    demandAgencyName: '경상북도 칠곡군 수도사업소',
    demandAgencyRegion: '경상북도 칠곡군',
    categoryName: '토목감리용역',
    designSupervisionType: '감리',
    contractMethod: '제한경쟁',
    bidNoticeNo: '20260528112',
    firstContractDate: '2026-05-28',
    firstContractAmount: 1574000000,
    completionDate: '2028-07-06',
    manual: {
      startDate: '2026-06-15',
      orderDate: '2026-05-20',
      totalConstructionAmount: '4,200,000,000',
      totalConstructionAmountStatus: '예상',
      powerDistributionAmount: '380,000,000',
      powerDistributionAmountStatus: '확인',
      controlAmount: '',
      controlAmountStatus: '예상',
      buildingControlAmount: '',
      buildingControlAmountStatus: '예상',
      note: '장기계약 총완수일자 기준 검토 필요',
    },
  },
  {
    contractNo: 'R26TA01905487',
    vendorName: '탑인더스트리(주)',
    contractName: '하수관로 신설사업 건설사업관리용역(2차수)',
    demandAgencyName: '부산광역시 강서구',
    demandAgencyRegion: '부산광역시 강서구',
    categoryName: '상하수도설계용역',
    designSupervisionType: '설계',
    contractMethod: '제한경쟁',
    bidNoticeNo: '20260528074',
    firstContractDate: '2026-05-28',
    firstContractAmount: 640600000,
    completionDate: '2029-03-29',
    manual: {
      startDate: '',
      orderDate: '',
      totalConstructionAmount: '',
      totalConstructionAmountStatus: '예상',
      powerDistributionAmount: '',
      powerDistributionAmountStatus: '예상',
      controlAmount: '',
      controlAmountStatus: '예상',
      buildingControlAmount: '',
      buildingControlAmountStatus: '예상',
      note: '',
    },
  },
  {
    contractNo: 'R26TA01903743',
    vendorName: '우리기술사사무소',
    contractName: '26년 정기 위험성평가 및 근골격계부담작업 유해요인조사 용역',
    demandAgencyName: '서울특별시교육청 서울다솜관광고등학교',
    demandAgencyRegion: '서울특별시',
    categoryName: '안전진단용역',
    designSupervisionType: '감리',
    contractMethod: '수의계약',
    bidNoticeNo: '20260529031',
    firstContractDate: '2026-05-29',
    firstContractAmount: 2860000,
    completionDate: '2026-09-30',
    manual: {
      startDate: '',
      orderDate: '',
      totalConstructionAmount: '',
      totalConstructionAmountStatus: '예상',
      powerDistributionAmount: '',
      powerDistributionAmountStatus: '예상',
      controlAmount: '',
      controlAmountStatus: '예상',
      buildingControlAmount: '',
      buildingControlAmountStatus: '예상',
      note: '',
    },
  },
])

const reportCandidateItems = ref([
  {
    contractNo: 'R26TA01916361',
    contractName: '삼척시 성장관리계획구역 지정 및 성장관리계획 수립 용역',
    demandAgencyName: '강원특별자치도 삼척시',
    categoryName: '도시계획용역',
    designSupervisionType: '설계',
    firstContractDate: '2026-05-29',
    firstContractAmount: 99935000,
    completionDate: '2027-11-25',
    bidNoticeNo: '20260529221',
    vendorName: '도시종합기술단',
  },
  {
    contractNo: 'R26TA01914917',
    contractName: '2030년 통영 도시관리계획 수립 용역(총괄분 및 1차분)',
    demandAgencyName: '경상남도 통영시',
    categoryName: '도시계획용역',
    designSupervisionType: '설계',
    firstContractDate: '2026-05-29',
    firstContractAmount: 300000000,
    completionDate: '2028-11-20',
    bidNoticeNo: '20260529104',
    vendorName: '한려엔지니어링',
  },
  {
    contractNo: 'R26TA01906263',
    contractName: '서성지구 농촌공간정비사업 석면감리용역',
    demandAgencyName: '한국농어촌공사 전남지역본부 화순지사',
    categoryName: '건축감리용역',
    designSupervisionType: '감리',
    firstContractDate: '2026-05-28',
    firstContractAmount: 45000000,
    completionDate: '2027-12-20',
    bidNoticeNo: '20260528048',
    vendorName: '해담건축사사무소',
  },
  {
    contractNo: 'R26TA01922207',
    contractName: '하수도정비 기본계획 변경 및 침수대응 설계용역',
    demandAgencyName: '경기도 광주시',
    categoryName: '상하수도설계용역',
    designSupervisionType: '설계',
    firstContractDate: '2026-05-30',
    firstContractAmount: 730000000,
    completionDate: '2028-02-15',
    bidNoticeNo: '20260530077',
    vendorName: '수엔지니어링',
  },
  {
    contractNo: 'R26TA01924188',
    contractName: '송전탑 주변지역 정비 실시설계용역',
    demandAgencyName: '한국전력공사',
    categoryName: '전기설계용역',
    designSupervisionType: '설계',
    firstContractDate: '2026-05-30',
    firstContractAmount: 88000000,
    completionDate: '2026-12-20',
    bidNoticeNo: '20260530119',
    vendorName: '대한전력기술',
  },
])

const isReportModalOpen = ref(false)
const activeReportTab = ref('select')
const reportModalSearch = ref('')
const reportModalFilters = reactive({
  dateBasis: 'first',
  dateType: 'range',
  year: '',
  month: '',
  rangeStart: '2026-05',
  rangeEnd: '2026-05',
  designSupervisionType: '',
})
const keywordDictionary = reactive({
  include: ['하수도', '상수도', '배수지'],
  exclude: ['송전탑', '석면', '조경'],
})
const keywordDraft = reactive({
  include: keywordDictionary.include.join(', '),
  exclude: keywordDictionary.exclude.join(', '),
})
const reportSelectedCandidates = ref([])
const manualInputItem = ref(null)
const reportTabs = [
  { value: 'keyword', label: '키워드 사전' },
  { value: 'select', label: '선택하기' },
  { value: 'reselect', label: '재선택하기' },
  { value: 'change', label: '변경하기' },
]
const manualAmountFields = [
  { key: 'totalConstructionAmount', label: '총공사금액' },
  { key: 'powerDistributionAmount', label: '수배전금액' },
  { key: 'controlAmount', label: '제어금액' },
  { key: 'buildingControlAmount', label: '빌딩제어금액' },
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
    if (reportFilters.dminsttNmDetail && !(item.demandAgencyRegion || '').includes(reportFilters.dminsttNmDetail)) return false
    if (reportFilters.contractName && !item.contractName.includes(reportFilters.contractName)) return false
    if (reportFilters.designSupervisionType && item.designSupervisionType !== reportFilters.designSupervisionType) return false
    if (reportFilters.categoryName && item.categoryName !== reportFilters.categoryName) return false
    if (reportFilters.firstCntrctDate && item.firstContractDate !== reportFilters.firstCntrctDate) return false
    if (!matchesDateFilter(item, reportFilters)) return false
    return true
  })
})

const modalBaseCandidates = computed(() => {
  const keyword = reportModalSearch.value.trim()
  const existing = new Set(reportItems.value.map((item) => item.contractNo))
  return reportCandidateItems.value.filter((item) => {
    if ((activeReportTab.value === 'select' || activeReportTab.value === 'reselect') && existing.has(item.contractNo)) return false
    if (reportModalFilters.designSupervisionType && item.designSupervisionType !== reportModalFilters.designSupervisionType) return false
    if (!matchesDateFilter(item, reportModalFilters)) return false
    if (keyword && !candidateTargetText(item).includes(keyword)) return false
    return true
  })
})

const recommendedCandidates = computed(() =>
  modalBaseCandidates.value.filter((item) => Boolean(matchedKeyword(item, 'include'))),
)

const excludedCandidates = computed(() =>
  modalBaseCandidates.value.filter((item) => Boolean(matchedKeyword(item, 'exclude'))),
)

const neutralCandidates = computed(() =>
  modalBaseCandidates.value.filter(
    (item) => !matchedKeyword(item, 'include') && !matchedKeyword(item, 'exclude'),
  ),
)

const isAllRecommendedChecked = computed(() => {
  if (recommendedCandidates.value.length === 0) return false
  return recommendedCandidates.value.every((item) => isCandidateChecked(item))
})

function splitWords(value) {
  const source = Array.isArray(value) ? value.join(',') : value
  return source
    .split(',')
    .map((word) => word.trim())
    .filter(Boolean)
}

function candidateTargetText(item) {
  return `${item.contractName} ${item.demandAgencyName} ${item.categoryName}`
}

function matchedKeyword(item, type) {
  const target = candidateTargetText(item)
  return splitWords(keywordDictionary[type]).find((word) => target.includes(word)) || ''
}

function itemBasisDate(item, filtersForDate) {
  return filtersForDate.dateBasis === 'first' ? item.firstContractDate : item.completionDate
}

function matchesDateFilter(item, filtersForDate) {
  const dateValue = itemBasisDate(item, filtersForDate)
  if (!dateValue) return true
  if (filtersForDate.dateType === 'year') {
    return !filtersForDate.year || dateValue.startsWith(filtersForDate.year)
  }
  if (filtersForDate.dateType === 'month') {
    return !filtersForDate.month || dateValue.startsWith(filtersForDate.month)
  }
  if (filtersForDate.dateType === 'range') {
    const monthValue = dateValue.slice(0, 7)
    if (filtersForDate.rangeStart && monthValue < filtersForDate.rangeStart) return false
    if (filtersForDate.rangeEnd && monthValue > filtersForDate.rangeEnd) return false
  }
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
  reportSelectedCandidates.value.push({ ...item, manual: createDefaultManual() })
}

function toggleRecommendedCandidates() {
  if (isAllRecommendedChecked.value) {
    const visible = new Set(recommendedCandidates.value.map((item) => item.contractNo))
    reportSelectedCandidates.value = reportSelectedCandidates.value.filter(
      (item) => !visible.has(item.contractNo),
    )
    return
  }
  for (const item of recommendedCandidates.value) {
    if (!isCandidateChecked(item)) reportSelectedCandidates.value.push({ ...item, manual: createDefaultManual() })
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

function saveKeywordMock() {
  keywordDictionary.include = splitWords(keywordDraft.include)
  keywordDictionary.exclude = splitWords(keywordDraft.exclude)
  activeReportTab.value = 'select'
}

function createDefaultManual() {
  return {
    startDate: '',
    orderDate: '',
    totalConstructionAmount: '',
    totalConstructionAmountStatus: '예상',
    powerDistributionAmount: '',
    powerDistributionAmountStatus: '예상',
    controlAmount: '',
    controlAmountStatus: '예상',
    buildingControlAmount: '',
    buildingControlAmountStatus: '예상',
    note: '',
  }
}

function openManualInput(item) {
  if (!item.manual) item.manual = createDefaultManual()
  manualInputItem.value = item
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

.manual-btn {
  padding: 5px 10px;
  background-color: #0f766e;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.type-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 44px;
  height: 24px;
  padding: 0 8px;
  border-radius: 999px;
  background: #dbeafe;
  color: #1e40af;
  font-weight: 700;
}

.type-badge.supervision {
  background: #dcfce7;
  color: #166534;
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

.report-selection-body {
  flex-direction: column;
}

.modal-filter-band {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  padding: 10px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  background: #f8fafc;
}

.recommendation-grid {
  flex: 1;
  min-height: 0;
  display: grid;
  grid-template-columns: repeat(3, minmax(220px, 1fr));
  gap: 10px;
}

.recommendation-pane h4,
.selected-pane h4 {
  margin: 0 0 10px;
  font-size: 14px;
}

.recommendation-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 10px;
}

.recommendation-heading h4 {
  margin: 0;
}

.recommendation-pane.exclude {
  border-color: #fecaca;
}

.recommendation-pane.include {
  border-color: #bbf7d0;
}

.recommendation-pane.neutral {
  border-color: #fde68a;
}

.recommendation-reason {
  display: block;
  margin-bottom: 4px;
  color: #475569;
  font-size: 12px;
  font-weight: 700;
}

.selected-pane {
  flex: 0 0 150px;
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
  flex-wrap: wrap;
}

.modal-search-input {
  flex: 1 1 260px;
  min-width: 180px;
  padding: 8px;
  box-sizing: border-box;
}

.keyword-input {
  flex-basis: 220px;
}

.keyword-config {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(2, minmax(260px, 1fr));
  gap: 12px;
  padding: 14px;
  overflow: auto;
}

.keyword-card {
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 14px;
  background: #fff;
}

.keyword-card h4 {
  margin: 0 0 12px;
  color: #0f172a;
}

.keyword-chip-row {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  min-height: 32px;
  margin-bottom: 12px;
}

.keyword-chip {
  display: inline-flex;
  align-items: center;
  height: 26px;
  padding: 0 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
}

.keyword-chip.include {
  background: #dcfce7;
  color: #166534;
}

.keyword-chip.exclude {
  background: #fee2e2;
  color: #991b1b;
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

.manual-modal {
  width: min(680px, 92vw);
  height: auto;
  max-height: 86vh;
}

.manual-form {
  display: grid;
  grid-template-columns: repeat(2, minmax(220px, 1fr));
  gap: 12px;
  padding: 16px;
  overflow: auto;
}

.manual-form label {
  display: flex;
  flex-direction: column;
  gap: 6px;
  color: #334155;
  font-weight: 700;
  font-size: 13px;
}

.manual-amount-row {
  display: grid;
  grid-template-columns: 1fr 110px;
  gap: 8px;
  align-items: end;
}

.manual-note-field {
  grid-column: 1 / -1;
}

.manual-note-field textarea {
  min-height: 76px;
  padding: 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
  resize: vertical;
  font: inherit;
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
