<template>
  <LegacySidebarLayout>
    <div class="dashboard-page">
      <header class="page-header">
        <div class="title-block">
          <div class="title-row">
            <span class="header-accent"></span>
            <div class="header-titles">
              <h1>조달시장 관리 시스템</h1>
              <p class="header-sub">Procurement Market Management System</p>
            </div>
          </div>
        </div>
      </header>

      <!-- 대시보드 공통 기간 필터 (모든 탭 데이터에 반영) -->
      <section class="filter-bar">
        <div class="filter-row">
          <span class="filter-label">기간</span>
          <label class="filter-radio">
            <input type="radio" v-model="dashboardFilterMode" value="year" />
            <span>연도 선택</span>
          </label>
          <select
            v-model="dashboardYear"
            class="filter-select"
            :disabled="dashboardFilterMode !== 'year'"
          >
            <option v-for="y in dashboardYearOptions" :key="y" :value="y">{{ y }}년</option>
          </select>
          <label class="filter-radio">
            <input type="radio" v-model="dashboardFilterMode" value="range" />
            <span>기간 지정</span>
          </label>
          <input
            v-model="dashboardFrom"
            type="date"
            class="filter-date"
            :disabled="dashboardFilterMode !== 'range'"
          />
          <span class="filter-sep">~</span>
          <input
            v-model="dashboardTo"
            type="date"
            class="filter-date"
            :disabled="dashboardFilterMode !== 'range'"
          />
          <button type="button" class="filter-apply" @click="applyDashboardFilter">조회</button>
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

      <!-- 탭별 조건 필터 (탭 선택에 따라 동적으로 표시) -->
      <section
        v-if="activeTab === '시장현황' || activeTab === '수요기관별' || activeTab === '지역별'"
        class="condition-filter-bar"
      >
        <span class="filter-label">데이터</span>

        <!-- 시장현황: 다중선택 체크박스 -->
        <template v-if="activeTab === '시장현황'">
          <div class="check-group">
            <label
              v-for="src in marketDataSourceOptions"
              :key="src.value"
              class="check-item"
            >
              <input type="checkbox" v-model="marketDataSources" :value="src.value" />
              <span class="check-dot" :style="{ background: src.color }"></span>
              <span>{{ src.label }}</span>
            </label>
          </div>
          <span class="condition-hint">복수 선택 시 합산 조회</span>
        </template>

        <!-- 수요기관별 / 지역별: 단일선택 세그먼트 버튼 -->
        <template v-else>
          <div class="data-source-segment" role="tablist">
            <button type="button" class="segment-btn" :class="{ active: dataSource === 'all' }" @click="dataSource = 'all'">물품+3자단가</button>
            <button type="button" class="segment-btn" :class="{ active: dataSource === 'procurement' }" @click="dataSource = 'procurement'">물품</button>
            <button type="button" class="segment-btn" :class="{ active: dataSource === 'shopping_mall' }" @click="dataSource = 'shopping_mall'">3자단가</button>
            <button type="button" class="segment-btn" :class="{ active: dataSource === 'service' }" @click="dataSource = 'service'">용역</button>
            <button type="button" class="segment-btn" :class="{ active: dataSource === 'construction' }" @click="dataSource = 'construction'">공사</button>
          </div>
        </template>
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
        <div class="section-title-row">
          <h2 class="section-title">수요기관별 물품 조달시장 분석</h2>
        </div>

        <div v-if="agencyLoading" class="loading-banner loading-banner-prominent">
          <div class="loading-spinner loading-spinner-large"></div>
          <p class="loading-text">로딩 중</p>
          <p class="loading-sub">수요기관별 집계 데이터를 불러오고 있습니다.</p>
        </div>
        <template v-else-if="!agencyLoading">
          <div v-if="agencyError" class="info-banner">
            <div class="banner-left">
              <div class="info-icon">!</div>
              <div class="banner-text">
                <strong>데이터 조회 실패</strong>
                <p>{{ agencyError }}</p>
              </div>
            </div>
          </div>
          <div v-else-if="agencyLoaded && !agencyDetailRows.length" class="info-banner">
            <div class="banner-left">
              <div class="info-icon">i</div>
              <div class="banner-text">
                <strong>데이터 없음</strong>
                <p>선택한 기간에 수요기관별 데이터가 없습니다. 기간을 바꿔 보세요.</p>
              </div>
            </div>
          </div>

          <div class="chart-card wide">
            <h3>수요기관별 물품 조달시장 (Top 10)</h3>
            <div class="hbar-chart">
              <div class="hbar-axis-row">
                <span class="hbar-axis-placeholder"></span>
                <div class="hbar-axis-ticks">
                  <span
                    v-for="(tick, i) in agencyTopSalesAxisTicks"
                    :key="i"
                    class="hbar-axis-tick"
                    >{{ tick }}</span
                  >
                </div>
              </div>
              <div class="hbar-list">
                <div
                  v-for="row in agencyTopSales"
                  :key="row.label"
                  class="hbar-row chart-hover-wrap"
                >
                  <span class="hbar-label">{{ row.label }}</span>
                  <div class="hbar-track" :title="row.tooltip">
                    <div class="hbar-fill" :style="{ width: row.width }"></div>
                    <span v-if="row.tooltip" class="chart-tooltip">{{ row.tooltip }}</span>
                  </div>
                </div>
              </div>
              <div class="hbar-legend">계약금액</div>
            </div>
          </div>

          <div class="chart-grid">
            <div class="chart-card">
              <h3>수요기관별 계약건수</h3>
              <div class="agency-chart-with-axis">
                <div class="agency-y-axis">
                  <span v-for="(tick, i) in agencyCountAxisTicks" :key="i" class="agency-y-tick">{{
                    tick
                  }}</span>
                </div>
                <div class="agency-chart-scroll">
                  <div class="bar-chart bar-chart-horizontal green">
                    <div
                      v-for="bar in agencyCountBars"
                      :key="bar.label"
                      class="bar-column-fixed chart-hover-wrap"
                    >
                      <div class="bar-value-wrap">
                        <div class="bar" :style="{ height: bar.height }" :title="bar.tooltip"></div>
                        <span v-if="bar.tooltip" class="chart-tooltip chart-tooltip-bar">{{
                          bar.tooltip
                        }}</span>
                      </div>
                      <span class="bar-label-fixed" :title="bar.label">{{ bar.label }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <div class="chart-card">
              <h3>수요기관별 평균 계약단가</h3>
              <div class="agency-chart-with-axis">
                <div class="agency-y-axis">
                  <span v-for="(tick, i) in agencyAvgAxisTicks" :key="i" class="agency-y-tick">{{
                    tick
                  }}</span>
                </div>
                <div class="agency-chart-scroll">
                  <div class="bar-chart bar-chart-horizontal orange">
                    <div
                      v-for="bar in agencyAvgBars"
                      :key="bar.label"
                      class="bar-column-fixed chart-hover-wrap"
                    >
                      <div class="bar-value-wrap">
                        <div class="bar" :style="{ height: bar.height }" :title="bar.tooltip"></div>
                        <span v-if="bar.tooltip" class="chart-tooltip chart-tooltip-bar">{{
                          bar.tooltip
                        }}</span>
                      </div>
                      <span class="bar-label-fixed" :title="bar.label">{{ bar.label }}</span>
                    </div>
                  </div>
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
        </template>
      </section>

      <section v-if="activeTab === '지역별'" class="section">
        <div class="section-title-row">
          <h2 class="section-title">지역별 조달시장 분석</h2>
        </div>

        <div class="region-category-bar">
          <button
            v-for="cat in regionCategoryTabs"
            :key="cat.value"
            type="button"
            class="region-category-btn"
            :class="{ active: regionCategoryTab === cat.value }"
            @click="regionCategoryTab = cat.value"
          >
            {{ cat.label }}
          </button>
        </div>

        <div v-if="regionLoading" class="loading-banner loading-banner-prominent">
          <div class="loading-spinner loading-spinner-large"></div>
          <p class="loading-text">로딩 중</p>
          <p class="loading-sub">지역별 물품 집계 데이터를 불러오고 있습니다.</p>
        </div>
        <template v-else-if="!regionLoading">
          <div v-if="regionError" class="info-banner">
            <div class="banner-left">
              <div class="info-icon">!</div>
              <div class="banner-text">
                <strong>데이터 조회 실패</strong>
                <p>{{ regionError }}</p>
              </div>
            </div>
          </div>
          <div
            v-else-if="regionLoaded && !regionDetailRows.length && showRegionGoodsData"
            class="info-banner"
          >
            <div class="banner-left">
              <div class="info-icon">i</div>
              <div class="banner-text">
                <strong>데이터 없음</strong>
                <p>선택한 기간에 지역별 물품 데이터가 없습니다. 기간을 바꿔 보세요.</p>
              </div>
            </div>
          </div>
          <template v-else>
            <div class="chart-card wide">
              <h3>지역별 {{ regionCategoryTab }} 매출 현황</h3>
              <div v-if="showRegionGoodsData" class="region-chart-with-axis">
                <div class="region-y-axis">
                  <span v-for="(tick, i) in regionAmountAxisTicks" :key="i" class="region-y-tick">{{
                    tick
                  }}</span>
                </div>
                <div class="region-chart-scroll">
                  <div class="bar-chart bar-chart-horizontal blue">
                    <div
                      v-for="bar in regionAmountBars"
                      :key="bar.label"
                      class="bar-column-fixed chart-hover-wrap"
                    >
                      <div class="bar-value-wrap">
                        <div class="bar" :style="{ height: bar.height }" :title="bar.tooltip"></div>
                        <span v-if="bar.tooltip" class="chart-tooltip chart-tooltip-bar">{{
                          bar.tooltip
                        }}</span>
                      </div>
                      <span class="bar-label-fixed" :title="bar.label">{{ bar.label }}</span>
                    </div>
                  </div>
                </div>
              </div>
              <div v-else class="region-placeholder">
                <p>물품 데이터만 제공됩니다. '전체' 또는 '물품' 탭을 선택하세요.</p>
              </div>
            </div>

            <div class="chart-grid">
              <div class="chart-card">
                <h3>지역별 {{ regionCategoryTab }} 매출 비율</h3>
                <div v-if="showRegionGoodsData" class="pie-area">
                  <div class="pie"></div>
                  <div class="pie-labels">
                    <span
                      v-for="item in regionPieLabels"
                      :key="item.label"
                      :style="{ color: item.color }"
                    >
                      {{ item.label }}: {{ item.value }}
                    </span>
                  </div>
                </div>
                <div v-else class="region-placeholder"><p>데이터 없음</p></div>
              </div>
              <div class="chart-card">
                <h3>지역별 {{ regionCategoryTab }} 계약건수</h3>
                <div v-if="showRegionGoodsData" class="agency-chart-with-axis">
                  <div class="agency-y-axis">
                    <span
                      v-for="(tick, i) in regionCountAxisTicks"
                      :key="i"
                      class="agency-y-tick"
                      >{{ tick }}</span
                    >
                  </div>
                  <div class="agency-chart-scroll">
                    <div class="bar-chart bar-chart-horizontal purple">
                      <div
                        v-for="bar in regionCountBars"
                        :key="bar.label"
                        class="bar-column-fixed chart-hover-wrap"
                      >
                        <div class="bar-value-wrap">
                          <div
                            class="bar"
                            :style="{ height: bar.height }"
                            :title="bar.tooltip"
                          ></div>
                          <span v-if="bar.tooltip" class="chart-tooltip chart-tooltip-bar">{{
                            bar.tooltip
                          }}</span>
                        </div>
                        <span class="bar-label-fixed">{{ bar.label }}</span>
                      </div>
                    </div>
                  </div>
                </div>
                <div v-else class="region-placeholder"><p>데이터 없음</p></div>
              </div>
            </div>

            <div class="table-card">
              <h3>지역별 {{ regionCategoryTab }} 상세 현황</h3>
              <div class="table-wrapper">
                <table class="detail-table">
                  <thead>
                    <tr>
                      <th>순위</th>
                      <th>지역</th>
                      <th>물품+3자단가</th>
                      <th>용역</th>
                      <th>공사</th>
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
                      <td>{{ row.total }}</td>
                      <td>{{ row.count }}</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </template>
        </template>
      </section>

      <section v-if="activeTab === '순위분석'" class="section">
        <div class="rank-analysis-head">
          <h2 class="rank-section-title">영역별 / 시기별 순위 분석</h2>
          <div class="rank-filter-rows">
            <div class="rank-filter-row">
              <span class="filter-label">데이터</span>
              <div class="data-source-segment" role="tablist">
                <button type="button" class="segment-btn" :class="{ active: dataSource === 'all' }" @click="dataSource = 'all'">물품+3자단가</button>
                <button type="button" class="segment-btn" :class="{ active: dataSource === 'procurement' }" @click="dataSource = 'procurement'">물품</button>
                <button type="button" class="segment-btn" :class="{ active: dataSource === 'shopping_mall' }" @click="dataSource = 'shopping_mall'">3자단가</button>
                <button type="button" class="segment-btn" :class="{ active: dataSource === 'service' }" @click="dataSource = 'service'">용역</button>
                <button type="button" class="segment-btn" :class="{ active: dataSource === 'construction' }" @click="dataSource = 'construction'">공사</button>
              </div>
            </div>
            <div class="rank-filter-row">
              <span class="filter-label">기간 기준</span>
              <div class="data-source-segment" role="tablist">
                <button type="button" class="segment-btn" :class="{ active: rankDateBasis === 'FINAL' }" @click="rankDateBasis = 'FINAL'">최종계약일</button>
                <button type="button" class="segment-btn" :class="{ active: rankDateBasis === 'FIRST' }" @click="rankDateBasis = 'FIRST'">최초계약일</button>
              </div>
            </div>
            <p class="rank-period-hint">조회 구간은 페이지 상단 <strong>기간</strong> 설정(연도·기간 지정)을 따릅니다.</p>
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
            <div
              v-for="item in excellentAlerts"
              :key="item.company"
              class="alert-card"
              :class="item.statusClass"
            >
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
                  <td>
                    <span class="status-pill" :class="row.statusClass">{{ row.status }}</span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </section>

      <section v-if="activeTab === '민수관리'" class="section">
        <h2 class="section-title">민수 계약 관리</h2>
        <div class="private-header">
          <span>
            조달 데이터에 없는 계약을 수기로 등록합니다. 분류(물품·공사·용역·쇼핑몰)를 선택하면 해당 영역 집계에 포함되며, 별도의 「민수」 구분으로 표시되지 않습니다.
          </span>
          <button type="button" class="add-button" @click="openPrivateModal">
            <span class="plus">＋</span>
            민수 계약 추가
          </button>
        </div>

        <p class="private-api-note">등록·수정·삭제는 백엔드 API와 테이블 설계 반영 후 연결됩니다.</p>

        <div class="table-wrapper">
          <table class="detail-table private-table">
            <thead>
              <tr>
                <th>유형</th>
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
              <tr v-if="privateRows.length === 0">
                <td colspan="9" class="no-data-cell">
                  등록된 계약이 없습니다. 백엔드 API 연동 후 이 목록에 표시됩니다.
                </td>
              </tr>
              <tr v-for="row in privateRows" :key="row.id" :class="{ highlight: row.highlight }">
                <td><span class="type-pill" :class="typePillClass(row.type)">{{ row.type || '물품' }}</span></td>
                <td>
                  <span class="row-title">{{ row.product }}</span>
                </td>
                <td>{{ row.client }}</td>
                <td>{{ row.region }}</td>
                <td>{{ row.amount }}</td>
                <td>{{ row.qty }}</td>
                <td>{{ row.date }}</td>
                <td class="year-cell">{{ row.year }}</td>
                <td>
                  <div class="action-buttons">
                    <button type="button" class="icon-btn" disabled title="API 연동 후 사용">✎</button>
                    <button type="button" class="icon-btn danger" disabled title="API 연동 후 사용">🗑</button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <!-- 민수 계약 추가 모달 -->
      <Teleport to="body">
        <div v-if="showPrivateModal" class="private-modal-overlay" @click.self="closePrivateModal">
          <div class="private-modal">
            <div class="private-modal-header">
              <h3>민수 계약 추가</h3>
              <button type="button" class="modal-close" aria-label="닫기" @click="closePrivateModal">×</button>
            </div>
            <form class="private-modal-body" @submit.prevent>
              <div class="form-row">
                <label>유형 <span class="required">*</span></label>
                <select v-model="privateForm.type" required class="form-input">
                  <option value="물품">물품</option>
                  <option value="공사">공사</option>
                  <option value="용역">용역</option>
                  <option value="쇼핑몰">쇼핑몰(3자단가)</option>
                </select>
              </div>
              <div class="form-row">
                <label>제품명 / 계약명 <span class="required">*</span></label>
                <input v-model="privateForm.product" type="text" required placeholder="계약명 또는 품목" class="form-input" />
              </div>
              <div class="form-row">
                <label>고객사 / 수요기관 <span class="required">*</span></label>
                <input v-model="privateForm.client" type="text" required placeholder="예: 현대중공업" class="form-input" />
              </div>
              <div class="form-row">
                <label>지역</label>
                <input v-model="privateForm.region" type="text" placeholder="예: 울산" class="form-input" />
              </div>
              <div class="form-row">
                <label>계약금액 <span class="required">*</span></label>
                <input v-model="privateForm.amount" type="text" required placeholder="예: 300,000,000" class="form-input" />
              </div>
              <div class="form-row">
                <label>수량</label>
                <input v-model="privateForm.qty" type="text" placeholder="예: 500" class="form-input" />
              </div>
              <div class="form-row">
                <label>계약일자 <span class="required">*</span></label>
                <input v-model="privateForm.date" type="date" required class="form-input" />
              </div>
              <div class="form-row">
                <label>연차</label>
                <input v-model="privateForm.year" type="text" placeholder="예: 1차년도 (선택)" class="form-input" />
              </div>
              <div class="private-modal-footer">
                <button type="button" class="btn-cancel" @click="closePrivateModal">취소</button>
                <button type="button" class="btn-submit" disabled title="API 연동 후 사용">저장 (준비 중)</button>
              </div>
            </form>
          </div>
        </div>
      </Teleport>
    </div>
  </LegacySidebarLayout>
</template>

<script setup>
import LegacySidebarLayout from './components/LegacySidebarLayout.vue'

import axios from 'axios'
import { ref, computed, onMounted, watch } from 'vue'

const activeTab = ref('시장현황')

/** 수요기관별·지역별·순위분석 데이터 소스: all | procurement | shopping_mall | service | construction */
const dataSource = ref('all')

/** 시장현황 탭: 다중선택 데이터 소스 체크박스 */
const marketDataSourceOptions = [
  { value: 'procurement', label: '물품', color: '#3b82f6' },
  { value: 'shopping_mall', label: '3자단가', color: '#8b5cf6' },
  { value: 'service', label: '용역', color: '#10b981' },
  { value: 'construction', label: '공사', color: '#f59e0b' },
]
const marketDataSources = ref(['procurement', 'shopping_mall', 'service', 'construction'])

/** 순위분석(추후 API): 수요기관별과 동일 FINAL=최종계약일 / FIRST=최초계약일 */
const rankDateBasis = ref('FINAL')

// 대시보드 공통 기간 필터 (모든 탭 데이터에 반영)
const dashboardFilterMode = ref('year') // 'year' | 'range'
const currentYear = new Date().getFullYear()
// 기본 연도: 현재 데이터가 2025년까지라 2025 고정. 추후 데이터 확보 시 currentYear 로 변경
const dashboardYear = ref(2025)
const dashboardFrom = ref(`${currentYear}-01-01`)
const dashboardTo = ref(`${currentYear}-12-31`)
const dashboardYearOptions = ref(
  Array.from({ length: currentYear - 2016 }, (_, i) => currentYear - i),
)

/** 현재 필터에 따른 from/to (yyyy-mm-dd) */
const dashboardPeriod = computed(() => {
  if (dashboardFilterMode.value === 'year') {
    const y = dashboardYear.value
    return { from: `${y}-01-01`, to: `${y}-12-31` }
  }
  return {
    from: dashboardFrom.value || `${currentYear}-01-01`,
    to: dashboardTo.value || `${currentYear}-12-31`,
  }
})

/** 필터 적용: 캐시 무효화 후 현재 탭 데이터 재조회 */
function applyDashboardFilter() {
  agencyLoaded.value = false
  regionLoaded.value = false
  if (activeTab.value === '수요기관별') {
    fetchDemandAgencyMarket()
  }
  if (activeTab.value === '지역별') {
    fetchRegionMarket()
  }
}

const tabs = [
  { label: '시장현황', icon: '📊' },
  { label: '수요기관별', icon: '🏛️' },
  { label: '지역별', icon: '📍' },
  { label: '순위분석', icon: '🏆' },
  { label: '우수제품', icon: '🎖️' },
  { label: '민수관리', icon: '🗂️' },
]

const summaryStats = ref([
  { label: '전체 매출액', value: '136.6억', colorClass: 'blue' },
  { label: '전체 계약건수', value: '22건', colorClass: 'green' },
  { label: '평균 계약금액', value: '6.2억', colorClass: 'orange' },
  { label: '우수제품 비율', value: '98.5%', colorClass: 'purple' },
])

const revenueBars = ref([
  { label: '물품+3자단가', height: '70%' },
  { label: '용역', height: '25%' },
  { label: '공사', height: '80%' },
])

const countBars = ref([
  { label: '물품+3자단가', height: '70%' },
  { label: '용역', height: '90%' },
  { label: '공사', height: '75%' },
])

const detailItems = ref([
  { label: '물품+3자단가', count: 7, amount: '55.0억', color: '#3498db' },
  { label: '용역', count: 8, amount: '17.1억', color: '#2ecc71' },
  { label: '공사', count: 7, amount: '64.5억', color: '#f39c12' },
])

const agencyTopSales = ref([
  { label: '행정안전부', width: '88%' },
  { label: '과학기술정보통신부', width: '8%' },
  { label: '국방부', width: '6%' },
  { label: '교육부', width: '4%' },
  { label: '환경부', width: '3%' },
])

const agencyCountBars = ref([
  { label: '행정안전부', height: '90%' },
  { label: '과학기술정보통신부', height: '35%' },
  { label: '국방부', height: '35%' },
  { label: '교육부', height: '35%' },
  { label: '환경부', height: '35%' },
])

const agencyAvgBars = ref([
  { label: '행정안전부', height: '90%' },
  { label: '과학기술정보통신부', height: '18%' },
  { label: '국방부', height: '10%' },
  { label: '교육부', height: '6%' },
  { label: '환경부', height: '6%' },
])

const agencyDetailRows = ref([
  { rank: 1, name: '행정안전부', sales: '50.8억', count: '3건', avg: '16.9억' },
  { rank: 2, name: '과학기술정보통신부', sales: '2.0억', count: '1건', avg: '2.0억' },
  { rank: 3, name: '국방부', sales: '1.2억', count: '1건', avg: '1.2억' },
  { rank: 4, name: '교육부', sales: '5000만', count: '1건', avg: '5000만' },
  { rank: 5, name: '환경부', sales: '4500만', count: '1건', avg: '4500만' },
])

// ===== 수요기관별 탭: 백엔드 연동 (/api/report/demand-agency-market) =====
const agencyLoaded = ref(false)
const agencyLoading = ref(false)
const agencyError = ref('')
// Top10 매출액 차트 상단 축 라벨 (데이터 max 기준 0%~100%)
const agencyTopSalesAxisTicks = ref(['0원', '0원', '0원', '0원', '0원'])
const agencyCountAxisTicks = ref(['0건', '0건', '0건', '0건', '0건'])
const agencyAvgAxisTicks = ref(['0원', '0원', '0원', '0원', '0원'])

const toNumber = (v) => {
  if (v === null || v === undefined) return 0
  if (typeof v === 'number') return Number.isFinite(v) ? v : 0
  const n = Number(String(v).replace(/,/g, '').trim())
  return Number.isFinite(n) ? n : 0
}

// 간단 금액 표기: 원 단위 가정 → 억/만 중심 표기
const formatKrwCompact = (amount) => {
  const n = toNumber(amount)
  const abs = Math.abs(n)
  const sign = n < 0 ? '-' : ''
  const EOK = 100_000_000
  const MAN = 10_000

  if (abs >= EOK) return `${sign}${(abs / EOK).toFixed(1)}억`
  if (abs >= MAN) return `${sign}${Math.round(abs / MAN)}만`
  return `${sign}${Math.round(abs)}원`
}

const pct = (value, max) => {
  const v = toNumber(value)
  const m = toNumber(max)
  if (m <= 0) return '0%'
  const p = Math.max(0, Math.min(100, (v / m) * 100))
  return `${p.toFixed(0)}%`
}

const fetchDemandAgencyMarket = async () => {
  const { from, to } = dashboardPeriod.value
  agencyLoading.value = true
  agencyError.value = ''
  try {
    const { data } = await axios.get('/api/report/demand-agency-market', {
      params: { dateBasis: 'FINAL', from, to, topN: 10, dataSource: dataSource.value },
    })

    if (!data || data.success !== true || !data.data) {
      throw new Error('API 응답 형식이 올바르지 않습니다.')
    }

    const topSales = Array.isArray(data.data.topSales) ? data.data.topSales : []
    const topCount = Array.isArray(data.data.topContractCount) ? data.data.topContractCount : []
    const topAvg = Array.isArray(data.data.topAvgAmount) ? data.data.topAvgAmount : []

    const maxSales = topSales.reduce((m, r) => Math.max(m, toNumber(r?.salesAmount)), 0)
    const maxCount = topCount.reduce((m, r) => Math.max(m, toNumber(r?.contractCount)), 0)
    const maxAvg = topAvg.reduce((m, r) => Math.max(m, toNumber(r?.avgAmount)), 0)

    // Top10 매출액 축 라벨: 0, 25%, 50%, 75%, 100% 구간에 맞는 금액 표기
    agencyTopSalesAxisTicks.value = [0, 0.25, 0.5, 0.75, 1].map((r) =>
      formatKrwCompact(maxSales * r),
    )

    // 계약건수 Y축: 상단=max → 하단=0 (5단계)
    agencyCountAxisTicks.value = [1, 0.75, 0.5, 0.25, 0].map(
      (r) => `${Math.round(maxCount * r).toLocaleString()}건`,
    )
    // 평균 계약단가 Y축: 상단=max → 하단=0 (5단계)
    agencyAvgAxisTicks.value = [1, 0.75, 0.5, 0.25, 0].map((r) => formatKrwCompact(maxAvg * r))

    // 1) 수요기관별 물품 조달시장 (Top 10) - 계약금액 가로바 (hover 시 상세금액)
    agencyTopSales.value = topSales.map((r) => ({
      label: r?.demandAgencyName ?? '-',
      width: pct(r?.salesAmount, maxSales),
      tooltip: formatKrwCompact(r?.salesAmount),
    }))

    // 2) 수요기관별 계약건수 - 세로바 (고정 높이 컨테이너 내 %로 수치 반영, hover 시 건수)
    agencyCountBars.value = topCount.map((r) => ({
      label: r?.demandAgencyName ?? '-',
      height: pct(r?.contractCount, maxCount),
      tooltip: `${toNumber(r?.contractCount).toLocaleString()}건`,
    }))

    // 3) 수요기관별 평균 계약단가 - 세로바 (hover 시 상세금액)
    agencyAvgBars.value = topAvg.map((r) => ({
      label: r?.demandAgencyName ?? '-',
      height: pct(r?.avgAmount, maxAvg),
      tooltip: formatKrwCompact(r?.avgAmount),
    }))

    // 4) 수요기관 상세 현황 - Top Sales 기준 동일 N건으로 표시 (순위/수요기관/매출액/계약건수/평균단가)
    const baseRows = topSales.length ? topSales : topCount.length ? topCount : topAvg
    agencyDetailRows.value = baseRows.map((r, idx) => ({
      rank: toNumber(r?.rank) || idx + 1,
      name: r?.demandAgencyName ?? '-',
      sales: formatKrwCompact(r?.salesAmount),
      count: `${toNumber(r?.contractCount)}건`,
      avg: formatKrwCompact(r?.avgAmount),
    }))

    agencyLoaded.value = true
  } catch (e) {
    agencyError.value = e?.message || '수요기관별 데이터 조회 실패'
    agencyTopSales.value = []
    agencyTopSalesAxisTicks.value = ['0원', '0원', '0원', '0원', '0원']
    agencyCountAxisTicks.value = ['0건', '0건', '0건', '0건', '0건']
    agencyAvgAxisTicks.value = ['0원', '0원', '0원', '0원', '0원']
    agencyCountBars.value = []
    agencyAvgBars.value = []
    agencyDetailRows.value = []
  } finally {
    agencyLoading.value = false
  }
}

const fetchRegionMarket = async () => {
  const { from, to } = dashboardPeriod.value
  regionLoading.value = true
  regionError.value = ''
  try {
    const { data } = await axios.get('/api/report/region-market', {
      params: { from, to, dataSource: dataSource.value },
    })

    if (!data || data.success !== true || !data.data) {
      throw new Error('API 응답 형식이 올바르지 않습니다.')
    }

    const regions = Array.isArray(data.data.regions) ? data.data.regions : []
    const maxAmount = regions.reduce((m, r) => Math.max(m, toNumber(r?.salesAmount)), 0)
    const maxCount = regions.reduce((m, r) => Math.max(m, toNumber(r?.contractCount)), 0)
    const totalAmount = regions.reduce((s, r) => s + toNumber(r?.salesAmount), 0)

    // 매출 현황 Y축
    regionAmountAxisTicks.value = [1, 0.75, 0.5, 0.25, 0].map((r) =>
      formatKrwCompact(maxAmount * r),
    )
    // 계약건수 Y축
    regionCountAxisTicks.value = [1, 0.75, 0.5, 0.25, 0].map(
      (r) => `${Math.round(maxCount * r).toLocaleString()}건`,
    )

    regionAmountBars.value = regions.map((r) => ({
      label: r?.region ?? '-',
      height: pct(r?.salesAmount, maxAmount),
      tooltip: formatKrwCompact(r?.salesAmount),
    }))

    // 계약건수 그래프: 좌측부터 계약건수 내림차순
    const byCount = [...regions].sort(
      (a, b) => toNumber(b?.contractCount) - toNumber(a?.contractCount),
    )
    regionCountBars.value = byCount.map((r) => ({
      label: r?.region ?? '-',
      height: pct(r?.contractCount, maxCount),
      tooltip: `${toNumber(r?.contractCount).toLocaleString()}건`,
    }))

    // 매출 비율 (pie labels)
    const PIE_COLORS = [
      '#3f7cf1',
      '#2ecc71',
      '#f39c12',
      '#e74c3c',
      '#9b59b6',
      '#1abc9c',
      '#34495e',
      '#7f8c8d',
      '#16a085',
      '#8e44ad',
    ]
    regionPieLabels.value = regions.map((r, i) => {
      const amt = toNumber(r?.salesAmount)
      const pctVal = totalAmount > 0 ? ((amt / totalAmount) * 100).toFixed(1) : '0.0'
      return {
        label: r?.region ?? '-',
        value: `${pctVal}%`,
        color: PIE_COLORS[i % PIE_COLORS.length],
      }
    })

    // 상세 현황 테이블 (현재 API는 단일 소스 집계이므로 용역/공사는 '-')
    regionDetailRows.value = regions.map((r, idx) => ({
      rank: toNumber(r?.rank) || idx + 1,
      region: r?.region ?? '-',
      goods: formatKrwCompact(r?.salesAmount),
      service: '-',
      construction: '-',
      total: formatKrwCompact(r?.salesAmount),
      count: `${toNumber(r?.contractCount)}건`,
    }))

    regionLoaded.value = true
  } catch (e) {
    regionError.value = e?.message || '지역별 물품 데이터 조회 실패'
    regionAmountAxisTicks.value = ['0원', '0원', '0원', '0원', '0원']
    regionCountAxisTicks.value = ['0건', '0건', '0건', '0건', '0건']
    regionAmountBars.value = []
    regionCountBars.value = []
    regionPieLabels.value = []
    regionDetailRows.value = []
  } finally {
    regionLoading.value = false
  }
}

const regionCategoryTabs = [
  { label: '전체', value: '전체' },
  { label: '물품', value: '물품' },
  { label: '공사', value: '공사' },
  { label: '용역', value: '용역' },
  { label: '3자단가', value: '3자단가' },
]
const regionCategoryTab = ref('전체')

// 지역별 물품 데이터: API 연동 (전체/물품 탭에서만 표시)
const regionLoading = ref(false)
const regionLoaded = ref(false)
const regionError = ref('')
const regionAmountAxisTicks = ref(['0원', '0원', '0원', '0원', '0원'])
const regionAmountBars = ref([])
const regionCountAxisTicks = ref(['0건', '0건', '0건', '0건', '0건'])
const regionCountBars = ref([])
const regionPieLabels = ref([])
const regionDetailRows = ref([])

const showRegionGoodsData = computed(
  () => regionCategoryTab.value === '전체' || regionCategoryTab.value === '물품',
)

const regionLegend = ref([
  { label: '공사', color: '#f39c12' },
  { label: '물품+3자단가', color: '#3f7cf1' },
  { label: '용역', color: '#2ecc71' },
])

const regionStackedBars = ref([
  {
    name: '서울',
    segments: [
      { label: '물품+3자단가', height: '75%', color: '#3f7cf1' },
      { label: '용역', height: '10%', color: '#2ecc71' },
      { label: '공사', height: '10%', color: '#f39c12' },
    ],
  },
  {
    name: '충북',
    segments: [{ label: '공사', height: '64%', color: '#f39c12' }],
  },
  {
    name: '경기',
    segments: [
      { label: '물품+3자단가', height: '6%', color: '#3f7cf1' },
      { label: '용역', height: '24%', color: '#2ecc71' },
      { label: '공사', height: '16%', color: '#f39c12' },
    ],
  },
  {
    name: '충남',
    segments: [{ label: '공사', height: '25%', color: '#f39c12' }],
  },
  {
    name: '울산',
    segments: [{ label: '물품+3자단가', height: '15%', color: '#3f7cf1' }],
  },
  {
    name: '강원',
    segments: [{ label: '공사', height: '10%', color: '#f39c12' }],
  },
  {
    name: '전북',
    segments: [{ label: '공사', height: '7%', color: '#f39c12' }],
  },
  {
    name: '대전',
    segments: [{ label: '물품+3자단가', height: '6%', color: '#3f7cf1' }],
  },
])

// regionPieLabels, regionCountBars, regionDetailRows are populated by fetchRegionMarket

const activeRankTab = ref('매출액 순위')
const rankTabs = ['매출액 순위', '계약건수 순위', '평균단가 순위']

const rankTopItems = ref([
  { rank: 1, title: '컴퓨터 장비 일괄구매', count: 2, amount: '50.0억원', badgeClass: 'gold' },
  { rank: 2, title: '노트북', count: 1, amount: '2.0억원', badgeClass: 'silver' },
  { rank: 3, title: '프린터', count: 1, amount: '1.2억원', badgeClass: 'bronze' },
  { rank: 4, title: '책상', count: 1, amount: '8000만원', badgeClass: 'blue' },
  { rank: 5, title: '사무용 의자', count: 1, amount: '5000만원', badgeClass: 'blue' },
  { rank: 6, title: 'LED 조명', count: 1, amount: '4500만원', badgeClass: 'blue' },
])

const rankSummaryRows = ref([
  {
    rank: 1,
    name: '컴퓨터 장비 일괄구매',
    sales: '50.0억원',
    count: '2건',
    avg: '25.0억원',
    badgeClass: 'gold',
  },
  { rank: 2, name: '노트북', sales: '2.0억원', count: '1건', avg: '2.0억원', badgeClass: 'silver' },
  { rank: 3, name: '프린터', sales: '1.2억원', count: '1건', avg: '1.2억원', badgeClass: 'bronze' },
  { rank: 4, name: '책상', sales: '8000만원', count: '1건', avg: '8000만원', badgeClass: 'blue' },
  {
    rank: 5,
    name: '사무용 의자',
    sales: '5000만원',
    count: '1건',
    avg: '5000만원',
    badgeClass: 'blue',
  },
  {
    rank: 6,
    name: 'LED 조명',
    sales: '4500만원',
    count: '1건',
    avg: '4500만원',
    badgeClass: 'blue',
  },
])

const excellentByRegion = ref([
  { region: '서울', company: '탑오피스', count: 3 },
  { region: '경기', company: '정보가구', count: 3 },
  { region: '부산', company: '한국가구', count: 1 },
  { region: '인천', company: '밝은조명', count: 1 },
  { region: '대전', company: '테크솔루션', count: 1 },
])

const excellentByCompany = ref([
  { company: '탑오피스', items: '사무용 의자, 프린터, LED 조명', region: '서울', count: 3 },
  { company: '정보가구', items: '사무용 의자, 프린터, 노트북', region: '경기', count: 3 },
  { company: '한국가구', items: '사무용 의자', region: '부산', count: 1 },
  { company: '밝은조명', items: 'LED 조명', region: '인천', count: 1 },
  { company: '테크솔루션', items: '노트북', region: '대전', count: 1 },
])

const excellentAlerts = ref([
  {
    company: '한국가구',
    region: '부산',
    product: '사무용 의자 (P001)',
    start: '2024-03-10',
    end: '2026-03-09',
    status: '만료임박',
    statusClass: 'warning',
  },
  {
    company: '밝은조명',
    region: '인천',
    product: 'LED 조명 (P004)',
    start: '2024-07-22',
    end: '2026-07-21',
    status: '유효',
    statusClass: 'success',
  },
  {
    company: '테크솔루션',
    region: '대전',
    product: '노트북 (P005)',
    start: '2024-09-10',
    end: '2026-09-09',
    status: '유효',
    statusClass: 'success',
  },
])

const excellentDetailRows = ref([
  {
    code: 'P001',
    name: '사무용 의자',
    company: '탑오피스',
    region: '서울',
    start: '2024-01-15',
    end: '2026-01-14',
    status: '만료',
    statusClass: 'danger',
  },
  {
    code: 'P001',
    name: '사무용 의자',
    company: '정보가구',
    region: '경기',
    start: '2024-02-20',
    end: '2026-02-19',
    status: '만료임박',
    statusClass: 'warning',
  },
  {
    code: 'P001',
    name: '사무용 의자',
    company: '한국가구',
    region: '부산',
    start: '2024-03-10',
    end: '2026-03-09',
    status: '만료임박',
    statusClass: 'warning',
  },
  {
    code: 'P002',
    name: '프린터',
    company: '탑오피스',
    region: '서울',
    start: '2024-04-05',
    end: '2026-04-04',
    status: '만료임박',
    statusClass: 'warning',
  },
  {
    code: 'P002',
    name: '프린터',
    company: '정보가구',
    region: '경기',
    start: '2024-05-12',
    end: '2026-05-11',
    status: '유효',
    statusClass: 'success',
  },
  {
    code: 'P004',
    name: 'LED 조명',
    company: '탑오피스',
    region: '서울',
    start: '2024-06-18',
    end: '2026-06-17',
    status: '유효',
    statusClass: 'success',
  },
  {
    code: 'P004',
    name: 'LED 조명',
    company: '밝은조명',
    region: '인천',
    start: '2024-07-22',
    end: '2026-07-21',
    status: '유효',
    statusClass: 'success',
  },
  {
    code: 'P005',
    name: '노트북',
    company: '정보가구',
    region: '경기',
    start: '2024-08-15',
    end: '2026-08-14',
    status: '유효',
    statusClass: 'success',
  },
  {
    code: 'P005',
    name: '노트북',
    company: '테크솔루션',
    region: '대전',
    start: '2024-09-10',
    end: '2026-09-09',
    status: '유효',
    statusClass: 'success',
  },
])

/** 민수 계약 목록 — API 연동 후 채움 */
const privateRows = ref([])

const showPrivateModal = ref(false)
const privateForm = ref({
  type: '물품',
  product: '',
  client: '',
  region: '',
  amount: '',
  qty: '',
  date: '',
  year: '',
})
function typePillClass(type) {
  if (type === '공사') return 'type-pill-construction'
  if (type === '용역') return 'type-pill-service'
  if (type === '쇼핑몰') return 'type-pill-shopping'
  return 'type-pill-goods'
}

function openPrivateModal() {
  privateForm.value = {
    type: '물품',
    product: '',
    client: '',
    region: '',
    amount: '',
    qty: '',
    date: '',
    year: '',
  }
  showPrivateModal.value = true
}

function closePrivateModal() {
  showPrivateModal.value = false
}

const loadDashboardData = async () => {
  // TODO: 시장현황 요약/차트용 /api/report/market 등 연동
}

// TODO (report data integration)
// 1) /api/report/* endpoint별 응답 스키마 확정 및 매핑
// 2) 탭 전환 시 필요한 데이터만 로딩 (캐시/재사용 전략 포함)
// 3) 로딩/에러/빈 데이터 상태 UI 추가
// 4) 차트 데이터 비율/축 값 계산 로직 분리 (util 또는 composable)
// 5) 필터(기간/카테고리) 입력값을 API 파라미터로 연동

onMounted(() => {
  loadDashboardData()
  if (activeTab.value === '수요기관별' && !agencyLoaded.value) {
    fetchDemandAgencyMarket()
  }
})

watch(activeTab, (tab) => {
  if (tab === '수요기관별' && !agencyLoaded.value && !agencyLoading.value) {
    fetchDemandAgencyMarket()
  }
  if (tab === '지역별' && !regionLoaded.value && !regionLoading.value) {
    fetchRegionMarket()
  }
})

// 기간 필터가 바뀌면 캐시 무효화 + 현재 탭이면 즉시 재조회
watch([dashboardFilterMode, dashboardYear, dashboardFrom, dashboardTo], () => {
  agencyLoaded.value = false
  regionLoaded.value = false
  if (activeTab.value === '수요기관별' && !agencyLoading.value) {
    fetchDemandAgencyMarket()
  }
  if (activeTab.value === '지역별' && !regionLoading.value) {
    fetchRegionMarket()
  }
})

watch(dataSource, () => {
  agencyLoaded.value = false
  regionLoaded.value = false
  fetchDemandAgencyMarket()
  fetchRegionMarket()
})

// 시장현황 다중 선택 변경 시 재조회 (API 연동 후 loadMarketData() 호출로 교체)
watch(marketDataSources, () => {
  // TODO: 시장현황 API 연동 후 loadMarketData() 호출
})
</script>

<style scoped>
.dashboard-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.page-header {
  padding: 20px 20px 18px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%);
  border-radius: 12px;
  margin-bottom: 4px;
}

.title-block {
  width: 100%;
}

.title-row {
  display: flex;
  align-items: stretch;
  gap: 14px;
}

.header-accent {
  width: 4px;
  min-height: 44px;
  border-radius: 4px;
  background: linear-gradient(180deg, #60a5fa 0%, #3b82f6 100%);
  flex-shrink: 0;
}

.header-titles {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 4px;
}

.header-titles h1 {
  margin: 0;
  font-size: 1.5rem;
  font-weight: 700;
  color: #f8fafc;
  letter-spacing: -0.02em;
  line-height: 1.3;
}

.header-sub {
  margin: 0;
  font-size: 0.8125rem;
  color: #94a3b8;
  font-weight: 400;
  letter-spacing: 0.01em;
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

/* 수요기관별 로딩 표시 */
.loading-banner {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  background: #edf4ff;
  border-radius: 12px;
  padding: 24px;
  border: 1px solid #d6e5ff;
}

.loading-banner-prominent {
  min-height: 160px;
  padding: 32px 24px;
  background: linear-gradient(180deg, #e8f0fe 0%, #edf4ff 100%);
  border: 2px solid #3f6ff0;
}

.loading-spinner {
  width: 36px;
  height: 36px;
  border: 3px solid #d6e5ff;
  border-top-color: #3f6ff0;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

.loading-spinner-large {
  width: 48px;
  height: 48px;
  border-width: 4px;
  border-color: #d6e5ff;
  border-top-color: #3f6ff0;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.loading-text {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
}

.loading-sub {
  margin: 0;
  font-size: 13px;
  color: #6b7a99;
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

.filter-bar {
  background: #edf4ff;
  border: 1px solid #d6e5ff;
  border-radius: 12px;
  padding: 12px 16px;
}

.condition-filter-bar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px 16px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 10px 16px;
}

.check-group {
  display: flex;
  flex-wrap: wrap;
  gap: 6px 14px;
}

.check-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  font-size: 14px;
  color: #374151;
  user-select: none;
}

.check-item input[type='checkbox'] {
  width: 15px;
  height: 15px;
  cursor: pointer;
  accent-color: #3f6ff0;
  margin: 0;
}

.check-dot {
  display: inline-block;
  width: 9px;
  height: 9px;
  border-radius: 50%;
  flex-shrink: 0;
}

.condition-hint {
  font-size: 12px;
  color: #94a3b8;
  margin-left: 4px;
}

.filter-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
}

.filter-label {
  font-weight: 600;
  color: #1e3a5f;
  margin-right: 4px;
}

.filter-radio {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  font-size: 14px;
  color: #374151;
}

.filter-radio input {
  margin: 0;
}

.filter-select {
  padding: 6px 10px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 14px;
  min-width: 90px;
}

.filter-select:disabled,
.filter-date:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.filter-date {
  padding: 6px 10px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 14px;
}

.filter-sep {
  color: #6b7280;
  font-size: 14px;
}

.filter-apply {
  padding: 8px 16px;
  background: #3f6ff0;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
}

.filter-apply:hover {
  background: #3558d4;
}

.section-title-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px 20px;
  margin-bottom: 4px;
}

.section-title-row .section-title {
  margin: 0;
  flex-shrink: 0;
}

.data-source-bar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px 16px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 12px 16px;
}

.data-source-bar.inline {
  background: transparent;
  border: none;
  padding: 0;
  gap: 8px 12px;
}

.data-source-hint-inline {
  font-size: 12px;
  color: #64748b;
  white-space: nowrap;
}

.data-source-segment {
  display: inline-flex;
  border-radius: 10px;
  overflow: hidden;
  border: 1px solid #cbd5e1;
}

.segment-btn {
  padding: 8px 16px;
  font-size: 14px;
  font-weight: 500;
  border: none;
  background: #fff;
  color: #475569;
  cursor: pointer;
  transition: background 0.15s, color 0.15s;
}

.segment-btn + .segment-btn {
  border-left: 1px solid #e2e8f0;
}

.segment-btn:hover {
  background: #f1f5f9;
}

.segment-btn.active {
  background: linear-gradient(180deg, #3d5a73 0%, #34495e 100%);
  color: #f8fafc;
}

.data-source-hint {
  margin: 0;
  flex: 1 1 100%;
  font-size: 12px;
  color: #64748b;
  line-height: 1.4;
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

/* 지역별: 물품/공사/용역/3자단가 선택 탭 */
.region-category-bar {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
  padding: 8px;
  background: #f0f4f8;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
}

.region-category-btn {
  flex: 1;
  border: none;
  background: transparent;
  padding: 10px 14px;
  border-radius: 10px;
  color: #475569;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition:
    background 0.2s,
    color 0.2s;
}

.region-category-btn:hover {
  background: #e2e8f0;
  color: #334155;
}

.region-category-btn.active {
  background: #3f6ff0;
  color: white;
  box-shadow: 0 2px 6px rgba(63, 111, 240, 0.3);
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
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
  margin-top: 16px;
}

/* 그리드 셀이 내용보다 작아질 수 있도록(절반 너비 유지) */
.chart-grid .chart-card {
  min-width: 0;
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

/* 수요기관별 계약건수/평균단가: Y축 + 스크롤 영역, 카드 안에서만 스크롤 */
.agency-chart-with-axis {
  display: flex;
  align-items: stretch;
  gap: 10px;
  min-width: 0;
  min-height: 0;
  overflow: hidden;
}

/* Y축: 0이 맨 아래, max가 맨 위, 막대 영역 140px와 높이 맞춤 (grid로 틱 위치 정확히 맞춤) */
.agency-y-axis {
  display: grid;
  grid-template-rows: repeat(5, 1fr);
  flex-shrink: 0;
  width: 56px;
  height: 140px;
  padding: 0 6px 0 0;
  font-size: 11px;
  color: #64748b;
  text-align: right;
  align-self: flex-start;
  box-sizing: border-box;
}

.agency-y-tick {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  white-space: nowrap;
  flex-shrink: 0;
  line-height: 1;
}

.agency-chart-scroll {
  flex: 1;
  min-width: 0;
  overflow-x: auto;
  overflow-y: hidden;
  padding-bottom: 8px;
  -webkit-overflow-scrolling: touch;
}

.agency-chart-scroll::-webkit-scrollbar {
  height: 8px;
}

.agency-chart-scroll::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 4px;
}

/* 막대 영역은 항상 위쪽 고정(140px), 기관명은 아래에 가변 → 그래프 높이/기준선 일정 */
.bar-chart.bar-chart-horizontal {
  display: flex;
  flex-direction: row;
  gap: 10px;
  align-items: flex-start;
  min-height: 180px;
  height: auto;
  min-width: min-content;
  padding-right: 8px;
}

/* 막대 열: 고정 너비로 그래프 모양 일정, 기관명은 여러 줄로 전부 표기 */
.bar-chart-horizontal .bar-column-fixed {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  width: 76px;
  min-width: 76px;
  flex-shrink: 0;
}

/* 막대 높이 %가 적용되도록 고정 높이 컨테이너 */
.bar-value-wrap {
  position: relative;
  height: 140px;
  width: 100%;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  min-width: 48px;
}

.bar-chart-horizontal .bar-column-fixed .bar {
  width: 100%;
  min-height: 4px;
  max-height: 100%;
}

/* 그래프 hover 툴팁 */
.chart-hover-wrap {
  position: relative;
}

.chart-tooltip {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  bottom: 100%;
  margin-bottom: 6px;
  padding: 6px 10px;
  background: #1e293b;
  color: #fff;
  font-size: 12px;
  font-weight: 500;
  border-radius: 8px;
  white-space: nowrap;
  z-index: 10;
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.15s ease;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.chart-hover-wrap:hover .chart-tooltip {
  opacity: 1;
}

.chart-tooltip-bar {
  bottom: auto;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  margin: 0;
}

.hbar-track .chart-tooltip {
  bottom: 100%;
  top: auto;
  left: 50%;
  transform: translateX(-50%);
  margin-bottom: 6px;
  margin-right: 0;
}

.hbar-track:hover .chart-tooltip {
  opacity: 1;
}

.bar-label-nowrap {
  white-space: nowrap;
  text-align: center;
  font-size: 11px;
  color: #4b5b73;
  line-height: 1.2;
  word-break: keep-all;
}

/* 수요기관별 계약건수/평균단가: 기관명 전체 표기(여러 줄 허용) */
.bar-label-fixed {
  display: block;
  width: 76px;
  min-height: 2.4em;
  white-space: normal;
  word-break: keep-all;
  line-break: strict;
  text-align: center;
  font-size: 10px;
  color: #4b5b73;
  line-height: 1.25;
}

.stacked-chart {
  background: #ffffff;
  border: 1px solid #eee;
  border-radius: 12px;
  padding: 16px;
}

/* 지역별 매출 현황: Y축 + 막대 (물품 API 연동) */
.region-chart-with-axis {
  display: flex;
  align-items: stretch;
  gap: 10px;
  min-height: 0;
  overflow: hidden;
}

.region-chart-scroll {
  flex: 1;
  min-width: 0;
  overflow-x: auto;
  overflow-y: hidden;
  padding-bottom: 8px;
  -webkit-overflow-scrolling: touch;
}

.region-chart-scroll::-webkit-scrollbar {
  height: 8px;
}

.region-chart-scroll::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 4px;
}

.region-placeholder {
  padding: 24px;
  text-align: center;
  color: #94a3b8;
  font-size: 14px;
}

.region-y-tick {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  white-space: nowrap;
  flex-shrink: 0;
  line-height: 1;
}

.region-stacked-with-axis {
  display: flex;
  gap: 12px;
  align-items: flex-end;
}

/* Y축: 막대 영역과 높이 맞춤 (지역별 매출 현황: 140px) */
.region-chart-with-axis .region-y-axis {
  display: grid;
  grid-template-rows: repeat(5, 1fr);
  flex-shrink: 0;
  width: 56px;
  height: 140px;
  padding: 0 6px 0 0;
  font-size: 11px;
  color: #64748b;
  text-align: right;
  align-self: flex-start;
  box-sizing: border-box;
}

.region-y-axis span {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  line-height: 1;
}

.stacked-bars {
  display: flex;
  gap: 16px;
  align-items: flex-end;
  height: 220px;
  flex: 1;
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

/* Top10 금액 축: 행과 동일한 그리드로 라벨이 데이터(바) 위치와 정확히 맞도록 */
.hbar-axis-row {
  display: grid;
  grid-template-columns: 120px 1fr;
  gap: 20px;
  align-items: center;
  color: #9aa6b2;
  font-size: 12px;
  margin-bottom: 4px;
}

.hbar-axis-placeholder {
  min-width: 0;
}

.hbar-axis-ticks {
  display: flex;
  justify-content: space-between;
  align-items: center;
  min-width: 0;
}

.hbar-axis-tick {
  flex-shrink: 0;
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

.rank-analysis-head {
  margin-bottom: 16px;
}

.rank-section-title {
  margin: 0 0 12px;
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
}

.rank-filter-rows {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.rank-filter-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px 14px;
}

.rank-filter-row .filter-label {
  font-size: 13px;
  font-weight: 600;
  color: #475569;
  min-width: 4.5em;
}

.rank-period-hint {
  margin: 0;
  font-size: 12px;
  color: #64748b;
  line-height: 1.45;
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

.private-api-note {
  margin: 0 0 12px;
  font-size: 12px;
  color: #64748b;
  line-height: 1.45;
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

.icon-btn:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.type-pill {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 6px;
  font-size: 11px;
  font-weight: 600;
}
.type-pill-goods { background: #e3f2fd; color: #1565c0; }
.type-pill-construction { background: #e8f5e9; color: #2e7d32; }
.type-pill-service { background: #f3e5f5; color: #6a1b9a; }
.type-pill-shopping { background: #fff3e0; color: #e65100; }

.no-data-cell {
  text-align: center;
  color: #6b7a99;
  padding: 32px 16px !important;
}

/* 민수 계약 추가 모달 */
.private-modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}
.private-modal {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
  width: 90%;
  max-width: 440px;
  max-height: 90vh;
  overflow: auto;
}
.private-modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #e0e0e0;
}
.private-modal-header h3 {
  margin: 0;
  font-size: 1.1em;
  color: #111827;
}
.modal-close {
  background: none;
  border: none;
  font-size: 24px;
  line-height: 1;
  color: #6b7280;
  cursor: pointer;
  padding: 0 4px;
}
.modal-close:hover {
  color: #111827;
}
.private-modal-body {
  padding: 20px;
}
.private-modal-body .form-row {
  margin-bottom: 14px;
}
.private-modal-body .form-row label {
  display: block;
  margin-bottom: 4px;
  font-size: 13px;
  color: #374151;
}
.private-modal-body .form-row .required {
  color: #e74c3c;
}
.private-modal-body .form-input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 14px;
  box-sizing: border-box;
}
.private-modal-body .form-input:focus {
  outline: none;
  border-color: #111827;
}
.private-modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #eee;
}
.private-modal-footer .btn-cancel {
  padding: 8px 16px;
  background: #f3f4f6;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  color: #374151;
}
.private-modal-footer .btn-cancel:hover {
  background: #e5e7eb;
}
.private-modal-footer .btn-submit {
  padding: 8px 16px;
  background: #111827;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
}
.private-modal-footer .btn-submit:hover {
  background: #1f2937;
}
.private-modal-footer .btn-submit:disabled {
  opacity: 0.55;
  cursor: not-allowed;
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
}
</style>
