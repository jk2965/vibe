<template>
  <div class="write-container">
    <PageHeader title="게시글 수정" />

    <div class="write-card">
      <div class="form-group">
        <label>제목</label>
        <input v-model="form.title" type="text" placeholder="제목을 입력하세요" maxlength="200">
        <span class="char-count">{{ form.title.length }}/200</span>
      </div>
      <div class="form-group">
        <label>내용</label>
        <textarea v-model="form.content" placeholder="내용을 입력하세요" rows="14"></textarea>
      </div>
      <p v-if="errorMsg" class="error-msg">{{ errorMsg }}</p>
      <div class="btn-row">
        <button @click="submit" class="btn-submit">저장</button>
        <button @click="$router.push(`/board/${postId}`)" class="btn-cancel">취소</button>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import PageHeader from './PageHeader.vue'

export default {
  name: 'BoardEdit',
  components: { PageHeader },
  data() {
    return {
      postId: this.$route.params.id,
      form: { title: '', content: '' },
      errorMsg: ''
    }
  },
  mounted() {
    this.loadPost()
  },
  methods: {
    async loadPost() {
      try {
        const res = await axios.get(`http://localhost:8090/api/board/${this.postId}`)
        const userId = localStorage.getItem('userId')
        if (res.data.authorId !== userId) {
          this.$router.push(`/board/${this.postId}`)
          return
        }
        this.form.title = res.data.title
        this.form.content = res.data.content
      } catch (e) {
        this.$router.push('/board')
      }
    },
    async submit() {
      this.errorMsg = ''
      if (!this.form.title.trim()) { this.errorMsg = '제목을 입력하세요.'; return }
      if (!this.form.content.trim()) { this.errorMsg = '내용을 입력하세요.'; return }
      try {
        await axios.put(`http://localhost:8090/api/board/${this.postId}`, {
          title: this.form.title.trim(),
          content: this.form.content.trim()
        }, {
          params: { requesterId: localStorage.getItem('userId') }
        })
        this.$router.push(`/board/${this.postId}`)
      } catch (e) {
        this.errorMsg = e.response?.data?.message || '수정에 실패했습니다.'
      }
    }
  }
}
</script>

<style scoped>
.write-container { max-width: 900px; margin: 0 auto; padding: 24px; }

.write-card { background: white; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.08); padding: 28px; }
.form-group { margin-bottom: 20px; position: relative; }
.form-group label { display: block; margin-bottom: 8px; font-weight: bold; font-size: 14px; }
.form-group input {
  width: 100%; padding: 10px 12px; border: 1px solid #ddd; border-radius: 6px;
  box-sizing: border-box; font-size: 15px;
}
.form-group textarea {
  width: 100%; padding: 12px; border: 1px solid #ddd; border-radius: 6px;
  box-sizing: border-box; font-size: 14px; resize: vertical; line-height: 1.6;
}
.char-count { position: absolute; right: 12px; bottom: 10px; font-size: 12px; color: #aaa; }

.btn-row { display: flex; gap: 10px; }
.btn-submit { padding: 11px 36px; background: #1976d2; color: white; border: none; border-radius: 6px; font-size: 15px; cursor: pointer; }
.btn-submit:hover { background: #1565c0; }
.btn-cancel { padding: 11px 24px; background: white; color: #555; border: 1px solid #ddd; border-radius: 6px; font-size: 15px; cursor: pointer; }
.btn-cancel:hover { background: #f5f5f5; }
.error-msg { color: red; font-size: 13px; margin-bottom: 12px; }
</style>
