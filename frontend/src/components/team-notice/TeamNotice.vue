<template>
  <div class="board-container">
    <PageHeader :title="selectedTeam ? selectedTeam + ' 공지사항' : '팀별 공지사항'" />

    <div class="list-card">
      <div class="card-header">
        <div class="header-left">
          <h2>{{ selectedTeam ? selectedTeam + ' 공지사항' : '팀별 공지사항' }}</h2>
          <select v-if="isSuperAdmin" v-model="selectedTeam" @change="onTeamChange" class="team-select">
            <option value="1팀">1팀</option>
            <option value="2팀">2팀</option>
          </select>
        </div>
        <button v-if="selectedTeam && canWrite" @click="$router.push('/team-notice/write?team=' + selectedTeam)" class="btn-write">글쓰기</button>
      </div>

      <template v-if="true">
        <table class="board-table">
          <thead>
            <tr>
              <th class="col-no">번호</th>
              <th class="col-title">제목</th>
              <th class="col-author">작성자</th>
              <th class="col-date">작성일</th>
              <th class="col-views">조회</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="posts.length === 0">
              <td colspan="5" class="empty">등록된 공지사항이 없습니다.</td>
            </tr>
            <tr v-for="(post, index) in posts" :key="post.id" @click="goDetail(post.id)" class="post-row">
              <td class="col-no">{{ totalCount - (pageNum - 1) * 10 - index }}</td>
              <td class="col-title title-cell">
                {{ post.title }}
                <span v-if="post.commentCount > 0" class="comment-count">[{{ post.commentCount }}]</span>
              </td>
              <td class="col-author">{{ post.authorName }}</td>
              <td class="col-date">{{ formatDate(post.createdAt) }}</td>
              <td class="col-views">{{ post.views }}</td>
            </tr>
          </tbody>
        </table>

        <div class="pagination">
          <button @click="changePage(pageNum - 1)" :disabled="pageNum <= 1" class="page-btn">이전</button>
          <button
            v-for="p in pageRange"
            :key="p"
            @click="changePage(p)"
            :class="['page-num', { active: p === pageNum }]"
          >{{ p }}</button>
          <button @click="changePage(pageNum + 1)" :disabled="pageNum >= totalPages" class="page-btn">다음</button>
        </div>
      </template>

      <p v-if="errorMsg" class="error-msg">{{ errorMsg }}</p>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
// PageHeader.vue 공통 헤더 컴포넌트 사용
import PageHeader from '../common/PageHeader.vue'

export default {
  name: 'TeamNotice',
  // PageHeader.vue 컴포넌트 등록
  components: { PageHeader },
  data() {
    // localStorage에서 관리자 레벨 읽기 (0: 일반, 1: 중간관리자, 2: 최고관리자)
    const adminLevel = parseInt(localStorage.getItem('adminLevel') || '0')
    // localStorage에서 현재 로그인 사용자의 소속 팀 읽기
    const userTeam = localStorage.getItem('team') || ''
    // localStorage에서 팀장 여부 읽기
    const isTeamLeader = localStorage.getItem('isTeamLeader') === 'true'
    // 최고관리자(레벨 2 이상) 여부 판별
    const isSuperAdmin = adminLevel >= 2
    return {
      // 팀 공지사항 목록 배열
      posts: [],
      // 현재 페이지 번호 (1부터 시작)
      pageNum: 1,
      // 전체 페이지 수
      totalPages: 1,
      // 전체 게시글 수 (역순 번호 계산에 사용)
      totalCount: 0,
      // 현재 선택된 팀: 최고관리자는 기본값 '1팀', 일반 사용자는 자신의 팀
      selectedTeam: isSuperAdmin ? '1팀' : userTeam,
      // 중간관리자 이상 여부
      isAdmin: adminLevel >= 1,
      // 최고관리자 여부 (팀 선택 드롭다운 노출 조건)
      isSuperAdmin,
      // 팀장 여부
      isTeamLeader,
      // 현재 로그인 사용자의 소속 팀
      userTeam,
      // 에러 메시지 (목록 조회 실패 시 표시)
      errorMsg: ''
    }
  },
  computed: {
    // 글쓰기 권한: 최고관리자, 중간관리자, 팀장인 경우 가능
    canWrite() {
      return this.isSuperAdmin || this.isAdmin || this.isTeamLeader
    },
    // 현재 페이지 그룹에서 표시할 페이지 번호 배열 (최대 5개씩 표시)
    pageRange() {
      const start = Math.floor((this.pageNum - 1) / 5) * 5 + 1
      const end = Math.min(start + 4, this.totalPages)
      const range = []
      for (let i = start; i <= end; i++) range.push(i)
      return range
    }
  },
  // 컴포넌트 마운트 시 선택된 팀이 있으면 공지사항 목록 최초 로드
  mounted() {
    if (this.selectedTeam) this.fetchPosts()
  },
  methods: {
    // 팀 선택 드롭다운 변경 시 호출: 페이지 초기화 후 새 팀 데이터 재조회
    onTeamChange() {
      this.pageNum = 1
      this.posts = []
      this.totalCount = 0
      this.totalPages = 1
      if (this.selectedTeam) this.fetchPosts()
    },
    // GET /api/team-notice 호출 → TeamNoticeController.java (팀별 공지사항 목록 조회, 팀 접근 권한 검증)
    async fetchPosts() {
      this.errorMsg = ''
      try {
        const res = await axios.get('http://localhost:8090/api/team-notice', {
          params: {
            team: this.selectedTeam,
            requesterId: localStorage.getItem('userId'),
            pageNum: this.pageNum
          }
        })
        // 응답에서 게시글 목록, 전체 페이지 수, 전체 게시글 수 저장
        this.posts = res.data.list
        this.totalPages = res.data.pages
        this.totalCount = res.data.total
      } catch (e) {
        this.errorMsg = e.response?.data?.message || '공지사항 목록 조회 실패'
      }
    },
    // 페이지 변경: 유효 범위 체크 후 해당 페이지 데이터 재조회
    changePage(p) {
      if (p < 1 || p > this.totalPages) return
      this.pageNum = p
      this.fetchPosts()
    },
    // 팀 공지사항 상세 페이지로 이동 → TeamNoticeDetail.vue
    goDetail(id) {
      this.$router.push(`/team-notice/${id}`)
    },
    // 날짜 문자열을 YYYY-MM-DD 형식으로 변환 (날짜 없으면 '-' 반환)
    formatDate(dateStr) {
      if (!dateStr) return '-'
      return dateStr.substring(0, 10)
    }
  }
}
</script>

<style scoped>
.board-container { max-width: 900px; margin: 0 auto; padding: 24px; }
.list-card { background: white; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.08); padding: 24px; }
.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.header-left { display: flex; align-items: center; gap: 12px; }
.card-header h2 { margin: 0; font-size: 18px; }
.team-select { padding: 6px 10px; border: 1px solid #ddd; border-radius: 6px; font-size: 14px; }
.btn-write { padding: 8px 20px; background: #1a73e8; color: white; border: none; border-radius: 6px; font-size: 14px; cursor: pointer; }
.btn-write:hover { background: #1558b0; }
.board-table { width: 100%; border-collapse: collapse; }
.board-table th, .board-table td { padding: 12px 10px; text-align: left; border-bottom: 1px solid #eee; font-size: 14px; }
.board-table th { background: #f5f5f5; font-weight: bold; }
.col-no { width: 60px; text-align: center; }
.col-author { width: 110px; }
.col-date { width: 100px; }
.col-views { width: 60px; text-align: center; }
.post-row { cursor: pointer; }
.post-row:hover { background: #f9f9f9; }
.title-cell { color: #1a1a1a; font-weight: 500; }
.comment-count { color: #1a73e8; font-size: 13px; margin-left: 4px; }
.empty { text-align: center; color: #999; padding: 40px; }
.error-msg { color: red; font-size: 13px; margin-top: 12px; }
.pagination { display: flex; justify-content: center; align-items: center; gap: 6px; margin-top: 24px; }
.page-btn { padding: 6px 14px; border: 1px solid #ddd; background: white; border-radius: 4px; cursor: pointer; font-size: 13px; }
.page-btn:disabled { color: #bbb; cursor: not-allowed; }
.page-btn:not(:disabled):hover { background: #f0f0f0; }
.page-num { width: 34px; height: 34px; border: 1px solid #ddd; background: white; border-radius: 4px; cursor: pointer; font-size: 13px; }
.page-num:hover { background: #f0f0f0; }
.page-num.active { background: #1a73e8; color: white; border-color: #1a73e8; font-weight: bold; }
</style>
