<template>
  <!-- 앱의 최상위 컴포넌트: router/index.js에서 정의한 라우트에 따라 페이지 컴포넌트를 렌더링함 -->
  <div class="app-root">
    <!-- 게시판 관련 경로일 때만 사이드바 + 콘텐츠 레이아웃 적용 -->
    <template v-if="showSidebar">
      <div class="layout-with-sidebar">
        <!-- 왼쪽 사이드바: 게시판 메뉴 목록 표시 -->
        <Sidebar />
        <!-- 오른쪽 메인 콘텐츠 영역 -->
        <div class="main-content">
          <router-view />
        </div>
      </div>
    </template>

    <!-- 그 외 페이지(로그인, 홈 등)는 사이드바 없이 전체 너비로 표시 -->
    <template v-else>
      <router-view />
    </template>
  </div>
</template>

<script>
// 게시판 사이드바 컴포넌트
import Sidebar from './components/common/Sidebar.vue'

export default {
  name: 'App',
  components: { Sidebar },
  computed: {
    // 현재 경로가 게시판 관련인지 확인하여 사이드바 표시 여부 결정
    showSidebar() {
      const boardPaths = ['/board', '/notice', '/team-notice', '/archive', '/team-archive', '/faq', '/board-hub', '/required', '/my-posts']
      return boardPaths.some(p => this.$route.path.startsWith(p))
    }
  }
}
</script>

<style>
* { box-sizing: border-box; }

.app-root {
  min-height: 100vh;
}

/* 사이드바 포함 레이아웃: 왼쪽 사이드바 + 오른쪽 콘텐츠 */
.layout-with-sidebar {
  display: flex;
  min-height: 100vh;
}

/* 사이드바 오른쪽 메인 콘텐츠 영역 */
.main-content {
  flex: 1;
  min-width: 0;
  overflow-x: hidden;
}
</style>
