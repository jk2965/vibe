<template>
  <div class="faq-container">
    <PageHeader title="FAQ" />

    <div class="faq-card">
      <div class="card-header">
        <h2>자주 묻는 질문</h2>
        <button v-if="isAdmin" @click="$router.push('/faq/write')" class="btn-write">FAQ 등록</button>
      </div>

      <div v-if="faqs.length === 0" class="empty">등록된 질문이 없습니다.</div>

      <div v-for="(faq, index) in faqs" :key="faq.id" class="accordion-item" :class="{ open: openId === faq.id }">
        <!-- 질문 헤더 (클릭 영역) -->
        <div class="accordion-header" @click="toggle(faq)">
          <span class="q-num">Q.{{ String(index + 1).padStart(2, '0') }}</span>
          <span class="q-title">{{ faq.title }}</span>
          <!-- 첨부파일이 있는 경우 파일명과 개수 표시 -->
          <span v-if="faq.fileCount > 0" class="faq-file-badge">
            📎 {{ faq.firstFileName }}<span v-if="faq.fileCount > 1"> 외 {{ faq.fileCount - 1 }}개</span>
          </span>
          <span class="toggle-btn">{{ openId === faq.id ? '닫기 ∧' : '열기 ∨' }}</span>
        </div>

        <!-- 펼쳐진 내용 -->
        <div v-if="openId === faq.id" class="accordion-body">
          <div v-if="faq.fullContent">
            <div v-if="faq.fullContent.content" class="answer-block">
              <div class="answer-text tiptap-display" v-html="faq.fullContent.content"></div>
            </div>
            <div v-else class="no-answer">아직 답변이 등록되지 않았습니다.</div>

            <!-- 수정/삭제 버튼 (관리자 또는 작성자) -->
            <div v-if="isAdmin || isAuthor(faq)" class="body-actions">
              <div class="right-actions">
                <button v-if="isAdmin || isAuthor(faq)" @click.stop="$router.push(`/faq/edit/${faq.id}`)" class="btn-edit">수정</button>
                <button v-if="canDelete(faq)" @click.stop="confirmDelete(faq.id)" class="btn-delete">삭제</button>
              </div>
            </div>
          </div>
          <div v-else class="loading-inner">불러오는 중...</div>
        </div>
      </div>

      <!-- 페이징 -->
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
// 공통 페이지 헤더 컴포넌트 (PageHeader.vue)
import PageHeader from '../common/PageHeader.vue'
// 코드 블록 신택스 하이라이팅 라이브러리 (highlight.js) - Tiptap 에디터 본문의 코드 블록 강조
import hljs from 'highlight.js'

export default {
  name: 'Faq',
  components: { PageHeader },
  data() {
    return {
      // FAQ 목록 (각 항목에 fullContent: null을 추가해 lazy loading 구현)
      faqs: [],
      // 현재 페이지 번호
      pageNum: 1,
      // 전체 페이지 수
      totalPages: 1,
      // 현재 펼쳐진(아코디언) FAQ 항목 ID (한 번에 하나만 펼침)
      openId: null
    }
  },
  computed: {
    // 현재 로그인한 사용자 ID
    userId() { return localStorage.getItem('userId') || '' },
    // 현재 사용자의 관리자 레벨
    adminLevel() { return parseInt(localStorage.getItem('adminLevel') || '0') },
    // 관리자 여부 (adminLevel 1 이상이면 FAQ 등록/수정/삭제 가능)
    isAdmin() { return this.adminLevel >= 1 },
    // 페이지네이션: 5개 단위로 페이지 번호 배열 계산
    pageRange() {
      const start = Math.floor((this.pageNum - 1) / 5) * 5 + 1
      const end = Math.min(start + 4, this.totalPages)
      const range = []
      for (let i = start; i <= end; i++) range.push(i)
      return range
    }
  },
  // 컴포넌트 마운트 시 FAQ 목록 조회
  mounted() {
    this.fetchFaqs()
  },
  // DOM 업데이트 후 highlight.js로 코드 블록 재하이라이팅 (아코디언 펼칠 때마다 실행)
  updated() { this.$nextTick(() => hljs.highlightAll()) },
  methods: {
    // GET /api/faq 호출 → FaqController.java
    // 페이지별 FAQ 목록 조회, fullContent는 아코디언 열 때 lazy loading
    async fetchFaqs() {
      try {
        const res = await axios.get('/api/faq', { params: { pageNum: this.pageNum } })
        // 각 FAQ에 fullContent: null 추가 (상세 내용 lazy loading용)
        this.faqs = res.data.list.map(f => ({ ...f, fullContent: null }))
        this.totalPages = res.data.pages
        // 페이지 변경 시 열린 아코디언 초기화
        this.openId = null
      } catch (e) {
        console.error('FAQ 목록 조회 실패:', e)
      }
    },
    // 아코디언 토글: 같은 항목 클릭 시 닫고, 다른 항목 클릭 시 열기
    // GET /api/faq/:id 호출 → FaqController.java (fullContent가 없을 때만 상세 조회)
    async toggle(faq) {
      if (this.openId === faq.id) {
        // 이미 열린 항목 클릭 시 닫기
        this.openId = null
        return
      }
      this.openId = faq.id
      // fullContent가 null인 경우에만 상세 내용 API 호출 (중복 호출 방지)
      if (!faq.fullContent) {
        try {
          const res = await axios.get(`/api/faq/${faq.id}`)
          faq.fullContent = res.data
        } catch (e) {
          console.error('FAQ 상세 조회 실패:', e)
        }
      }
    },
    // 페이지 번호 클릭 시 해당 페이지로 이동하고 목록 재조회
    changePage(p) {
      if (p < 1 || p > this.totalPages) return
      this.pageNum = p
      this.fetchFaqs()
    },
    // 현재 사용자가 해당 FAQ의 작성자인지 확인
    isAuthor(faq) {
      return faq.authorId === this.userId
    },
    // 삭제 권한: 작성자 본인 또는 관리자
    canDelete(faq) {
      return faq.authorId === this.userId || this.isAdmin
    },
    // DELETE /api/faq/:id 호출 → FaqController.java
    // 삭제 확인 후 FAQ 삭제, 성공 시 목록 새로고침
    async confirmDelete(id) {
      if (!confirm('질문을 삭제하시겠습니까?')) return
      try {
        await axios.delete(`/api/faq/${id}`, { params: { requesterId: this.userId } })
        this.fetchFaqs()
      } catch (e) {
        alert(e.response?.data?.message || '삭제 중 오류가 발생했습니다.')
      }
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
.faq-container { max-width: 900px; margin: 0 auto; padding: 24px; }

.faq-card { background: white; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.08); overflow: hidden; }

.card-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 20px 24px; border-bottom: 2px solid #6a1b9a;
}
.card-header h2 { margin: 0; font-size: 18px; }

.btn-write { padding: 8px 20px; background: #1a73e8; color: white; border: none; border-radius: 6px; font-size: 14px; cursor: pointer; }
.btn-write:hover { background: #1558b0; }

.empty { text-align: center; color: #999; padding: 60px; }

/* 아코디언 아이템 */
.accordion-item { border-bottom: 1px solid #e0e0e0; }
.accordion-item:last-of-type { border-bottom: none; }

.accordion-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 18px 24px;
  cursor: pointer;
  transition: background 0.15s;
}
.accordion-header:hover { background: #f9f4ff; }
.accordion-item.open .accordion-header { background: #f3e5f5; }

.q-num {
  font-size: 14px;
  font-weight: bold;
  color: #6a1b9a;
  min-width: 48px;
  padding: 4px 8px;
  background: #f3e5f5;
  border-radius: 4px;
  text-align: center;
}

.q-title { flex: 1; font-size: 15px; color: #222; font-weight: 500; }

.faq-file-badge {
  font-size: 12px;
  color: #555;
  background: #f5f5f5;
  border-radius: 4px;
  padding: 2px 8px;
  white-space: nowrap;
  flex-shrink: 0;
}

.toggle-btn {
  font-size: 13px;
  color: #6a1b9a;
  font-weight: bold;
  white-space: nowrap;
}

/* 펼쳐진 내용 */
.accordion-body { background: #fafafa; padding: 20px 24px; border-top: 1px solid #e0e0e0; }

.answer-block { display: flex; gap: 14px; margin-bottom: 16px; align-items: flex-start; }
.answer-badge {
  min-width: 40px; height: 40px; border-radius: 50%;
  background: #6a1b9a; color: white;
  display: flex; align-items: center; justify-content: center;
  font-size: 13px; font-weight: bold; flex-shrink: 0;
}
.answer-text { flex: 1; font-size: 14px; line-height: 1.7; color: #333; }
.answer-meta { font-size: 12px; color: #999; margin-top: 4px; }

.no-answer { color: #bbb; font-size: 14px; padding: 8px 0 16px; }

.question-content {
  font-size: 14px; line-height: 1.7; color: #555;
  padding: 12px 16px;
  background: white; border-radius: 6px; border: 1px solid #eee;
  margin-bottom: 14px;
}

.body-actions { display: flex; justify-content: flex-end; margin-top: 10px; }
.right-actions { display: flex; gap: 8px; }

.btn-answer-manage {
  padding: 5px 14px; background: #1565c0; color: white;
  border: none; border-radius: 4px; font-size: 12px; cursor: pointer;
}
.btn-answer-manage:hover { filter: brightness(0.9); }
.btn-edit {
  padding: 5px 14px; background: #6a1b9a; color: white;
  border: none; border-radius: 4px; font-size: 12px; cursor: pointer;
}
.btn-edit:hover { filter: brightness(0.9); }
.btn-delete {
  padding: 5px 14px; background: #c62828; color: white;
  border: none; border-radius: 4px; font-size: 12px; cursor: pointer;
}
.btn-delete:hover { filter: brightness(0.9); }

.loading-inner { text-align: center; color: #bbb; padding: 20px; font-size: 14px; }

/* 페이징 */
.pagination { display: flex; justify-content: center; align-items: center; gap: 6px; padding: 20px; }
.page-btn { padding: 6px 14px; border: 1px solid #ddd; background: white; border-radius: 4px; cursor: pointer; font-size: 13px; }
.page-btn:disabled { color: #bbb; cursor: not-allowed; }
.page-btn:not(:disabled):hover { background: #f0f0f0; }
.page-num { width: 34px; height: 34px; border: 1px solid #ddd; background: white; border-radius: 4px; cursor: pointer; font-size: 13px; }
.page-num:hover { background: #f0f0f0; }
.page-num.active { background: #1a73e8; color: white; border-color: #1a73e8; font-weight: bold; }
</style>

<style>
.tiptap-display p { margin: 0 0 6px 0; }
.tiptap-display ul, .tiptap-display ol { padding-left: 20px; margin: 4px 0; }
.tiptap-display strong { font-weight: bold; }
.tiptap-display em { font-style: italic; }
.tiptap-display u { text-decoration: underline; }
.tiptap-display img { max-width: 100%; border-radius: 4px; }
.tiptap-display pre { background: #f4f4f4; border-radius: 4px; padding: 12px; overflow-x: auto; margin: 8px 0; }
.tiptap-display pre code.hljs { background: #f4f4f4; border-radius: 4px; font-size: 13px; padding: 0; }
/* 표(테이블) 스타일 */
.tiptap-display table { border-collapse: collapse; width: 100%; margin: 12px 0; }
.tiptap-display th, .tiptap-display td { border: 1px solid #ccc; padding: 8px 10px; font-size: 14px; }
.tiptap-display th { background: #f5f5f5; font-weight: bold; }
</style>
