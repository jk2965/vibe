<template>
  <div class="detail-card">
    <div class="post-header">
      <h2 class="post-title">{{ post.title }}</h2>
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
import FileList from './FileList.vue'
import hljs from 'highlight.js'

export default {
  name: 'PostDetailCard',
  components: { FileList },
  props: {
    post: { type: Object, required: true },
    canEdit: { type: Boolean, default: false },
    canDelete: { type: Boolean, default: false },
    editRoute: { type: String, required: true },
    backRoute: { type: String, required: true }
  },
  emits: ['delete', 'file-deleted'],
  mounted() { this.$nextTick(() => hljs.highlightAll()) },
  updated() { this.$nextTick(() => hljs.highlightAll()) }
}
</script>

<style scoped>
.detail-card { background: white; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.08); padding: 32px; margin-bottom: 16px; }
.post-header { border-bottom: 2px solid #eee; padding-bottom: 16px; margin-bottom: 24px; }
.post-title { margin: 0 0 12px 0; font-size: 22px; line-height: 1.4; }
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
</style>
