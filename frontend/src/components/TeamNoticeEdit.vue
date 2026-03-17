<template>
  <div class="page-container">
    <PageHeader title="팀 공지사항 수정" />
    <PostEditForm
      :initialTitle="form.title"
      :initialContent="form.content"
      :existingFiles="existingFiles"
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
import PageHeader from './PageHeader.vue'
import PostEditForm from './PostEditForm.vue'

export default {
  name: 'TeamNoticeEdit',
  components: { PageHeader, PostEditForm },
  data() {
    return {
      postId: this.$route.params.id,
      form: { title: '', content: '' },
      existingFiles: [],
      submitting: false,
      errorMsg: ''
    }
  },
  mounted() {
    this.loadPost()
  },
  methods: {
    async loadPost() {
      try {
        const res = await axios.get(`http://localhost:8090/api/team-notice/${this.postId}`, {
          params: { requesterId: localStorage.getItem('userId') }
        })
        if (res.data.authorId !== localStorage.getItem('userId')) {
          this.$router.push(`/team-notice/${this.postId}`)
          return
        }
        this.form.title = res.data.title
        this.form.content = res.data.content || ''
        this.existingFiles = res.data.files || []
      } catch (e) {
        this.$router.push('/team-notice')
      }
    },
    async handleSubmit({ title, content, pendingFiles }) {
      this.submitting = true
      this.errorMsg = ''
      try {
        const userId = localStorage.getItem('userId')
        await axios.put(`http://localhost:8090/api/team-notice/${this.postId}`, { title, content }, { params: { requesterId: userId } })
        for (const file of pendingFiles) {
          const fd = new FormData()
          fd.append('file', file)
          await axios.post(`http://localhost:8090/api/team-notice/${this.postId}/files`, fd, { params: { requesterId: userId } })
        }
        this.$router.push(`/team-notice/${this.postId}`)
      } catch (e) {
        this.errorMsg = e.response?.data?.message || '수정에 실패했습니다.'
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
