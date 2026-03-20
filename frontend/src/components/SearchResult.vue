<template>
  <div class="board-container">
    <PageHeader :title="headerTitle" />

    <div class="list-card">
      <div class="card-header">
        <!-- 검색 타입과 키워드를 조합한 결과 설명 문구 -->
        <h2>
          <span class="search-type-badge" :class="searchType">
            {{ searchType === 'title' ? '제목' : '태그' }}
          </span>
          "{{ keyword }}" 검색 결과
        </h2>
      </div>

      <!-- 검색 결과가 전혀 없을 때 표시 -->
      <div v-if="!hasAny" class="empty">검색 결과가 없습니다.</div>

      <!-- 검색 결과가 있을 때 통합 테이블로 표시 -->
      <template v-else>
        <!-- 전체 건수 표시 -->
        <div class="total-count">총 {{ flatList.length }}건</div>

        <!-- 검색 결과 통합 테이블 -->
        <table class="board-table">
          <thead>
            <tr>
              <th class="col-no">번호</th>
              <th class="col-board">게시판</th>
              <th class="col-title">제목</th>
              <th class="col-tags">태그</th>
              <th class="col-author">작성자</th>
              <th class="col-date">작성일</th>
              <th class="col-views">조회</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="(post, index) in pagedList"
              :key="post.boardPath + '-' + post.id"
              class="post-row"
              @click="$router.push(post.boardPath + '/' + post.id)"
            >
              <td class="col-no">{{ flatList.length - ((pageNum - 1) * 10 + index) }}</td>
              <td class="col-board"><span class="board-badge">{{ post.boardLabel }}</span></td>
              <td class="col-title title-cell">
                <!-- 필독 게시글 아이콘 -->
                <span v-if="post.isRequired" class="required-badge">📌</span>
                <!-- 제목 검색일 때 키워드 하이라이트 -->
                <span v-if="searchType === 'title'" v-html="highlight(post.title)"></span>
                <span v-else>{{ post.title }}</span>
              </td>
              <td class="col-tags">
                <!-- 태그 목록 표시 (있는 경우에만), 태그 검색 시 일치 태그 하이라이트 -->
                <template v-if="post.tags">
                  <span
                    v-for="tag in parseTags(post.tags)"
                    :key="tag"
                    class="tag-chip"
                    :class="{ highlight: searchType === 'tag' && tag.includes(keyword) }"
                  >{{ tag }}</span>
                </template>
              </td>
              <td class="col-author">{{ post.authorName }}</td>
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
      </template>
    </div>
  </div>
</template>

<script>
// axios: 서버 검색 API 요청에 사용
import axios from 'axios'
// PageHeader.vue: 페이지 상단 제목 표시 공통 컴포넌트
import PageHeader from './common/PageHeader.vue'

// 게시글 통합 검색 결과 페이지 - router/index.js의 /search 경로에 대응
// Sidebar.vue에서 검색 시 이 페이지로 이동, SearchController.java의 GET /api/search 엔드포인트에서 데이터 수신
export default {
  name: 'SearchResult',
  // PageHeader: 페이지 상단 제목 표시
  components: { PageHeader },
  data() {
    return {
      // 게시판별 검색 결과 목록 (fetchResults 호출 후 서버 응답으로 채워짐)
      results: {
        notice:      [],  // 전체 공지사항 검색 결과
        teamNotice:  [],  // 팀별 공지사항 검색 결과
        board:       [],  // 자유 게시판 검색 결과
        archive:     [],  // 전사 자료실 검색 결과
        teamArchive: []   // 팀별 자료실 검색 결과
      },
      // 현재 페이지 번호 (1부터 시작)
      pageNum: 1
    }
  },
  computed: {
    // URL 쿼리 파라미터에서 검색 타입 읽기 ('title' 또는 'tag', 기본값 'title')
    searchType() {
      return this.$route.query.type || 'title'
    },
    // URL 쿼리 파라미터에서 검색 키워드 읽기
    keyword() {
      return this.$route.query.keyword || ''
    },
    // 페이지 헤더에 표시할 제목 (검색 타입에 따라 변경)
    headerTitle() {
      return this.searchType === 'title' ? '제목 검색' : '태그 검색'
    },
    // 검색 결과가 하나라도 있는지 여부 (빈 화면 분기에 사용)
    hasAny() {
      return Object.values(this.results).some(list => list && list.length > 0)
    },
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
    // 게시판별 검색 결과를 하나의 배열로 병합 (팀별 게시판은 "X팀 공지사항" 형식으로 boardLabel 생성)
    flatList() {
      const list = []
      for (const section of this.sections) {
        for (const post of (this.results[section.key] || [])) {
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
    // 현재 페이지에 표시할 검색 결과 목록 (10개 단위 슬라이스)
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
  // 컴포넌트 마운트 시 검색 실행
  mounted() {
    this.fetchResults()
  },
  watch: {
    // 같은 /search 페이지에서 검색 조건이 바뀔 때(예: 사이드바에서 재검색) 자동으로 재조회
    '$route.query'() {
      this.fetchResults()
    }
  },
  methods: {
    // GET /api/search?type={type}&keyword={keyword} 로 통합 검색 결과 조회
    async fetchResults() {
      // 검색어가 없으면 결과 초기화 후 종료
      if (!this.keyword.trim()) {
        this.results = { notice: [], teamNotice: [], board: [], archive: [], teamArchive: [] }
        return
      }
      try {
        const res = await axios.get('http://localhost:8090/api/search', {
          params: { type: this.searchType, keyword: this.keyword }
        })
        // 서버 응답을 게시판별로 분리하여 저장 (null/undefined 방지를 위해 || [] 사용)
        this.results = {
          notice:      res.data.notice      || [],
          teamNotice:  res.data.teamNotice  || [],
          board:       res.data.board       || [],
          archive:     res.data.archive     || [],
          teamArchive: res.data.teamArchive || []
        }
        // 새 검색 시 첫 페이지로 초기화
        this.pageNum = 1
      } catch (e) {
        console.error('검색 실패:', e)
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
    },
    // 쉼표로 구분된 태그 문자열을 배열로 파싱 (공백 제거 후 빈 문자열 필터링)
    parseTags(tagsStr) {
      if (!tagsStr) return []
      return tagsStr.split(',').map(t => t.trim()).filter(Boolean)
    },
    // 텍스트에서 검색 키워드를 찾아 <mark> 태그로 감싸 하이라이트 표시
    // XSS 방지를 위해 키워드 삽입 전 원본 텍스트를 이스케이프 처리
    highlight(text) {
      if (!text || !this.keyword) return text
      // 특수문자 이스케이프 후 키워드를 정규식으로 치환
      const escaped = text.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
      const safeKeyword = this.keyword.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
      return escaped.replace(new RegExp(safeKeyword, 'gi'), match => `<mark>${match}</mark>`)
    }
  }
}
</script>

<style scoped>
.board-container { max-width: 1000px; margin: 0 auto; padding: 24px; }

.list-card { background: white; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.08); padding: 24px; }
.card-header { margin-bottom: 12px; }
.card-header h2 { margin: 0; font-size: 17px; display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }

/* 검색 타입 배지 (제목/태그 구분 색상) */
.search-type-badge {
  font-size: 12px;
  padding: 2px 10px;
  border-radius: 12px;
  font-weight: bold;
}
.search-type-badge.title { background: #e3f2fd; color: #1565c0; }
.search-type-badge.tag   { background: #e8f5e9; color: #2e7d32; }

/* 전체 건수 표시 */
.total-count { font-size: 13px; color: #888; margin-bottom: 12px; }

.board-table { width: 100%; border-collapse: collapse; margin-bottom: 8px; }
.board-table th, .board-table td { padding: 10px 8px; text-align: left; border-bottom: 1px solid #eee; font-size: 14px; }
.board-table th { background: #f5f5f5; font-weight: bold; }
.col-no     { width: 50px; text-align: center; }
.col-board  { width: 80px; }
.col-title  { min-width: 160px; }
.col-tags   { width: 160px; }
.col-author { width: 80px; }
.col-date   { width: 90px; }
.col-views  { width: 50px; text-align: center; }

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

/* 태그 칩 스타일 */
.tag-chip {
  display: inline-block;
  background: #e3f2fd;
  color: #1565c0;
  border-radius: 12px;
  padding: 2px 8px;
  font-size: 12px;
  margin: 2px 2px;
}
/* 검색 키워드와 일치하는 태그 강조 */
.tag-chip.highlight { background: #fff9c4; color: #f57f17; border: 1px solid #f9a825; }

/* 제목 내 키워드 하이라이트 */
:deep(mark) {
  background: #fff176;
  color: inherit;
  border-radius: 2px;
  padding: 0 1px;
}

.empty { text-align: center; color: #999; padding: 60px; font-size: 15px; }

.pagination { display: flex; justify-content: center; align-items: center; gap: 6px; margin-top: 24px; }
.page-btn { padding: 6px 14px; border: 1px solid #ddd; background: white; border-radius: 4px; cursor: pointer; font-size: 13px; }
.page-btn:disabled { color: #bbb; cursor: not-allowed; }
.page-btn:not(:disabled):hover { background: #f0f0f0; }
.page-num { width: 34px; height: 34px; border: 1px solid #ddd; background: white; border-radius: 4px; cursor: pointer; font-size: 13px; }
.page-num:hover { background: #f0f0f0; }
.page-num.active { background: #1a73e8; color: white; border-color: #1a73e8; font-weight: bold; }
</style>
