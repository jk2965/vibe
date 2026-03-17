<template>
  <div class="page-container">
    <PageHeader :title="post ? post.team + ' 자료실' : '팀별 자료실'" />
    <PostDetailCard
      v-if="post"
      :post="post"
      :canEdit="canEdit"
      :canDelete="canDelete"
      :editRoute="`/team-archive/edit/${post.id}`"
      backRoute="/team-archive"
      @delete="deletePost"
      @file-deleted="id => post.files = post.files.filter(f => f.id !== id)"
    >
      <template #extra-meta>
        <span class="team-badge">{{ post.team }}</span>
        <span class="sep">|</span>
      </template>
    </PostDetailCard>
    <CommentSection v-if="post" :boardId="$route.params.id" />
    <div v-if="!post" class="loading">자료를 불러오는 중...</div>
  </div>
</template>

<script>
import axios from 'axios'
import PageHeader from './PageHeader.vue'
import CommentSection from './CommentSection.vue'
import PostDetailCard from './PostDetailCard.vue'

export default {
  name: 'TeamArchiveDetail',
  components: { PageHeader, CommentSection, PostDetailCard },
  data() {
    return {
      post: null,
      userId: localStorage.getItem('userId') || '',
      adminLevel: parseInt(localStorage.getItem('adminLevel') || '0')
    }
  },
  computed: {
    canEdit() { return this.post && this.post.authorId === this.userId },
    canDelete() { return this.post && (this.post.authorId === this.userId || this.adminLevel >= 1) }
  },
  mounted() {
    this.fetchPost()
  },
  methods: {
    async fetchPost() {
      try {
        const res = await axios.get(`http://localhost:8090/api/team-archive/${this.$route.params.id}`, {
          params: { requesterId: this.userId }
        })
        this.post = res.data
      } catch (e) {
        if (e.response?.status === 403) {
          alert('접근 권한이 없습니다.')
          this.$router.push('/team-archive')
        }
      }
    },
    async deletePost() {
      if (!confirm('이 자료를 삭제하시겠습니까?')) return
      try {
        await axios.delete(`http://localhost:8090/api/team-archive/${this.post.id}`, { params: { requesterId: this.userId } })
        this.$router.push('/team-archive')
      } catch (e) {
        alert(e.response?.data?.message || '삭제에 실패했습니다.')
      }
    }
  }
}
</script>

<style scoped>
.page-container { max-width: 960px; margin: 0 auto; padding: 24px; }
.team-badge { background: #e8f5e9; color: #2e7d32; padding: 2px 8px; border-radius: 10px; font-size: 12px; font-weight: bold; }
.sep { margin: 0 6px; }
.loading { text-align: center; padding: 60px; color: #999; }
</style>
