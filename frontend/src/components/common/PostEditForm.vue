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
// TiptapEditor.vue: 리치 텍스트 에디터 컴포넌트 (본문 수정에 사용)
import TiptapEditor from './TiptapEditor.vue'
// FileList.vue: 기존 첨부파일 목록 표시 및 개별 삭제 컴포넌트
import FileList from './FileList.vue'
// FileUploadInput.vue: 새 파일 추가 입력 컴포넌트
import FileUploadInput from './FileUploadInput.vue'

// 글 수정 공통 폼 컴포넌트 - BoardEdit.vue, NoticeEdit.vue, ArchiveEdit.vue 등에서 사용
// router/index.js의 /*/edit/:id 경로들에 대응
export default {
  name: 'PostEditForm',
  // TiptapEditor: 본문 편집기, FileList: 기존 파일 목록, FileUploadInput: 새 파일 추가
  components: { TiptapEditor, FileList, FileUploadInput },
  props: {
    // 수정 전 원본 제목 (서버에서 로드한 데이터를 부모가 전달)
    initialTitle: { type: String, default: '' },
    // 수정 전 원본 본문 HTML (서버에서 로드한 데이터를 부모가 전달)
    initialContent: { type: String, default: '' },
    // 서버에 이미 저장된 기존 첨부파일 목록 (FileList.vue에 전달)
    existingFiles: { type: Array, default: () => [] },
    // 서버 저장 중 여부 (true이면 저장 버튼 비활성화)
    submitting: { type: Boolean, default: false },
    // 부모에서 전달받는 외부 에러 메시지
    errorMsg: { type: String, default: '' },
    // TiptapEditor에 전달할 이미지 업로드 API URL
    imageUploadUrl: { type: String, default: '/api/archive/image' },
    // 저장 버튼의 배경색 (게시판 종류별로 색상 다르게 설정 가능)
    submitBtnColor: { type: String, default: '#1976d2' },
    // 파일 관련 UI 표시 여부 (FAQ 등 파일 불필요한 게시판에서 false)
    showFiles: { type: Boolean, default: true },
    // 제목 입력란의 레이블 텍스트
    titleLabel: { type: String, default: '제목' },
    // 본문 입력란의 레이블 텍스트
    contentLabel: { type: String, default: '내용' }
  },
  // submit: 저장 완료 시 { title, content, pendingFiles } emit / cancel: 취소 시 emit
  emits: ['submit', 'cancel'],
  data() {
    return {
      // 편집 중인 제목 (initialTitle prop으로 초기화)
      localTitle: this.initialTitle,
      // 편집 중인 본문 HTML (initialContent prop으로 초기화, TiptapEditor와 v-model 바인딩)
      localContent: this.initialContent,
      // 기존 첨부파일 목록 로컬 복사본 (개별 삭제 시 이 배열에서 제거)
      localExistingFiles: [...this.existingFiles],
      // 새로 추가할 파일 대기 목록 (FileUploadInput과 v-model 바인딩)
      localFiles: [],
      // 폼 유효성 검사 실패 시 표시할 로컬 에러 메시지
      localError: ''
    }
  },
  computed: {
    // 로컬 에러 또는 부모에서 전달받은 에러 중 하나를 표시 (로컬 에러 우선)
    displayError() {
      return this.localError || this.errorMsg
    }
  },
  watch: {
    // 부모에서 initialTitle이 변경될 때 (비동기 API 응답 후 데이터 수신 시) 로컬 제목 동기화
    initialTitle(val) { this.localTitle = val },
    // 부모에서 initialContent가 변경될 때 로컬 본문 동기화
    initialContent(val) { this.localContent = val },
    // 부모에서 existingFiles가 변경될 때 로컬 파일 목록 동기화 (원본 배열 복사)
    existingFiles(val) { this.localExistingFiles = [...val] }
  },
  methods: {
    // 저장 버튼 클릭 시 유효성 검사 후 부모에게 submit 이벤트 emit
    handleSubmit() {
      // 이전 에러 초기화
      this.localError = ''
      // 제목 미입력 시 에러 표시 후 중단
      if (!this.localTitle.trim()) { this.localError = `${this.titleLabel}을 입력하세요.`; return }
      // 본문 미입력 또는 빈 단락만 있는 경우 에러 표시 후 중단
      if (!this.localContent || this.localContent === '<p></p>') { this.localError = `${this.contentLabel}을 입력하세요.`; return }
      // 유효성 검사 통과 시 부모에게 제목, 본문, 새 첨부파일 목록 전달
      this.$emit('submit', {
        title: this.localTitle.trim(),
        content: this.localContent,
        pendingFiles: this.localFiles
      })
    },
    // FileList.vue에서 file-deleted 이벤트 수신 시 해당 파일을 로컬 목록에서 제거
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
