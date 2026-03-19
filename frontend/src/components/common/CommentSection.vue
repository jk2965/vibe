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
// API 호출을 위한 axios 임포트
import axios from 'axios'

// 댓글 + 답글(대댓글) 섹션 컴포넌트 - BoardDetail.vue에서 사용
// router/index.js의 /board/:id 상세 페이지 하단에 렌더링됨
export default {
  name: 'CommentSection',
  props: {
    // 댓글이 속한 게시글의 ID (API 요청 파라미터로 사용)
    boardId: { type: String, required: true }
  },
  data() {
    return {
      // 서버에서 불러온 전체 댓글 목록 (삭제된 댓글 포함)
      comments: [],
      // 새 최상위 댓글 입력값
      newComment: '',
      // 답글 입력값
      replyContent: '',
      // 현재 답글 작성 폼이 열려있는 댓글의 ID (null이면 닫힘)
      replyingToId: null,
      // 현재 수정 모드인 댓글의 ID (null이면 수정 모드 없음)
      editingId: null,
      // 수정 중인 댓글 내용 입력값
      editContent: '',
      // 현재 로그인한 사용자 ID (localStorage에서 조회, 본인 댓글 수정/삭제 권한 판단에 사용)
      userId: localStorage.getItem('userId') || '',
      // 현재 로그인한 사용자 이름 (댓글 등록 시 authorName으로 전송)
      username: localStorage.getItem('username') || ''
    }
  },
  computed: {
    // 삭제되지 않은 댓글 수 (isDeleted !== 1) - 헤더의 댓글 카운트 표시용
    activeCount() {
      return this.comments.filter(c => c.isDeleted !== 1).length
    },
    // 서버의 평탄한 댓글 목록을 트리 구조로 변환 후 depth 정보를 포함한 flat 배열로 반환
    // depth 값에 따라 템플릿에서 들여쓰기 CSS padding 적용
    flatComments() {
      // 댓글 ID를 키로 하는 맵 생성 (트리 구조 구성에 사용)
      const map = {}
      this.comments.forEach(c => { map[c.id] = { ...c, children: [] } })
      const roots = []
      this.comments.forEach(c => {
        // parentId가 있고 부모 댓글이 존재하면 부모의 children에 추가
        if (c.parentId && map[c.parentId]) {
          map[c.parentId].children.push(map[c.id])
        } else {
          // parentId 없으면 최상위 루트 댓글
          roots.push(map[c.id])
        }
      })
      // 트리를 depth 포함 flat 배열로 변환
      const result = []
      // 재귀 함수: 각 노드를 depth와 함께 result 배열에 추가
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
    // 컴포넌트 마운트 시 해당 게시글의 댓글 목록을 서버에서 불러옴
    this.fetchComments()
  },
  methods: {
    // GET /api/comment?boardId=... - boardId에 해당하는 전체 댓글 목록 조회
    async fetchComments() {
      try {
        const res = await axios.get('http://localhost:8090/api/comment', {
          params: { boardId: this.boardId }
        })
        // 서버 응답 데이터를 comments 배열에 저장
        this.comments = res.data
      } catch (e) {
        console.error('댓글 조회 실패:', e)
      }
    },
    // POST /api/comment - 새 최상위 댓글 등록 (빈 내용이면 무시)
    async submitComment() {
      if (!this.newComment.trim()) return
      try {
        await axios.post('http://localhost:8090/api/comment', {
          boardId: this.boardId,
          authorId: this.userId,
          authorName: this.username,
          content: this.newComment.trim()
        })
        // 등록 후 입력창 초기화 및 댓글 목록 새로고침
        this.newComment = ''
        await this.fetchComments()
      } catch (e) {
        alert(e.response?.data?.message || '댓글 등록에 실패했습니다.')
      }
    },
    // 답글 폼 토글: 같은 댓글 재클릭 시 닫기, 다른 댓글 클릭 시 열기
    toggleReply(commentId, authorName) {
      if (this.replyingToId === commentId) {
        // 이미 열려있는 답글 폼을 닫고 입력값 초기화
        this.replyingToId = null
        this.replyContent = ''
      } else {
        // 해당 댓글의 답글 폼 열기 (authorName이 있으면 @멘션 자동 입력)
        this.replyingToId = commentId
        this.replyContent = authorName ? `@${authorName} ` : ''
      }
    },
    // 답글 작성 취소 시 폼 닫기 및 입력값 초기화
    cancelReply() {
      this.replyingToId = null
      this.replyContent = ''
    },
    // POST /api/comment - 답글 등록 (parentId 포함하여 부모 댓글 지정)
    async submitReply(parentId) {
      if (!this.replyContent.trim()) return
      try {
        await axios.post('http://localhost:8090/api/comment', {
          boardId: this.boardId,
          authorId: this.userId,
          authorName: this.username,
          content: this.replyContent.trim(),
          // parentId를 통해 계층 구조 형성
          parentId: parentId
        })
        // 등록 후 답글 폼 닫기 및 목록 새로고침
        this.replyingToId = null
        this.replyContent = ''
        await this.fetchComments()
      } catch (e) {
        alert(e.response?.data?.message || '답글 등록에 실패했습니다.')
      }
    },
    // 수정 버튼 클릭 시 해당 댓글의 수정 모드 활성화 및 기존 내용 로드
    startEdit(comment) {
      this.editingId = comment.id
      this.editContent = comment.content
    },
    // 수정 취소 시 수정 모드 비활성화 및 입력값 초기화
    cancelEdit() {
      this.editingId = null
      this.editContent = ''
    },
    // PUT /api/comment/:id - 댓글 내용 수정 (requesterId로 본인 여부 서버 검증)
    async submitEdit(id) {
      if (!this.editContent.trim()) return
      try {
        await axios.put(`http://localhost:8090/api/comment/${id}`, {
          content: this.editContent.trim()
        }, {
          // requesterId 쿼리 파라미터로 본인 확인
          params: { requesterId: this.userId }
        })
        // 수정 후 수정 모드 종료 및 목록 새로고침
        this.editingId = null
        this.editContent = ''
        await this.fetchComments()
      } catch (e) {
        alert(e.response?.data?.message || '수정에 실패했습니다.')
      }
    },
    // DELETE /api/comment/:id - 댓글 삭제 (소프트 삭제: isDeleted = 1로 설정됨)
    async deleteComment(id) {
      if (!confirm('삭제하시겠습니까?')) return
      try {
        await axios.delete(`http://localhost:8090/api/comment/${id}`, {
          // requesterId 쿼리 파라미터로 본인 확인
          params: { requesterId: this.userId }
        })
        // 삭제 후 댓글 목록 새로고침
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
