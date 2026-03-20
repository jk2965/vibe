<template>
  <div class="board-container">
    <PageHeader :title="selectedTeam ? selectedTeam + ' 자료실' : '팀별 자료실'" />

    <div class="list-card">
      <div class="card-header">
        <div class="header-left">
          <h2>{{ selectedTeam ? selectedTeam + ' 자료실' : '팀별 자료실' }}</h2>
          <select v-if="isSuperAdmin" v-model="selectedTeam" @change="onTeamChange" class="team-select">
            <option value="1팀">1팀</option>
            <option value="2팀">2팀</option>
          </select>
        </div>
        <button v-if="selectedTeam && canWrite" @click="$router.push('/team-archive/write?team=' + selectedTeam)" class="btn-write">글쓰기</button>
      </div>

      <template v-if="true">
        <table class="board-table">
          <thead>
            <tr>
              <th class="col-no">번호</th>
              <th class="col-title">제목</th>
              <th class="col-author">작성자</th>
              <th class="col-date">작성일</th>
              <th class="col-file">파일</th>
              <th class="col-views">조회</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="posts.length === 0">
              <td colspan="6" class="empty">등록된 자료가 없습니다.</td>
            </tr>
            <tr v-for="(post, index) in posts" :key="post.id" @click="goDetail(post.id)" class="post-row">
              <td class="col-no">{{ totalCount - (pageNum - 1) * 10 - index }}</td>
              <td class="col-title title-cell">
                {{ post.title }}
                <span v-if="post.commentCount > 0" class="comment-count">[{{ post.commentCount }}]</span>
                <span v-if="post.fileCount > 0" class="file-badge">📎{{ post.fileCount }}</span>
              </td>
              <td class="col-author">{{ post.authorName }}</td>
              <td class="col-date">{{ formatDate(post.createdAt) }}</td>
              <td class="col-file">{{ post.fileCount || 0 }}</td>
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
// 공통 페이지 헤더 컴포넌트 (PageHeader.vue)
import PageHeader from '../common/PageHeader.vue'

export default {
  name: 'TeamArchive',
  components: { PageHeader },
  data() {
    // 로컬스토리지에서 현재 로그인 사용자의 권한 정보 읽기
    const adminLevel = parseInt(localStorage.getItem('adminLevel') || '0')
    // 현재 사용자가 소속된 팀 이름
    const userTeam = localStorage.getItem('team') || ''
    // 팀장 여부 (localStorage의 'isTeamLeader' 값)
    const isTeamLeader = localStorage.getItem('isTeamLeader') === 'true'
    // 슈퍼 관리자 여부 (adminLevel 2 이상)
    const isSuperAdmin = adminLevel >= 2
    return {
      // 자료실 게시글 목록
      posts: [],
      // 현재 페이지 번호
      pageNum: 1,
      // 전체 페이지 수
      totalPages: 1,
      // 전체 게시글 수 (번호 역순 계산용)
      totalCount: 0,
      // 현재 선택된 팀 (슈퍼 관리자는 '1팀' 기본, 일반 사용자는 본인 팀)
      selectedTeam: isSuperAdmin ? '1팀' : userTeam,
      // 일반 관리자 여부 (adminLevel 1 이상)
      isAdmin: adminLevel >= 1,
      // 슈퍼 관리자 여부
      isSuperAdmin,
      // 팀장 여부
      isTeamLeader,
      // 사용자 소속 팀명
      userTeam,
      // 오류 메시지
      errorMsg: ''
    }
  },
  computed: {
    // 글쓰기 권한: 슈퍼 관리자, 일반 관리자, 팀장만 가능
    canWrite() {
      return this.isSuperAdmin || this.isAdmin || this.isTeamLeader
    },
    // 페이지네이션: 현재 페이지 기준으로 5개씩 표시할 페이지 번호 배열 계산
    pageRange() {
      const start = Math.floor((this.pageNum - 1) / 5) * 5 + 1
      const end = Math.min(start + 4, this.totalPages)
      const range = []
      for (let i = start; i <= end; i++) range.push(i)
      return range
    }
  },
  // 컴포넌트 마운트 시 선택된 팀이 있으면 게시글 목록 조회
  mounted() {
    if (this.selectedTeam) this.fetchPosts()
  },
  methods: {
    // 팀 선택 변경 시 페이지 초기화 후 재조회
    onTeamChange() {
      this.pageNum = 1
      this.posts = []
      this.totalCount = 0
      this.totalPages = 1
      if (this.selectedTeam) this.fetchPosts()
    },
    // GET /api/team-archive 호출 → TeamArchiveController.java
    // team, requesterId, pageNum 파라미터로 팀별 자료실 목록 페이징 조회
    async fetchPosts() {
      this.errorMsg = ''
      try {
        const res = await axios.get('http://localhost:8090/api/team-archive', {
          params: {
            team: this.selectedTeam,
            requesterId: localStorage.getItem('userId'),
            pageNum: this.pageNum
          }
        })
        // 게시글 목록, 전체 페이지 수, 전체 게시글 수 저장
        this.posts = res.data.list
        this.totalPages = res.data.pages
        this.totalCount = res.data.total
      } catch (e) {
        this.errorMsg = e.response?.data?.message || '자료실 목록 조회 실패'
      }
    },
    // 페이지 번호 클릭 시 해당 페이지로 이동하고 목록 재조회
    changePage(p) {
      if (p < 1 || p > this.totalPages) return
      this.pageNum = p
      this.fetchPosts()
    },
    // 게시글 행 클릭 시 상세 페이지(/team-archive/:id)로 이동
    goDetail(id) {
      this.$router.push(`/team-archive/${id}`)
    },
    // 날짜 문자열에서 'YYYY-MM-DD' 형식만 추출
    formatDate(dateStr) {
      if (!dateStr) return '-'
      return dateStr.substring(0, 10)
    }
  }
}
</script>

<style scoped>
.board-container { max-width: 960px; margin: 0 auto; padding: 24px; }
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
.col-file { width: 50px; text-align: center; }
.col-views { width: 60px; text-align: center; }
.post-row { cursor: pointer; }
.post-row:hover { background: #f9f9f9; }
.title-cell { color: #1a1a1a; font-weight: 500; }
.comment-count { color: #1a73e8; font-size: 13px; margin-left: 4px; }
.file-badge { font-size: 12px; color: #888; margin-left: 6px; }
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
