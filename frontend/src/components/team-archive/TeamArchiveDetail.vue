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
// 공통 페이지 헤더 컴포넌트 (PageHeader.vue)
import PageHeader from '../common/PageHeader.vue'
// 공통 댓글 섹션 컴포넌트 (CommentSection.vue) - boardId prop으로 게시글 ID 전달
import CommentSection from '../common/CommentSection.vue'
// 공통 게시글 상세 카드 컴포넌트 (PostDetailCard.vue) - 제목, 내용, 파일, 수정/삭제 버튼 표시
import PostDetailCard from '../common/PostDetailCard.vue'

export default {
  name: 'TeamArchiveDetail',
  components: { PageHeader, CommentSection, PostDetailCard },
  data() {
    return {
      // 조회된 게시글 상세 데이터 (null이면 로딩 중)
      post: null,
      // 현재 로그인한 사용자 ID
      userId: localStorage.getItem('userId') || '',
      // 현재 로그인한 사용자의 관리자 레벨
      adminLevel: parseInt(localStorage.getItem('adminLevel') || '0')
    }
  },
  computed: {
    // 수정 권한: 작성자 본인만 가능
    canEdit() { return this.post && this.post.authorId === this.userId },
    // 삭제 권한: 작성자 본인 또는 관리자(adminLevel 1 이상)
    canDelete() { return this.post && (this.post.authorId === this.userId || this.adminLevel >= 1) }
  },
  // 컴포넌트 마운트 시 게시글 상세 조회
  mounted() {
    this.fetchPost()
  },
  methods: {
    // GET /api/team-archive/:id 호출 → TeamArchiveController.java
    // URL 파라미터의 id로 게시글 상세 조회, 접근 권한 없으면 목록으로 리다이렉트
    async fetchPost() {
      try {
        const res = await axios.get(`http://localhost:8090/api/team-archive/${this.$route.params.id}`, {
          params: { requesterId: this.userId }
        })
        // 게시글 상세 데이터 저장 (files 배열 포함)
        this.post = res.data
      } catch (e) {
        // 403 Forbidden: 다른 팀 자료실 접근 시 권한 없음 처리
        if (e.response?.status === 403) {
          alert('접근 권한이 없습니다.')
          this.$router.push('/team-archive')
        }
      }
    },
    // DELETE /api/team-archive/:id 호출 → TeamArchiveController.java
    // 삭제 확인 후 게시글 삭제, 성공 시 목록으로 이동
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
