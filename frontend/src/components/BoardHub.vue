<template>
  <div class="hub-container">
    <PageHeader title="게시판" />

    <div class="hub-box">
      <h2 class="hub-title">게시판 선택</h2>
      <div class="hub-grid">
        <button @click="$router.push('/notice')" class="hub-btn notice-btn">
          <span class="hub-icon">📢</span>
          <span class="hub-label">전체 공지사항</span>
          <span class="hub-desc">전체 구성원 대상 공지</span>
        </button>
        <button v-if="canAccessTeamNotice" @click="$router.push('/team-notice')" class="hub-btn team-notice-btn">
          <span class="hub-icon">👥</span>
          <span class="hub-label">팀별 공지사항</span>
          <span class="hub-desc">소속 팀 공지</span>
        </button>
        <button @click="$router.push('/board')" class="hub-btn board-btn">
          <span class="hub-icon">📋</span>
          <span class="hub-label">자유 게시판</span>
          <span class="hub-desc">자유롭게 소통하는 공간</span>
        </button>
        <button @click="$router.push('/archive')" class="hub-btn archive-btn">
          <span class="hub-icon">🗂️</span>
          <span class="hub-label">전체 자료실</span>
          <span class="hub-desc">자료 파일 및 문서 공유</span>
        </button>
        <button v-if="canAccessTeamArchive" @click="$router.push('/team-archive')" class="hub-btn team-archive-btn">
          <span class="hub-icon">📁</span>
          <span class="hub-label">팀별 자료실</span>
          <span class="hub-desc">소속 팀 자료 공유</span>
        </button>
        <button @click="$router.push('/faq')" class="hub-btn faq-btn">
          <span class="hub-icon">❓</span>
          <span class="hub-label">FAQ</span>
          <span class="hub-desc">자주 묻는 질문과 답변</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import PageHeader from './common/PageHeader.vue'

export default {
  name: 'BoardHub',
  components: { PageHeader },
  computed: {
    canAccessTeamNotice() {
      const adminLevel = parseInt(localStorage.getItem('adminLevel') || '0')
      const team = localStorage.getItem('team') || ''
      return adminLevel >= 2 || !!team
    },
    canAccessTeamArchive() {
      const adminLevel = parseInt(localStorage.getItem('adminLevel') || '0')
      const team = localStorage.getItem('team') || ''
      return adminLevel >= 2 || !!team
    }
  }
}
</script>

<style scoped>
.hub-container { max-width: 700px; margin: 0 auto; padding: 24px; }

.hub-box {
  background: white;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.09);
  padding: 36px 32px;
}
.hub-title {
  margin: 0 0 28px 0;
  font-size: 20px;
  font-weight: bold;
  color: #333;
}

.hub-grid {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.hub-btn {
  display: flex;
  align-items: center;
  gap: 18px;
  padding: 20px 24px;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  text-align: left;
  transition: opacity 0.2s, transform 0.1s;
  color: white;
}
.hub-btn:hover { opacity: 0.88; transform: translateX(4px); }

.hub-icon { font-size: 26px; flex-shrink: 0; }
.hub-label { font-size: 16px; font-weight: bold; flex: 1; }
.hub-desc { font-size: 12px; font-weight: normal; opacity: 0.85; }

.notice-btn { background: #00796b; }
.team-notice-btn { background: #5c6bc0; }
.board-btn { background: #f57c00; }
.archive-btn { background: #1565c0; }
.team-archive-btn { background: #2e7d32; }
.faq-btn { background: #6a1b9a; }
</style>
