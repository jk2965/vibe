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
    <!-- 태그 입력: 엔터/쉼표로 태그 추가, 최대 10개 -->
    <div class="form-group">
      <label>태그 <span class="tag-hint">아래 태그는 최대 10개까지 지정할 수 있습니다.</span></label>
      <div class="tag-input-wrap">
        <span v-for="tag in localTags" :key="tag" class="tag-chip">
          {{ tag }}<button type="button" @click="removeTag(tag)" class="tag-remove">×</button>
        </span>
        <input
          v-if="localTags.length < 10"
          v-model="tagInput"
          type="text"
          class="tag-text-input"
          placeholder="태그 입력 후 Enter 또는 쉼표"
          @keydown.enter.prevent="addTag"
          @keydown="onTagKeydown"
        />
      </div>
    </div>
    <!-- 필독 체크박스: showRequired=true이고 권한자(관리자/팀장)일 때만 표시 -->
    <div v-if="showRequired" class="form-group required-check-wrap">
      <label class="required-label">
        <input type="checkbox" v-model="localIsRequired" />
        <span>📌 필독 게시글로 등록</span>
      </label>
    </div>
    <div v-if="showFiles" class="form-group">
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
// TiptapEditor.vue: 리치 텍스트 에디터 컴포넌트 (본문 입력에 사용)
import TiptapEditor from './TiptapEditor.vue'
// FileUploadInput.vue: 파일 선택 및 대기 목록 관리 컴포넌트
import FileUploadInput from './FileUploadInput.vue'

// 새 글 작성 공통 폼 컴포넌트 - BoardWrite.vue, NoticeWrite.vue, ArchiveWrite.vue 등에서 사용
// router/index.js의 /*/write 경로들에 대응
export default {
  name: 'PostWriteForm',
  // TiptapEditor: 본문 편집기, FileUploadInput: 파일 첨부 입력
  components: { TiptapEditor, FileUploadInput },
  props: {
    // 서버 전송 중 여부 (true이면 등록 버튼 비활성화)
    submitting: { type: Boolean, default: false },
    // 부모에서 전달받는 외부 에러 메시지
    errorMsg: { type: String, default: '' },
    // TiptapEditor에 전달할 이미지 업로드 API URL
    imageUploadUrl: { type: String, default: '/api/archive/image' },
    // 등록 버튼의 배경색 (게시판 종류별로 색상 다르게 설정 가능)
    submitBtnColor: { type: String, default: '#1976d2' },
    // 파일 첨부 영역 표시 여부 (FAQ 등 파일 불필요한 게시판에서 false)
    showFiles: { type: Boolean, default: true },
    // 필독 체크박스 표시 여부 (관리자/팀장만 true로 전달)
    showRequired: { type: Boolean, default: false },
    // 제목 입력란의 레이블 텍스트
    titleLabel: { type: String, default: '제목' },
    // 본문 입력란의 레이블 텍스트
    contentLabel: { type: String, default: '내용' }
  },
  // submit: 작성 완료 시 { title, content, pendingFiles } emit / cancel: 취소 시 emit
  emits: ['submit', 'cancel'],
  data() {
    return {
      // 제목 입력값 (로컬 상태)
      localTitle: '',
      // 본문 HTML 문자열 (TiptapEditor와 v-model 바인딩)
      localContent: '',
      // 첨부 대기 중인 File 객체 배열 (FileUploadInput과 v-model 바인딩)
      localFiles: [],
      // 필독 여부 체크박스 상태
      localIsRequired: false,
      // 태그 목록 (최대 10개)
      localTags: [],
      // 태그 입력 중인 텍스트
      tagInput: '',
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
  methods: {
    addTag() {
      const tag = this.tagInput.trim().replace(/,$/, '')
      if (tag && !this.localTags.includes(tag) && this.localTags.length < 10) {
        this.localTags.push(tag)
      }
      this.tagInput = ''
    },
    onTagKeydown(e) {
      if (e.key === ',') { e.preventDefault(); this.addTag() }
    },
    removeTag(tag) {
      this.localTags = this.localTags.filter(t => t !== tag)
    },
    // 등록 버튼 클릭 시 유효성 검사 후 부모에게 submit 이벤트 emit
    handleSubmit() {
      // 이전 에러 초기화
      this.localError = ''
      // 제목 미입력 시 에러 표시 후 중단
      if (!this.localTitle.trim()) { this.localError = `${this.titleLabel}을 입력하세요.`; return }
      // 본문 미입력 또는 빈 단락만 있는 경우 에러 표시 후 중단
      if (!this.localContent || this.localContent === '<p></p>') { this.localError = `${this.contentLabel}을 입력하세요.`; return }
      // 유효성 검사 통과 시 부모에게 제목, 본문, 첨부파일 목록, 필독 여부 전달
      this.$emit('submit', {
        title: this.localTitle.trim(),
        content: this.localContent,
        pendingFiles: this.localFiles,
        isRequired: this.localIsRequired ? 1 : 0,
        tags: this.localTags.join(',')
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
.required-check-wrap { margin-bottom: 16px; }
.required-label { display: flex; align-items: center; gap: 8px; cursor: pointer; font-size: 14px; font-weight: bold; color: #c62828; }
.required-label input[type="checkbox"] { width: 16px; height: 16px; accent-color: #c62828; cursor: pointer; }
.tag-hint { font-size: 12px; font-weight: normal; color: #888; margin-left: 8px; }
.tag-input-wrap { display: flex; flex-wrap: wrap; gap: 6px; padding: 8px 10px; border: 1px solid #ddd; border-radius: 6px; min-height: 42px; align-items: center; }
.tag-chip { display: inline-flex; align-items: center; background: #e3f2fd; color: #1565c0; border-radius: 16px; padding: 3px 10px; font-size: 13px; gap: 4px; }
.tag-remove { background: none; border: none; color: #1565c0; cursor: pointer; font-size: 15px; padding: 0; line-height: 1; }
.tag-text-input { border: none; outline: none; font-size: 14px; min-width: 160px; flex: 1; background: transparent; }
</style>
