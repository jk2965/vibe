<template>
  <div class="page-container">
    <PageHeader title="글쓰기" />
    <PostWriteForm
      :submitting="submitting"
      :errorMsg="errorMsg"
      submitBtnColor="#1976d2"
      @submit="handleSubmit"
      @cancel="$router.push('/board')"
    />
  </div>
</template>

<script>
import axios from 'axios'
// PageHeader.vue 공통 헤더 컴포넌트 사용
import PageHeader from '../common/PageHeader.vue'
// PostWriteForm.vue 게시글 작성 폼 공통 컴포넌트 사용
import PostWriteForm from '../common/PostWriteForm.vue'

export default {
  name: 'BoardWrite',
  // PageHeader.vue, PostWriteForm.vue 컴포넌트 등록
  components: { PageHeader, PostWriteForm },
  data() {
    return {
      submitting: false,  // 제출 중 상태 플래그 (중복 제출 방지 및 버튼 비활성화 용)
      errorMsg: ''        // 등록 실패 시 사용자에게 표시할 오류 메시지
    }
  },
  methods: {
    // PostWriteForm.vue에서 submit 이벤트 발생 시 호출 → 게시글 생성 및 파일 업로드 처리
    async handleSubmit({ title, content, pendingFiles }) {
      this.submitting = true   // 제출 시작: 버튼 비활성화
      this.errorMsg = ''       // 이전 오류 메시지 초기화
      try {
        const userId = localStorage.getItem('userId')  // 로컬스토리지에서 현재 사용자 ID 로드
        // POST /api/board → BoardController.java: 게시글 신규 생성
        const res = await axios.post('http://localhost:8090/api/board', {
          title, content,
          authorId: userId,
          authorName: localStorage.getItem('username')  // 로컬스토리지에서 작성자명 로드
        })
        // 첨부 파일이 있는 경우 순차적으로 업로드
        for (const file of pendingFiles) {
          const fd = new FormData()
          fd.append('file', file)
          // POST /api/board/:id/files → BoardController.java: 파일 첨부 업로드
          await axios.post(`http://localhost:8090/api/board/${res.data.id}/files`, fd, { params: { requesterId: userId } })
        }
        // 등록 완료 후 생성된 게시글 상세 페이지로 이동 → BoardDetail.vue
        this.$router.push(`/board/${res.data.id}`)
      } catch (e) {
        this.errorMsg = e.response?.data?.message || '등록에 실패했습니다.'
      } finally {
        this.submitting = false  // 제출 완료: 버튼 다시 활성화
      }
    }
  }
}
</script>

<style scoped>
.page-container { max-width: 900px; margin: 0 auto; padding: 24px; }
</style>
