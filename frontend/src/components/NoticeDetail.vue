<template>
  <div class="page-container">
    <PageHeader title="전체 공지사항" />
    <PostDetailCard
      v-if="post"
      :post="post"
      :canEdit="canEdit"
      :canDelete="canDelete"
      :editRoute="`/notice/edit/${post.id}`"
      backRoute="/notice"
      @delete="deletePost"
      @file-deleted="id => post.files = post.files.filter(f => f.id !== id)"
    />
    <CommentSection v-if="post" :boardId="$route.params.id" />
    <div v-if="!post" class="loading">공지사항을 불러오는 중...</div>
  </div>
</template>

<script>
import axios from 'axios'
import PageHeader from './PageHeader.vue'
import CommentSection from './CommentSection.vue'
import PostDetailCard from './PostDetailCard.vue'

export default {
  name: 'NoticeDetail',
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
        const res = await axios.get(`http://localhost:8090/api/notice/${this.$route.params.id}`)
        this.post = res.data
      } catch (e) {
        console.error('공지사항 조회 실패:', e)
      }
    },
    async deletePost() {
      if (!confirm('공지사항을 삭제하시겠습니까?')) return
      try {
        await axios.delete(`http://localhost:8090/api/notice/${this.post.id}`, { params: { requesterId: this.userId } })
        this.$router.push('/notice')
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
