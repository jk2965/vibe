<template>
  <div class="write-card">
    <div class="form-group">
      <label>{{ titleLabel }}</label>
      <input v-model="localTitle" type="text" :placeholder="`${titleLabel}을 입력하세요`" maxlength="500">
    </div>
    <div class="form-group">
      <label>{{ contentLabel }}</label>
      <TiptapEditor v-model="localContent" :imageUploadUrl="imageUploadUrl" />
    </div>
    <slot name="extra-fields"></slot>
    <div v-if="showFiles && localExistingFiles.length > 0" class="form-group">
      <label>기존 첨부파일</label>
      <FileList :files="localExistingFiles" :canDelete="true" @file-deleted="handleFileDeleted" />
    </div>
    <div v-if="showFiles" class="form-group">
      <label>파일 추가</label>
      <FileUploadInput v-model="localFiles" />
    </div>
    <p v-if="displayError" class="error-msg">{{ displayError }}</p>
    <div class="btn-row">
      <button @click="handleSubmit" class="btn-submit" :style="{ background: submitBtnColor }" :disabled="submitting">
        {{ submitting ? '저장 중...' : '저장' }}
      </button>
      <button @click="$emit('cancel')" class="btn-cancel">취소</button>
    </div>
  </div>
</template>

<script>
import TiptapEditor from './TiptapEditor.vue'
import FileList from './FileList.vue'
import FileUploadInput from './FileUploadInput.vue'

export default {
  name: 'PostEditForm',
  components: { TiptapEditor, FileList, FileUploadInput },
  props: {
    initialTitle: { type: String, default: '' },
    initialContent: { type: String, default: '' },
    existingFiles: { type: Array, default: () => [] },
    submitting: { type: Boolean, default: false },
    errorMsg: { type: String, default: '' },
    imageUploadUrl: { type: String, default: '/api/archive/image' },
    submitBtnColor: { type: String, default: '#1976d2' },
    showFiles: { type: Boolean, default: true },
    titleLabel: { type: String, default: '제목' },
    contentLabel: { type: String, default: '내용' }
  },
  emits: ['submit', 'cancel'],
  data() {
    return {
      localTitle: this.initialTitle,
      localContent: this.initialContent,
      localExistingFiles: [...this.existingFiles],
      localFiles: [],
      localError: ''
    }
  },
  computed: {
    displayError() {
      return this.localError || this.errorMsg
    }
  },
  watch: {
    initialTitle(val) { this.localTitle = val },
    initialContent(val) { this.localContent = val },
    existingFiles(val) { this.localExistingFiles = [...val] }
  },
  methods: {
    handleSubmit() {
      this.localError = ''
      if (!this.localTitle.trim()) { this.localError = `${this.titleLabel}을 입력하세요.`; return }
      if (!this.localContent || this.localContent === '<p></p>') { this.localError = `${this.contentLabel}을 입력하세요.`; return }
      this.$emit('submit', {
        title: this.localTitle.trim(),
        content: this.localContent,
        pendingFiles: this.localFiles
      })
    },
    handleFileDeleted(id) {
      this.localExistingFiles = this.localExistingFiles.filter(f => f.id !== id)
    }
  }
}
</script>

<style scoped>
.write-card { background: white; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.08); padding: 28px; }
.form-group { margin-bottom: 20px; }
.form-group label { display: block; margin-bottom: 8px; font-weight: bold; font-size: 14px; }
.form-group input { width: 100%; padding: 10px 12px; border: 1px solid #ddd; border-radius: 6px; box-sizing: border-box; font-size: 15px; }
.btn-row { display: flex; gap: 10px; margin-top: 8px; }
.btn-submit { padding: 11px 36px; color: white; border: none; border-radius: 6px; font-size: 15px; cursor: pointer; }
.btn-submit:hover:not(:disabled) { filter: brightness(0.9); }
.btn-submit:disabled { opacity: 0.6; cursor: not-allowed; }
.btn-cancel { padding: 11px 24px; background: white; color: #555; border: 1px solid #ddd; border-radius: 6px; font-size: 15px; cursor: pointer; }
.btn-cancel:hover { background: #f5f5f5; }
.error-msg { color: red; font-size: 13px; margin-bottom: 12px; }
</style>
