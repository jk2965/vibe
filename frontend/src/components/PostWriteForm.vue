<template>
  <div class="write-card">
    <div class="form-group">
      <label>제목</label>
      <input v-model="localTitle" type="text" placeholder="제목을 입력하세요" maxlength="200">
    </div>
    <div class="form-group">
      <label>내용</label>
      <TiptapEditor v-model="localContent" :imageUploadUrl="imageUploadUrl" />
    </div>
    <slot name="extra-fields"></slot>
    <div class="form-group">
      <label>파일 첨부</label>
      <FileUploadInput v-model="localFiles" />
    </div>
    <p v-if="displayError" class="error-msg">{{ displayError }}</p>
    <div class="btn-row">
      <button @click="handleSubmit" class="btn-submit" :style="{ background: submitBtnColor }" :disabled="submitting">
        {{ submitting ? '등록 중...' : '등록' }}
      </button>
      <button @click="$emit('cancel')" class="btn-cancel">취소</button>
    </div>
  </div>
</template>

<script>
import TiptapEditor from './TiptapEditor.vue'
import FileUploadInput from './FileUploadInput.vue'

export default {
  name: 'PostWriteForm',
  components: { TiptapEditor, FileUploadInput },
  props: {
    submitting: { type: Boolean, default: false },
    errorMsg: { type: String, default: '' },
    imageUploadUrl: { type: String, default: '/api/archive/image' },
    submitBtnColor: { type: String, default: '#1976d2' }
  },
  emits: ['submit', 'cancel'],
  data() {
    return {
      localTitle: '',
      localContent: '',
      localFiles: [],
      localError: ''
    }
  },
  computed: {
    displayError() {
      return this.localError || this.errorMsg
    }
  },
  methods: {
    handleSubmit() {
      this.localError = ''
      if (!this.localTitle.trim()) { this.localError = '제목을 입력하세요.'; return }
      if (!this.localContent || this.localContent === '<p></p>') { this.localError = '내용을 입력하세요.'; return }
      this.$emit('submit', {
        title: this.localTitle.trim(),
        content: this.localContent,
        pendingFiles: this.localFiles
      })
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
