<template>
  <div class="page-container">
    <PageHeader title="FAQ" />
    <PostEditForm
      titleLabel="질문"
      contentLabel="답변"
      :showFiles="false"
      :imageUploadUrl="null"
      :initialTitle="initialTitle"
      :initialContent="initialContent"
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
import PostEditForm from './PostEditForm.vue'

export default {
  name: 'FaqEdit',
  components: { PageHeader, PostEditForm },
  data() {
    return {
      initialTitle: '',
      initialContent: '',
      submitting: false,
      errorMsg: ''
    }
  },
  mounted() {
    this.loadFaq()
  },
  methods: {
    async loadFaq() {
      try {
        const userId = localStorage.getItem('userId') || ''
        const adminLevel = parseInt(localStorage.getItem('adminLevel') || '0')
        const res = await axios.get(`/api/faq/${this.$route.params.id}`)
        const faq = res.data
        if (faq.authorId !== userId && adminLevel < 1) {
          alert('수정 권한이 없습니다.')
          this.$router.push('/faq')
          return
        }
        this.initialTitle = faq.title
        this.initialContent = faq.content || ''
      } catch (e) {
        console.error('FAQ 로드 실패:', e)
      }
    },
    async handleSubmit({ title, content }) {
      this.submitting = true
      this.errorMsg = ''
      try {
        const userId = localStorage.getItem('userId') || ''
        await axios.put(`/api/faq/${this.$route.params.id}`, { title, content }, {
          params: { requesterId: userId }
        })
        this.$router.push('/faq')
      } catch (e) {
        this.errorMsg = e.response?.data?.message || '수정 중 오류가 발생했습니다.'
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
