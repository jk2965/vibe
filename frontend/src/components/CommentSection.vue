<template>
  <div class="comment-card">
    <h3 class="comment-title">댓글 <span class="comment-count">{{ activeCount }}</span></h3>

    <div v-if="flatComments.length === 0" class="comment-empty">첫 댓글을 작성해보세요.</div>

    <div v-for="item in flatComments" :key="item.id">
      <div
        class="comment-item"
        :class="{ 'reply-item': item.depth > 0 }"
        :style="item.depth > 0 ? { paddingLeft: (item.depth * 32) + 'px' } : {}"
      >
        <!-- 들여쓰기 화살표 -->
        <div v-if="item.depth > 0" class="reply-indicator">↳</div>

        <div class="comment-body">
          <!-- 삭제된 댓글 -->
          <div v-if="item.isDeleted === 1" class="comment-deleted">삭제되었습니다.</div>

          <!-- 정상 댓글 -->
          <template v-else>
            <div class="comment-meta">
              <span class="comment-author">{{ item.authorName }}</span>
              <span class="comment-date">{{ item.createdAt }}</span>
              <div class="comment-actions">
                <button @click="toggleReply(item.id, null)" class="btn-c-reply">답글</button>
                <template v-if="item.authorId === userId">
                  <button @click="startEdit(item)" class="btn-c-edit">수정</button>
                  <button @click="deleteComment(item.id)" class="btn-c-delete">삭제</button>
                </template>
              </div>
            </div>

            <!-- 수정 모드 -->
            <div v-if="editingId === item.id" class="edit-mode">
              <textarea v-model="editContent" rows="3" class="edit-textarea"></textarea>
              <div class="edit-btns">
                <button @click="submitEdit(item.id)" class="btn-c-save">저장</button>
                <button @click="cancelEdit" class="btn-c-cancel">취소</button>
              </div>
            </div>
            <div v-else class="comment-content">{{ item.content }}</div>

            <!-- 답글 작성 폼 -->
            <div v-if="replyingToId === item.id" class="reply-write">
              <textarea v-model="replyContent" rows="2" placeholder="답글을 입력하세요" class="reply-textarea"></textarea>
              <div class="reply-bottom">
                <span class="char-hint">{{ replyContent.length }}/1000</span>
                <div>
                  <button @click="submitReply(item.id)" class="btn-c-save">등록</button>
                  <button @click="cancelReply" class="btn-c-cancel">취소</button>
                </div>
              </div>
            </div>
          </template>
        </div>
      </div>
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
</template>

<script>
import axios from 'axios'

export default {
  name: 'CommentSection',
  props: {
    boardId: { type: String, required: true }
  },
  data() {
    return {
      comments: [],
      newComment: '',
      replyContent: '',
      replyingToId: null,
      editingId: null,
      editContent: '',
      userId: localStorage.getItem('userId') || '',
      username: localStorage.getItem('username') || ''
    }
  },
  computed: {
    activeCount() {
      return this.comments.filter(c => c.isDeleted !== 1).length
    },
    flatComments() {
      // 트리 구조로 변환
      const map = {}
      this.comments.forEach(c => { map[c.id] = { ...c, children: [] } })
      const roots = []
      this.comments.forEach(c => {
        if (c.parentId && map[c.parentId]) {
          map[c.parentId].children.push(map[c.id])
        } else {
          roots.push(map[c.id])
        }
      })
      // 트리를 depth 포함 flat 배열로 변환
      const result = []
      const flatten = (nodes, depth) => {
        nodes.forEach(n => {
          result.push({ ...n, depth })
          if (n.children && n.children.length > 0) {
            flatten(n.children, depth + 1)
          }
        })
      }
      flatten(roots, 0)
      return result
    }
  },
  mounted() {
    this.fetchComments()
  },
  methods: {
    async fetchComments() {
      try {
        const res = await axios.get('http://localhost:8090/api/comment', {
          params: { boardId: this.boardId }
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
          boardId: this.boardId,
          authorId: this.userId,
          authorName: this.username,
          content: this.newComment.trim()
        })
        this.newComment = ''
        await this.fetchComments()
      } catch (e) {
        alert(e.response?.data?.message || '댓글 등록에 실패했습니다.')
      }
    },
    toggleReply(commentId, authorName) {
      if (this.replyingToId === commentId) {
        this.replyingToId = null
        this.replyContent = ''
      } else {
        this.replyingToId = commentId
        this.replyContent = authorName ? `@${authorName} ` : ''
      }
    },
    cancelReply() {
      this.replyingToId = null
      this.replyContent = ''
    },
    async submitReply(parentId) {
      if (!this.replyContent.trim()) return
      try {
        await axios.post('http://localhost:8090/api/comment', {
          boardId: this.boardId,
          authorId: this.userId,
          authorName: this.username,
          content: this.replyContent.trim(),
          parentId: parentId
        })
        this.replyingToId = null
        this.replyContent = ''
        await this.fetchComments()
      } catch (e) {
        alert(e.response?.data?.message || '답글 등록에 실패했습니다.')
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
        alert(e.response?.data?.message || '수정에 실패했습니다.')
      }
    },
    async deleteComment(id) {
      if (!confirm('삭제하시겠습니까?')) return
      try {
        await axios.delete(`http://localhost:8090/api/comment/${id}`, {
          params: { requesterId: this.userId }
        })
        await this.fetchComments()
      } catch (e) {
        alert(e.response?.data?.message || '삭제에 실패했습니다.')
      }
    }
  }
}
</script>

<style scoped>
.comment-card { background: white; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.08); padding: 24px; }
.comment-title { margin: 0 0 20px 0; font-size: 16px; font-weight: bold; }
.comment-count { color: #1976d2; }
.comment-empty { text-align: center; color: #bbb; padding: 24px 0; font-size: 14px; }

/* 댓글/답글 공통 */
.comment-item { border-top: 1px solid #f0f0f0; padding: 14px 0; display: flex; gap: 8px; }
.reply-item { background: #fafafa; border-radius: 6px; border-top: 1px solid #f0f0f0; }
.reply-indicator { color: #bbb; font-size: 16px; flex-shrink: 0; padding-top: 2px; }
.comment-body { flex: 1; min-width: 0; }

.comment-meta { display: flex; align-items: center; gap: 10px; margin-bottom: 8px; flex-wrap: wrap; }
.comment-author { font-weight: bold; font-size: 13px; }
.comment-date { font-size: 12px; color: #aaa; flex: 1; }
.comment-actions { display: flex; gap: 6px; }
.comment-content { font-size: 14px; line-height: 1.7; color: #333; white-space: pre-wrap; word-break: break-word; }
.comment-deleted { font-size: 13px; color: #bbb; font-style: italic; padding: 4px 0; }

/* 답글 작성 폼 */
.reply-write { margin-top: 10px; padding: 12px; background: #f5f8ff; border-radius: 8px; border: 1px solid #e3eaff; }
.reply-textarea { width: 100%; padding: 10px; border: 1px solid #c5cae9; border-radius: 6px; box-sizing: border-box; font-size: 14px; resize: none; line-height: 1.6; background: white; }
.reply-textarea:focus { border-color: #1976d2; outline: none; }
.reply-bottom { display: flex; justify-content: space-between; align-items: center; margin-top: 8px; }

/* 수정 */
.edit-mode { margin-top: 4px; }
.edit-textarea { width: 100%; padding: 10px; border: 1px solid #1976d2; border-radius: 6px; box-sizing: border-box; font-size: 14px; resize: none; line-height: 1.6; }
.edit-btns { display: flex; gap: 6px; margin-top: 8px; justify-content: flex-end; }

/* 버튼 */
.btn-c-reply { padding: 3px 10px; background: #e8f5e9; color: #2e7d32; border: 1px solid #c8e6c9; border-radius: 4px; cursor: pointer; font-size: 12px; }
.btn-c-reply:hover { background: #c8e6c9; }
.btn-c-edit { padding: 3px 10px; background: #f5f5f5; color: #555; border: 1px solid #ddd; border-radius: 4px; cursor: pointer; font-size: 12px; }
.btn-c-edit:hover { background: #e0e0e0; }
.btn-c-delete { padding: 3px 10px; background: #fdecea; color: #e53935; border: 1px solid #ffcdd2; border-radius: 4px; cursor: pointer; font-size: 12px; }
.btn-c-delete:hover { background: #ffcdd2; }
.btn-c-save { padding: 6px 16px; background: #1976d2; color: white; border: none; border-radius: 4px; cursor: pointer; font-size: 13px; }
.btn-c-save:hover { background: #1565c0; }
.btn-c-cancel { padding: 6px 16px; background: white; color: #555; border: 1px solid #ddd; border-radius: 4px; cursor: pointer; font-size: 13px; margin-left: 6px; }
.btn-c-cancel:hover { background: #f5f5f5; }

/* 댓글 작성 */
.comment-write { border-top: 2px solid #eee; padding-top: 20px; margin-top: 8px; }
.write-textarea { width: 100%; padding: 12px; border: 1px solid #ddd; border-radius: 6px; box-sizing: border-box; font-size: 14px; resize: none; line-height: 1.6; }
.write-textarea:focus { border-color: #1976d2; outline: none; }
.write-bottom { display: flex; justify-content: space-between; align-items: center; margin-top: 8px; }
.char-hint { font-size: 12px; color: #aaa; }
.btn-c-submit { padding: 8px 24px; background: #1976d2; color: white; border: none; border-radius: 6px; cursor: pointer; font-size: 14px; }
.btn-c-submit:hover { background: #1565c0; }
</style>
