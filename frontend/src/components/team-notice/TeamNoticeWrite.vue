<template>
  <div class="page-container">
    <PageHeader :title="team + ' 공지사항 작성'" />
    <PostWriteForm
      :submitting="submitting"
      :errorMsg="errorMsg"
      :showRequired="true"
      submitBtnColor="#5c6bc0"
      @submit="handleSubmit"
      @cancel="$router.push('/team-notice')"
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
  name: 'TeamNoticeWrite',
  // PageHeader, PostWriteForm 공통 컴포넌트 등록
  components: { PageHeader, PostWriteForm },
  data() {
    return {
      // URL 쿼리 파라미터 ?team= 에서 팀명 읽기, 없으면 localStorage의 사용자 소속 팀 사용
      team: this.$route.query.team || localStorage.getItem('team') || '',
      // 폼 제출 중 여부 (중복 제출 방지용)
      submitting: false,
      // 에러 메시지 (등록 실패 시 표시)
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
    // PostWriteForm.vue에서 submit 이벤트 발생 시 호출되는 팀 공지사항 등록 핸들러
    async handleSubmit({ title, content, pendingFiles, isRequired }) {
      // 제출 시작: 중복 제출 방지 플래그 설정
      this.submitting = true
      this.errorMsg = ''
      try {
        // localStorage에서 현재 로그인 사용자 ID 가져오기
        const userId = localStorage.getItem('userId')
        // POST /api/team-notice 호출 → TeamNoticeController.java (팀 공지사항 등록, 팀 정보 포함)
        const res = await axios.post('http://localhost:8090/api/team-notice', {
          title, content, isRequired,
          authorId: userId,
          authorName: localStorage.getItem('username'),
          team: this.team
        }, { params: { requesterId: userId } })
        // 첨부파일이 있는 경우 순서대로 업로드 처리
        for (const file of pendingFiles) {
          const fd = new FormData()
          fd.append('file', file)
          // POST /api/team-notice/:id/files 호출 → TeamNoticeController.java (파일 업로드)
          await axios.post(`http://localhost:8090/api/team-notice/${res.data.id}/files`, fd, { params: { requesterId: userId } })
        }
        // 등록 성공 시 작성된 게시글 상세 페이지로 이동 → TeamNoticeDetail.vue
        this.$router.push(`/team-notice/${res.data.id}`)
      } catch (e) {
        this.errorMsg = e.response?.data?.message || '등록에 실패했습니다.'
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
