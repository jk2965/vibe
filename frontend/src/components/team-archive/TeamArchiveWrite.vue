<template>
  <div class="page-container">
    <PageHeader title="팀별 자료실 글쓰기" />
    <PostWriteForm
      :submitting="submitting"
      :errorMsg="errorMsg"
      :showRequired="canSetRequired"
      submitBtnColor="#2e7d32"
      @submit="handleSubmit"
      @cancel="$router.push('/team-archive')"
    />
  </div>
</template>

<script>
import axios from 'axios'
// 공통 페이지 헤더 컴포넌트 (PageHeader.vue)
import PageHeader from '../common/PageHeader.vue'
// 공통 게시글 작성 폼 컴포넌트 (PostWriteForm.vue) - TiptapEditor 및 파일 첨부 포함
import PostWriteForm from '../common/PostWriteForm.vue'

export default {
  name: 'TeamArchiveWrite',
  components: { PageHeader, PostWriteForm },
  data() {
    return {
      // 팀명: URL 쿼리 파라미터(?team=)가 우선, 없으면 로컬스토리지에서 읽기
      team: this.$route.query.team || localStorage.getItem('team') || '',
      // 폼 제출 중 여부 (중복 제출 방지)
      submitting: false,
      // 오류 메시지
      errorMsg: '',
      adminLevel: parseInt(localStorage.getItem('adminLevel') || '0'),
      isTeamLeader: localStorage.getItem('isTeamLeader') === 'true'
    }
  },
  computed: {
    canSetRequired() {
      return this.adminLevel >= 1 || this.isTeamLeader
    }
  },
  methods: {
    // PostWriteForm에서 submit 이벤트 발생 시 호출
    // POST /api/team-archive 호출 → TeamArchiveController.java (게시글 등록)
    // POST /api/team-archive/:id/files 호출 → TeamArchiveController.java (파일 업로드, 복수)
    async handleSubmit({ title, content, pendingFiles, isRequired }) {
      this.submitting = true
      this.errorMsg = ''
      try {
        const userId = localStorage.getItem('userId')
        // 게시글 본문 등록 API 호출
        const res = await axios.post('http://localhost:8090/api/team-archive', {
          title, content, isRequired,
          authorId: userId,
          authorName: localStorage.getItem('username'),
          team: this.team
        }, { params: { requesterId: userId } })
        // 첨부 파일 순서대로 개별 업로드 (FormData 사용)
        for (const file of pendingFiles) {
          const fd = new FormData()
          fd.append('file', file)
          await axios.post(`http://localhost:8090/api/team-archive/${res.data.id}/files`, fd, { params: { requesterId: userId } })
        }
        // 등록 완료 후 상세 페이지로 이동
        this.$router.push(`/team-archive/${res.data.id}`)
      } catch (e) {
        this.errorMsg = e.response?.data?.message || '등록에 실패했습니다.'
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
