<template>
  <div class="page-container">
    <PageHeader title="게시글 수정" />
    <PostEditForm
      :initialTitle="form.title"
      :initialContent="form.content"
      :existingFiles="existingFiles"
      :submitting="submitting"
      :errorMsg="errorMsg"
      submitBtnColor="#1976d2"
      @submit="handleSubmit"
      @cancel="$router.push(`/board/${postId}`)"
    />
  </div>
</template>

<script>
import axios from 'axios'
// PageHeader.vue 공통 헤더 컴포넌트 사용
import PageHeader from '../common/PageHeader.vue'
// PostEditForm.vue 게시글 수정 폼 공통 컴포넌트 사용
import PostEditForm from '../common/PostEditForm.vue'

export default {
  name: 'BoardEdit',
  // PageHeader.vue, PostEditForm.vue 컴포넌트 등록
  components: { PageHeader, PostEditForm },
  data() {
    return {
      postId: this.$route.params.id,        // URL 파라미터에서 수정할 게시글 ID 추출
      form: { title: '', content: '' },     // 수정 폼 데이터 (서버에서 로드 후 채움)
      existingFiles: [],                    // 기존 첨부 파일 목록 (PostEditForm.vue로 전달)
      submitting: false,                    // 제출 중 상태 플래그 (중복 제출 방지)
      errorMsg: ''                          // 수정 실패 시 사용자에게 표시할 오류 메시지
    }
  },
  // 컴포넌트 마운트 시 기존 게시글 데이터를 서버에서 로드
  mounted() {
    this.loadPost()
  },
  methods: {
    // GET /api/board/:id → BoardController.java: 기존 게시글 데이터 로드 및 작성자 권한 검증
    async loadPost() {
      try {
        const res = await axios.get(`http://localhost:8090/api/board/${this.postId}`)
        // 작성자 본인이 아닌 경우 수정 불가 → 상세 페이지로 리다이렉트
        if (res.data.authorId !== localStorage.getItem('userId')) {
          this.$router.push(`/board/${this.postId}`)
          return
        }
        this.form.title = res.data.title          // 기존 제목을 폼에 설정
        this.form.content = res.data.content || '' // 기존 본문을 폼에 설정
        this.existingFiles = res.data.files || []  // 기존 첨부 파일 목록 설정
      } catch (e) {
        // 게시글 조회 실패 시 목록 페이지로 이동
        this.$router.push('/board')
      }
    },
    // PostEditForm.vue에서 submit 이벤트 발생 시 호출 → 게시글 수정 및 신규 파일 업로드 처리
    async handleSubmit({ title, content, pendingFiles }) {
      this.submitting = true   // 제출 시작: 버튼 비활성화
      this.errorMsg = ''       // 이전 오류 메시지 초기화
      try {
        const userId = localStorage.getItem('userId')  // 로컬스토리지에서 현재 사용자 ID 로드
        // PUT /api/board/:id → BoardController.java: 게시글 제목/본문 수정
        await axios.put(`http://localhost:8090/api/board/${this.postId}`, { title, content }, { params: { requesterId: userId } })
        // 새로 추가된 첨부 파일이 있는 경우 순차적으로 업로드
        for (const file of pendingFiles) {
          const fd = new FormData()
          fd.append('file', file)
          // POST /api/board/:id/files → BoardController.java: 파일 추가 업로드
          await axios.post(`http://localhost:8090/api/board/${this.postId}/files`, fd, { params: { requesterId: userId } })
        }
        // 수정 완료 후 해당 게시글 상세 페이지로 이동 → BoardDetail.vue
        this.$router.push(`/board/${this.postId}`)
      } catch (e) {
        this.errorMsg = e.response?.data?.message || '수정에 실패했습니다.'
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
