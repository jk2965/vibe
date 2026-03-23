<template>
  <div class="faq-detail-container">
    <PageHeader title="FAQ" />

    <div v-if="faq" class="detail-card">
      <!-- 질문 영역 -->
      <div class="question-section">
        <div class="section-label q-label">Q</div>
        <div class="section-body">
          <h2 class="faq-title">{{ faq.title }}</h2>
          <div class="meta">
            <span>{{ faq.authorName }}</span>
            <span class="sep">·</span>
            <span>{{ formatDate(faq.createdAt) }}</span>
            <span class="sep">·</span>
            <span>조회 {{ faq.views }}</span>
          </div>
          <div class="faq-content tiptap-display" v-html="faq.content"></div>
          <!-- 첨부파일 목록: FAQ 파일은 /api/faq/files/... 엔드포인트 사용 -->
          <FileList
            v-if="faq.files && faq.files.length > 0"
            :files="faq.files"
            :canDelete="canDelete"
            apiBase="/api/faq"
            @file-deleted="handleFileDeleted"
          />
        </div>
      </div>

      <!-- 답변 영역 -->
      <div v-if="faq.answer" class="answer-section">
        <div class="section-label a-label">A</div>
        <div class="section-body">
          <div class="answer-meta">
            <span class="answered-by">{{ faq.answeredBy }}</span>
            <span class="sep">·</span>
            <span>{{ formatDate(faq.answeredAt) }}</span>
          </div>
          <div class="faq-content tiptap-display" v-html="faq.answer"></div>
        </div>
      </div>
      <div v-else class="no-answer">아직 답변이 등록되지 않았습니다.</div>

      <!-- 관리자 답변 입력 -->
      <div v-if="isAdmin" class="answer-input-section">
        <h3 class="answer-input-title">{{ faq.answer ? '답변 수정' : '답변 등록' }}</h3>
        <TiptapEditor v-model="answerContent" :imageUploadUrl="null" />
        <div v-if="answerError" class="error-msg">{{ answerError }}</div>
        <div class="answer-btn-row">
          <button @click="submitAnswer" :disabled="answerSubmitting" class="btn-answer">
            {{ answerSubmitting ? '저장 중...' : '답변 저장' }}
          </button>
        </div>
      </div>

      <!-- 하단 버튼 -->
      <div class="action-bar">
        <button @click="$router.push('/faq')" class="btn-back">목록으로</button>
        <div class="right-btns">
          <button v-if="canEdit" @click="$router.push(`/faq/edit/${faq.id}`)" class="btn-edit">수정</button>
          <button v-if="canDelete" @click="confirmDelete" class="btn-delete">삭제</button>
        </div>
      </div>
    </div>

    <div v-else class="loading">불러오는 중...</div>
  </div>
</template>

<script>
import axios from 'axios'
// 공통 페이지 헤더 컴포넌트 (PageHeader.vue)
import PageHeader from '../common/PageHeader.vue'
// Tiptap 리치텍스트 에디터 컴포넌트 (TiptapEditor.vue) - 관리자 답변 입력에 사용
import TiptapEditor from '../common/TiptapEditor.vue'
// 첨부파일 목록 표시 및 다운로드/삭제 컴포넌트 (FileList.vue)
import FileList from '../common/FileList.vue'

export default {
  name: 'FaqDetail',
  components: { PageHeader, TiptapEditor, FileList },
  data() {
    return {
      // 조회된 FAQ 상세 데이터 (질문, 답변, 작성자 정보 포함)
      faq: null,
      // 관리자가 입력하는 답변 내용 (TiptapEditor v-model)
      answerContent: '',
      // 답변 저장 중 여부 (중복 제출 방지)
      answerSubmitting: false,
      // 답변 저장 오류 메시지
      answerError: ''
    }
  },
  computed: {
    // 현재 로그인한 사용자 ID
    userId() { return localStorage.getItem('userId') || '' },
    // 현재 사용자의 관리자 레벨
    adminLevel() { return parseInt(localStorage.getItem('adminLevel') || '0') },
    // 관리자 여부 (adminLevel 1 이상이면 답변 등록/수정 가능)
    isAdmin() { return this.adminLevel >= 1 },
    // 수정 권한: 작성자 본인만 가능
    canEdit() {
      return this.faq && this.faq.authorId === this.userId
    },
    // 삭제 권한: 작성자 본인 또는 관리자
    canDelete() {
      return this.faq && (this.faq.authorId === this.userId || this.isAdmin)
    }
  },
  // 컴포넌트 마운트 시 FAQ 상세 조회
  mounted() {
    this.fetchFaq()
  },
  methods: {
    // GET /api/faq/:id 호출 → FaqController.java
    // URL 파라미터의 id로 FAQ 상세(질문+답변) 조회, 기존 답변을 에디터에 로드
    async fetchFaq() {
      try {
        const res = await axios.get(`/api/faq/${this.$route.params.id}`)
        this.faq = res.data
        // 기존 답변이 있으면 에디터 초기값으로 설정
        this.answerContent = this.faq.answer || ''
      } catch (e) {
        console.error('FAQ 조회 실패:', e)
      }
    },
    // PUT /api/faq/:id/answer 호출 → FaqController.java
    // 관리자가 답변을 등록하거나 수정, 저장 후 FAQ 재조회
    async submitAnswer() {
      // 빈 답변 제출 방지 (Tiptap 빈 상태는 '<p></p>')
      if (!this.answerContent || this.answerContent === '<p></p>') {
        this.answerError = '답변 내용을 입력하세요.'
        return
      }
      this.answerSubmitting = true
      this.answerError = ''
      try {
        // 답변 작성자 이름(username) 로컬스토리지에서 읽기
        const username = localStorage.getItem('username') || this.userId
        await axios.put(`/api/faq/${this.faq.id}/answer`, {
          answer: this.answerContent,
          answeredBy: username
        }, { params: { requesterId: this.userId } })
        // 저장 후 FAQ 데이터 재조회 (답변 반영)
        await this.fetchFaq()
      } catch (e) {
        this.answerError = e.response?.data?.message || '답변 저장 중 오류가 발생했습니다.'
      } finally {
        this.answerSubmitting = false
      }
    },
    // DELETE /api/faq/:id 호출 → FaqController.java
    // 삭제 확인 후 FAQ 삭제, 성공 시 FAQ 목록으로 이동
    async confirmDelete() {
      if (!confirm('질문을 삭제하시겠습니까?')) return
      try {
        await axios.delete(`/api/faq/${this.faq.id}`, {
          params: { requesterId: this.userId }
        })
        this.$router.push('/faq')
      } catch (e) {
        alert(e.response?.data?.message || '삭제 중 오류가 발생했습니다.')
      }
    },
    // FileList에서 file-deleted 이벤트 수신 시 로컬 파일 목록에서 해당 파일 제거
    handleFileDeleted(fileId) {
      if (this.faq && this.faq.files) {
        this.faq.files = this.faq.files.filter(f => f.id !== fileId)
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
.faq-detail-container { max-width: 900px; margin: 0 auto; padding: 24px; }

.detail-card { background: white; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.08); overflow: hidden; }

.question-section, .answer-section {
  display: flex;
  gap: 0;
  border-bottom: 1px solid #eee;
}

.section-label {
  width: 56px;
  min-width: 56px;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding-top: 28px;
  font-size: 24px;
  font-weight: bold;
}
.q-label { color: #6a1b9a; background: #f3e5f5; }
.a-label { color: #1565c0; background: #e3f2fd; }

.section-body { flex: 1; padding: 24px; }

.faq-title { margin: 0 0 8px 0; font-size: 20px; font-weight: bold; color: #222; }

.meta { font-size: 13px; color: #888; margin-bottom: 16px; }
.sep { margin: 0 6px; }

.faq-content { font-size: 15px; line-height: 1.7; color: #333; }

.answer-meta { font-size: 13px; color: #888; margin-bottom: 12px; }
.answered-by { font-weight: bold; color: #1565c0; }

.no-answer {
  padding: 20px 24px;
  color: #999;
  font-size: 14px;
  border-bottom: 1px solid #eee;
}

.answer-input-section {
  padding: 24px;
  background: #fafafa;
  border-bottom: 1px solid #eee;
}
.answer-input-title { margin: 0 0 12px 0; font-size: 15px; font-weight: bold; color: #1565c0; }
.error-msg { color: #c62828; font-size: 13px; margin-top: 8px; }
.answer-btn-row { display: flex; justify-content: flex-end; margin-top: 12px; }
.btn-answer {
  padding: 8px 24px; background: #1565c0; color: white;
  border: none; border-radius: 6px; font-size: 14px; cursor: pointer;
}
.btn-answer:hover { filter: brightness(0.9); }
.btn-answer:disabled { opacity: 0.6; cursor: not-allowed; }

.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
}
.right-btns { display: flex; gap: 8px; }

.btn-back { padding: 8px 18px; background: #f5f5f5; border: 1px solid #ddd; border-radius: 6px; font-size: 14px; cursor: pointer; }
.btn-back:hover { background: #eee; }
.btn-edit { padding: 8px 18px; background: #6a1b9a; color: white; border: none; border-radius: 6px; font-size: 14px; cursor: pointer; }
.btn-edit:hover { filter: brightness(0.9); }
.btn-delete { padding: 8px 18px; background: #c62828; color: white; border: none; border-radius: 6px; font-size: 14px; cursor: pointer; }
.btn-delete:hover { filter: brightness(0.9); }

.loading { text-align: center; padding: 60px; color: #999; }
</style>

<style>
/* Tiptap 렌더링 스타일 (전역) */
.tiptap-display p { margin: 0 0 8px 0; }
.tiptap-display ul, .tiptap-display ol { padding-left: 24px; margin: 8px 0; }
.tiptap-display strong { font-weight: bold; }
.tiptap-display em { font-style: italic; }
.tiptap-display u { text-decoration: underline; }
.tiptap-display img { max-width: 100%; border-radius: 4px; }
.tiptap-display h1 { font-size: 1.6em; margin: 12px 0 6px; }
.tiptap-display h2 { font-size: 1.4em; margin: 10px 0 4px; }
.tiptap-display h3 { font-size: 1.2em; margin: 8px 0 4px; }
</style>
