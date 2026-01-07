<template>
  <div class="main-layout-view">
    <div id="header">
      <h1>G2B 조회 프로그램</h1>
      <div id="serviceKeyContainer">
        <label for="serviceKey" style="margin-right: 10px;">Service Key 입력:</label>
        <input type="text" v-model="serviceKey" id="serviceKey" style="width: 400px;">
      </div>
    </div>

    <div id="sidebar">
      <h2>엔드포인트 선택</h2>
      <a href="#" @click.prevent="loadEndpoint('/1230000/ao/CntrctInfoService/getCntrctInfoListThng')">계약현황에 대한 물품조회</a>
      <a href="#" @click.prevent="loadEndpoint('/1230000/ao/CntrctInfoService/getCntrctInfoListThngDetail')">계약현황에 대한 물품세부조회</a>
      <a href="#" @click.prevent="loadEndpoint('/1230000/ao/CntrctInfoService/getCntrctInfoListCnstwk')">계약현황에 대한 공사조회</a>
      <a href="#" @click.prevent="loadEndpoint('/1230000/ao/CntrctInfoService/getCntrctInfoListServc')">계약현황에 대한 용역조회</a>
      <a href="#" @click.prevent="loadEndpoint('/1230000/at/ShoppingMallPrdctInfoService/getShoppingMallPrdctInfoList')">계약현황에 대한 3자단가조회</a>
    </div>

    <div id="content">
      <form id="apiForm" @submit.prevent="fetchData">
        <label for="inqryBgnDt">시작일 입력:</label>
        <input type="text" v-model="inqryBgnDt" id="inqryBgnDt" placeholder="202401010000" maxlength="12" required>

        <label for="inqryEndDt">종료일 입력:</label>
        <input type="text" v-model="inqryEndDt" id="inqryEndDt" placeholder="202401012359" maxlength="12" required>

        <button type="submit">데이터 가져오기</button>
      </form>

      <div style="margin-top: 20px;" class="action-buttons">
        <button type="button" @click="runThingProcedure">물품 데이터 수동생성</button>
        <button type="button" @click="runServcProcedure">용역 데이터 수동생성</button>
        <button type="button" @click="runCnstwkProcedure">공사 데이터 수동생성</button>
        <button type="button" @click="runTopProcedure">탑 데이터 수동생성</button>
      </div>

      <!-- Navigation to other views -->
      <div style="margin-top: 40px; border-top: 1px solid #ddd; padding-top: 20px;">
        <h3>사용자 뷰 이동</h3>
        <button @click="$router.push('/goods')">물품 (Page1)</button>
        <button @click="$router.push('/services')">용역 (Page2)</button>
        <button @click="$router.push('/constructions')">공사 (Page3)</button>
        <button @click="$router.push('/top-contracts')">TOP (Page4)</button>
        <button @click="$router.push('/shopping-mall')">3자단가 (Page5)</button>
        <button @click="$router.push('/target-projects')">수주대상 (Page6)</button>
        <button @click="$router.push('/work-in-progress')">작업중 (Imsi)</button>
      </div>
    </div>

    <!-- 로딩 표시 -->
    <div v-if="isLoading" id="overlay"></div>
    <div v-if="isLoading" id="loading">로딩 중{{ loadingDots }}</div>
  </div>
</template>

<script setup>
import { ref, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();

const serviceKey = ref('%2FwZoB2Eujc4Y0tk%2Fd8HcpY9mrcupnHiFHTHwXNTZ9LZSXXwcbYfHwi7%2Fedrq%2B7eXV%2BkP1TIriT3BbKQX4kQKSQ%3D%3D');
const inqryBgnDt = ref('');
const inqryEndDt = ref('');
const selectedEndpoint = ref('');
const isLoading = ref(false);
const loadingDots = ref('');
let loadingInterval = null;

const loadEndpoint = (endpoint) => {
  selectedEndpoint.value = endpoint;
  alert(`선택된 엔드포인트: ${endpoint}`);
};

const showLoading = () => {
  isLoading.value = true;
  let count = 0;
  loadingInterval = setInterval(() => {
    count = (count + 1) % 4;
    loadingDots.value = '.'.repeat(count);
  }, 500);
};

const hideLoading = () => {
  isLoading.value = false;
  if (loadingInterval) clearInterval(loadingInterval);
};

const fetchData = () => {
  if (!selectedEndpoint.value) {
    alert('엔드포인트를 선택해주세요.');
    return;
  }
  showLoading();
  // TODO: axios.post('/fetch', { ... })
  setTimeout(() => {
    hideLoading();
    alert('데이터를 성공적으로 가져왔습니다! (Mock 0m 2s)');
  }, 2000);
};

const callManualProcess = (endpoint) => {
  if (!confirm("정말 실행하시겠습니까?")) return;
  showLoading();
  // TODO: axios.post(endpoint)
  setTimeout(() => {
    hideLoading();
    alert(`실행 완료: ${endpoint} (Mock)`);
  }, 1500);
};

const runThingProcedure = () => callManualProcess('/api/manual-process/dailyThings');
const runServcProcedure = () => callManualProcess('/api/manual-process/dailyServices');
const runCnstwkProcedure = () => callManualProcess('/api/manual-process/dailyConstructions');
const runTopProcedure = () => callManualProcess('/api/manual-process/dailyTopDatas');

onUnmounted(() => {
  if (loadingInterval) clearInterval(loadingInterval);
});
</script>

<style scoped>
.main-layout-view {
  font-family: Arial, sans-serif;
}

#loading {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 24px;
  color: #333;
  z-index: 1000;
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 0 10px rgba(0,0,0,0.3);
}

#overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 999;
}

#sidebar {
  position: fixed;
  top: 80px;
  left: 0;
  width: 250px;
  height: calc(100% - 80px);
  background-color: #f4f4f4;
  padding: 20px;
  box-shadow: 2px 0 5px rgba(0, 0, 0, 0.1);
  z-index: 100;
}

#content {
  margin-left: 290px;
  margin-top: 80px;
  padding: 20px;
}

#header {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 50px;
  background-color: #fff;
  padding: 20px;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
  z-index: 101;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

a {
  display: block;
  margin-bottom: 10px;
  text-decoration: none;
  color: #333;
}

a:hover {
  color: #007bff;
}

#serviceKeyContainer {
  display: flex;
  align-items: center;
  margin-right: 50px;
}

button {
  margin-right: 10px;
  margin-bottom: 10px;
  padding: 8px 15px;
  cursor: pointer;
}

input[type="text"] {
  padding: 5px;
}
</style>