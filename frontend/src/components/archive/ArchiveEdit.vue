<template>
  <div class="page-container">
    <PageHeader title="자료실 수정" />
    <PostEditForm
      :initialTitle="form.title"
      :initialContent="form.content"
      :existingFiles="existingFiles"
      :initialTags="form.tags"
      :submitting="submitting"
      :errorMsg="errorMsg"
      submitBtnColor="#1a73e8"
      @submit="handleSubmit"
      @cancel="$router.push(`/archive/${postId}`)"
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
  name: 'ArchiveEdit',
  // PageHeader.vue, PostEditForm.vue 컴포넌트 등록
  components: { PageHeader, PostEditForm },
  data() {
    return {
      postId: this.$route.params.id,        // URL 파라미터에서 수정할 자료 ID 추출
      form: { title: '', content: '', tags: '' },     // 수정 폼 데이터 (서버에서 로드 후 채움)
      existingFiles: [],                    // 기존 첨부 파일 목록 (PostEditForm.vue로 전달)
      submitting: false,                    // 제출 중 상태 플래그 (중복 제출 방지)
      errorMsg: ''                          // 수정 실패 시 사용자에게 표시할 오류 메시지
    }
  },
  // 컴포넌트 마운트 시 기존 자료 데이터를 서버에서 로드
  mounted() {
    this.loadPost()
  },
  methods: {
    // GET /api/archive/:id → ArchiveController.java: 기존 자료 데이터 로드 (수정 폼 초기화용)
    async loadPost() {
      try {
        const res = await axios.get(`http://localhost:8090/api/archive/${this.postId}`)
        this.form.title = res.data.title           // 기존 제목을 폼에 설정
        this.form.content = res.data.content || ''
        this.form.tags = res.data.tags || ''      // 기존 태그 목록을 폼에 설정
        this.existingFiles = res.data.files || []  // 기존 첨부 파일 목록 설정
      } catch (e) {
        // 자료 조회 실패 시 자료실 목록 페이지로 이동
        this.$router.push('/archive')
      }
    },
    // PostEditForm.vue에서 submit 이벤트 발생 시 호출 → 자료 수정 및 신규 파일 업로드 처리
    async handleSubmit({ title, content, pendingFiles, tags }) {
      this.submitting = true   // 제출 시작: 버튼 비활성화
      this.errorMsg = ''       // 이전 오류 메시지 초기화
      try {
        const userId = localStorage.getItem('userId')  // 로컬스토리지에서 현재 사용자 ID 로드
        // PUT /api/archive/:id → ArchiveController.java: 자료 제목/본문 수정 (requesterId로 권한 검증)
        await axios.put(`http://localhost:8090/api/archive/${this.postId}`, { title, content, tags }, { params: { requesterId: userId } })
        // 새로 추가된 첨부 파일이 있는 경우 순차적으로 업로드
        for (const file of pendingFiles) {
          const fd = new FormData()
          fd.append('file', file)
          // POST /api/archive/:id/files → ArchiveController.java: 파일 추가 업로드
          await axios.post(`http://localhost:8090/api/archive/${this.postId}/files`, fd, { params: { requesterId: userId } })
        }
        // 수정 완료 후 해당 자료 상세 페이지로 이동 → ArchiveDetail.vue
        this.$router.push(`/archive/${this.postId}`)
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
.page-container { max-width: 960px; margin: 0 auto; padding: 24px; }
</style>
