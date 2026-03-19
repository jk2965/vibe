// Vue 3의 앱 생성 함수 임포트
import { createApp } from 'vue'
// 최상위 컴포넌트 App.vue 임포트
import App from './App.vue'
// router/index.js에서 정의한 라우터 임포트
import router from './router'
// 전역 스타일시트 적용
import './style.css'
// 코드 블록 구문 강조(highlight.js)용 GitHub 테마 CSS 적용 (TiptapEditor.vue와 연동)
import 'highlight.js/styles/github.css'

// Vue 앱 생성 후 라우터 등록, index.html의 #app 엘리먼트에 마운트
createApp(App).use(router).mount('#app')
