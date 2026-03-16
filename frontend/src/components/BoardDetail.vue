<template>
  <div class="detail-container">
    <PageHeader title="자유 게시판" />

    <div v-if="post" class="detail-card">
      <div class="post-header">
        <h2 class="post-title">{{ post.title }}</h2>
        <div class="post-meta">
          <span>{{ post.authorName }}</span>
          <span class="sep">|</span>
          <span>{{ post.createdAt }}</span>
          <span class="sep">|</span>
          <span>조회 {{ post.views }}</span>
        </div>
      </div>
      <div class="post-content">{{ post.content }}</div>
      <div class="btn-row">
        <button @click="$router.push('/board')" class="btn-back">목록으로</button>
        <div class="btn-right">
          <button v-if="canEdit" @click="$router.push(`/board/edit/${post.id}`)" class="btn-edit">수정</button>
          <button v-if="canDelete" @click="deletePost" class="btn-delete">삭제</button>
        </div>
      </div>
    </div>

    <!-- 댓글 영역 -->
    <div v-if="post" class="comment-card">
      <h3 class="comment-title">댓글 <span class="comment-count">{{ comments.length }}</span></h3>

      <!-- 댓글 목록 -->
      <div v-if="comments.length === 0" class="comment-empty">첫 댓글을 작성해보세요.</div>
      <div v-for="c in comments" :key="c.id" class="comment-item">
        <div class="comment-meta">
          <span class="comment-author">{{ c.authorName }}</span>
          <span class="comment-date">{{ c.createdAt }}</span>
          <div v-if="c.authorId === userId" class="comment-actions">
            <button @click="startEdit(c)" class="btn-c-edit">수정</button>
            <button @click="deleteComment(c.id)" class="btn-c-delete">삭제</button>
          </div>
        </div>

        <!-- 수정 모드 -->
        <div v-if="editingId === c.id" class="edit-mode">
          <textarea v-model="editContent" rows="3" class="edit-textarea"></textarea>
          <div class="edit-btns">
            <button @click="submitEdit(c.id)" class="btn-c-save">저장</button>
            <button @click="cancelEdit" class="btn-c-cancel">취소</button>
          </div>
        </div>
        <!-- 일반 표시 -->
        <div v-else class="comment-content">{{ c.content }}</div>
      </div>

      <!-- 댓글 작성 -->
      <div class="comment-write">
        <textarea v-model="newComment" rows="3" placeholder="댓글을 입력하세요" class="write-textarea"></textarea>
        <div class="write-bottom">
          <span class="char-hint">{{ newComment.length }}/1000</span>
          <button @click="submitComment" class="btn-c-submit">등록</button>
        </div>
      </div>
    </div>

    <div v-else class="loading">게시글을 불러오는 중...</div>
  </div>
</template>

<script>
import axios from 'axios'
import PageHeader from './PageHeader.vue'

export default {
  name: 'BoardDetail',
  components: { PageHeader },
  data() {
    return {
      post: null,
      comments: [],
      newComment: '',
      editingId: null,
      editContent: '',
      userId: localStorage.getItem('userId') || '',
      isAdmin: localStorage.getItem('isAdmin') === 'true'
    }
  },
  computed: {
    canEdit() {
      return this.post && this.post.authorId === this.userId
    },
    canDelete() {
      return this.post && (this.post.authorId === this.userId || this.isAdmin)
    }
  },
  mounted() {
    this.fetchPost()
    this.fetchComments()
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
    async fetchComments() {
      try {
        const res = await axios.get('http://localhost:8090/api/comment', {
          params: { boardId: this.$route.params.id }
        })
        this.comments = res.data
      } catch (e) {
        console.error('댓글 조회 실패:', e)
      }
    },
    async submitComment() {
      if (!this.newComment.trim()) return
      try {
        await axios.post('http://localhost:8090/api/comment', {
          boardId: this.$route.params.id,
          authorId: this.userId,
          authorName: localStorage.getItem('username') || '',
          content: this.newComment.trim()
        })
        this.newComment = ''
        await this.fetchComments()
      } catch (e) {
        alert(e.response?.data?.message || '댓글 등록에 실패했습니다.')
      }
    },
    startEdit(comment) {
      this.editingId = comment.id
      this.editContent = comment.content
    },
    cancelEdit() {
      this.editingId = null
      this.editContent = ''
    },
    async submitEdit(id) {
      if (!this.editContent.trim()) return
      try {
        await axios.put(`http://localhost:8090/api/comment/${id}`, {
          content: this.editContent.trim()
        }, {
          params: { requesterId: this.userId }
        })
        this.editingId = null
        this.editContent = ''
        await this.fetchComments()
      } catch (e) {
        alert(e.response?.data?.message || '댓글 수정에 실패했습니다.')
      }
    },
    async deleteComment(id) {
      if (!confirm('댓글을 삭제하시겠습니까?')) return
      try {
        await axios.delete(`http://localhost:8090/api/comment/${id}`, {
          params: { requesterId: this.userId }
        })
        await this.fetchComments()
      } catch (e) {
        alert(e.response?.data?.message || '댓글 삭제에 실패했습니다.')
      }
    },
    async deletePost() {
      if (!confirm('게시글을 삭제하시겠습니까?')) return
      try {
        await axios.delete(`http://localhost:8090/api/board/${this.post.id}`, {
          params: { requesterId: this.userId }
        })
        this.$router.push('/board')
      } catch (e) {
        alert(e.response?.data?.message || '삭제에 실패했습니다.')
      }
    }
  }
}
</script>

<style scoped>
.detail-container { max-width: 900px; margin: 0 auto; padding: 24px; }

/* 게시글 카드 */
.detail-card { background: white; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.08); padding: 32px; margin-bottom: 16px; }
.post-header { border-bottom: 2px solid #eee; padding-bottom: 16px; margin-bottom: 24px; }
.post-title { margin: 0 0 12px 0; font-size: 22px; line-height: 1.4; }
.post-meta { font-size: 13px; color: #888; }
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

/* 댓글 카드 */
.comment-card { background: white; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.08); padding: 24px; }
.comment-title { margin: 0 0 20px 0; font-size: 16px; font-weight: bold; }
.comment-count { color: #1976d2; }
.comment-empty { text-align: center; color: #bbb; padding: 24px 0; font-size: 14px; }

.comment-item { border-top: 1px solid #f0f0f0; padding: 14px 0; }
.comment-meta { display: flex; align-items: center; gap: 10px; margin-bottom: 8px; }
.comment-author { font-weight: bold; font-size: 13px; }
.comment-date { font-size: 12px; color: #aaa; flex: 1; }
.comment-actions { display: flex; gap: 6px; }
.btn-c-edit { padding: 3px 10px; background: #f5f5f5; color: #555; border: 1px solid #ddd; border-radius: 4px; cursor: pointer; font-size: 12px; }
.btn-c-edit:hover { background: #e0e0e0; }
.btn-c-delete { padding: 3px 10px; background: #fdecea; color: #e53935; border: 1px solid #ffcdd2; border-radius: 4px; cursor: pointer; font-size: 12px; }
.btn-c-delete:hover { background: #ffcdd2; }

.comment-content { font-size: 14px; line-height: 1.7; color: #333; white-space: pre-wrap; }

.edit-mode { margin-top: 4px; }
.edit-textarea { width: 100%; padding: 10px; border: 1px solid #1976d2; border-radius: 6px; box-sizing: border-box; font-size: 14px; resize: none; line-height: 1.6; }
.edit-btns { display: flex; gap: 6px; margin-top: 8px; justify-content: flex-end; }
.btn-c-save { padding: 6px 16px; background: #1976d2; color: white; border: none; border-radius: 4px; cursor: pointer; font-size: 13px; }
.btn-c-save:hover { background: #1565c0; }
.btn-c-cancel { padding: 6px 16px; background: white; color: #555; border: 1px solid #ddd; border-radius: 4px; cursor: pointer; font-size: 13px; }
.btn-c-cancel:hover { background: #f5f5f5; }

/* 댓글 작성 */
.comment-write { border-top: 2px solid #eee; padding-top: 20px; margin-top: 8px; }
.write-textarea { width: 100%; padding: 12px; border: 1px solid #ddd; border-radius: 6px; box-sizing: border-box; font-size: 14px; resize: none; line-height: 1.6; }
.write-textarea:focus { border-color: #1976d2; outline: none; }
.write-bottom { display: flex; justify-content: space-between; align-items: center; margin-top: 8px; }
.char-hint { font-size: 12px; color: #aaa; }
.btn-c-submit { padding: 8px 24px; background: #1976d2; color: white; border: none; border-radius: 6px; cursor: pointer; font-size: 14px; }
.btn-c-submit:hover { background: #1565c0; }

.loading { text-align: center; padding: 60px; color: #999; }
</style>
