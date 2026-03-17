<template>
  <div class="detail-container">
    <PageHeader :title="post ? post.team + ' 공지사항' : '팀별 공지사항'" />

    <div v-if="post" class="detail-card">
      <div class="post-header">
        <h2 class="post-title">{{ post.title }}</h2>
        <div class="post-meta">
          <span class="team-badge">{{ post.team }}</span>
          <span class="sep">|</span>
          <span>{{ post.authorName }}</span>
          <span class="sep">|</span>
          <span>{{ post.createdAt }}</span>
          <span class="sep">|</span>
          <span>조회 {{ post.views }}</span>
        </div>
      </div>
      <div class="post-content">{{ post.content }}</div>
      <div class="btn-row">
        <button @click="$router.push('/team-notice')" class="btn-back">목록으로</button>
        <div class="btn-right">
          <button v-if="canEdit" @click="$router.push(`/team-notice/edit/${post.id}`)" class="btn-edit">수정</button>
          <button v-if="canDelete" @click="deletePost" class="btn-delete">삭제</button>
        </div>
      </div>
    </div>

    <CommentSection v-if="post" :boardId="$route.params.id" />

    <div v-else class="loading">공지사항을 불러오는 중...</div>
  </div>
</template>

<script>
import axios from 'axios'
import PageHeader from './PageHeader.vue'
import CommentSection from './CommentSection.vue'

export default {
  name: 'TeamNoticeDetail',
  components: { PageHeader, CommentSection },
  data() {
    return {
      post: null,
      userId: localStorage.getItem('userId') || '',
      adminLevel: parseInt(localStorage.getItem('adminLevel') || '0')
    }
  },
  computed: {
    canEdit() {
      return this.post && this.post.authorId === this.userId
    },
    canDelete() {
      return this.post && (this.post.authorId === this.userId || this.adminLevel >= 1)
    }
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
        console.error('팀 공지사항 조회 실패:', e)
      }
    },
    async deletePost() {
      if (!confirm('공지사항을 삭제하시겠습니까?')) return
      try {
        await axios.delete(`http://localhost:8090/api/team-notice/${this.post.id}`, {
          params: { requesterId: this.userId }
        })
        this.$router.push('/team-notice')
      } catch (e) {
        alert(e.response?.data?.message || '삭제에 실패했습니다.')
      }
    }
  }
}
</script>

<style scoped>
.detail-container { max-width: 900px; margin: 0 auto; padding: 24px; }
.detail-card { background: white; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.08); padding: 32px; margin-bottom: 16px; }
.post-header { border-bottom: 2px solid #eee; padding-bottom: 16px; margin-bottom: 24px; }
.post-title { margin: 0 0 12px 0; font-size: 22px; line-height: 1.4; }
.post-meta { font-size: 13px; color: #888; }
.team-badge { background: #e8eaf6; color: #3949ab; padding: 2px 8px; border-radius: 10px; font-size: 12px; font-weight: bold; }
.sep { margin: 0 8px; }
.post-content { min-height: 200px; font-size: 15px; line-height: 1.8; white-space: pre-wrap; word-break: break-word; margin-bottom: 32px; }
.btn-row { display: flex; justify-content: space-between; align-items: center; }
.btn-right { display: flex; gap: 8px; }
.btn-back { padding: 10px 24px; background: white; color: #555; border: 1px solid #ddd; border-radius: 6px; font-size: 14px; cursor: pointer; }
.btn-back:hover { background: #f5f5f5; }
.btn-edit { padding: 10px 24px; background: #f57c00; color: white; border: none; border-radius: 6px; font-size: 14px; cursor: pointer; }
.btn-edit:hover { background: #e65100; }
.btn-delete { padding: 10px 24px; background: #e53935; color: white; border: none; border-radius: 6px; font-size: 14px; cursor: pointer; }
.btn-delete:hover { background: #c62828; }
.loading { text-align: center; padding: 60px; color: #999; }
</style>
