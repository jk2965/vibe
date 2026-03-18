<template>
  <div class="page-container">
    <PageHeader title="FAQ" />
    <PostWriteForm
      titleLabel="질문"
      contentLabel="답변"
      :showFiles="false"
      :imageUploadUrl="null"
      :submitting="submitting"
      :errorMsg="errorMsg"
      submitBtnColor="#6a1b9a"
      @submit="handleSubmit"
      @cancel="$router.push('/faq')"
    />
  </div>
</template>

<script>
import axios from 'axios'
import PageHeader from './PageHeader.vue'
import PostWriteForm from './PostWriteForm.vue'

export default {
  name: 'FaqWrite',
  components: { PageHeader, PostWriteForm },
  data() {
    return {
      submitting: false,
      errorMsg: ''
    }
  },
  methods: {
    async handleSubmit({ title, content }) {
      this.submitting = true
      this.errorMsg = ''
      try {
        const userId = localStorage.getItem('userId') || ''
        const username = localStorage.getItem('username') || ''
        await axios.post('/api/faq', {
          title, content, authorId: userId, authorName: username
        }, { params: { requesterId: userId } })
        this.$router.push('/faq')
      } catch (e) {
        this.errorMsg = e.response?.data?.message || '저장 중 오류가 발생했습니다.'
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
