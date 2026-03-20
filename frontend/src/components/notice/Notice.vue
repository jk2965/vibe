<template>
  <div class="board-container">
    <PageHeader title="전체 공지사항" />

    <div class="list-card">
      <div class="card-header">
        <h2>공지사항 목록</h2>
        <button v-if="canWrite" @click="$router.push('/notice/write')" class="btn-write">글쓰기</button>
      </div>

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
    </div>
  </div>
</template>

<script>
import axios from 'axios'
// PageHeader.vue 공통 헤더 컴포넌트 사용
import PageHeader from '../common/PageHeader.vue'

export default {
  name: 'Notice',
  // PageHeader.vue 컴포넌트 등록
  components: { PageHeader },
  data() {
    // localStorage에서 관리자 레벨 읽기 (0: 일반, 1: 팀장/중간관리자, 2: 최고관리자)
    const adminLevel = parseInt(localStorage.getItem('adminLevel') || '0')
    // localStorage에서 팀장 여부 읽기
    const isTeamLeader = localStorage.getItem('isTeamLeader') === 'true'
    return {
      // 공지사항 목록 배열
      posts: [],
      // 현재 페이지 번호 (1부터 시작)
      pageNum: 1,
      // 전체 페이지 수
      totalPages: 1,
      // 전체 게시글 수 (역순 번호 계산에 사용)
      totalCount: 0,
      // 글쓰기 버튼 노출 여부: 관리자(레벨 1 이상) 또는 팀장인 경우 가능
      canWrite: adminLevel >= 1 || isTeamLeader
    }
  },
  computed: {
    // 현재 페이지 그룹에서 표시할 페이지 번호 배열 (최대 5개씩 표시)
    pageRange() {
      const start = Math.floor((this.pageNum - 1) / 5) * 5 + 1
      const end = Math.min(start + 4, this.totalPages)
      const range = []
      for (let i = start; i <= end; i++) range.push(i)
      return range
    }
  },
  // 컴포넌트 마운트 시 공지사항 목록 최초 로드
  mounted() {
    this.fetchPosts()
  },
  methods: {
    // GET /api/notice 호출 → NoticeController.java (페이지네이션 포함 목록 조회)
    async fetchPosts() {
      try {
        const res = await axios.get('http://localhost:8090/api/notice', {
          params: { pageNum: this.pageNum }
        })
        // 응답에서 게시글 목록, 전체 페이지 수, 전체 게시글 수 저장
        this.posts = res.data.list
        this.totalPages = res.data.pages
        this.totalCount = res.data.total
      } catch (e) {
        console.error('공지사항 목록 조회 실패:', e)
      }
    },
    // 페이지 변경: 유효 범위 체크 후 해당 페이지 데이터 재조회
    changePage(p) {
      if (p < 1 || p > this.totalPages) return
      this.pageNum = p
      this.fetchPosts()
    },
    // 공지사항 상세 페이지로 이동 → NoticeDetail.vue
    goDetail(id) {
      this.$router.push(`/notice/${id}`)
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
.card-header h2 { margin: 0; font-size: 18px; }
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
.pagination { display: flex; justify-content: center; align-items: center; gap: 6px; margin-top: 24px; }
.page-btn { padding: 6px 14px; border: 1px solid #ddd; background: white; border-radius: 4px; cursor: pointer; font-size: 13px; }
.page-btn:disabled { color: #bbb; cursor: not-allowed; }
.page-btn:not(:disabled):hover { background: #f0f0f0; }
.page-num { width: 34px; height: 34px; border: 1px solid #ddd; background: white; border-radius: 4px; cursor: pointer; font-size: 13px; }
.page-num:hover { background: #f0f0f0; }
.page-num.active { background: #1a73e8; color: white; border-color: #1a73e8; font-weight: bold; }
</style>
