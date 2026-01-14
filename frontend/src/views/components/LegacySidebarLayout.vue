<template>
  <div class="legacy-layout">
    <div class="sidebar" :class="{ 'expanded': isSidebarExpanded }" @mouseenter="isSidebarExpanded = true" @mouseleave="isSidebarExpanded = false">
      <div class="sidebar-header" :class="{ 'collapsed': !isSidebarExpanded, 'expanded': isSidebarExpanded }">
        <span>{{ isSidebarExpanded ? 'G2B PROJECT' : 'G2B' }}</span>
      </div>
      <ul>
        <li class="menu-item" id="toggle-report-data" @click="toggleReportSubmenu">
          <span class="menu-label">보고서 데이터</span>
        </li>
        <ul v-show="isReportSubmenuOpen" class="submenu" style="padding-left: 20px;">
          <li class="menu-item" @click="$router.push('/report-goods')">
            <span class="menu-label">물품</span>
          </li>
          <li class="menu-item" @click="$router.push('/report-dashboard')">
            <span class="menu-label">대시보드</span>
          </li>
        </ul>
        <li class="menu-item" id="toggle-related-market-db" @click="toggleSubmenu">
          <span class="menu-label">관련업계 전체시장 DB</span>
        </li>
        <ul v-show="isSubmenuOpen" class="submenu" style="padding-left: 20px;">
          <li class="menu-item" @click="$router.push('/goods')">
            <span class="menu-label">물품</span>
          </li>
          <li class="menu-item" @click="$router.push('/services')">
            <span class="menu-label">용역</span>
          </li>
          <li class="menu-item" @click="$router.push('/constructions')">
            <span class="menu-label">공사</span>
          </li>
          <li class="menu-item" @click="$router.push('/shopping-mall')">
            <span class="menu-label">3자단가</span>
          </li>
        </ul>
        <li class="menu-item" @click="$router.push('/top-contracts')">
          <span class="menu-label">탑인더스트리 & 탑정보통신 수주현황DB</span>
        </li>
        <li class="menu-item" @click="$router.push('/target-projects')">
          <span class="menu-label">수주대상 사업탐색</span>
        </li>
      </ul>
    </div>
    <div class="content">
      <slot></slot>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const isSidebarExpanded = ref(false);
const isSubmenuOpen = ref(false);
const isReportSubmenuOpen = ref(false);

const toggleSubmenu = () => {
  isSubmenuOpen.value = !isSubmenuOpen.value;
};

const toggleReportSubmenu = () => {
  isReportSubmenuOpen.value = !isReportSubmenuOpen.value;
};
</script>

<style scoped>
.legacy-layout {
  font-family: Arial, sans-serif;
  margin: 0;
  display: flex;
  min-height: 100vh;
}

.sidebar {
  width: 75px;
  height: 100vh;
  background-color: #2c3e50;
  color: #ecf0f1;
  position: fixed;
  transition: width 0.3s ease;
  overflow: hidden;
  z-index: 1000;
}

.sidebar.expanded {
  width: 300px;
}

.sidebar::after {
  content: '';
  display: block;
  width: 8px;
  height: 100%;
  background: linear-gradient(to right, rgba(255, 255, 255, 0.1), rgba(255, 255, 255, 0));
  position: absolute;
  right: 0;
  top: 0;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.sidebar:hover::after {
  opacity: 1;
}

.sidebar-header {
  background-color: #34495e;
  color: #ecf0f1;
  padding: 20px;
  text-align: center;
  font-size: 1.5em;
  font-weight: 500;
  transition: all 0.3s ease;
  overflow: hidden;
  white-space: nowrap;
}

.sidebar-header.collapsed span {
  display: block;
  writing-mode: vertical-rl;
  transform: rotate(180deg);
}

.sidebar-header.expanded span {
  display: inline;
}

.menu-item {
  padding: 20px;
  display: flex;
  align-items: center;
  transition: background-color 0.3s;
  cursor: pointer;
}

.menu-item:hover {
  background-color: #34495e;
}

.menu-label {
  white-space: nowrap;
  opacity: 0;
  transition: opacity 0.3s ease, transform 0.3s ease;
  transform: translateX(-20px);
}

.sidebar.expanded .menu-label {
  opacity: 1;
  transform: translateX(0);
}

.content {
  margin-left: 75px;
  padding: 20px;
  transition: margin-left 0.3s ease;
  width: calc(100% - 75px);
}

.sidebar.expanded ~ .content {
  margin-left: 300px;
  width: calc(100% - 300px);
}

/* Specific H1 styles from legacy templates */
:deep(h1) {
  background-color: #34495e;
  color: #ecf0f1;
  padding: 15px;
  border-radius: 8px;
  border: 2px solid #2c3e50;
  text-align: center;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
}

:deep(h1.goods) {
  border-left: 15px solid #3498db;
}

:deep(h1.services) {
  border-left: 15px solid #e67e22;
}

:deep(h1.construction) {
  border-left: 15px solid #27ae60;
}

:deep(h1.servicesSelected) {
  border-left: 15px solid #e84393;
}
</style>