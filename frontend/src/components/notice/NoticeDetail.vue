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
// PageHeader.vue 공통 헤더 컴포넌트 사용
import PageHeader from '../common/PageHeader.vue'
// CommentSection.vue 댓글 영역 공통 컴포넌트 사용
import CommentSection from '../common/CommentSection.vue'
// PostDetailCard.vue 게시글 상세 카드 공통 컴포넌트 사용
import PostDetailCard from '../common/PostDetailCard.vue'

export default {
  name: 'NoticeDetail',
  // PageHeader, CommentSection, PostDetailCard 공통 컴포넌트 등록
  components: { PageHeader, CommentSection, PostDetailCard },
  data() {
    return {
      // 공지사항 상세 데이터 (null이면 로딩 중)
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
  // 컴포넌트 마운트 시 공지사항 상세 데이터 로드
  mounted() {
    this.fetchPost()
  },
  methods: {
    // GET /api/notice/:id 호출 → NoticeController.java (단건 상세 조회, 조회수 증가)
    async fetchPost() {
      try {
        const res = await axios.get(`http://localhost:8090/api/notice/${this.$route.params.id}`)
        // 응답 데이터를 post에 저장
        this.post = res.data
      } catch (e) {
        console.error('공지사항 조회 실패:', e)
      }
    },
    // DELETE /api/notice/:id 호출 → NoticeController.java (삭제 후 목록으로 이동)
    async deletePost() {
      if (!confirm('공지사항을 삭제하시겠습니까?')) return
      try {
        await axios.delete(`http://localhost:8090/api/notice/${this.post.id}`, { params: { requesterId: this.userId } })
        // 삭제 성공 시 공지사항 목록으로 이동 → Notice.vue
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
