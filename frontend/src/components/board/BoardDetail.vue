<template>
  <div class="page-container">
    <PageHeader title="자유 게시판" />
    <PostDetailCard
      v-if="post"
      :post="post"
      :canEdit="canEdit"
      :canDelete="canDelete"
      :editRoute="`/board/edit/${post.id}`"
      backRoute="/board"
      @delete="deletePost"
      @file-deleted="id => post.files = post.files.filter(f => f.id !== id)"
    />
    <CommentSection v-if="post" :boardId="$route.params.id" />
    <div v-if="!post" class="loading">게시글을 불러오는 중...</div>
  </div>
</template>

<script>
import axios from 'axios'
import PageHeader from '../common/PageHeader.vue'
import CommentSection from '../common/CommentSection.vue'
import PostDetailCard from '../common/PostDetailCard.vue'

export default {
  name: 'BoardDetail',
  components: { PageHeader, CommentSection, PostDetailCard },
  data() {
    return {
      post: null,
      userId: localStorage.getItem('userId') || '',
      isAdmin: localStorage.getItem('isAdmin') === 'true'
    }
  },
  computed: {
    canEdit() { return this.post && this.post.authorId === this.userId },
    canDelete() { return this.post && (this.post.authorId === this.userId || this.isAdmin) }
  },
  mounted() {
    this.fetchPost()
  },
  methods: {
    async fetchPost() {
      try {
        const res = await axios.get(`http://localhost:8090/api/board/${this.$route.params.id}`)
        this.post = res.data
      } catch (e) {
        console.error('게시글 조회 실패:', e)
      }
    },
    async deletePost() {
      if (!confirm('게시글을 삭제하시겠습니까?')) return
      try {
        await axios.delete(`http://localhost:8090/api/board/${this.post.id}`, { params: { requesterId: this.userId } })
        this.$router.push('/board')
      } catch (e) {
        alert(e.response?.data?.message || '삭제에 실패했습니다.')
      }
    }
  }
}
</script>

<style scoped>
.page-container { max-width: 900px; margin: 0 auto; padding: 24px; }
.loading { text-align: center; padding: 60px; color: #999; }
</style>
