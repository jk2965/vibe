<template>
  <div class="home-container">
    <div class="home-box">
      <div class="header">
        <h1>관리 시스템</h1>
        <p class="welcome">{{ username }} {{position}}님 환영합니다</p>
      </div>

      <div class="menu-grid">
        <button @click="$router.push('/attendance')" class="menu-btn attendance-btn">
          <span class="menu-icon">🕐</span>
          <span class="menu-label">출퇴근 기록 조회</span>
        </button>
        <button @click="$router.push('/vacation')" class="menu-btn vacation-btn">
          <span class="menu-icon">🏖️</span>
          <span class="menu-label">휴가 등록</span>
        </button>
        <button @click="$router.push('/calendar')" class="menu-btn calendar-btn">
          <span class="menu-icon">📅</span>
          <span class="menu-label">일정 관리</span>
        </button>
        <button @click="$router.push('/notice')" class="menu-btn board-btn">
          <span class="menu-icon">📋</span>
          <span class="menu-label">게시판</span>
        </button>
        <button v-if="canAccessHr" @click="$router.push('/hr')" class="menu-btn hr-btn">
          <span class="menu-icon">👥</span>
          <span class="menu-label">인사 관리</span>
        </button>
      </div>

      <button @click="logout" class="btn-logout">로그아웃</button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Home',
  // API 호출 없음 - 모든 데이터는 로컬스토리지에서 읽음 (로그인 시 Login.vue에서 저장)
  computed: {
    // 로그인한 사용자 이름 (로컬스토리지의 'username')
    username() {
      return localStorage.getItem('username') || ''
    },
    // 로그인한 사용자 직급 (로컬스토리지의 'position')
    position() {
      return localStorage.getItem('position') || ''
    },
    // 관리자 레벨 (0: 일반, 1: 관리자, 2: 슈퍼 관리자)
    adminLevel() {
      return parseInt(localStorage.getItem('adminLevel') || '0')
    },
    // 일반 관리자 이상 여부
    isAdmin() {
      return this.adminLevel >= 1
    },
    // 팀장 여부 (로컬스토리지의 'isTeamLeader')
    isTeamLeader() {
      return localStorage.getItem('isTeamLeader') === 'true'
    },
    // 인사 관리 메뉴 접근 권한: 관리자 또는 팀장만 표시
    canAccessHr() {
      return this.isAdmin || this.isTeamLeader
    }
  },
  methods: {
    // 로그아웃: 로컬스토리지의 모든 인증 정보를 삭제하고 로그인 페이지로 이동
    logout() {
      localStorage.removeItem('loggedIn')
      localStorage.removeItem('userId')
      localStorage.removeItem('username')
      localStorage.removeItem('position')
      localStorage.removeItem('adminLevel')
      localStorage.removeItem('team')
      localStorage.removeItem('isTeamLeader')
      this.$router.push('/login')
    }
  }
}
</script>

<style scoped>
.home-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: #f0f2f5;
}
.home-box {
  background: white;
  padding: 48px 40px;
  border-radius: 16px;
  box-shadow: 0 2px 16px rgba(0,0,0,0.1);
  width: 420px;
  text-align: center;
}
.header h1 {
  margin: 0 0 8px 0;
  font-size: 24px;
}
.welcome {
  color: #666;
  margin: 0 0 40px 0;
  font-size: 15px;
}
.menu-grid {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 24px;
}
.menu-btn {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 24px;
  border: none;
  border-radius: 10px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  transition: opacity 0.2s;
}
.menu-btn:hover { opacity: 0.88; }
.attendance-btn { background: #4CAF50; color: white; }
.vacation-btn { background: #2196F3; color: white; }
.calendar-btn { background: #00897b; color: white; }
.board-btn { background: #f57c00; color: white; }
.hr-btn { background: #7b1fa2; color: white; }
.menu-icon { font-size: 24px; }
.menu-label { flex: 1; text-align: left; }
.btn-logout {
  width: 100%;
  padding: 12px;
  background: white;
  color: #e53935;
  border: 1px solid #e53935;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
}
.btn-logout:hover { background: #fdecea; }
</style>
