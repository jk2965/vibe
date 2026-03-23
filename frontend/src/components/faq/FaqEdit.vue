<template>
  <div class="page-container">
    <PageHeader title="FAQ" />
    <PostEditForm
      titleLabel="질문"
      contentLabel="답변"
      :initialTitle="initialTitle"
      :initialContent="initialContent"
      :existingFiles="existingFiles"
      :submitting="submitting"
      :errorMsg="errorMsg"
      submitBtnColor="#1a73e8"
      @submit="handleSubmit"
      @cancel="$router.push('/faq')"
    />
  </div>
</template>

<script>
import axios from 'axios'
// 공통 페이지 헤더 컴포넌트 (PageHeader.vue)
import PageHeader from '../common/PageHeader.vue'
// 공통 게시글 수정 폼 컴포넌트 (PostEditForm.vue) — 기존 파일 목록 및 새 파일 추가 포함
import PostEditForm from '../common/PostEditForm.vue'

export default {
  name: 'FaqEdit',
  components: { PageHeader, PostEditForm },
  data() {
    return {
      // 기존 FAQ 제목 (PostEditForm의 initialTitle prop으로 전달)
      initialTitle: '',
      // 기존 FAQ 내용 (PostEditForm의 initialContent prop으로 전달)
      initialContent: '',
      // 서버에 이미 저장된 기존 첨부파일 목록 (PostEditForm의 existingFiles prop으로 전달)
      existingFiles: [],
      // 폼 제출 중 여부 (중복 제출 방지)
      submitting: false,
      // 오류 메시지
      errorMsg: ''
    }
  },
  // 컴포넌트 마운트 시 기존 FAQ 데이터 로드
  mounted() {
    this.loadFaq()
  },
  methods: {
    // GET /api/faq/:id 호출 → FaqController.java
    // 기존 FAQ 제목/내용 로드, 작성자나 관리자가 아니면 목록으로 리다이렉트
    async loadFaq() {
      try {
        const userId = localStorage.getItem('userId') || ''
        const adminLevel = parseInt(localStorage.getItem('adminLevel') || '0')
        const res = await axios.get(`/api/faq/${this.$route.params.id}`)
        const faq = res.data
        // 권한 검사: 작성자 본인이 아니고 관리자도 아니면 수정 불가
        if (faq.authorId !== userId && adminLevel < 1) {
          alert('수정 권한이 없습니다.')
          this.$router.push('/faq')
          return
        }
        // 기존 제목과 내용을 폼 초기값으로 설정
        this.initialTitle = faq.title
        this.initialContent = faq.content || ''
        // 기존 첨부파일 목록 설정 (PostEditForm의 FileList에 표시됨)
        this.existingFiles = faq.files || []
      } catch (e) {
        console.error('FAQ 로드 실패:', e)
      }
    },
    // PostEditForm에서 submit 이벤트 발생 시 호출 - FAQ 수정 후 새 첨부파일 순차 업로드
    // PUT /api/faq/:id → FaqController.java (FAQ 질문 수정)
    // POST /api/faq/:id/files → FaqController.java (새 파일 첨부 업로드)
    async handleSubmit({ title, content, pendingFiles }) {
      this.submitting = true
      this.errorMsg = ''
      try {
        const userId = localStorage.getItem('userId') || ''
        const postId = this.$route.params.id
        await axios.put(`/api/faq/${postId}`, { title, content }, {
          params: { requesterId: userId }
        })
        // 새로 추가된 첨부 파일이 있는 경우 순차적으로 업로드
        for (const file of pendingFiles) {
          const fd = new FormData()
          fd.append('file', file)
          // POST /api/faq/:id/files → FaqController.java: 파일 추가 업로드
          await axios.post(`http://localhost:8090/api/faq/${postId}/files`, fd, { params: { requesterId: userId } })
        }
        // 수정 완료 후 FAQ 목록으로 이동
        this.$router.push('/faq')
      } catch (e) {
        this.errorMsg = e.response?.data?.message || '수정 중 오류가 발생했습니다.'
      } finally {
        this.submitting = false
      }
    }
  }
}
</script>

<style scoped>
.page-container { max-width: 900px; margin: 0 auto; padding: 24px; }
</style>
