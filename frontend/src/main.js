import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import './api/axios-setup'
import './assets/responsive.css'

// DataTables 기본 스타일 (필수)
import 'datatables.net-dt/css/dataTables.dataTables.min.css'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.mount('#app')
