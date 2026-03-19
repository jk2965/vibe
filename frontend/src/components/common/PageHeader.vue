<template>
  <div class="page-header">
    <h1>{{ title }}</h1>
    <div class="header-actions">
      <span class="username">{{ username }} {{ position }}님</span>
      <button @click="$router.push('/home')" class="btn-home">홈으로</button>
      <button @click="logout" class="btn-logout">로그아웃</button>
    </div>
  </div>
</template>

<script>
// 각 페이지 상단에 공통으로 사용되는 헤더 컴포넌트
// Home.vue, Board.vue, Notice.vue, Archive.vue 등 모든 주요 페이지에서 사용
// router/index.js에서 requireAuth로 보호된 페이지에 배치됨
export default {
  name: 'PageHeader',
  props: {
    // 현재 페이지 이름 (예: '자유게시판', '공지사항') - h1 태그로 표시
    title: {
      type: String,
      required: true
    }
  },
  computed: {
    // 현재 로그인한 사용자 이름 (localStorage에서 조회)
    username() {
      return localStorage.getItem('username') || ''
    },
    // 현재 로그인한 사용자 직급/직위 (localStorage에서 조회)
    position() {
      return localStorage.getItem('position') || ''
    }
  },
  methods: {
    // 로그아웃: localStorage에서 인증 관련 항목 모두 제거 후 /login으로 이동
    logout() {
      // 로그인 상태 플래그 제거
      localStorage.removeItem('loggedIn')
      // 사용자 ID 제거
      localStorage.removeItem('userId')
      // 사용자 이름 제거
      localStorage.removeItem('username')
      // 직급 정보 제거
      localStorage.removeItem('position')
      // router/index.js의 /login 경로로 이동
      this.$router.push('/login')
    }
  }
}
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}
.page-header h1 { margin: 0; }
.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 15px;
}
.username { color: #555; }
.btn-home {
  padding: 8px 16px;
  background: #607d8b;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
}
.btn-home:hover { background: #455a64; }
.btn-logout {
  padding: 8px 16px;
  background: #e53935;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
}
.btn-logout:hover { background: #c62828; }
</style>
