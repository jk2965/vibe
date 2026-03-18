<template>
  <div class="page-container">
    <PageHeader title="자료실 글쓰기" />
    <PostWriteForm
      :submitting="submitting"
      :errorMsg="errorMsg"
      submitBtnColor="#1565c0"
      @submit="handleSubmit"
      @cancel="$router.push('/archive')"
    />
  </div>
</template>

<script>
import axios from 'axios'
import PageHeader from '../common/PageHeader.vue'
import PostWriteForm from '../common/PostWriteForm.vue'

export default {
  name: 'ArchiveWrite',
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
        const res = await axios.post('http://localhost:8090/api/archive', {
          title, content,
          authorId: userId,
          authorName: localStorage.getItem('username')
        }, { params: { requesterId: userId } })
        for (const file of pendingFiles) {
          const fd = new FormData()
          fd.append('file', file)
          await axios.post(`http://localhost:8090/api/archive/${res.data.id}/files`, fd, { params: { requesterId: userId } })
        }
        this.$router.push(`/archive/${res.data.id}`)
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
