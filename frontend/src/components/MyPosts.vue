<template>
  <div class="board-container">
    <PageHeader title="내 게시글" />

    <div class="list-card">
      <div class="card-header">
        <h2>내가 작성한 게시글 목록</h2>
      </div>

      <!-- 전체 게시글 통합 테이블 -->
      <table class="board-table">
        <thead>
          <tr>
            <th class="col-no">번호</th>
            <th class="col-board">게시판</th>
            <th class="col-title">제목</th>
            <th class="col-date">작성일</th>
            <th class="col-views">조회</th>
          </tr>
        </thead>
        <tbody>
          <!-- 게시글 없을 때 -->
          <tr v-if="flatList.length === 0">
            <td colspan="5" class="empty-cell">작성한 게시글이 없습니다.</td>
          </tr>
          <!-- 현재 페이지 게시글 목록 -->
          <tr
            v-for="(post, index) in pagedList"
            :key="post.boardPath + '-' + post.id"
            class="post-row"
            @click="$router.push(post.boardPath + '/' + post.id)"
          >
            <td class="col-no">{{ flatList.length - ((pageNum - 1) * 10 + index) }}</td>
            <td class="col-board"><span class="board-badge">{{ post.boardLabel }}</span></td>
            <td class="col-title title-cell">
              <span v-if="post.isRequired" class="required-badge">📌</span>
              {{ post.title }}
            </td>
            <td class="col-date">{{ formatDate(post.createdAt) }}</td>
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
// axios: 서버 API 요청에 사용
import axios from 'axios'
// PageHeader.vue: 페이지 상단 제목 표시 공통 컴포넌트
import PageHeader from './common/PageHeader.vue'

// 내가 작성한 게시글을 전체 게시판에 걸쳐 조회하는 페이지 - router/index.js의 /my-posts 경로에 대응
// MyPostsController.java의 GET /api/my-posts 엔드포인트를 통해 데이터 수신
export default {
  name: 'MyPosts',
  // PageHeader: 페이지 상단 '내 게시글' 제목 표시
  components: { PageHeader },
  data() {
    return {
      // 게시판별 내 게시글 목록 (fetchMyPosts 호출 후 서버 응답으로 채워짐)
      posts: {
        notice:      [],  // 전체 공지사항 내 게시글
        teamNotice:  [],  // 팀별 공지사항 내 게시글
        board:       [],  // 자유 게시판 내 게시글
        archive:     [],  // 전사 자료실 내 게시글
        teamArchive: []   // 팀별 자료실 내 게시글
      },
      // 현재 페이지 번호 (1부터 시작)
      pageNum: 1
    }
  },
  computed: {
    // 게시판별 섹션 정의 (teamBased: 팀별 게시판 여부, teamLabel: 팀명 뒤에 붙을 접미사)
    sections() {
      return [
        { key: 'notice',      label: '공지사항',  path: '/notice' },
        { key: 'teamNotice',  label: '팀공지사항', path: '/team-notice', teamBased: true, teamLabel: ' 공지사항' },
        { key: 'board',       label: '자유게시판', path: '/board' },
        { key: 'archive',     label: '전체자료실', path: '/archive' },
        { key: 'teamArchive', label: '팀자료실',  path: '/team-archive', teamBased: true, teamLabel: ' 자료실' }
      ]
    },
    // 게시판별 목록을 하나의 배열로 병합 (팀별 게시판은 "X팀 공지사항" 형식으로 boardLabel 생성)
    flatList() {
      const list = []
      for (const section of this.sections) {
        for (const post of (this.posts[section.key] || [])) {
          const boardLabel = section.teamBased && post.team
            ? post.team + section.teamLabel
            : section.label
          list.push({ ...post, boardLabel, boardPath: section.path })
        }
      }
      return list
    },
    // 전체 페이지 수 계산 (최소 1)
    totalPages() {
      return Math.max(1, Math.ceil(this.flatList.length / 10))
    },
    // 현재 페이지에 표시할 게시글 목록 (10개 단위 슬라이스)
    pagedList() {
      const start = (this.pageNum - 1) * 10
      return this.flatList.slice(start, start + 10)
    },
    // 페이지네이션 버튼 번호 배열 (5개 단위 그룹)
    pageRange() {
      const start = Math.floor((this.pageNum - 1) / 5) * 5 + 1
      const end = Math.min(start + 4, this.totalPages)
      const range = []
      for (let i = start; i <= end; i++) range.push(i)
      return range
    }
  },
  // 컴포넌트 마운트 시 내 게시글 목록 로드
  mounted() {
    this.fetchMyPosts()
  },
  methods: {
    // GET /api/my-posts?userId={id} 로 내 게시글 전체 목록 조회 후 게시판별로 분류하여 posts에 저장
    async fetchMyPosts() {
      try {
        // localStorage에 저장된 로그인 사용자 ID로 내 게시글 조회
        const userId = localStorage.getItem('userId') || ''
        const res = await axios.get('http://localhost:8090/api/my-posts', { params: { userId } })
        // 서버 응답을 게시판별로 분리하여 저장 (null/undefined 방지를 위해 || [] 사용)
        this.posts = {
          notice:      res.data.notice      || [],
          teamNotice:  res.data.teamNotice  || [],
          board:       res.data.board       || [],
          archive:     res.data.archive     || [],
          teamArchive: res.data.teamArchive || []
        }
        // 데이터 갱신 시 첫 페이지로 초기화
        this.pageNum = 1
      } catch (e) {
        console.error('내 게시글 조회 실패:', e)
      }
    },
    // 페이지 변경: 유효 범위 체크 후 해당 페이지로 이동
    changePage(p) {
      if (p < 1 || p > this.totalPages) return
      this.pageNum = p
    },
    // ISO 형식 날짜 문자열에서 YYYY-MM-DD 앞 10자리만 추출하여 표시
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

.board-table { width: 100%; border-collapse: collapse; margin-bottom: 8px; }
.board-table th, .board-table td { padding: 12px 10px; text-align: left; border-bottom: 1px solid #eee; font-size: 14px; }
.board-table th { background: #f5f5f5; font-weight: bold; }
.col-no { width: 60px; text-align: center; }
.col-board { width: 90px; }
.col-date { width: 100px; }
.col-views { width: 60px; text-align: center; }

.board-badge {
  display: inline-block;
  background: #e8eaf6;
  color: #3949ab;
  border-radius: 4px;
  padding: 2px 6px;
  font-size: 12px;
  white-space: nowrap;
}

.post-row { cursor: pointer; }
.post-row:hover { background: #f9f9f9; }
.title-cell { color: #1a1a1a; font-weight: 500; }
.required-badge { font-size: 13px; margin-right: 4px; }

.empty-cell { text-align: center; color: #999; padding: 60px; font-size: 15px; }

.pagination { display: flex; justify-content: center; align-items: center; gap: 6px; margin-top: 24px; }
.page-btn { padding: 6px 14px; border: 1px solid #ddd; background: white; border-radius: 4px; cursor: pointer; font-size: 13px; }
.page-btn:disabled { color: #bbb; cursor: not-allowed; }
.page-btn:not(:disabled):hover { background: #f0f0f0; }
.page-num { width: 34px; height: 34px; border: 1px solid #ddd; background: white; border-radius: 4px; cursor: pointer; font-size: 13px; }
.page-num:hover { background: #f0f0f0; }
.page-num.active { background: #1a73e8; color: white; border-color: #1a73e8; font-weight: bold; }
</style>
