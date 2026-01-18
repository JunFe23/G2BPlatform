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
          <li class="menu-item" @click="$router.push('/report-services')">
            <span class="menu-label">용역</span>
          </li>
          <li class="menu-item" @click="$router.push('/report-constructions')">
            <span class="menu-label">공사</span>
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
  width: 76px;
  height: calc(100vh - 24px);
  margin: 12px;
  background: linear-gradient(180deg, #223247 0%, #1f2a37 100%);
  color: #ecf0f1;
  position: fixed;
  transition: width 0.35s ease, box-shadow 0.35s ease, transform 0.35s ease;
  overflow: hidden;
  z-index: 1000;
  border-radius: 18px;
  box-shadow: 0 12px 24px rgba(15, 23, 42, 0.25);
  border: 1px solid rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(6px);
}

.sidebar.expanded {
  width: 300px;
  box-shadow: 0 16px 36px rgba(15, 23, 42, 0.35);
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
  background: rgba(255, 255, 255, 0.08);
  color: #f8fafc;
  margin: 10px;
  padding: 16px;
  text-align: center;
  font-size: 1.1em;
  font-weight: 700;
  letter-spacing: 0.6px;
  transition: all 0.3s ease;
  overflow: hidden;
  white-space: nowrap;
  border-radius: 14px;
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
  margin: 8px 10px;
  padding: 12px 14px;
  display: flex;
  align-items: center;
  gap: 10px;
  transition: background-color 0.25s ease, transform 0.25s ease, box-shadow 0.25s ease;
  cursor: pointer;
  border-radius: 12px;
  color: #e2e8f0;
}

.menu-item:hover {
  background: rgba(255, 255, 255, 0.08);
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.08);
  transform: translateX(2px);
}

.menu-label {
  white-space: nowrap;
  opacity: 0;
  transition: opacity 0.3s ease, transform 0.3s ease;
  transform: translateX(-12px);
  font-weight: 600;
}

.sidebar.expanded .menu-label {
  opacity: 1;
  transform: translateX(0);
}

.content {
  margin-left: 100px;
  padding: 20px 24px;
  transition: margin-left 0.3s ease;
  width: calc(100% - 100px);
}

.sidebar.expanded ~ .content {
  margin-left: 330px;
  width: calc(100% - 330px);
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