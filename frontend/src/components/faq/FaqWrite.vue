<template>
  <div class="page-container">
    <PageHeader title="FAQ" />
    <PostWriteForm
      titleLabel="질문"
      contentLabel="답변"
      :showFiles="false"
      :imageUploadUrl="null"
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
// 공통 게시글 작성 폼 컴포넌트 (PostWriteForm.vue) - showFiles=false로 파일 첨부 숨김
import PostWriteForm from '../common/PostWriteForm.vue'

export default {
  name: 'FaqWrite',
  components: { PageHeader, PostWriteForm },
  data() {
    return {
      // 폼 제출 중 여부 (중복 제출 방지)
      submitting: false,
      // 오류 메시지
      errorMsg: ''
    }
  },
  methods: {
    // PostWriteForm에서 submit 이벤트 발생 시 호출 (파일 없이 title, content만 사용)
    // POST /api/faq 호출 → FaqController.java (FAQ 질문 등록)
    async handleSubmit({ title, content }) {
      this.submitting = true
      this.errorMsg = ''
      try {
        // 로컬스토리지에서 작성자 정보 읽기
        const userId = localStorage.getItem('userId') || ''
        const username = localStorage.getItem('username') || ''
        await axios.post('/api/faq', {
          title, content, authorId: userId, authorName: username
        }, { params: { requesterId: userId } })
        // 등록 완료 후 FAQ 목록으로 이동
        this.$router.push('/faq')
      } catch (e) {
        this.errorMsg = e.response?.data?.message || '저장 중 오류가 발생했습니다.'
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
