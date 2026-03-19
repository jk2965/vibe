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
// PageHeader.vue 공통 헤더 컴포넌트 사용
import PageHeader from '../common/PageHeader.vue'
// CommentSection.vue 댓글 영역 공통 컴포넌트 사용
import CommentSection from '../common/CommentSection.vue'
// PostDetailCard.vue 게시글 상세 카드 공통 컴포넌트 사용 (슬롯으로 팀 배지 추가)
import PostDetailCard from '../common/PostDetailCard.vue'

export default {
  name: 'TeamNoticeDetail',
  // PageHeader, CommentSection, PostDetailCard 공통 컴포넌트 등록
  components: { PageHeader, CommentSection, PostDetailCard },
  data() {
    return {
      // 팀 공지사항 상세 데이터 (null이면 로딩 중)
      post: null,
      // 현재 로그인 사용자 ID (권한 확인에 사용)
      userId: localStorage.getItem('userId') || '',
      // 관리자 레벨 (0: 일반, 1 이상: 관리자)
      adminLevel: parseInt(localStorage.getItem('adminLevel') || '0')
    }
  },
  computed: {
    // 수정 권한: 본인 작성 게시글인 경우에만 허용
    canEdit() { return this.post && this.post.authorId === this.userId },
    // 삭제 권한: 본인 작성이거나 관리자(레벨 1 이상)인 경우 허용
    canDelete() { return this.post && (this.post.authorId === this.userId || this.adminLevel >= 1) }
  },
  // 컴포넌트 마운트 시 팀 공지사항 상세 데이터 로드
  mounted() {
    this.fetchPost()
  },
  methods: {
    // GET /api/team-notice/:id 호출 → TeamNoticeController.java (단건 상세 조회, 팀 접근 권한 검증)
    async fetchPost() {
      try {
        const res = await axios.get(`http://localhost:8090/api/team-notice/${this.$route.params.id}`, {
          params: { requesterId: this.userId }
        })
        // 응답 데이터를 post에 저장
        this.post = res.data
      } catch (e) {
        // 403 에러: 다른 팀 게시글 접근 시 목록으로 리다이렉트
        if (e.response?.status === 403) {
          alert('접근 권한이 없습니다.')
          this.$router.push('/team-notice')
        }
      }
    },
    // DELETE /api/team-notice/:id 호출 → TeamNoticeController.java (삭제 후 목록으로 이동)
    async deletePost() {
      if (!confirm('공지사항을 삭제하시겠습니까?')) return
      try {
        await axios.delete(`http://localhost:8090/api/team-notice/${this.post.id}`, { params: { requesterId: this.userId } })
        // 삭제 성공 시 팀 공지사항 목록으로 이동 → TeamNotice.vue
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
