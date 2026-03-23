<template>
  <div class="board-container">
    <PageHeader title="Q&A" />

    <div class="list-card">
      <div class="card-header">
        <h2>Q&A 게시판</h2>
        <!-- 모든 로그인 사용자가 질문 작성 가능 -->
        <button @click="$router.push('/qna/write')" class="btn-write">질문하기</button>
      </div>

      <table class="board-table">
        <thead>
          <tr>
            <th class="col-no">번호</th>
            <th class="col-title">제목</th>
            <th class="col-author">작성자</th>
            <th class="col-date">작성일</th>
            <th class="col-answer">답변</th>
            <th class="col-views">조회</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="posts.length === 0">
            <td colspan="6" class="empty">등록된 질문이 없습니다.</td>
          </tr>
          <tr v-for="(post, index) in posts" :key="post.id" @click="goDetail(post.id)" class="post-row">
            <!-- 역순 번호: 전체 수 - 이전 페이지 항목 수 - 현재 인덱스 -->
            <td class="col-no">{{ totalCount - (pageNum - 1) * 10 - index }}</td>
            <td class="col-title title-cell">{{ post.title }}</td>
            <td class="col-author">{{ post.authorName }}</td>
            <td class="col-date">{{ formatDate(post.createdAt) }}</td>
            <td class="col-answer">
              <!-- 답변 수와 상태를 함께 표시 -->
              <span :class="post.answerCount > 0 ? 'badge-answered' : 'badge-waiting'">
                {{ post.answerCount > 0 ? `답변 ${post.answerCount}` : '답변대기' }}
              </span>
            </td>
            <td class="col-views">{{ post.views }}</td>
          </tr>
        </tbody>
      </table>

      <!-- 페이지네이션 -->
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
// 공통 페이지 헤더 컴포넌트
import PageHeader from '../common/PageHeader.vue'

export default {
  name: 'Qna',
  components: { PageHeader },
  data() {
    return {
      // Q&A 질문 목록
      posts: [],
      // 현재 페이지 번호
      pageNum: 1,
      // 전체 페이지 수
      totalPages: 1,
      // 전체 질문 수 (역순 번호 계산용)
      totalCount: 0
    }
  },
  computed: {
    // 현재 페이지 그룹의 페이지 번호 배열 (5개 단위)
    pageRange() {
      const start = Math.floor((this.pageNum - 1) / 5) * 5 + 1
      const end = Math.min(start + 4, this.totalPages)
      const range = []
      for (let i = start; i <= end; i++) range.push(i)
      return range
    }
  },
  mounted() {
    // 컴포넌트 마운트 시 질문 목록 조회
    this.fetchPosts()
  },
  methods: {
    // GET /api/qna 호출 → QnaController.java: 페이지별 질문 목록 조회
    async fetchPosts() {
      try {
        const res = await axios.get('/api/qna', { params: { pageNum: this.pageNum } })
        this.posts = res.data.list
        this.totalPages = res.data.pages
        this.totalCount = res.data.total
      } catch (e) {
        console.error('Q&A 목록 조회 실패:', e)
      }
    },
    // 페이지 번호 변경 후 목록 재조회
    changePage(p) {
      if (p < 1 || p > this.totalPages) return
      this.pageNum = p
      this.fetchPosts()
    },
    // 질문 상세 페이지로 이동
    goDetail(id) {
      this.$router.push(`/qna/${id}`)
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
.card-header h2 { margin: 0; font-size: 18px; }
.btn-write { padding: 8px 20px; background: #1a73e8; color: white; border: none; border-radius: 6px; font-size: 14px; cursor: pointer; }
.btn-write:hover { background: #1558b0; }
.board-table { width: 100%; border-collapse: collapse; }
.board-table th, .board-table td { padding: 12px 10px; text-align: left; border-bottom: 1px solid #eee; font-size: 14px; }
.board-table th { background: #f5f5f5; font-weight: bold; }
.col-no { width: 60px; text-align: center; }
.col-author { width: 100px; }
.col-date { width: 100px; }
.col-answer { width: 100px; text-align: center; }
.col-views { width: 60px; text-align: center; }
.post-row { cursor: pointer; }
.post-row:hover { background: #f9f9f9; }
.title-cell { color: #1a1a1a; font-weight: 500; }
/* 답변완료 배지 (녹색): 답변이 1개 이상일 때 "답변 N" 형태로 표시 */
.badge-answered {
  background: #e8f5e9; color: #2e7d32;
  border-radius: 12px; padding: 2px 10px; font-size: 12px; font-weight: bold;
}
/* 답변대기 배지 (주황색) */
.badge-waiting {
  background: #fff3e0; color: #e65100;
  border-radius: 12px; padding: 2px 10px; font-size: 12px; font-weight: bold;
}
.empty { text-align: center; color: #999; padding: 40px; }
.pagination { display: flex; justify-content: center; align-items: center; gap: 6px; margin-top: 24px; }
.page-btn { padding: 6px 14px; border: 1px solid #ddd; background: white; border-radius: 4px; cursor: pointer; font-size: 13px; }
.page-btn:disabled { color: #bbb; cursor: not-allowed; }
.page-btn:not(:disabled):hover { background: #f0f0f0; }
.page-num { width: 34px; height: 34px; border: 1px solid #ddd; background: white; border-radius: 4px; cursor: pointer; font-size: 13px; }
.page-num:hover { background: #f0f0f0; }
.page-num.active { background: #1a73e8; color: white; border-color: #1a73e8; font-weight: bold; }
</style>
