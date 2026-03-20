<template>
  <div class="page-container">
    <PageHeader title="팀별 자료실 수정" />
    <PostEditForm
      :initialTitle="form.title"
      :initialContent="form.content"
      :existingFiles="existingFiles"
      :initialTags="form.tags"
      :submitting="submitting"
      :errorMsg="errorMsg"
      submitBtnColor="#2e7d32"
      @submit="handleSubmit"
      @cancel="$router.push(`/team-archive/${postId}`)"
    />
  </div>
</template>

<script>
import axios from 'axios'
// 공통 페이지 헤더 컴포넌트 (PageHeader.vue)
import PageHeader from '../common/PageHeader.vue'
// 공통 게시글 수정 폼 컴포넌트 (PostEditForm.vue) - 기존 제목/내용/파일 초기값 prop으로 전달
import PostEditForm from '../common/PostEditForm.vue'

export default {
  name: 'TeamArchiveEdit',
  components: { PageHeader, PostEditForm },
  data() {
    return {
      // URL 파라미터에서 수정할 게시글 ID 추출
      postId: this.$route.params.id,
      // 수정 폼 데이터 (기존 제목/내용을 loadPost에서 채움)
      form: { title: '', content: '', tags: '' },
      // 기존에 업로드된 파일 목록 (PostEditForm에 초기값으로 전달)
      existingFiles: [],
      // 폼 제출 중 여부 (중복 제출 방지)
      submitting: false,
      // 오류 메시지
      errorMsg: ''
    }
  },
  // 컴포넌트 마운트 시 기존 게시글 데이터 로드
  mounted() {
    this.loadPost()
  },
  methods: {
    // GET /api/team-archive/:id 호출 → TeamArchiveController.java
    // 기존 게시글 데이터(제목, 내용, 파일 목록)를 불러와 폼에 채움
    async loadPost() {
      try {
        const res = await axios.get(`http://localhost:8090/api/team-archive/${this.postId}`, {
          params: { requesterId: localStorage.getItem('userId') }
        })
        // 기존 제목과 내용을 폼에 설정
        this.form.title = res.data.title
        this.form.content = res.data.content || ''
        this.form.tags = res.data.tags || ''
        // 기존 첨부 파일 목록 설정
        this.existingFiles = res.data.files || []
      } catch (e) {
        // 조회 실패(권한 없음 등) 시 목록으로 이동
        this.$router.push('/team-archive')
      }
    },
    // PostEditForm에서 submit 이벤트 발생 시 호출
    // PUT /api/team-archive/:id 호출 → TeamArchiveController.java (게시글 수정)
    // POST /api/team-archive/:id/files 호출 → TeamArchiveController.java (신규 파일 업로드)
    async handleSubmit({ title, content, pendingFiles, tags }) {
      this.submitting = true
      this.errorMsg = ''
      try {
        const userId = localStorage.getItem('userId')
        // 게시글 본문(제목, 내용) 수정 API 호출
        await axios.put(`http://localhost:8090/api/team-archive/${this.postId}`, { title, content, tags }, { params: { requesterId: userId } })
        // 새로 추가된 파일 개별 업로드 (FormData 사용)
        for (const file of pendingFiles) {
          const fd = new FormData()
          fd.append('file', file)
          await axios.post(`http://localhost:8090/api/team-archive/${this.postId}/files`, fd, { params: { requesterId: userId } })
        }
        // 수정 완료 후 상세 페이지로 이동
        this.$router.push(`/team-archive/${this.postId}`)
      } catch (e) {
        this.errorMsg = e.response?.data?.message || '수정에 실패했습니다.'
      } finally {
        this.submitting = false
      }
    }
  }
}
</script>

<style scoped>
.page-container { max-width: 960px; margin: 0 auto; padding: 24px; }
</style>
