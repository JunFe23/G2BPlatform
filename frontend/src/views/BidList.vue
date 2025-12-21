<template>
  <div class="layout-container">
    <div class="sidebar">
      <div class="sidebar-header">
        <span>G2B PROJECT</span>
      </div>
      <ul>
        <li class="menu-item" @click="toggleSubmenu">
          <span class="menu-label">관련업계 전체시장 DB</span>
        </li>

        <ul v-show="isSubmenuOpen" class="submenu">
          <li class="menu-item">
            <router-link to="/goods" class="menu-label">물품</router-link>
          </li>
          <li class="menu-item">
            <router-link to="/services" class="menu-label">용역</router-link>
          </li>
          <li class="menu-item">
            <router-link to="/construction" class="menu-label">공사</router-link>
          </li>
        </ul>

        <li class="menu-item">
          <a href="#" class="menu-label">수주현황DB</a>
        </li>
      </ul>
    </div>

    <div class="content">
      <h1 class="page-title">관련업계 전체시장 DB - {{ categoryLabel }}</h1>

      <div class="search-container">
        <input v-model="filters.dminsttNm" type="text" placeholder="수요기관명 검색">
        <input v-model="filters.dminsttNmDetail" type="text" placeholder="수요기관지역명 검색">
        <input v-model="filters.prdctClsfcNo" type="text" placeholder="품명내용 검색">
        <input v-model="filters.cntctCnclsMthdNm" type="text" placeholder="입찰계약방법 검색">
        <input v-model="filters.firstCntrctDate" type="text" placeholder="최초계약일자 검색">

        <select v-model="filters.dateType" class="search-select">
          <option value="year">연도 검색</option>
          <option value="month">특정 월 검색</option>
          <option value="range">기간 검색</option>
        </select>

        <select v-if="filters.dateType === 'year'" v-model="filters.year" class="search-select">
          <option value="">선택</option>
          <option v-for="y in years" :key="y" :value="y">{{ y }}</option>
        </select>

        <input v-if="filters.dateType === 'month'" v-model="filters.month" type="month">

        <template v-if="filters.dateType === 'range'">
          <input v-model="filters.rangeStart" type="month" placeholder="시작월">
          <input v-model="filters.rangeEnd" type="month" placeholder="종료월">
        </template>

        <button @click="triggerSearch" class="btn-search">검색</button>

        <label class="checkbox-label">
          <input type="checkbox" v-model="filters.showSavedOnly" @change="triggerSearch">
          저장된 데이터만 보기
        </label>

        <button @click="downloadExcel" class="btn-excel" :disabled="isLoadingExcel">
          {{ isLoadingExcel ? '다운로드 중...' : '엑셀 다운로드' }}
        </button>
      </div>

      <DataTable
        ref="tableRef"
        class="display"
        :options="dtOptions"
        :ajax="ajaxConfig"
      >
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
      </DataTable>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue';
import { useRoute } from 'vue-router';
import axios from 'axios';
import DataTable from 'datatables.net-vue3';
import DataTablesCore from 'datatables.net-dt';
import $ from 'jquery';

DataTable.use(DataTablesCore);

// Props로 현재 카테고리 받기 (router에서 전달)
const props = defineProps({
  category: { type: String, default: 'goods' }
});

const route = useRoute();
const isSubmenuOpen = ref(true);
const isLoadingExcel = ref(false);
const tableRef = ref(null);

// 화면 타이틀 계산
const categoryLabel = computed(() => {
  const labels = {
    goods: '물품',
    services: '용역',
    construction: '공사' // ▼ [추가] 매핑 추가
  };
  return labels[props.category] || '기타';
});

const years = Array.from({ length: 9 }, (_, i) => 2025 - i);

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
  showSavedOnly: false,
});

// 카테고리가 변경되면 테이블 다시 로드 (라우트 변경 감지)
watch(() => props.category, () => {
  triggerSearch();
});

const ajaxConfig = {
  url: '/api/data', // 백엔드 API 주소 확인 필요
  type: 'GET',
  data: (d) => ({
    ...d,
    category: props.category,
    ...filters,
    showSavedOnly: filters.showSavedOnly ? 1 : 0,
  })
};

const dtOptions = {
  processing: true,
  serverSide: true,
  searching: false,
  lengthChange: false, // 페이지 당 건수 드롭다운 숨김 (필요시 true)
  pageLength: 20,
  language: {
    processing: '<span>데이터 로딩중...</span>'
  },
  columns: [
    { data: 'cmpNm' },
    {
      data: 'cntrctNm',
      render: (data, type, row) => row.cntrctDtlInfoUrl
        ? `<a href="${encodeURI(row.cntrctDtlInfoUrl)}" target="_blank">${data ?? ''}</a>`
        : data
    },
    { data: 'dminsttNm' },
    { data: 'dminsttNmDetail' },
    { data: 'prdctClsfcNo' },
    { data: 'cntctCnclsMthdNm' },
    { data: 'ntceNo' },
    { data: 'firstCntrctDate' },
    { data: 'firstCntrctAmt', render: $.fn.dataTable.render.number(',', '.', 0) },
    { data: 'cntrctDate' },
    { data: 'thtmCntrctAmt', render: $.fn.dataTable.render.number(',', '.', 0) },
    { data: 'cntrctCnt' },
    {
      data: 'checked',
      orderable: false,
      render: (data, type, row) => `<input type="checkbox" class="save-checkbox" data-id="${row.id}" ${data ? 'checked' : ''}>`
    },
  ]
};

const triggerSearch = () => {
  if (tableRef.value) tableRef.value.dt.draw();
};

const toggleSubmenu = () => {
  isSubmenuOpen.value = !isSubmenuOpen.value;
};

const downloadExcel = async () => {
  isLoadingExcel.value = true;
  try {
    const response = await axios.post('/api/data/excel', {
      category: props.category,
      ...filters,
      showSavedOnly: filters.showSavedOnly ? 1 : 0
    }, { responseType: 'blob' });

    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', '조회결과.xlsx');
    document.body.appendChild(link);
    link.click();
    link.remove();
  } catch (error) {
    alert('다운로드 실패');
  } finally {
    isLoadingExcel.value = false;
  }
};

onMounted(() => {
  // 체크박스 이벤트 위임
  $('.display').on('change', '.save-checkbox', function() {
    const id = $(this).data('id');
    const checked = $(this).is(':checked');
    axios.post('/api/update-checked', { category: props.category, id, checked })
      .catch(e => console.error(e));
  });
});
</script>

<style scoped>
/* 레이아웃 스타일 */
.layout-container { display: flex; width: 100%; min-height: 100vh; font-family: 'Noto Sans KR', sans-serif; }

/* 사이드바 */
.sidebar { width: 75px; background: #2c3e50; color: #ecf0f1; position: fixed; height: 100vh; transition: width 0.3s; z-index: 100; overflow: hidden; }
.sidebar:hover { width: 300px; }
.sidebar-header { background: #34495e; padding: 20px; text-align: center; white-space: nowrap; font-weight: bold; font-size: 1.2rem; }
.menu-item { padding: 15px 20px; cursor: pointer; display: flex; align-items: center; transition: background 0.2s; }
.menu-item:hover { background: #34495e; }
.menu-label { white-space: nowrap; opacity: 0; transform: translateX(-20px); transition: all 0.3s; color: inherit; text-decoration: none; width: 100%; display: block;}
.sidebar:hover .menu-label { opacity: 1; transform: translateX(0); }
.submenu { background: #263544; padding-left: 20px; }

/* 컨텐츠 */
.content { margin-left: 75px; padding: 30px; width: calc(100% - 75px); transition: margin-left 0.3s; }
.sidebar:hover ~ .content { margin-left: 300px; width: calc(100% - 300px); }
.page-title { background: #34495e; color: #fff; padding: 15px; border-radius: 5px; text-align: center; margin-bottom: 20px; }

/* 검색 영역 */
.search-container { display: flex; flex-wrap: wrap; gap: 8px; justify-content: flex-end; margin-bottom: 15px; align-items: center; }
input, select { padding: 8px; border: 1px solid #ddd; border-radius: 4px; }
.btn-search { background: #34495e; color: white; border: none; padding: 8px 16px; border-radius: 4px; cursor: pointer; }
.btn-excel { background: #27ae60; color: white; border: none; padding: 8px 16px; border-radius: 4px; cursor: pointer; }
.btn-excel:disabled { background: #ccc; }
.checkbox-label { display: flex; align-items: center; gap: 5px; cursor: pointer; font-size: 0.9rem; margin: 0 10px; }

/* 테이블 스타일 조정 */
:deep(table.dataTable thead th) { text-align: center; font-size: 0.9rem; background: #f8f9fa; }
:deep(table.dataTable tbody td) { font-size: 0.9rem; vertical-align: middle; }
</style>
