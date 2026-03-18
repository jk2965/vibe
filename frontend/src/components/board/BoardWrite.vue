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
import PageHeader from '../common/PageHeader.vue'
import PostWriteForm from '../common/PostWriteForm.vue'

export default {
  name: 'BoardWrite',
  components: { PageHeader, PostWriteForm },
  data() {
    return { submitting: false, errorMsg: '' }
  },
  methods: {
    async handleSubmit({ title, content, pendingFiles }) {
      this.submitting = true
      this.errorMsg = ''
      try {
        const userId = localStorage.getItem('userId')
        const res = await axios.post('http://localhost:8090/api/board', {
          title, content,
          authorId: userId,
          authorName: localStorage.getItem('username')
        })
        for (const file of pendingFiles) {
          const fd = new FormData()
          fd.append('file', file)
          await axios.post(`http://localhost:8090/api/board/${res.data.id}/files`, fd, { params: { requesterId: userId } })
        }
        this.$router.push(`/board/${res.data.id}`)
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
.page-container { max-width: 900px; margin: 0 auto; padding: 24px; }
</style>
