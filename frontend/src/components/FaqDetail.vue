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
import PageHeader from './PageHeader.vue'
import TiptapEditor from './TiptapEditor.vue'

export default {
  name: 'FaqDetail',
  components: { PageHeader, TiptapEditor },
  data() {
    return {
      faq: null,
      answerContent: '',
      answerSubmitting: false,
      answerError: ''
    }
  },
  computed: {
    userId() { return localStorage.getItem('userId') || '' },
    adminLevel() { return parseInt(localStorage.getItem('adminLevel') || '0') },
    isAdmin() { return this.adminLevel >= 1 },
    canEdit() {
      return this.faq && this.faq.authorId === this.userId
    },
    canDelete() {
      return this.faq && (this.faq.authorId === this.userId || this.isAdmin)
    }
  },
  mounted() {
    this.fetchFaq()
  },
  methods: {
    async fetchFaq() {
      try {
        const res = await axios.get(`/api/faq/${this.$route.params.id}`)
        this.faq = res.data
        this.answerContent = this.faq.answer || ''
      } catch (e) {
        console.error('FAQ 조회 실패:', e)
      }
    },
    async submitAnswer() {
      if (!this.answerContent || this.answerContent === '<p></p>') {
        this.answerError = '답변 내용을 입력하세요.'
        return
      }
      this.answerSubmitting = true
      this.answerError = ''
      try {
        const username = localStorage.getItem('username') || this.userId
        await axios.put(`/api/faq/${this.faq.id}/answer`, {
          answer: this.answerContent,
          answeredBy: username
        }, { params: { requesterId: this.userId } })
        await this.fetchFaq()
      } catch (e) {
        this.answerError = e.response?.data?.message || '답변 저장 중 오류가 발생했습니다.'
      } finally {
        this.answerSubmitting = false
      }
    },
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
