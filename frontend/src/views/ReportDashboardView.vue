<template>
  <LegacySidebarLayout>
    <div class="dashboard-page">
      <header class="page-header">
        <div class="title-block">
          <div class="title-row">
            <span class="logo-dot"></span>
            <h1>조달시장 관리 시스템</h1>
          </div>
          <p>Procurement Market Management System</p>
        </div>
      </header>

      <section class="info-banner">
        <div class="banner-left">
          <div class="info-icon">i</div>
          <div class="banner-text">
            <strong>연차계약 표시 방식</strong>
            <p>각 연도별 계약을 개별적으로 표시합니다</p>
            <span class="example-pill">예시: 2020년 50억원 계약 → 2020년 25억원, 2021년 25억원 (분리)</span>
          </div>
        </div>
        <div class="banner-right">
          <span>연도별 분리</span>
          <label class="switch">
            <input type="checkbox" v-model="isYearSeparated">
            <span class="slider"></span>
          </label>
        </div>
      </section>

      <section class="tab-bar">
        <button
          v-for="tab in tabs"
          :key="tab.label"
          class="tab-button"
          :class="{ active: tab.label === activeTab }"
          @click="activeTab = tab.label"
        >
          <span class="tab-icon">{{ tab.icon }}</span>
          <span>{{ tab.label }}</span>
        </button>
      </section>

      <section v-if="activeTab === '시장현황'" class="section">
        <h2 class="section-title">
          <span class="section-icon">📈</span>
          연차계약 목록
        </h2>
        <div class="contract-list">
          <article v-for="item in contractCards" :key="item.id" class="contract-card" :class="item.tintClass">
            <div class="card-header">
              <div>
                <div class="card-title">
                  <strong>{{ item.title }}</strong>
                  <span class="category-pill" :class="item.categoryClass">{{ item.category }}</span>
                </div>
                <div class="card-subtitle">{{ item.org }}</div>
                <div class="card-meta">최초계약: {{ item.firstContract }}</div>
              </div>
              <div class="card-total">
                <div class="total-amount">전체: {{ item.total }}</div>
                <div class="total-year">{{ item.years }}개년</div>
              </div>
            </div>
            <div class="year-rows">
              <div v-for="year in item.yearsBreakdown" :key="year.label" class="year-row">
                <span class="year-pill">{{ year.label }}</span>
                <span class="year-date">{{ year.date }}</span>
                <span class="year-amount">{{ year.amount }}</span>
              </div>
            </div>
          </article>
        </div>
      </section>

      <section v-if="activeTab === '시장현황'" class="section">
        <h2 class="section-title">전체 조달시장 현황</h2>
        <div class="summary-cards">
          <div v-for="stat in summaryStats" :key="stat.label" class="summary-card">
            <p class="summary-label">{{ stat.label }}</p>
            <p class="summary-value" :class="stat.colorClass">{{ stat.value }}</p>
          </div>
        </div>

        <div class="chart-grid">
          <div class="chart-card">
            <h3>영역별 매출액 현황</h3>
            <div class="bar-chart">
              <div v-for="bar in revenueBars" :key="bar.label" class="bar-column">
                <div class="bar" :style="{ height: bar.height }"></div>
                <span>{{ bar.label }}</span>
              </div>
            </div>
          </div>
          <div class="chart-card">
            <h3>영역별 계약건수</h3>
            <div class="bar-chart green">
              <div v-for="bar in countBars" :key="bar.label" class="bar-column">
                <div class="bar" :style="{ height: bar.height }"></div>
                <span>{{ bar.label }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="chart-grid">
          <div class="chart-card">
            <h3>물품+3자단가: 우수제품 vs 일반제품</h3>
            <div class="donut-wrap">
              <div class="pie"></div>
              <div class="pie-label left">우수제품: 54.1억</div>
              <div class="pie-label right">일반제품: 8000만</div>
            </div>
          </div>
          <div class="chart-card">
            <h3>영역별 상세 현황</h3>
            <div class="detail-list">
              <div v-for="detail in detailItems" :key="detail.label" class="detail-item">
                <span class="dot" :style="{ backgroundColor: detail.color }"></span>
                <div class="detail-text">
                  <strong>{{ detail.label }}</strong>
                  <span>계약 {{ detail.count }}건</span>
                </div>
                <span class="detail-amount">{{ detail.amount }}</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section v-if="activeTab === '수요기관별'" class="section">
        <h2 class="section-title">수요기관별 물품 조달시장 분석</h2>

        <div class="chart-card wide">
          <h3>수요기관별 물품 조달시장 (Top 10)</h3>
          <div class="hbar-chart">
            <div class="hbar-axis">
              <span v-for="tick in ['0만', '15.0억', '30.0억', '45.0억', '60.0억']" :key="tick">{{ tick }}</span>
            </div>
            <div class="hbar-list">
              <div v-for="row in agencyTopSales" :key="row.label" class="hbar-row">
                <span class="hbar-label">{{ row.label }}</span>
                <div class="hbar-track">
                  <div class="hbar-fill" :style="{ width: row.width }"></div>
                </div>
              </div>
            </div>
            <div class="hbar-legend">매출액</div>
          </div>
        </div>

        <div class="chart-grid">
          <div class="chart-card">
            <h3>수요기관별 계약건수</h3>
            <div class="bar-chart green">
              <div v-for="bar in agencyCountBars" :key="bar.label" class="bar-column">
                <div class="bar" :style="{ height: bar.height }"></div>
                <span>{{ bar.label }}</span>
              </div>
            </div>
          </div>
          <div class="chart-card">
            <h3>수요기관별 평균 계약단가</h3>
            <div class="bar-chart orange">
              <div v-for="bar in agencyAvgBars" :key="bar.label" class="bar-column">
                <div class="bar" :style="{ height: bar.height }"></div>
                <span>{{ bar.label }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="table-card">
          <h3>수요기관 상세 현황</h3>
          <div class="table-wrapper">
            <table class="detail-table">
              <thead>
                <tr>
                  <th>순위</th>
                  <th>수요기관</th>
                  <th>매출액</th>
                  <th>계약건수</th>
                  <th>평균단가</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in agencyDetailRows" :key="row.rank">
                  <td>{{ row.rank }}</td>
                  <td>{{ row.name }}</td>
                  <td>{{ row.sales }}</td>
                  <td>{{ row.count }}</td>
                  <td>{{ row.avg }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </section>

      <section v-if="activeTab === '지역별'" class="section">
        <h2 class="section-title">지역별 조달시장 분석</h2>

        <div class="chart-card wide">
          <h3>지역별 전체 매출 현황</h3>
          <div class="stacked-chart">
            <div class="stacked-grid">
              <span v-for="tick in ['0만', '15.0억', '30.0억', '45.0억', '60.0억']" :key="tick">{{ tick }}</span>
            </div>
            <div class="stacked-bars">
              <div v-for="region in regionStackedBars" :key="region.name" class="stacked-column">
                <div class="stacked-track">
                  <div
                    v-for="segment in region.segments"
                    :key="segment.label"
                    class="stacked-segment"
                    :style="{ height: segment.height, backgroundColor: segment.color }"
                  ></div>
                </div>
                <span class="stacked-label">{{ region.name }}</span>
              </div>
            </div>
            <div class="stacked-legend">
              <span v-for="legend in regionLegend" :key="legend.label" class="legend-item">
                <span class="legend-dot" :style="{ backgroundColor: legend.color }"></span>
                {{ legend.label }}
              </span>
            </div>
          </div>
        </div>

        <div class="chart-grid">
          <div class="chart-card">
            <h3>지역별 매출 비율</h3>
            <div class="pie-area">
              <div class="pie"></div>
              <div class="pie-labels">
                <span v-for="item in regionPieLabels" :key="item.label" :style="{ color: item.color }">
                  {{ item.label }}: {{ item.value }}
                </span>
              </div>
            </div>
          </div>
          <div class="chart-card">
            <h3>지역별 계약건수</h3>
            <div class="bar-chart purple">
              <div v-for="bar in regionCountBars" :key="bar.label" class="bar-column">
                <div class="bar" :style="{ height: bar.height }"></div>
                <span>{{ bar.label }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="table-card">
          <h3>지역별 상세 현황</h3>
          <div class="table-wrapper">
            <table class="detail-table">
              <thead>
                <tr>
                  <th>순위</th>
                  <th>지역</th>
                  <th>물품+3자단가</th>
                  <th>용역</th>
                  <th>공사</th>
                  <th>민수</th>
                  <th>전체매출</th>
                  <th>계약건수</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in regionDetailRows" :key="row.rank">
                  <td>{{ row.rank }}</td>
                  <td>{{ row.region }}</td>
                  <td>{{ row.goods }}</td>
                  <td>{{ row.service }}</td>
                  <td>{{ row.construction }}</td>
                  <td>{{ row.private }}</td>
                  <td>{{ row.total }}</td>
                  <td>{{ row.count }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </section>

      <section v-if="activeTab === '순위분석'" class="section">
        <div class="rank-header">
          <h2>영역별 / 시기별 순위 분석</h2>
          <div class="rank-filters">
            <button class="filter-pill">물품+3자단가</button>
            <button class="filter-pill">전체 기간</button>
          </div>
        </div>

        <div class="rank-tabs">
          <button
            v-for="tab in rankTabs"
            :key="tab"
            class="rank-tab"
            :class="{ active: activeRankTab === tab }"
            @click="activeRankTab = tab"
          >
            {{ tab }}
          </button>
        </div>

        <div class="rank-card">
          <div class="rank-card-title">매출액 TOP 10</div>
          <ul class="rank-list">
            <li v-for="item in rankTopItems" :key="item.rank" class="rank-item">
              <span class="rank-badge" :class="item.badgeClass">{{ item.rank }}</span>
              <div class="rank-info">
                <strong>{{ item.title }}</strong>
                <span>계약 {{ item.count }}건</span>
              </div>
              <span class="rank-amount">{{ item.amount }}</span>
            </li>
          </ul>
        </div>

        <div class="table-card">
          <h3>종합 순위표</h3>
          <div class="table-wrapper">
            <table class="detail-table">
              <thead>
                <tr>
                  <th>순위</th>
                  <th>제품/서비스명</th>
                  <th>총 매출액</th>
                  <th>계약건수</th>
                  <th>평균 단가</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in rankSummaryRows" :key="row.rank">
                  <td>
                    <span class="rank-badge small" :class="row.badgeClass">{{ row.rank }}</span>
                  </td>
                  <td>{{ row.name }}</td>
                  <td>{{ row.sales }}</td>
                  <td>{{ row.count }}</td>
                  <td>{{ row.avg }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </section>

      <section v-if="activeTab === '우수제품'" class="section">
        <h2 class="section-title">우수제품 보유 현황</h2>

        <div class="summary-cards">
          <div class="summary-card">
            <p class="summary-label">전체 우수제품</p>
            <p class="summary-value blue">9개</p>
          </div>
          <div class="summary-card">
            <p class="summary-label">보유 업체</p>
            <p class="summary-value green">5개사</p>
          </div>
          <div class="summary-card">
            <p class="summary-label">동일제품 보유 경쟁사</p>
            <p class="summary-value orange">3개사</p>
          </div>
        </div>

        <div class="chart-grid">
          <div class="chart-card">
            <h3>지역별 우수제품 보유 현황</h3>
            <div class="info-list">
              <div v-for="item in excellentByRegion" :key="item.region" class="info-item">
                <div class="info-left">
                  <span class="info-icon blue">📍</span>
                  <div>
                    <strong>{{ item.region }}</strong>
                    <p>{{ item.company }}</p>
                  </div>
                </div>
                <span class="count-pill">{{ item.count }}개</span>
              </div>
            </div>
          </div>
          <div class="chart-card">
            <h3>업체별 우수제품 보유 현황</h3>
            <div class="info-list">
              <div v-for="item in excellentByCompany" :key="item.company" class="info-item">
                <div class="info-left">
                  <span class="info-icon green">🏢</span>
                  <div>
                    <strong>{{ item.company }}</strong>
                    <p>{{ item.items }}</p>
                  </div>
                </div>
                <div class="pill-group">
                  <span class="soft-pill">{{ item.region }}</span>
                  <span class="count-pill">{{ item.count }}개</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="table-card">
          <h3>탑오피스/정보가구와 동일 제품 보유 업체</h3>
          <div class="alert-list">
            <div v-for="item in excellentAlerts" :key="item.company" class="alert-card" :class="item.statusClass">
              <div class="alert-header">
                <div class="alert-title">
                  <span class="info-icon red">🏢</span>
                  <strong>{{ item.company }}</strong>
                  <span class="soft-pill">{{ item.region }}</span>
                </div>
                <span class="status-pill" :class="item.statusClass">{{ item.status }}</span>
              </div>
              <div class="alert-body">
                <div>제품: {{ item.product }}</div>
                <div>취득: {{ item.start }} · 만료: {{ item.end }}</div>
              </div>
            </div>
          </div>
        </div>

        <div class="table-card">
          <h3>전체 우수제품 상세 현황</h3>
          <div class="table-wrapper">
            <table class="detail-table">
              <thead>
                <tr>
                  <th>제품코드</th>
                  <th>제품명</th>
                  <th>업체명</th>
                  <th>지역</th>
                  <th>취득일자</th>
                  <th>유효기간</th>
                  <th>상태</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in excellentDetailRows" :key="row.code + row.company">
                  <td>{{ row.code }}</td>
                  <td>{{ row.name }}</td>
                  <td>{{ row.company }}</td>
                  <td>{{ row.region }}</td>
                  <td>{{ row.start }}</td>
                  <td>{{ row.end }}</td>
                  <td><span class="status-pill" :class="row.statusClass">{{ row.status }}</span></td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </section>

      <section v-if="activeTab === '민수관리'" class="section">
        <h2 class="section-title">민수 계약 관리</h2>
        <div class="private-header">
          <span>조달시장이 아닌 민수 계약 관리</span>
          <button class="add-button">
            <span class="plus">＋</span>
            민수 계약 추가
          </button>
        </div>

        <div class="table-wrapper">
          <table class="detail-table private-table">
            <thead>
              <tr>
                <th>제품명</th>
                <th>고객사</th>
                <th>지역</th>
                <th>계약금액</th>
                <th>수량</th>
                <th>계약일자</th>
                <th>연차</th>
                <th>작업</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="row in privateRows"
                :key="row.id"
                :class="{ highlight: row.highlight }"
              >
                <td>
                  <span class="row-title">{{ row.product }}</span>
                  <span v-if="row.linked" class="link-pill">↩</span>
                </td>
                <td>{{ row.client }}</td>
                <td>{{ row.region }}</td>
                <td>{{ row.amount }}</td>
                <td>{{ row.qty }}</td>
                <td>{{ row.date }}</td>
                <td class="year-cell">{{ row.year }}</td>
                <td>
                  <div class="action-buttons">
                    <button class="icon-btn">✎</button>
                    <button class="icon-btn danger">🗑</button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </div>
  </LegacySidebarLayout>
</template>

<script setup>
import LegacySidebarLayout from './components/LegacySidebarLayout.vue';

import { ref, onMounted } from 'vue';

const isYearSeparated = ref(true);
const activeTab = ref('시장현황');

const tabs = [
  { label: '시장현황', icon: '📊' },
  { label: '수요기관별', icon: '🏛️' },
  { label: '지역별', icon: '📍' },
  { label: '순위분석', icon: '🏆' },
  { label: '우수제품', icon: '🎖️' },
  { label: '민수관리', icon: '🗂️' },
];

const contractCards = ref([
  {
    id: 1,
    title: '컴퓨터 장비 일괄구매',
    category: '물품+3자단가',
    categoryClass: 'badge-blue',
    org: '행정안전부 | 서울',
    firstContract: '2020-03-15',
    total: '50.0억원',
    years: 2,
    tintClass: 'tint-blue',
    yearsBreakdown: [
      { label: '1차년도', date: '2020-03-15', amount: '25.0억원' },
      { label: '2차년도', date: '2021-03-15', amount: '25.0억원' },
    ],
  },
  {
    id: 2,
    title: '시설물 유지관리 용역',
    category: '용역',
    categoryClass: 'badge-dark',
    org: '국토교통부 | 경기',
    firstContract: '2023-06-01',
    total: '12.0억원',
    years: 3,
    tintClass: 'tint-gray',
    yearsBreakdown: [
      { label: '1차년도', date: '2023-06-01', amount: '4.0억원' },
      { label: '2차년도', date: '2024-06-01', amount: '4.0억원' },
      { label: '3차년도', date: '2025-06-01', amount: '4.0억원' },
    ],
  },
  {
    id: 3,
    title: '광역도로 건설',
    category: '공사',
    categoryClass: 'badge-green',
    org: '국토교통부 | 충북',
    firstContract: '2024-09-01',
    total: '30.0억원',
    years: 2,
    tintClass: 'tint-blue',
    yearsBreakdown: [
      { label: '1차년도', date: '2024-09-01', amount: '15.0억원' },
      { label: '2차년도', date: '2025-09-01', amount: '15.0억원' },
    ],
  },
  {
    id: 4,
    title: '사무기기 일괄납품',
    category: '민수',
    categoryClass: 'badge-purple',
    org: '현대중공업 | 울산',
    firstContract: '2024-11-10',
    total: '6.0억원',
    years: 2,
    tintClass: 'tint-purple',
    yearsBreakdown: [
      { label: '1차년도', date: '2024-11-10', amount: '3.0억원' },
      { label: '2차년도', date: '2025-11-10', amount: '3.0억원' },
    ],
  },
]);

const summaryStats = ref([
  { label: '전체 매출액', value: '144.4억', colorClass: 'blue' },
  { label: '전체 계약건수', value: '29건', colorClass: 'green' },
  { label: '평균 계약금액', value: '5.0억', colorClass: 'orange' },
  { label: '우수제품 비율', value: '98.5%', colorClass: 'purple' },
]);

const revenueBars = ref([
  { label: '물품+3자단가', height: '70%' },
  { label: '용역', height: '25%' },
  { label: '공사', height: '80%' },
  { label: '민수', height: '15%' },
]);

const countBars = ref([
  { label: '물품+3자단가', height: '70%' },
  { label: '용역', height: '90%' },
  { label: '공사', height: '75%' },
  { label: '민수', height: '78%' },
]);

const detailItems = ref([
  { label: '물품+3자단가', count: 7, amount: '55.0억', color: '#3498db' },
  { label: '용역', count: 8, amount: '17.1억', color: '#2ecc71' },
  { label: '공사', count: 7, amount: '64.5억', color: '#f39c12' },
  { label: '민수', count: 7, amount: '8.0억', color: '#e74c3c' },
]);

const agencyTopSales = ref([
  { label: '행정안전부', width: '88%' },
  { label: '과학기술정보통신부', width: '8%' },
  { label: '국방부', width: '6%' },
  { label: '교육부', width: '4%' },
  { label: '환경부', width: '3%' },
]);

const agencyCountBars = ref([
  { label: '행정안전부', height: '90%' },
  { label: '과학기술정보통신부', height: '35%' },
  { label: '국방부', height: '35%' },
  { label: '교육부', height: '35%' },
  { label: '환경부', height: '35%' },
]);

const agencyAvgBars = ref([
  { label: '행정안전부', height: '90%' },
  { label: '과학기술정보통신부', height: '18%' },
  { label: '국방부', height: '10%' },
  { label: '교육부', height: '6%' },
  { label: '환경부', height: '6%' },
]);

const agencyDetailRows = ref([
  { rank: 1, name: '행정안전부', sales: '50.8억', count: '3건', avg: '16.9억' },
  { rank: 2, name: '과학기술정보통신부', sales: '2.0억', count: '1건', avg: '2.0억' },
  { rank: 3, name: '국방부', sales: '1.2억', count: '1건', avg: '1.2억' },
  { rank: 4, name: '교육부', sales: '5000만', count: '1건', avg: '5000만' },
  { rank: 5, name: '환경부', sales: '4500만', count: '1건', avg: '4500만' },
]);

const regionLegend = ref([
  { label: '공사', color: '#f39c12' },
  { label: '물품+3자단가', color: '#3f7cf1' },
  { label: '민수', color: '#e74c3c' },
  { label: '용역', color: '#2ecc71' },
]);

const regionStackedBars = ref([
  {
    name: '서울',
    segments: [
      { label: '물품+3자단가', height: '75%', color: '#3f7cf1' },
      { label: '용역', height: '6%', color: '#2ecc71' },
      { label: '민수', height: '4%', color: '#e74c3c' },
      { label: '공사', height: '10%', color: '#f39c12' },
    ],
  },
  {
    name: '충북',
    segments: [
      { label: '공사', height: '60%', color: '#f39c12' },
      { label: '민수', height: '4%', color: '#e74c3c' },
    ],
  },
  {
    name: '경기',
    segments: [
      { label: '물품+3자단가', height: '6%', color: '#3f7cf1' },
      { label: '용역', height: '20%', color: '#2ecc71' },
      { label: '민수', height: '8%', color: '#e74c3c' },
      { label: '공사', height: '12%', color: '#f39c12' },
    ],
  },
  {
    name: '충남',
    segments: [
      { label: '공사', height: '25%', color: '#f39c12' },
    ],
  },
  {
    name: '울산',
    segments: [
      { label: '민수', height: '15%', color: '#e74c3c' },
    ],
  },
  {
    name: '강원',
    segments: [
      { label: '공사', height: '10%', color: '#f39c12' },
    ],
  },
  {
    name: '전북',
    segments: [
      { label: '공사', height: '7%', color: '#f39c12' },
    ],
  },
  {
    name: '대전',
    segments: [
      { label: '물품+3자단가', height: '6%', color: '#3f7cf1' },
    ],
  },
]);

const regionPieLabels = ref([
  { label: '서울', value: '40.2%', color: '#3f7cf1' },
  { label: '충북', value: '20.8%', color: '#2ecc71' },
  { label: '경기', value: '15.4%', color: '#f39c12' },
  { label: '충남', value: '8.3%', color: '#e74c3c' },
  { label: '울산', value: '5.1%', color: '#9b59b6' },
  { label: '강원', value: '4.2%', color: '#1abc9c' },
  { label: '전북', value: '2.4%', color: '#34495e' },
  { label: '대전', value: '1.7%', color: '#7f8c8d' },
  { label: '대구', value: '1.0%', color: '#16a085' },
  { label: '부산', value: '0.9%', color: '#8e44ad' },
]);

const regionCountBars = ref([
  { label: '서울', height: '85%' },
  { label: '충북', height: '25%' },
  { label: '경기', height: '85%' },
  { label: '충남', height: '12%' },
  { label: '울산', height: '45%' },
  { label: '강원', height: '12%' },
  { label: '전북', height: '12%' },
  { label: '대전', height: '12%' },
]);

const regionDetailRows = ref([
  { rank: 1, region: '서울', goods: '51.3억', service: '1.5억', construction: '5.0억', private: '2500만', total: '58.0억', count: '7건' },
  { rank: 2, region: '충북', goods: '0만', service: '0만', construction: '30.0억', private: '0만', total: '30.0억', count: '2건' },
  { rank: 3, region: '경기', goods: '1.2억', service: '12.0억', construction: '8.0억', private: '1.1억', total: '22.3억', count: '7건' },
  { rank: 4, region: '충남', goods: '0만', service: '0만', construction: '12.0억', private: '0만', total: '12.0억', count: '1건' },
  { rank: 5, region: '울산', goods: '0만', service: '1.2억', construction: '0만', private: '6.2억', total: '7.4억', count: '4건' },
  { rank: 6, region: '강원', goods: '0만', service: '0만', construction: '6.0억', private: '0만', total: '6.0억', count: '1건' },
  { rank: 7, region: '전북', goods: '0만', service: '0만', construction: '3.5억', private: '0만', total: '3.5억', count: '1건' },
  { rank: 8, region: '대전', goods: '2.0억', service: '0만', construction: '0만', private: '0만', total: '2.0억', count: '1건' },
  { rank: 9, region: '대구', goods: '0만', service: '9500만', construction: '0만', private: '0만', total: '9500만', count: '1건' },
  { rank: 10, region: '부산', goods: '0만', service: '8000만', construction: '0만', private: '0만', total: '8000만', count: '1건' },
  { rank: 11, region: '광주', goods: '0만', service: '6000만', construction: '0만', private: '0만', total: '6000만', count: '1건' },
  { rank: 12, region: '인천', goods: '4500만', service: '0만', construction: '0만', private: '0만', total: '4500만', count: '1건' },
  { rank: 13, region: '경북', goods: '0만', service: '0만', construction: '0만', private: '4000만', total: '4000만', count: '1건' },
]);

const activeRankTab = ref('매출액 순위');
const rankTabs = ['매출액 순위', '계약건수 순위', '평균단가 순위'];

const rankTopItems = ref([
  { rank: 1, title: '컴퓨터 장비 일괄구매', count: 2, amount: '50.0억원', badgeClass: 'gold' },
  { rank: 2, title: '노트북', count: 1, amount: '2.0억원', badgeClass: 'silver' },
  { rank: 3, title: '프린터', count: 1, amount: '1.2억원', badgeClass: 'bronze' },
  { rank: 4, title: '책상', count: 1, amount: '8000만원', badgeClass: 'blue' },
  { rank: 5, title: '사무용 의자', count: 1, amount: '5000만원', badgeClass: 'blue' },
  { rank: 6, title: 'LED 조명', count: 1, amount: '4500만원', badgeClass: 'blue' },
]);

const rankSummaryRows = ref([
  { rank: 1, name: '컴퓨터 장비 일괄구매', sales: '50.0억원', count: '2건', avg: '25.0억원', badgeClass: 'gold' },
  { rank: 2, name: '노트북', sales: '2.0억원', count: '1건', avg: '2.0억원', badgeClass: 'silver' },
  { rank: 3, name: '프린터', sales: '1.2억원', count: '1건', avg: '1.2억원', badgeClass: 'bronze' },
  { rank: 4, name: '책상', sales: '8000만원', count: '1건', avg: '8000만원', badgeClass: 'blue' },
  { rank: 5, name: '사무용 의자', sales: '5000만원', count: '1건', avg: '5000만원', badgeClass: 'blue' },
  { rank: 6, name: 'LED 조명', sales: '4500만원', count: '1건', avg: '4500만원', badgeClass: 'blue' },
]);

const excellentByRegion = ref([
  { region: '서울', company: '탑오피스', count: 3 },
  { region: '경기', company: '정보가구', count: 3 },
  { region: '부산', company: '한국가구', count: 1 },
  { region: '인천', company: '밝은조명', count: 1 },
  { region: '대전', company: '테크솔루션', count: 1 },
]);

const excellentByCompany = ref([
  { company: '탑오피스', items: '사무용 의자, 프린터, LED 조명', region: '서울', count: 3 },
  { company: '정보가구', items: '사무용 의자, 프린터, 노트북', region: '경기', count: 3 },
  { company: '한국가구', items: '사무용 의자', region: '부산', count: 1 },
  { company: '밝은조명', items: 'LED 조명', region: '인천', count: 1 },
  { company: '테크솔루션', items: '노트북', region: '대전', count: 1 },
]);

const excellentAlerts = ref([
  { company: '한국가구', region: '부산', product: '사무용 의자 (P001)', start: '2024-03-10', end: '2026-03-09', status: '만료임박', statusClass: 'warning' },
  { company: '밝은조명', region: '인천', product: 'LED 조명 (P004)', start: '2024-07-22', end: '2026-07-21', status: '유효', statusClass: 'success' },
  { company: '테크솔루션', region: '대전', product: '노트북 (P005)', start: '2024-09-10', end: '2026-09-09', status: '유효', statusClass: 'success' },
]);

const excellentDetailRows = ref([
  { code: 'P001', name: '사무용 의자', company: '탑오피스', region: '서울', start: '2024-01-15', end: '2026-01-14', status: '만료', statusClass: 'danger' },
  { code: 'P001', name: '사무용 의자', company: '정보가구', region: '경기', start: '2024-02-20', end: '2026-02-19', status: '만료임박', statusClass: 'warning' },
  { code: 'P001', name: '사무용 의자', company: '한국가구', region: '부산', start: '2024-03-10', end: '2026-03-09', status: '만료임박', statusClass: 'warning' },
  { code: 'P002', name: '프린터', company: '탑오피스', region: '서울', start: '2024-04-05', end: '2026-04-04', status: '만료임박', statusClass: 'warning' },
  { code: 'P002', name: '프린터', company: '정보가구', region: '경기', start: '2024-05-12', end: '2026-05-11', status: '유효', statusClass: 'success' },
  { code: 'P004', name: 'LED 조명', company: '탑오피스', region: '서울', start: '2024-06-18', end: '2026-06-17', status: '유효', statusClass: 'success' },
  { code: 'P004', name: 'LED 조명', company: '밝은조명', region: '인천', start: '2024-07-22', end: '2026-07-21', status: '유효', statusClass: 'success' },
  { code: 'P005', name: '노트북', company: '정보가구', region: '경기', start: '2024-08-15', end: '2026-08-14', status: '유효', statusClass: 'success' },
  { code: 'P005', name: '노트북', company: '테크솔루션', region: '대전', start: '2024-09-10', end: '2026-09-09', status: '유효', statusClass: 'success' },
]);

const privateRows = ref([
  { id: 1, product: '사무용 의자', client: '삼성전자', region: '경기', amount: '30,000,000원', qty: '300', date: '2025-01-25', year: '-', highlight: false },
  { id: 2, product: '책상', client: 'LG전자', region: '서울', amount: '25,000,000원', qty: '150', date: '2025-02-10', year: '-', highlight: false },
  { id: 3, product: 'LED 조명', client: '현대자동차', region: '울산', amount: '20,000,000원', qty: '400', date: '2025-03-05', year: '-', highlight: false },
  { id: 4, product: '노트북', client: 'SK하이닉스', region: '경기', amount: '80,000,000원', qty: '60', date: '2025-04-15', year: '-', highlight: false },
  { id: 5, product: '프린터', client: '포스코', region: '경북', amount: '40,000,000원', qty: '80', date: '2025-05-20', year: '-', highlight: false },
  { id: 6, product: '사무기기 일괄납품', client: '현대중공업', region: '울산', amount: '300,000,000원', qty: '500', date: '2024-11-10', year: '1차년도', highlight: true, linked: true },
  { id: 7, product: '사무기기 일괄납품', client: '현대중공업', region: '울산', amount: '300,000,000원', qty: '500', date: '2025-11-10', year: '2차년도', highlight: true, linked: true },
]);

const loadDashboardData = async () => {
  // TODO: Replace with API calls when endpoints are ready.
  // Example:
  // const response = await axios.get('/api/dashboard');
  // contractCards.value = response.data.contractCards;
};

// TODO (report data integration)
// 1) /api/report/* endpoint별 응답 스키마 확정 및 매핑
// 2) 탭 전환 시 필요한 데이터만 로딩 (캐시/재사용 전략 포함)
// 3) 로딩/에러/빈 데이터 상태 UI 추가
// 4) 차트 데이터 비율/축 값 계산 로직 분리 (util 또는 composable)
// 5) 필터(기간/카테고리) 입력값을 API 파라미터로 연동

onMounted(() => {
  loadDashboardData();
});
</script>

<style scoped>
.dashboard-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.page-header {
  padding: 16px 0 6px;
  border-bottom: 1px solid #1f2937;
  background: #1f2937;
  border-radius: 12px;
  padding-left: 16px;
  padding-right: 16px;
}

.title-block h1 {
  margin: 0;
  font-size: 28px;
  font-weight: 800;
  color: #f9fafb;
}

.title-block p {
  margin: 6px 0 8px;
  color: #e5e7eb;
  font-size: 14px;
}

.title-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo-dot {
  width: 22px;
  height: 22px;
  border-radius: 8px;
  border: 2px solid #93c5fd;
  background: #0f172a;
}

.info-banner {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #edf4ff;
  border-radius: 12px;
  padding: 16px;
  border: 1px solid #d6e5ff;
  gap: 16px;
}

.banner-left {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.info-icon {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  border: 2px solid #3f6ff0;
  color: #3f6ff0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
}

.banner-text strong {
  display: block;
  color: #2c3e50;
}

.banner-text p {
  margin: 4px 0 6px;
  color: #4b5b73;
}

.example-pill {
  display: inline-block;
  background: #dbe9ff;
  color: #3f6ff0;
  padding: 6px 10px;
  border-radius: 8px;
  font-size: 12px;
}

.banner-right {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #2c3e50;
}

.switch {
  position: relative;
  display: inline-block;
  width: 44px;
  height: 24px;
}

.switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #cfd7e6;
  transition: 0.2s;
  border-radius: 999px;
}

.slider:before {
  position: absolute;
  content: '';
  height: 18px;
  width: 18px;
  left: 3px;
  bottom: 3px;
  background-color: white;
  transition: 0.2s;
  border-radius: 50%;
}

.switch input:checked + .slider {
  background-color: #3f6ff0;
}

.switch input:checked + .slider:before {
  transform: translateX(20px);
}

.tab-bar {
  display: flex;
  gap: 10px;
  background: #f3f3f6;
  padding: 10px;
  border-radius: 14px;
}

.tab-button {
  flex: 1;
  border: none;
  background: transparent;
  padding: 10px;
  border-radius: 12px;
  color: #2c3e50;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  cursor: pointer;
}

.tab-button.active {
  background: white;
  border: 1px solid #e0e0e0;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.06);
}

.tab-icon {
  font-size: 18px;
}

.section {
  background: white;
  border-radius: 14px;
  padding: 16px;
  border: 1px solid #eee;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 16px;
  font-size: 16px;
  color: #2c3e50;
}

.contract-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.contract-card {
  border-radius: 14px;
  border: 1px solid #dfe7f2;
  padding: 16px;
  background: #f7fbff;
}

.contract-card.tint-gray {
  background: #f7f7f9;
}

.contract-card.tint-purple {
  background: #f8f1ff;
  border-color: #e5d7ff;
}

.card-header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  border-bottom: 1px solid #dfe7f2;
  padding-bottom: 12px;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.card-subtitle,
.card-meta {
  color: #6b7a99;
  margin-top: 6px;
}

.category-pill {
  font-size: 12px;
  padding: 4px 8px;
  border-radius: 999px;
  color: white;
}

.badge-blue {
  background: #2f6fff;
}

.badge-dark {
  background: #222222;
}

.badge-green {
  background: #2ecc71;
}

.badge-purple {
  background: #8e44ad;
}

.card-total {
  text-align: right;
  color: #2f4aa5;
}

.total-amount {
  font-weight: 700;
}

.year-rows {
  margin-top: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.year-row {
  display: grid;
  grid-template-columns: 90px 1fr auto;
  align-items: center;
  gap: 12px;
  background: white;
  border-radius: 10px;
  padding: 10px 12px;
  border: 1px solid #edf1f8;
}

.year-pill {
  background: #e6eefb;
  color: #3f6ff0;
  padding: 4px 8px;
  border-radius: 8px;
  font-size: 12px;
  text-align: center;
}

.year-amount {
  font-weight: 600;
  color: #2c3e50;
}

.summary-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 16px;
}

.summary-card {
  border: 1px solid #eee;
  border-radius: 12px;
  padding: 16px;
}

.summary-label {
  margin: 0;
  color: #7f8c8d;
}

.summary-value {
  margin: 10px 0 0;
  font-size: 20px;
  font-weight: 700;
}

.summary-value.blue {
  color: #3f6ff0;
}

.summary-value.green {
  color: #2ecc71;
}

.summary-value.orange {
  color: #f39c12;
}

.summary-value.purple {
  color: #9b59b6;
}

.chart-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-top: 16px;
}

.chart-card {
  border: 1px solid #eee;
  border-radius: 12px;
  padding: 16px;
}

.chart-card.wide {
  padding: 20px;
}

.chart-card h3 {
  margin: 0 0 12px;
  font-size: 14px;
}

.bar-chart {
  display: flex;
  gap: 12px;
  align-items: flex-end;
  height: 180px;
}

.bar-column {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.bar {
  width: 100%;
  background: #3f7cf1;
  border-radius: 8px 8px 0 0;
}

.bar-chart.green .bar {
  background: #34b67a;
}

.bar-chart.orange .bar {
  background: #f39c12;
}

.bar-chart.purple .bar {
  background: #8e5cf6;
}

.stacked-chart {
  background: #ffffff;
  border: 1px solid #eee;
  border-radius: 12px;
  padding: 16px;
}

.stacked-grid {
  display: flex;
  justify-content: space-between;
  color: #9aa6b2;
  font-size: 12px;
  margin-left: 20px;
}

.stacked-bars {
  display: flex;
  gap: 16px;
  align-items: flex-end;
  height: 220px;
  margin-top: 12px;
}

.stacked-column {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.stacked-track {
  width: 100%;
  height: 180px;
  display: flex;
  flex-direction: column-reverse;
  border-radius: 10px;
  overflow: hidden;
  background: #f5f7fb;
  border: 1px solid #edf1f8;
}

.stacked-segment {
  width: 100%;
}

.stacked-label {
  font-size: 12px;
  color: #6b7a99;
}

.stacked-legend {
  display: flex;
  gap: 16px;
  justify-content: center;
  margin-top: 12px;
  font-size: 12px;
  color: #6b7a99;
}

.pie-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.pie-labels {
  display: flex;
  flex-wrap: wrap;
  gap: 10px 16px;
  font-size: 12px;
  color: #6b7a99;
  justify-content: center;
}

.hbar-chart {
  background: #ffffff;
  border: 1px solid #eee;
  border-radius: 12px;
  padding: 16px;
}

.hbar-axis {
  display: flex;
  justify-content: space-between;
  color: #9aa6b2;
  font-size: 12px;
  margin-left: 140px;
}

.hbar-list {
  margin-top: 12px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.hbar-row {
  display: grid;
  grid-template-columns: 120px 1fr;
  gap: 20px;
  align-items: center;
}

.hbar-label {
  color: #4b5b73;
  font-size: 13px;
}

.hbar-track {
  width: 100%;
  height: 22px;
  border-radius: 6px;
  background: #ecf1f8;
  position: relative;
}

.hbar-fill {
  height: 100%;
  border-radius: 6px;
  background: #3f7cf1;
}

.hbar-legend {
  text-align: center;
  color: #3f7cf1;
  font-size: 12px;
  margin-top: 10px;
}

.table-card {
  border: 1px solid #eee;
  border-radius: 12px;
  padding: 16px;
  margin-top: 16px;
}

.table-card h3 {
  margin: 0 0 12px;
  font-size: 14px;
}

.detail-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}

.detail-table th,
.detail-table td {
  text-align: left;
  padding: 12px 8px;
  border-bottom: 1px solid #eee;
}

.detail-table th {
  color: #7f8c8d;
  font-weight: 600;
}

.rank-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.rank-header h2 {
  margin: 0;
  font-size: 16px;
}

.rank-filters {
  display: flex;
  gap: 10px;
}

.filter-pill {
  padding: 8px 14px;
  border-radius: 10px;
  border: 1px solid #e0e0e0;
  background: #f7f7f9;
  color: #2c3e50;
  font-size: 12px;
}

.rank-tabs {
  display: flex;
  background: #f3f3f6;
  border-radius: 12px;
  padding: 6px;
  gap: 6px;
  margin-bottom: 16px;
}

.rank-tab {
  flex: 1;
  border: none;
  background: transparent;
  padding: 8px;
  border-radius: 10px;
  font-size: 12px;
  cursor: pointer;
}

.rank-tab.active {
  background: white;
  border: 1px solid #e0e0e0;
}

.rank-card {
  border: 1px solid #eee;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 16px;
}

.rank-card-title {
  font-weight: 600;
  margin-bottom: 12px;
}

.rank-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.rank-item {
  display: grid;
  grid-template-columns: 36px 1fr auto;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f7f9fc;
  border-radius: 12px;
}

.rank-badge {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  color: white;
}

.rank-badge.small {
  width: 28px;
  height: 28px;
  font-size: 12px;
}

.rank-badge.gold {
  background: #f1c40f;
}

.rank-badge.silver {
  background: #bdc3c7;
}

.rank-badge.bronze {
  background: #e67e22;
}

.rank-badge.blue {
  background: #3f7cf1;
}

.rank-info strong {
  display: block;
}

.rank-info span {
  color: #6b7a99;
  font-size: 12px;
}

.rank-amount {
  font-weight: 600;
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #f7f9fc;
  border-radius: 12px;
  padding: 12px;
}

.info-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.info-icon {
  width: 28px;
  height: 28px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  background: #e9eef6;
}

.info-icon.blue {
  color: #3f7cf1;
}

.info-icon.green {
  color: #2ecc71;
}

.info-icon.red {
  color: #e74c3c;
}

.info-item p {
  margin: 6px 0 0;
  color: #6b7a99;
  font-size: 12px;
}

.count-pill {
  background: #111827;
  color: #fff;
  padding: 6px 10px;
  border-radius: 10px;
  font-size: 12px;
}

.soft-pill {
  background: #eef1f6;
  color: #2c3e50;
  padding: 6px 10px;
  border-radius: 10px;
  font-size: 12px;
}

.pill-group {
  display: flex;
  gap: 8px;
  align-items: center;
}

.alert-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.alert-card {
  border-radius: 14px;
  padding: 16px;
  border: 1px solid #f0d6d6;
  background: #fff2f2;
}

.alert-card.success {
  background: #f3f9f5;
  border-color: #d9eee0;
}

.alert-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.alert-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.alert-body {
  color: #6b7a99;
  font-size: 13px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.status-pill.danger {
  background: #e74c3c;
  color: #fff;
}

.status-pill.warning {
  background: #f39c12;
  color: #fff;
}

.private-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  color: #6b7a99;
  font-size: 13px;
}

.add-button {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: #111827;
  color: #fff;
  border: none;
  padding: 8px 14px;
  border-radius: 10px;
  font-size: 12px;
}

.add-button .plus {
  font-size: 14px;
}

.private-table th,
.private-table td {
  text-align: center;
}

.private-table td:first-child,
.private-table th:first-child {
  text-align: left;
}

.row-title {
  font-weight: 600;
}

.link-pill {
  margin-left: 6px;
  font-size: 11px;
  background: #e6eefb;
  color: #3f7cf1;
  padding: 2px 6px;
  border-radius: 8px;
}

.year-cell {
  color: #3f7cf1;
  font-weight: 600;
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 8px;
}

.icon-btn {
  border: 1px solid #e0e0e0;
  background: #fff;
  border-radius: 8px;
  padding: 4px 6px;
  font-size: 12px;
}

.icon-btn.danger {
  color: #e74c3c;
  border-color: #f3c9c9;
}

tr.highlight {
  background: #eef5ff;
}
.donut-wrap {
  position: relative;
  height: 180px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.pie {
  width: 140px;
  height: 140px;
  border-radius: 50%;
  background: conic-gradient(
    #3f7cf1 0 40%,
    #2ecc71 40% 61%,
    #f39c12 61% 76%,
    #e74c3c 76% 84%,
    #9b59b6 84% 89%,
    #1abc9c 89% 93%,
    #34495e 93% 96%,
    #7f8c8d 96% 98%,
    #16a085 98% 99%,
    #8e44ad 99% 100%
  );
}

.pie-label {
  position: absolute;
  font-size: 12px;
  color: #6b7a99;
}

.pie-label.left {
  left: 10px;
}

.pie-label.right {
  right: 10px;
}

.detail-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-item {
  display: grid;
  grid-template-columns: 20px 1fr auto;
  gap: 12px;
  padding: 12px;
  border-radius: 12px;
  background: #f7f9fc;
  align-items: center;
}

.detail-item .dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.detail-text {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-text span {
  color: #6b7a99;
  font-size: 12px;
}

.detail-amount {
  background: #eef1f6;
  padding: 6px 10px;
  border-radius: 10px;
  font-weight: 600;
}

@media (max-width: 1100px) {
  .chart-grid {
    grid-template-columns: 1fr;
  }
  .tab-bar {
    flex-wrap: wrap;
  }
  .tab-button {
    flex: 1 1 120px;
  }
  .card-header {
    flex-direction: column;
    align-items: flex-start;
  }
  .card-total {
    text-align: left;
  }
}
</style>
