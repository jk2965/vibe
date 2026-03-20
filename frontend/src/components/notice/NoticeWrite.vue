<template>
  <div class="page-container">
    <PageHeader title="공지사항 작성" />
    <PostWriteForm
      :submitting="submitting"
      :errorMsg="errorMsg"
      :showRequired="true"
      submitBtnColor="#1a73e8"
      @submit="handleSubmit"
      @cancel="$router.push('/notice')"
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
  name: 'NoticeWrite',
  // PageHeader, PostWriteForm 공통 컴포넌트 등록
  components: { PageHeader, PostWriteForm },
  data() {
    return {
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
    // PostWriteForm.vue에서 submit 이벤트 발생 시 호출되는 게시글 등록 핸들러
    async handleSubmit({ title, content, pendingFiles, isRequired, tags }) {
      // 제출 시작: 중복 제출 방지 플래그 설정
      this.submitting = true
      this.errorMsg = ''
      try {
        // localStorage에서 현재 로그인 사용자 ID 가져오기
        const userId = localStorage.getItem('userId')
        // POST /api/notice 호출 → NoticeController.java (공지사항 등록)
        const res = await axios.post('http://localhost:8090/api/notice', {
          title, content, isRequired, tags,
          authorId: userId,
          authorName: localStorage.getItem('username')
        }, { params: { requesterId: userId } })
        // 첨부파일이 있는 경우 순서대로 업로드 처리
        for (const file of pendingFiles) {
          const fd = new FormData()
          fd.append('file', file)
          // POST /api/notice/:id/files 호출 → NoticeController.java (파일 업로드)
          await axios.post(`http://localhost:8090/api/notice/${res.data.id}/files`, fd, { params: { requesterId: userId } })
        }
        // 등록 성공 시 작성된 게시글 상세 페이지로 이동 → NoticeDetail.vue
        this.$router.push(`/notice/${res.data.id}`)
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
