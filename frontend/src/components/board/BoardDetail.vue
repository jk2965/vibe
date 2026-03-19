<template>
  <div class="page-container">
    <PageHeader title="자유 게시판" />
    <PostDetailCard
      v-if="post"
      :post="post"
      :canEdit="canEdit"
      :canDelete="canDelete"
      :isRequired="isRequired"
      :canSetRequired="canSetRequired"
      :editRoute="`/board/edit/${post.id}`"
      backRoute="/board"
      @delete="deletePost"
      @file-deleted="id => post.files = post.files.filter(f => f.id !== id)"
      @toggleRequired="toggleRequired"
    />
    <CommentSection v-if="post" :boardId="$route.params.id" />
    <div v-if="!post" class="loading">게시글을 불러오는 중...</div>
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
  name: 'BoardDetail',
  // PageHeader.vue, CommentSection.vue, PostDetailCard.vue 컴포넌트 등록
  components: { PageHeader, CommentSection, PostDetailCard },
  data() {
    return {
      post: null,   // 조회된 게시글 데이터 (초기값 null → 로딩 표시에 활용)
      userId: localStorage.getItem('userId') || '',         // 로컬스토리지에서 현재 로그인 사용자 ID 로드
      isAdmin: localStorage.getItem('isAdmin') === 'true',  // 관리자 여부 (삭제 권한 판단에 사용)
      isRequired: false,
      adminLevel: parseInt(localStorage.getItem('adminLevel') || '0'),
      isTeamLeader: localStorage.getItem('isTeamLeader') === 'true'
    }
  },
  computed: {
    // 수정 가능 여부: 현재 로그인 사용자가 게시글 작성자인 경우에만 true
    canEdit() { return this.post && this.post.authorId === this.userId },
    // 삭제 가능 여부: 작성자 본인 또는 관리자인 경우 true
    canDelete() { return this.post && (this.post.authorId === this.userId || this.isAdmin) },
    // 필독 설정 권한: 관리자(adminLevel >= 1) 또는 팀장
    canSetRequired() { return this.adminLevel >= 1 || this.isTeamLeader }
  },
  // 컴포넌트 마운트 시 URL 파라미터의 게시글 ID로 상세 조회 시작
  mounted() {
    this.fetchPost()
  },
  methods: {
    // GET /api/board/:id → BoardController.java: 게시글 단건 상세 조회 (조회수 증가 포함)
    async fetchPost() {
      try {
        const res = await axios.get(`http://localhost:8090/api/board/${this.$route.params.id}`)
        this.post = res.data  // 서버에서 받은 게시글 데이터 저장
        this.isRequired = !!res.data.isRequired
      } catch (e) {
        console.error('게시글 조회 실패:', e)
      }
    },
    // PATCH /api/board/:id/required → 필독 설정/해제 토글
    async toggleRequired() {
      const newVal = this.isRequired ? 0 : 1
      try {
        await axios.patch(`http://localhost:8090/api/board/${this.$route.params.id}/required`,
          { isRequired: newVal },
          { params: { requesterId: localStorage.getItem('userId') } }
        )
        this.isRequired = !this.isRequired
      } catch (e) {
        alert(e.response?.data?.message || '필독 설정에 실패했습니다.')
      }
    },
    // DELETE /api/board/:id → BoardController.java: 게시글 삭제 후 목록 페이지로 이동
    async deletePost() {
      if (!confirm('게시글을 삭제하시겠습니까?')) return
      try {
        // requesterId 쿼리 파라미터로 삭제 요청자 ID 전달 (권한 검증용)
        await axios.delete(`http://localhost:8090/api/board/${this.post.id}`, { params: { requesterId: this.userId } })
        this.$router.push('/board')  // 삭제 성공 후 게시판 목록으로 이동
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
