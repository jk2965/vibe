<template>
  <div class="page-container">
    <PageHeader :title="post ? post.team + ' 공지사항' : '팀별 공지사항'" />
    <PostDetailCard
      v-if="post"
      :post="post"
      :canEdit="canEdit"
      :canDelete="canDelete"
      :editRoute="`/team-notice/edit/${post.id}`"
      backRoute="/team-notice"
      @delete="deletePost"
      @file-deleted="id => post.files = post.files.filter(f => f.id !== id)"
    >
      <template #extra-meta>
        <span class="team-badge">{{ post.team }}</span>
        <span class="sep">|</span>
      </template>
    </PostDetailCard>
    <CommentSection v-if="post" :boardId="$route.params.id" />
    <div v-if="!post" class="loading">공지사항을 불러오는 중...</div>
  </div>
</template>

<script>
import axios from 'axios'
import PageHeader from '../common/PageHeader.vue'
import CommentSection from '../common/CommentSection.vue'
import PostDetailCard from '../common/PostDetailCard.vue'

export default {
  name: 'TeamNoticeDetail',
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
        const res = await axios.get(`http://localhost:8090/api/team-notice/${this.$route.params.id}`, {
          params: { requesterId: this.userId }
        })
        this.post = res.data
      } catch (e) {
        if (e.response?.status === 403) {
          alert('접근 권한이 없습니다.')
          this.$router.push('/team-notice')
        }
      }
    },
    async deletePost() {
      if (!confirm('공지사항을 삭제하시겠습니까?')) return
      try {
        await axios.delete(`http://localhost:8090/api/team-notice/${this.post.id}`, { params: { requesterId: this.userId } })
        this.$router.push('/team-notice')
      } catch (e) {
        alert(e.response?.data?.message || '삭제에 실패했습니다.')
      }
    }
  }
}
</script>

<style scoped>
.page-container { max-width: 900px; margin: 0 auto; padding: 24px; }
.team-badge { background: #e8eaf6; color: #3949ab; padding: 2px 8px; border-radius: 10px; font-size: 12px; font-weight: bold; }
.sep { margin: 0 6px; }
.loading { text-align: center; padding: 60px; color: #999; }
</style>
