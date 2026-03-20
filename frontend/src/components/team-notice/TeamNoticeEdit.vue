<template>
  <div class="page-container">
    <PageHeader title="팀 공지사항 수정" />
    <PostEditForm
      :initialTitle="form.title"
      :initialContent="form.content"
      :existingFiles="existingFiles"
      :initialTags="form.tags"
      :submitting="submitting"
      :errorMsg="errorMsg"
      submitBtnColor="#5c6bc0"
      @submit="handleSubmit"
      @cancel="$router.push(`/team-notice/${postId}`)"
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
  name: 'TeamNoticeEdit',
  // PageHeader, PostEditForm 공통 컴포넌트 등록
  components: { PageHeader, PostEditForm },
  data() {
    return {
      // URL 파라미터에서 수정할 게시글 ID 추출
      postId: this.$route.params.id,
      // 수정 폼 데이터 (제목, 내용)
      form: { title: '', content: '', tags: '' },
      // 기존에 등록된 첨부파일 목록 (파일 삭제 기능에 사용)
      existingFiles: [],
      // 폼 제출 중 여부 (중복 제출 방지용)
      submitting: false,
      // 에러 메시지 (수정 실패 시 표시)
      errorMsg: ''
    }
  },
  // 컴포넌트 마운트 시 기존 게시글 데이터 로드
  mounted() {
    this.loadPost()
  },
  methods: {
    // GET /api/team-notice/:id 호출 → TeamNoticeController.java (수정할 게시글 데이터 로드 및 권한 검증)
    async loadPost() {
      try {
        const res = await axios.get(`http://localhost:8090/api/team-notice/${this.postId}`, {
          params: { requesterId: localStorage.getItem('userId') }
        })
        // 본인 게시글이 아닌 경우 상세 페이지로 리다이렉트 (권한 없음)
        if (res.data.authorId !== localStorage.getItem('userId')) {
          this.$router.push(`/team-notice/${this.postId}`)
          return
        }
        // 기존 제목, 내용, 첨부파일 목록을 폼에 세팅
        this.form.title = res.data.title
        this.form.content = res.data.content || ''
        this.form.tags = res.data.tags || ''
        this.existingFiles = res.data.files || []
      } catch (e) {
        // 조회 실패(또는 403 권한 없음) 시 팀 공지사항 목록으로 이동 → TeamNotice.vue
        this.$router.push('/team-notice')
      }
    },
    // PostEditForm.vue에서 submit 이벤트 발생 시 호출되는 팀 공지사항 수정 핸들러
    async handleSubmit({ title, content, pendingFiles, tags }) {
      // 제출 시작: 중복 제출 방지 플래그 설정
      this.submitting = true
      this.errorMsg = ''
      try {
        const userId = localStorage.getItem('userId')
        // PUT /api/team-notice/:id 호출 → TeamNoticeController.java (팀 공지사항 수정)
        await axios.put(`http://localhost:8090/api/team-notice/${this.postId}`, { title, content, tags }, { params: { requesterId: userId } })
        // 새로 추가된 첨부파일이 있는 경우 순서대로 업로드 처리
        for (const file of pendingFiles) {
          const fd = new FormData()
          fd.append('file', file)
          // POST /api/team-notice/:id/files 호출 → TeamNoticeController.java (파일 업로드)
          await axios.post(`http://localhost:8090/api/team-notice/${this.postId}/files`, fd, { params: { requesterId: userId } })
        }
        // 수정 성공 시 해당 게시글 상세 페이지로 이동 → TeamNoticeDetail.vue
        this.$router.push(`/team-notice/${this.postId}`)
      } catch (e) {
        this.errorMsg = e.response?.data?.message || '수정에 실패했습니다.'
      } finally {
        // 제출 완료 후 플래그 해제
        this.submitting = false
      }
    }
  }
}
</script>

<style scoped>
.page-container { max-width: 900px; margin: 0 auto; padding: 24px; }
</style>
