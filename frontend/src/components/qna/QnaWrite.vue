<template>
  <div class="write-container">
    <PageHeader title="Q&A" />
    <div class="write-card">
      <h2>질문하기</h2>
      <!-- 질문 작성 폼: 제목 입력 + Tiptap 에디터 -->
      <div class="form-group">
        <label>제목</label>
        <input v-model="title" type="text" placeholder="질문 제목을 입력하세요" class="input-title" />
      </div>
      <div class="form-group">
        <label>내용</label>
        <!-- Tiptap 에디터: imageUploadUrl은 작성 후 받은 ID 기준 경로 사용 -->
        <TiptapEditor v-model="content" imageUploadUrl="/api/qna/temp/images" />
      </div>
      <div class="btn-row">
        <button type="button" @click="$router.back()" class="btn-cancel">취소</button>
        <button type="button" @click="submit" class="btn-submit">등록</button>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
// 공통 페이지 헤더 컴포넌트
import PageHeader from '../common/PageHeader.vue'
// Tiptap 리치 텍스트 에디터
import TiptapEditor from '../common/TiptapEditor.vue'

export default {
  name: 'QnaWrite',
  components: { PageHeader, TiptapEditor },
  data() {
    return {
      // 질문 제목
      title: '',
      // 질문 본문 (Tiptap HTML)
      content: ''
    }
  },
  methods: {
    // POST /api/qna → QnaController.java: 새 질문 등록
    async submit() {
      if (!this.title.trim()) return alert('제목을 입력하세요.')
      if (!this.content.trim() || this.content === '<p></p>') return alert('내용을 입력하세요.')
      try {
        const res = await axios.post('/api/qna', {
          title: this.title,
          content: this.content,
          authorId: localStorage.getItem('userId'),
          authorName: localStorage.getItem('username')
        })
        // 작성 성공 시 상세 페이지로 이동
        this.$router.push(`/qna/${res.data.id}`)
      } catch (e) {
        alert(e.response?.data?.message || '등록 중 오류가 발생했습니다.')
      }
    }
  }
}
</script>

<style scoped>
.write-container { max-width: 860px; margin: 0 auto; padding: 24px; }
.write-card { background: white; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.08); padding: 28px; }
.write-card h2 { margin: 0 0 20px; font-size: 18px; }
.form-group { margin-bottom: 18px; }
.form-group label { display: block; font-size: 13px; font-weight: 600; color: #555; margin-bottom: 6px; }
.input-title { width: 100%; padding: 10px 12px; border: 1px solid #ddd; border-radius: 6px; font-size: 15px; box-sizing: border-box; outline: none; }
.input-title:focus { border-color: #1a73e8; }
.btn-row { display: flex; justify-content: flex-end; gap: 8px; margin-top: 20px; }
.btn-cancel { padding: 9px 22px; border: 1px solid #ddd; background: white; border-radius: 6px; font-size: 14px; cursor: pointer; }
.btn-cancel:hover { background: #f5f5f5; }
.btn-submit { padding: 9px 22px; background: #1a73e8; color: white; border: none; border-radius: 6px; font-size: 14px; cursor: pointer; font-weight: bold; }
.btn-submit:hover { background: #1558b0; }
</style>
