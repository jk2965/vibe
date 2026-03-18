<template>
  <div class="page-container">
    <PageHeader title="공지사항 작성" />
    <PostWriteForm
      :submitting="submitting"
      :errorMsg="errorMsg"
      submitBtnColor="#00796b"
      @submit="handleSubmit"
      @cancel="$router.push('/notice')"
    />
  </div>
</template>

<script>
import axios from 'axios'
import PageHeader from '../common/PageHeader.vue'
import PostWriteForm from '../common/PostWriteForm.vue'

export default {
  name: 'NoticeWrite',
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
        const res = await axios.post('http://localhost:8090/api/notice', {
          title, content,
          authorId: userId,
          authorName: localStorage.getItem('username')
        }, { params: { requesterId: userId } })
        for (const file of pendingFiles) {
          const fd = new FormData()
          fd.append('file', file)
          await axios.post(`http://localhost:8090/api/notice/${res.data.id}/files`, fd, { params: { requesterId: userId } })
        }
        this.$router.push(`/notice/${res.data.id}`)
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
