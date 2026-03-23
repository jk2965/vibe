<template>
  <div class="detail-card">
    <div class="post-header">
      <div class="post-title-row">
        <h2 class="post-title">{{ post.title }}</h2>
        <!-- 필독 설정/해제 버튼 (권한자만 표시) -->
        <button v-if="canSetRequired" @click="$emit('toggleRequired')" class="btn-required" :class="{ active: isRequired }">
          {{ isRequired ? '📌 필독 해제' : '📌 필독 설정' }}
        </button>
      </div>
      <div class="post-meta">
        <slot name="extra-meta"></slot>
        <span>{{ post.authorName }}</span>
        <span class="sep">|</span>
        <span>{{ post.createdAt }}</span>
        <span class="sep">|</span>
        <span>조회 {{ post.views }}</span>
      </div>
    </div>
    <div class="post-content tiptap-display" v-html="post.content"></div>
    <FileList
      :files="post.files || []"
      :canDelete="canDelete"
      @file-deleted="$emit('file-deleted', $event)"
    />
    <!-- 태그 표시 -->
    <div v-if="postTags.length > 0" class="tag-list">
      <span v-for="tag in postTags" :key="tag" class="tag-chip">{{ tag }}</span>
    </div>
    <div class="btn-row">
      <button @click="$router.push(backRoute)" class="btn-back">목록으로</button>
      <div class="btn-right">
        <button v-if="canEdit" @click="$router.push(editRoute)" class="btn-edit">수정</button>
        <button v-if="canDelete" @click="$emit('delete')" class="btn-delete">삭제</button>
      </div>
    </div>
  </div>
</template>

<script>
// FileList.vue: 첨부파일 목록 표시 및 삭제 컴포넌트
import FileList from './FileList.vue'
// highlight.js: 본문 내 코드 블록 구문 강조 처리 (main.js에서 github.css 로드됨)
import hljs from 'highlight.js'

// 게시글 상세 공통 카드 컴포넌트 - router/index.js의 각 /:id 상세 페이지에서 사용
// BoardDetail.vue, NoticeDetail.vue, ArchiveDetail.vue, TeamNoticeDetail.vue 등에서 사용
export default {
  name: 'PostDetailCard',
  // FileList: 첨부파일 다운로드/삭제 UI 제공
  components: { FileList },
  props: {
    // 표시할 게시글 데이터 객체 (title, content, authorName, createdAt, views, files 포함)
    post: { type: Object, required: true },
    // 수정 버튼 표시 여부 (작성자 본인 또는 관리자일 때 true)
    canEdit: { type: Boolean, default: false },
    // 삭제 버튼 표시 여부 및 FileList의 파일 삭제 버튼 활성화 여부
    canDelete: { type: Boolean, default: false },
    // 수정 버튼 클릭 시 이동할 라우트 경로 (예: /board/edit/1)
    editRoute: { type: String, required: true },
    // 목록으로 버튼 클릭 시 이동할 라우트 경로 (예: /board)
    backRoute: { type: String, required: true },
    // 현재 게시글의 필독 지정 여부
    isRequired: { type: Boolean, default: false },
    // 필독 설정/해제 버튼 표시 여부 (관리자 또는 팀장)
    canSetRequired: { type: Boolean, default: false }
  },
  computed: {
    postTags() {
      if (!this.post.tags) return []
      return this.post.tags.split(',').filter(t => t.trim())
    }
  },
  // delete: 삭제 버튼 클릭 시 부모에게 emit / file-deleted: 첨부파일 삭제 시 부모에게 emit / toggleRequired: 필독 설정/해제 emit
  emits: ['delete', 'file-deleted', 'toggleRequired'],
  // 컴포넌트 마운트 후 DOM 렌더링 완료 시점에 코드 블록 구문 강조 적용
  mounted() { this.$nextTick(() => hljs.highlightAll()) },
  // 게시글 내용이 동적으로 변경될 때마다 구문 강조 재적용 (비동기 데이터 로드 후 대응)
  updated() { this.$nextTick(() => hljs.highlightAll()) }
}
</script>

<style scoped>
.detail-card { background: white; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.08); padding: 32px; margin-bottom: 16px; }
.post-header { border-bottom: 2px solid #eee; padding-bottom: 16px; margin-bottom: 24px; }
.post-title-row { display: flex; align-items: flex-start; justify-content: space-between; gap: 12px; margin-bottom: 12px; }
.post-title { margin: 0; font-size: 22px; line-height: 1.4; flex: 1; }
.btn-required { flex-shrink: 0; padding: 6px 14px; background: #fff3e0; color: #e65100; border: 1px solid #ffcc80; border-radius: 6px; font-size: 13px; cursor: pointer; white-space: nowrap; transition: background 0.15s; }
.btn-required:hover { background: #ffe0b2; }
.btn-required.active { background: #fce4ec; color: #c62828; border-color: #ef9a9a; }
.btn-required.active:hover { background: #ffcdd2; }
.post-meta { font-size: 13px; color: #888; display: flex; align-items: center; flex-wrap: wrap; gap: 2px; }
.sep { margin: 0 6px; }
.post-content { min-height: 200px; font-size: 15px; line-height: 1.8; word-break: break-word; margin-bottom: 28px; }
.btn-row { display: flex; justify-content: space-between; align-items: center; }
.btn-right { display: flex; gap: 8px; }
.btn-back { padding: 10px 24px; background: white; color: #555; border: 1px solid #ddd; border-radius: 6px; font-size: 14px; cursor: pointer; }
.btn-back:hover { background: #f5f5f5; }
.btn-edit { padding: 10px 24px; background: #f57c00; color: white; border: none; border-radius: 6px; font-size: 14px; cursor: pointer; }
.btn-edit:hover { background: #e65100; }
.btn-delete { padding: 10px 24px; background: #e53935; color: white; border: none; border-radius: 6px; font-size: 14px; cursor: pointer; }
.btn-delete:hover { background: #c62828; }
.tag-list { display: flex; flex-wrap: wrap; gap: 6px; margin-top: 16px; margin-bottom: 16px; }
.tag-chip { background: #e3f2fd; color: #1565c0; border-radius: 16px; padding: 3px 12px; font-size: 13px; }
</style>

<style>
.tiptap-display h1 { font-size: 24px; margin: 16px 0 8px; }
.tiptap-display h2 { font-size: 20px; margin: 14px 0 6px; }
.tiptap-display h3 { font-size: 17px; margin: 12px 0 4px; }
.tiptap-display p { margin: 0 0 8px 0; }
.tiptap-display ul, .tiptap-display ol { padding-left: 24px; margin: 8px 0; }
.tiptap-display blockquote { border-left: 3px solid #ddd; margin: 8px 0; padding: 4px 12px; color: #666; }
.tiptap-display pre { background: #f4f4f4; border-radius: 4px; padding: 12px; overflow-x: auto; margin: 8px 0; }
.tiptap-display pre code.hljs { background: #f4f4f4; border-radius: 4px; font-size: 13px; padding: 0; }
.tiptap-display img { max-width: 100%; height: auto; border-radius: 4px; }
.tiptap-display iframe { max-width: 100%; border-radius: 6px; display: block; margin: 8px 0; }
/* 표(테이블) 스타일 */
.tiptap-display table { border-collapse: collapse; width: 100%; margin: 12px 0; }
.tiptap-display th, .tiptap-display td { border: 1px solid #ccc; padding: 8px 10px; font-size: 14px; }
.tiptap-display th { background: #f5f5f5; font-weight: bold; }
</style>
