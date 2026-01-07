<template>
  <LegacySidebarLayout>
    <h1 class="services">관련업계 전체시장 DB - 용역</h1>

    <!-- 검색 필드 -->
    <div class="search-container">
      <input type="text" v-model="filters.dminsttNm" placeholder="수요기관명 검색">
      <input type="text" v-model="filters.dminsttNmDetail" placeholder="수요기관지역명 검색">
      <input type="text" v-model="filters.prdctClsfcNo" placeholder="품명내용 검색">
      <input type="text" v-model="filters.cntctCnclsMthdNm" placeholder="입찰계약방법 검색">
      <input type="text" v-model="filters.firstCntrctDate" placeholder="최초계약일자 검색">

      <select v-model="filters.dateType" class="date-select">
        <option value="year">연도 검색</option>
        <option value="month">특정 월 검색</option>
        <option value="range">기간 검색</option>
      </select>

      <select v-if="filters.dateType === 'year'" v-model="filters.year" class="date-select">
        <option value="">선택</option>
        <option v-for="y in years" :key="y" :value="y">{{ y }}</option>
      </select>

      <input v-if="filters.dateType === 'month'" type="month" v-model="filters.month">
      
      <template v-if="filters.dateType === 'range'">
        <input type="month" v-model="filters.rangeStart" placeholder="시작월">
        <input type="month" v-model="filters.rangeEnd" placeholder="종료월">
      </template>

      <button @click="handleSearch" class="search-btn">검색</button>

      <label class="checkbox-label">
        <input type="checkbox" v-model="filters.showSavedOnly">
        저장된 데이터만 보기
      </label>

      <button @click="handleDownloadExcel" class="excel-btn">
        엑셀 다운로드
      </button>

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
              <a v-if="item.cntrctDtlInfoUrl" :href="item.cntrctDtlInfoUrl" target="_blank" rel="noopener noreferrer">{{ item.cntrctNm }}</a>
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
              <input type="checkbox" :checked="item.checked" @change="toggleSave(item)">
            </td>
          </tr>
        </tbody>
      </table>
      </div>
    </div>
  </LegacySidebarLayout>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import LegacySidebarLayout from './components/LegacySidebarLayout.vue';

// Mock Data Generator
const generateMockData = (count = 10) => {
  return Array.from({ length: count }, (_, i) => ({
    id: i,
    cmpNm: `(주)용역업체${i + 1}`,
    cntrctNm: `2025년 용역 계약 ${i + 1}건`,
    cntrctDtlInfoUrl: '#',
    dminsttNm: `수요기관${i + 1}`,
    dminsttNmDetail: `지역${i + 1}`,
    prdctClsfcNo: `용역분류${i + 1}`,
    cntctCnclsMthdNm: '일반경쟁',
    ntceNo: `20250100${i + 1}`,
    firstCntrctDate: '2025-01-01',
    firstCntrctAmt: 2000000 * (i + 1),
    cntrctDate: '2025-01-15',
    thtmCntrctAmt: 2200000 * (i + 1),
    cntrctCnt: 1,
    checked: i % 4 === 0
  }));
};

// State
const isLoading = ref(false);
const items = ref([]);
const filters = reactive({
  dminsttNm: '',
  dminsttNmDetail: '',
  prdctClsfcNo: '',
  cntctCnclsMthdNm: '',
  firstCntrctDate: '',
  dateType: 'year',
  year: '2025',
  month: '',
  rangeStart: '',
  rangeEnd: '',
  showSavedOnly: false
});

const years = ['2025', '2024', '2023', '2022', '2021', '2020'];

// Methods
const fetchData = async () => {
  isLoading.value = true;
  // TODO: 나중에 이 부분은 axios.get('/api/data', { params: { category: 'services', ...filters } })로 교체해야 함
  setTimeout(() => {
    items.value = generateMockData(15);
    isLoading.value = false;
  }, 800);
};

const handleSearch = () => {
  fetchData();
};

const handleDownloadExcel = () => {
  // TODO: 나중에 이 부분은 axios.post('/api/data/excel', requestData, { responseType: 'blob' })로 교체해야 함
  alert('엑셀 다운로드 (Mock)');
};

const toggleSave = (item) => {
  // TODO: 나중에 이 부분은 axios.post('/api/update-checked', { category: 'services', id: item.id, checked: !item.checked })로 교체해야 함
  item.checked = !item.checked;
  console.log(`ID: ${item.id}, Checked: ${item.checked} 업데이트`);
};

const formatNumber = (num) => {
  return num ? num.toLocaleString() : '';
};

// Lifecycle
onMounted(() => {
  fetchData();
});
</script>

<style scoped>
/* Reuse styles from GoodsView or put in global css. Since prompt says preserve styling and I'm using scoped, I'll copy paste for now to ensure isolation and pixel perfect match per file */
.search-container {
  margin-bottom: 20px;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

input[type="text"], select, input[type="month"] {
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
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.table-container {
  overflow-x: auto;
}

.data-table {
  width: 100%;
  margin-top: 20px;
  border-collapse: collapse;
}

.data-table th, .data-table td {
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

.loading-text, .no-data {
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
</style>