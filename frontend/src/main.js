import { createApp } from 'vue'
import App from './App.vue'
import router from './router'

// DataTables 기본 스타일 (필수)
import 'datatables.net-dt/css/dataTables.dataTables.min.css'

const app = createApp(App)

app.use(router)
app.mount('#app')
