<template>
  <div class="detail-container">
    <PageHeader title="Q&A" />

    <div v-if="qna" class="detail-card">
      <!-- 질문 헤더 -->
      <div class="question-header">
        <span class="q-badge">Q</span>
        <div class="q-title-wrap">
          <h2 class="q-title">{{ qna.title }}</h2>
          <div class="q-meta">
            <span>{{ qna.authorName }}</span>
            <span class="sep">|</span>
            <span>{{ formatDate(qna.createdAt) }}</span>
            <span class="sep">|</span>
            <span>조회 {{ qna.views }}</span>
          </div>
        </div>
      </div>

      <!-- 질문 본문 -->
      <div class="question-content tiptap-display" v-html="qna.content"></div>

      <!-- 질문 작성자 수정/삭제 버튼 -->
      <div v-if="isAuthor" class="question-actions">
        <button @click="startEditQuestion" class="btn-edit">수정</button>
        <button @click="deleteQuestion" class="btn-delete">삭제</button>
      </div>

      <!-- 질문 수정 폼 (인라인) -->
      <div v-if="editingQuestion" class="edit-form">
        <input v-model="editTitle" type="text" class="input-title" placeholder="제목" />
        <TiptapEditor v-model="editContent" imageUploadUrl="/api/qna/temp/images" />
        <div class="edit-btn-row">
          <button @click="editingQuestion = false" class="btn-cancel">취소</button>
          <button @click="submitEditQuestion" class="btn-submit">저장</button>
        </div>
      </div>

      <hr class="divider" />

      <!-- 답변 목록 섹션 -->
      <div class="answer-section">
        <h3 class="answer-title">
          답변 <span class="answer-cnt">{{ qna.answers ? qna.answers.length : 0 }}</span>
        </h3>

        <!-- 답변이 없는 경우 -->
        <div v-if="!qna.answers || qna.answers.length === 0" class="no-answer">
          아직 답변이 없습니다. 관리자 또는 팀장이 답변을 등록할 수 있습니다.
        </div>

        <!-- 답변 목록 -->
        <div v-for="answer in qna.answers" :key="answer.id" class="answer-item">
          <div class="answer-header">
            <span class="a-badge">A</span>
            <div class="a-meta-wrap">
              <span class="a-author">{{ answer.authorName }}</span>
              <span class="sep">|</span>
              <span class="a-date">{{ formatDate(answer.createdAt) }}</span>
            </div>
            <!-- 답변 작성자 또는 관리자만 수정/삭제 가능 -->
            <div v-if="isAnswerAuthor(answer) || isAdmin" class="answer-actions">
              <button @click="startEditAnswer(answer)" class="btn-edit-sm">수정</button>
              <button @click="deleteAnswer(answer.id)" class="btn-delete-sm">삭제</button>
            </div>
          </div>

          <!-- 답변 본문 (일반 표시) -->
          <div v-if="editingAnswerId !== answer.id" class="answer-content tiptap-display" v-html="answer.content"></div>

          <!-- 답변 수정 폼 (인라인) -->
          <div v-else class="edit-form">
            <TiptapEditor v-model="editAnswerContent" imageUploadUrl="/api/qna/temp/images" />
            <div class="edit-btn-row">
              <button @click="editingAnswerId = null" class="btn-cancel">취소</button>
              <button @click="submitEditAnswer(answer.id)" class="btn-submit">저장</button>
            </div>
          </div>
        </div>
      </div>

      <!-- 답변 작성 버튼 / 폼 (관리자/팀장만 표시) -->
      <div v-if="canAnswer" class="write-answer">
        <!-- 폼이 닫혀 있을 때: "답변 작성" 버튼만 표시 -->
        <button v-if="!showAnswerForm" @click="showAnswerForm = true" class="btn-answer-open">
          답변 작성
        </button>
        <!-- 폼이 열렸을 때: 에디터 + 등록/취소 버튼 표시 -->
        <div v-else>
          <h3 class="answer-title">답변 작성</h3>
          <TiptapEditor v-model="newAnswerContent" imageUploadUrl="/api/qna/temp/images" />
          <div class="edit-btn-row">
            <button @click="cancelAnswer" class="btn-cancel">취소</button>
            <button @click="submitAnswer" class="btn-submit">답변 등록</button>
          </div>
        </div>
      </div>

      <!-- 목록으로 버튼 -->
      <div class="footer-btn-row">
        <button @click="$router.push('/qna')" class="btn-list">목록으로</button>
      </div>
    </div>

    <div v-else class="loading">불러오는 중...</div>
  </div>
</template>

<script>
import axios from 'axios'
// 공통 페이지 헤더 컴포넌트
import PageHeader from '../common/PageHeader.vue'
// Tiptap 리치 텍스트 에디터 (답변 작성/수정용)
import TiptapEditor from '../common/TiptapEditor.vue'

export default {
  name: 'QnaDetail',
  components: { PageHeader, TiptapEditor },
  data() {
    return {
      // 질문 상세 데이터 (answers 배열 포함)
      qna: null,
      // 질문 수정 폼 표시 여부
      editingQuestion: false,
      // 질문 수정 중인 제목
      editTitle: '',
      // 질문 수정 중인 내용
      editContent: '',
      // 수정 중인 답변 ID (null이면 수정 폼 미표시)
      editingAnswerId: null,
      // 답변 수정 중인 내용
      editAnswerContent: '',
      // 새 답변 작성 내용
      newAnswerContent: '',
      // 답변 작성 폼 표시 여부 (버튼 클릭 후 열림)
      showAnswerForm: false
    }
  },
  computed: {
    // 현재 로그인 사용자 ID
    userId() { return localStorage.getItem('userId') || '' },
    // 현재 사용자의 관리자 레벨
    adminLevel() { return parseInt(localStorage.getItem('adminLevel') || '0') },
    // 팀장 여부
    isTeamLeader() { return localStorage.getItem('isTeamLeader') === 'true' },
    // 관리자 여부
    isAdmin() { return this.adminLevel >= 1 },
    // 답변 작성 권한: 관리자 또는 팀장
    canAnswer() { return this.isAdmin || this.isTeamLeader },
    // 현재 사용자가 질문 작성자인지 여부
    isAuthor() { return this.qna && this.qna.authorId === this.userId }
  },
  mounted() {
    // 라우트 파라미터의 id로 질문 상세 조회
    this.fetchDetail()
  },
  methods: {
    // GET /api/qna/:id → QnaController.java: 질문 + 답변 목록 조회
    async fetchDetail() {
      try {
        const res = await axios.get(`/api/qna/${this.$route.params.id}`)
        this.qna = res.data
      } catch (e) {
        alert('질문을 불러올 수 없습니다.')
        this.$router.push('/qna')
      }
    },
    // 현재 사용자가 해당 답변의 작성자인지 확인
    isAnswerAuthor(answer) {
      return answer.authorId === this.userId
    },
    // 날짜 문자열에서 'YYYY-MM-DD' 형식만 추출
    formatDate(dateStr) {
      if (!dateStr) return '-'
      return dateStr.substring(0, 10)
    },

    // --- 질문 수정/삭제 ---
    // 질문 수정 폼 열기: 현재 제목/내용을 편집 필드에 채움
    startEditQuestion() {
      this.editTitle = this.qna.title
      this.editContent = this.qna.content
      this.editingQuestion = true
    },
    // PUT /api/qna/:id → QnaController.java: 질문 수정 저장
    async submitEditQuestion() {
      if (!this.editTitle.trim()) return alert('제목을 입력하세요.')
      try {
        await axios.put(`/api/qna/${this.qna.id}`, {
          title: this.editTitle,
          content: this.editContent
        }, { params: { requesterId: this.userId } })
        this.editingQuestion = false
        // 수정 반영을 위해 재조회 (조회수 추가 증가 방지를 위해 직접 갱신)
        this.qna.title = this.editTitle
        this.qna.content = this.editContent
      } catch (e) {
        alert(e.response?.data?.message || '수정 중 오류가 발생했습니다.')
      }
    },
    // DELETE /api/qna/:id → QnaController.java: 질문 삭제
    async deleteQuestion() {
      if (!confirm('질문을 삭제하시겠습니까? 답변도 함께 삭제됩니다.')) return
      try {
        await axios.delete(`/api/qna/${this.qna.id}`, { params: { requesterId: this.userId } })
        this.$router.push('/qna')
      } catch (e) {
        alert(e.response?.data?.message || '삭제 중 오류가 발생했습니다.')
      }
    },

    // --- 답변 작성/수정/삭제 ---
    // 답변 작성 폼 취소: 내용 초기화 후 폼 닫기
    cancelAnswer() {
      this.newAnswerContent = ''
      this.showAnswerForm = false
    },
    // POST /api/qna/:id/answers → QnaController.java: 새 답변 등록
    async submitAnswer() {
      if (!this.newAnswerContent.trim() || this.newAnswerContent === '<p></p>')
        return alert('답변 내용을 입력하세요.')
      try {
        await axios.post(`/api/qna/${this.qna.id}/answers`, {
          content: this.newAnswerContent,
          authorId: this.userId,
          authorName: localStorage.getItem('username')
        })
        // 등록 후 폼 닫기 및 내용 초기화
        this.newAnswerContent = ''
        this.showAnswerForm = false
        // 답변 목록 갱신을 위해 상세 재조회
        await this.fetchDetail()
      } catch (e) {
        alert(e.response?.data?.message || '답변 등록 중 오류가 발생했습니다.')
      }
    },
    // 답변 수정 폼 열기: 해당 답변 내용을 편집 필드에 채움
    startEditAnswer(answer) {
      this.editingAnswerId = answer.id
      this.editAnswerContent = answer.content
    },
    // PUT /api/qna/:id/answers/:answerId → QnaController.java: 답변 수정 저장
    async submitEditAnswer(answerId) {
      if (!this.editAnswerContent.trim()) return alert('내용을 입력하세요.')
      try {
        await axios.put(`/api/qna/${this.qna.id}/answers/${answerId}`, {
          content: this.editAnswerContent
        }, { params: { requesterId: this.userId } })
        this.editingAnswerId = null
        // 수정 반영을 위해 해당 답변 내용만 업데이트
        const target = this.qna.answers.find(a => a.id === answerId)
        if (target) target.content = this.editAnswerContent
      } catch (e) {
        alert(e.response?.data?.message || '수정 중 오류가 발생했습니다.')
      }
    },
    // DELETE /api/qna/:id/answers/:answerId → QnaController.java: 답변 삭제
    async deleteAnswer(answerId) {
      if (!confirm('답변을 삭제하시겠습니까?')) return
      try {
        await axios.delete(`/api/qna/${this.qna.id}/answers/${answerId}`, {
          params: { requesterId: this.userId }
        })
        // 삭제된 답변을 목록에서 제거
        this.qna.answers = this.qna.answers.filter(a => a.id !== answerId)
      } catch (e) {
        alert(e.response?.data?.message || '삭제 중 오류가 발생했습니다.')
      }
    }
  }
}
</script>

<style scoped>
.detail-container { max-width: 860px; margin: 0 auto; padding: 24px; }
.detail-card { background: white; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.08); padding: 28px; }

/* 질문 헤더 */
.question-header { display: flex; gap: 16px; align-items: flex-start; margin-bottom: 16px; }
.q-badge {
  width: 36px; height: 36px; border-radius: 50%;
  background: #1a73e8; color: white;
  display: flex; align-items: center; justify-content: center;
  font-size: 16px; font-weight: bold; flex-shrink: 0;
}
.q-title-wrap { flex: 1; }
.q-title { margin: 0 0 6px; font-size: 20px; color: #111; }
.q-meta { font-size: 13px; color: #888; display: flex; gap: 6px; align-items: center; }
.sep { color: #ddd; }

/* 질문 본문 */
.question-content { font-size: 15px; line-height: 1.8; padding: 16px 0; }

/* 질문 수정/삭제 버튼 */
.question-actions { display: flex; justify-content: flex-end; gap: 8px; }

.divider { border: none; border-top: 1px solid #eee; margin: 20px 0; }

/* 답변 섹션 */
.answer-section { margin-top: 4px; }
.answer-title { font-size: 16px; color: #333; margin: 0 0 14px; }
.answer-cnt { color: #1a73e8; }
.no-answer { color: #aaa; font-size: 14px; padding: 16px 0; }

/* 개별 답변 */
.answer-item { border-top: 1px solid #f0f0f0; padding: 16px 0; }
.answer-header { display: flex; align-items: center; gap: 12px; margin-bottom: 10px; }
.a-badge {
  width: 30px; height: 30px; border-radius: 50%;
  background: #2e7d32; color: white;
  display: flex; align-items: center; justify-content: center;
  font-size: 13px; font-weight: bold; flex-shrink: 0;
}
.a-meta-wrap { flex: 1; display: flex; gap: 6px; align-items: center; font-size: 13px; color: #888; }
.a-author { font-weight: 600; color: #555; }
.answer-actions { display: flex; gap: 6px; }
.answer-content { font-size: 14px; line-height: 1.8; padding: 4px 0 4px 42px; }

/* 답변 작성 영역 */
.write-answer { margin-top: 24px; border-top: 2px solid #e3f2fd; padding-top: 20px; }
/* 답변 작성 버튼 (폼 열기 전) */
.btn-answer-open {
  padding: 9px 24px; background: #2e7d32; color: white;
  border: none; border-radius: 6px; font-size: 14px;
  font-weight: bold; cursor: pointer;
}
.btn-answer-open:hover { background: #1b5e20; }

/* 인라인 수정 폼 */
.edit-form { margin-top: 10px; display: flex; flex-direction: column; gap: 10px; }
.input-title { padding: 9px 12px; border: 1px solid #ddd; border-radius: 6px; font-size: 15px; outline: none; width: 100%; box-sizing: border-box; }
.input-title:focus { border-color: #1a73e8; }
.edit-btn-row { display: flex; justify-content: flex-end; gap: 8px; }

/* 공통 버튼 */
.btn-edit, .btn-delete,
.btn-cancel, .btn-submit, .btn-list {
  padding: 6px 16px; border-radius: 5px; font-size: 13px; cursor: pointer;
}
.btn-edit { background: #1a73e8; color: white; border: none; }
.btn-edit:hover { background: #1558b0; }
.btn-delete { background: #e53935; color: white; border: none; }
.btn-delete:hover { background: #c62828; }
.btn-cancel { background: white; border: 1px solid #ddd; color: #555; }
.btn-cancel:hover { background: #f5f5f5; }
.btn-submit { background: #1a73e8; color: white; border: none; font-weight: bold; }
.btn-submit:hover { background: #1558b0; }
.btn-edit-sm, .btn-delete-sm { padding: 3px 10px; border-radius: 4px; font-size: 12px; cursor: pointer; }
.btn-edit-sm { background: #e8f0fe; color: #1a73e8; border: 1px solid #c5d5f7; }
.btn-delete-sm { background: #fce4ec; color: #c62828; border: 1px solid #f5c0cd; }
.footer-btn-row { margin-top: 24px; padding-top: 16px; border-top: 1px solid #eee; }
.btn-list { background: #f5f5f5; border: 1px solid #ddd; color: #555; }
.btn-list:hover { background: #eee; }
.loading { text-align: center; color: #aaa; padding: 60px; }
</style>

<style>
/* 질문/답변 본문 HTML 렌더링용 전역 스타일 */
.tiptap-display h1 { font-size: 22px; margin: 14px 0 6px; }
.tiptap-display h2 { font-size: 18px; margin: 12px 0 5px; }
.tiptap-display h3 { font-size: 16px; margin: 10px 0 4px; }
.tiptap-display p { margin: 0 0 8px 0; }
.tiptap-display ul, .tiptap-display ol { padding-left: 24px; margin: 8px 0; }
.tiptap-display blockquote { border-left: 3px solid #ddd; margin: 8px 0; padding: 4px 12px; color: #666; }
.tiptap-display pre { background: #f4f4f4; border-radius: 4px; padding: 12px; overflow-x: auto; margin: 8px 0; }
.tiptap-display pre code { font-size: 13px; }
.tiptap-display img { max-width: 100%; height: auto; border-radius: 4px; }
.tiptap-display a { color: #1565c0; text-decoration: underline; }
.tiptap-display table { border-collapse: collapse; width: 100%; margin: 12px 0; }
.tiptap-display th, .tiptap-display td { border: 1px solid #ccc; padding: 8px 10px; font-size: 14px; }
.tiptap-display th { background: #f5f5f5; font-weight: bold; }
</style>
