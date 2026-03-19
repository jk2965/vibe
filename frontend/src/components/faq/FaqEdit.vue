<template>
  <div class="page-container">
    <PageHeader title="FAQ" />
    <PostEditForm
      titleLabel="질문"
      contentLabel="답변"
      :showFiles="false"
      :imageUploadUrl="null"
      :initialTitle="initialTitle"
      :initialContent="initialContent"
      :submitting="submitting"
      :errorMsg="errorMsg"
      submitBtnColor="#6a1b9a"
      @submit="handleSubmit"
      @cancel="$router.push('/faq')"
    />
  </div>
</template>

<script>
import axios from 'axios'
// 공통 페이지 헤더 컴포넌트 (PageHeader.vue)
import PageHeader from '../common/PageHeader.vue'
// 공통 게시글 수정 폼 컴포넌트 (PostEditForm.vue) - showFiles=false로 파일 첨부 숨김
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
      } catch (e) {
        console.error('FAQ 로드 실패:', e)
      }
    },
    // PostEditForm에서 submit 이벤트 발생 시 호출 (파일 없이 title, content만 사용)
    // PUT /api/faq/:id 호출 → FaqController.java (FAQ 질문 수정)
    async handleSubmit({ title, content }) {
      this.submitting = true
      this.errorMsg = ''
      try {
        const userId = localStorage.getItem('userId') || ''
        await axios.put(`/api/faq/${this.$route.params.id}`, { title, content }, {
          params: { requesterId: userId }
        })
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
