<template>
  <div class="page-container">
    <PageHeader title="팀별 자료실 글쓰기" />
    <PostWriteForm
      :submitting="submitting"
      :errorMsg="errorMsg"
      submitBtnColor="#2e7d32"
      @submit="handleSubmit"
      @cancel="$router.push('/team-archive')"
    />
  </div>
</template>

<script>
import axios from 'axios'
import PageHeader from './PageHeader.vue'
import PostWriteForm from './PostWriteForm.vue'

export default {
  name: 'TeamArchiveWrite',
  components: { PageHeader, PostWriteForm },
  data() {
    return {
      team: this.$route.query.team || localStorage.getItem('team') || '',
      submitting: false,
      errorMsg: ''
    }
  },
  methods: {
    async handleSubmit({ title, content, pendingFiles }) {
      this.submitting = true
      this.errorMsg = ''
      try {
        const userId = localStorage.getItem('userId')
        const res = await axios.post('http://localhost:8090/api/team-archive', {
          title, content,
          authorId: userId,
          authorName: localStorage.getItem('username'),
          team: this.team
        }, { params: { requesterId: userId } })
        for (const file of pendingFiles) {
          const fd = new FormData()
          fd.append('file', file)
          await axios.post(`http://localhost:8090/api/team-archive/${res.data.id}/files`, fd, { params: { requesterId: userId } })
        }
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
