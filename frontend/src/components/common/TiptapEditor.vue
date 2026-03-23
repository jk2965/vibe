<template>
  <div class="tiptap-wrapper">
    <div v-if="editor" class="toolbar">
      <button type="button" @click="editor.chain().focus().toggleBold().run()" :class="{ active: editor.isActive('bold') }" title="굵게"><strong>B</strong></button>
      <button type="button" @click="editor.chain().focus().toggleItalic().run()" :class="{ active: editor.isActive('italic') }" title="기울임"><em>I</em></button>
      <button type="button" @click="editor.chain().focus().toggleUnderline().run()" :class="{ active: editor.isActive('underline') }" title="밑줄"><u>U</u></button>
      <button type="button" @click="editor.chain().focus().toggleStrike().run()" :class="{ active: editor.isActive('strike') }" title="취소선"><s>S</s></button>
      <span class="divider"></span>
      <button type="button" @click="editor.chain().focus().toggleHeading({ level: 1 }).run()" :class="{ active: editor.isActive('heading', { level: 1 }) }">H1</button>
      <button type="button" @click="editor.chain().focus().toggleHeading({ level: 2 }).run()" :class="{ active: editor.isActive('heading', { level: 2 }) }">H2</button>
      <button type="button" @click="editor.chain().focus().toggleHeading({ level: 3 }).run()" :class="{ active: editor.isActive('heading', { level: 3 }) }">H3</button>
      <span class="divider"></span>
      <button type="button" @click="editor.chain().focus().toggleBulletList().run()" :class="{ active: editor.isActive('bulletList') }" title="글머리 기호">• 목록</button>
      <button type="button" @click="editor.chain().focus().toggleOrderedList().run()" :class="{ active: editor.isActive('orderedList') }" title="번호 목록">1. 목록</button>
      <button type="button" @click="editor.chain().focus().toggleBlockquote().run()" :class="{ active: editor.isActive('blockquote') }" title="인용">인용</button>
      <select @change="setCodeBlock($event)" title="코드 블록" :class="{ active: editor.isActive('codeBlock') }" class="lang-select">
        <option value="">코드</option>
        <option value="javascript">JavaScript</option>
        <option value="java">Java</option>
        <option value="html">HTML</option>
        <option value="css">CSS</option>
        <option value="python">Python</option>
        <option value="sql">SQL</option>
        <option value="bash">Bash</option>
      </select>
      <span class="divider"></span>
      <button type="button" @click="setLink" :class="{ active: editor.isActive('link') }" title="링크">링크</button>
      <button type="button" @click="editor.chain().focus().unsetLink().run()" :disabled="!editor.isActive('link')" title="링크 제거">링크 해제</button>
      <span class="divider"></span>
      <button type="button" @click="triggerImage" title="이미지 삽입">이미지</button>
      <input ref="imageInput" type="file" accept="image/*" style="display:none" @change="handleImageUpload">
      <button type="button" @click="insertYoutube" title="YouTube 영상 삽입">YouTube</button>
      <button type="button" @click="insertIframe" title="외부 영상/URL 임베드">외부영상</button>
      <span class="divider"></span>
      <!-- 표 삽입 드롭다운 -->
      <div class="dropdown-wrap" ref="tableDropdown">
        <button type="button" @click="toggleTableMenu" :class="{ active: editor.isActive('table') }" title="표 삽입/편집">표</button>
        <div v-if="showTableMenu" class="dropdown-menu">
          <button type="button" @click="insertTable">표 삽입 (3×3)</button>
          <hr>
          <button type="button" @click="editor.chain().focus().addColumnBefore().run()">열 앞에 추가</button>
          <button type="button" @click="editor.chain().focus().addColumnAfter().run()">열 뒤에 추가</button>
          <button type="button" @click="editor.chain().focus().deleteColumn().run()">현재 열 삭제</button>
          <hr>
          <button type="button" @click="editor.chain().focus().addRowBefore().run()">행 위에 추가</button>
          <button type="button" @click="editor.chain().focus().addRowAfter().run()">행 아래에 추가</button>
          <button type="button" @click="editor.chain().focus().deleteRow().run()">현재 행 삭제</button>
          <hr>
          <button type="button" @click="editor.chain().focus().deleteTable().run()">표 삭제</button>
        </div>
      </div>
      <!-- 차트 삽입 버튼 -->
      <button type="button" @click="showChartModal = true" title="차트/그래프 삽입">차트</button>
      <span class="divider"></span>
      <button type="button" @click="editor.chain().focus().unsetAllMarks().clearNodes().run()" title="서식 초기화">초기화</button>
    </div>
    <editor-content :editor="editor" class="tiptap-content" />

    <!-- 차트 삽입 모달 -->
    <ChartInsertModal
      v-if="showChartModal"
      @close="showChartModal = false"
      @insert="onChartInsert"
    />
  </div>
</template>

<script>
// Tiptap Vue 3용 에디터 코어 및 렌더링 컴포넌트 임포트
import { Editor, EditorContent } from '@tiptap/vue-3'
// Tiptap 커스텀 노드 생성을 위한 Node 클래스 임포트
import { Node } from '@tiptap/core'
// 기본 에디터 기능 묶음 (굵기, 기울임, 목록, 인용, 코드 등) 임포트
import StarterKit from '@tiptap/starter-kit'
// 이미지 삽입 확장 임포트
import Image from '@tiptap/extension-image'
// 밑줄 확장 임포트
import Underline from '@tiptap/extension-underline'
// 하이퍼링크 확장 임포트
import Link from '@tiptap/extension-link'
// 구문 강조 지원 코드 블록 확장 임포트 (lowlight 연동, main.js의 highlight.js CSS와 연동)
import CodeBlockLowlight from '@tiptap/extension-code-block-lowlight'
// YouTube 영상 임베드 확장 임포트
import Youtube from '@tiptap/extension-youtube'
// 표(테이블) 관련 Tiptap 확장 임포트 (v3.x는 named export 사용)
import { Table } from '@tiptap/extension-table'
import { TableRow } from '@tiptap/extension-table-row'
import { TableHeader } from '@tiptap/extension-table-header'
import { TableCell } from '@tiptap/extension-table-cell'
// 차트 삽입 모달 컴포넌트 임포트
import ChartInsertModal from './ChartInsertModal.vue'

// 외부 iframe 임베드를 위한 커스텀 Tiptap 노드 정의 (네이버TV, 카카오TV, Vimeo 등)
const IframeExtension = Node.create({
  name: 'iframe',
  group: 'block',
  atom: true,
  addAttributes() {
    return {
      src: { default: null },
      width: { default: '100%' },
      height: { default: '360' },
    }
  },
  parseHTML() { return [{ tag: 'iframe[src]' }] },
  renderHTML({ HTMLAttributes }) {
    return ['iframe', { ...HTMLAttributes, frameborder: '0', allowfullscreen: 'true', style: 'max-width:100%;border-radius:6px;display:block;margin:8px 0;' }]
  },
  addCommands() {
    return {
      setIframe: (options) => ({ commands }) =>
        commands.insertContent({ type: this.name, attrs: options })
    }
  }
})
// 코드 구문 강조를 위한 lowlight 코어 임포트
import { lowlight } from 'lowlight/lib/core'
// 지원 언어 개별 임포트 (highlight.js 기반)
import javascript from 'highlight.js/lib/languages/javascript'
import java from 'highlight.js/lib/languages/java'
import xml from 'highlight.js/lib/languages/xml'
import css from 'highlight.js/lib/languages/css'
import python from 'highlight.js/lib/languages/python'
import sql from 'highlight.js/lib/languages/sql'
import bash from 'highlight.js/lib/languages/bash'
// 이미지 업로드 API 호출을 위한 axios 임포트
import axios from 'axios'

// lowlight에 각 언어 등록 (코드 블록에서 해당 언어 구문 강조 활성화)
lowlight.registerLanguage('javascript', javascript)
lowlight.registerLanguage('java', java)
lowlight.registerLanguage('xml', xml)
// html은 xml 파서로 처리
lowlight.registerLanguage('html', xml)
lowlight.registerLanguage('css', css)
lowlight.registerLanguage('python', python)
lowlight.registerLanguage('sql', sql)
lowlight.registerLanguage('bash', bash)

// PostWriteForm.vue와 PostEditForm.vue에서 v-model로 사용되는 리치 텍스트 에디터 컴포넌트
export default {
  name: 'TiptapEditor',
  // EditorContent: Tiptap 에디터 DOM 렌더링 / ChartInsertModal: 차트 삽입 모달
  components: { EditorContent, ChartInsertModal },
  props: {
    // 부모로부터 v-model로 전달받는 HTML 문자열 (초기 에디터 내용)
    modelValue: { type: String, default: '' },
    // 이미지 업로드 API 엔드포인트 URL (부모 컴포넌트에서 주입)
    imageUploadUrl: { type: String, required: true }
  },
  // 에디터 내용 변경 시 부모에게 HTML 값을 emit
  emits: ['update:modelValue'],
  data() {
    return {
      // Tiptap Editor 인스턴스 (mounted에서 초기화)
      editor: null,
      // 표 편집 드롭다운 메뉴 표시 여부
      showTableMenu: false,
      // 차트 삽입 모달 표시 여부
      showChartModal: false,
    }
  },
  watch: {
    // 부모의 modelValue가 외부에서 변경될 때 에디터 내용을 동기화 (무한 루프 방지를 위해 값 비교 후 설정)
    modelValue(val) {
      if (this.editor && this.editor.getHTML() !== val) {
        this.editor.commands.setContent(val, false)
      }
    }
  },
  mounted() {
    // 표 드롭다운 외부 클릭 시 닫기
    this._closeTableMenu = (e) => {
      if (this.$refs.tableDropdown && !this.$refs.tableDropdown.contains(e.target)) {
        this.showTableMenu = false
      }
    }
    document.addEventListener('click', this._closeTableMenu)

    // 컴포넌트 마운트 시 Tiptap 에디터 인스턴스 생성 및 확장 등록
    this.editor = new Editor({
      // 초기 내용은 부모로부터 전달받은 HTML 문자열
      content: this.modelValue,
      extensions: [
        // StarterKit에서 기본 codeBlock은 비활성화 (CodeBlockLowlight로 대체)
        StarterKit.configure({ codeBlock: false }),
        // 이미지 삽입 지원
        Image,
        // 밑줄 지원
        Underline,
        // 링크: 클릭 시 자동 이동 비활성화, 자동 링크 감지 활성화
        Link.configure({ openOnClick: false, autolink: true }),
        // lowlight 기반 구문 강조 코드 블록
        CodeBlockLowlight.configure({ lowlight }),
        // YouTube: 개인정보 보호 모드(nocookie) 활성화
        Youtube.configure({ width: 640, height: 360, nocookie: true }),
        // 커스텀 iframe 임베드 확장
        IframeExtension,
        // 표(테이블) 확장: 헤더 셀 포함, 열/행 추가·삭제 지원
        Table.configure({ resizable: false }),
        TableRow,
        TableHeader,
        TableCell
      ],
      // 에디터 내용이 변경될 때마다 부모 컴포넌트에 HTML 값 emit
      onUpdate: () => {
        this.$emit('update:modelValue', this.editor.getHTML())
      }
    })
  },
  beforeUnmount() {
    // 표 드롭다운 외부 클릭 이벤트 리스너 제거
    document.removeEventListener('click', this._closeTableMenu)
    // 컴포넌트 언마운트 전 에디터 인스턴스 메모리 해제
    this.editor.destroy()
  },
  methods: {
    // 코드 블록 언어 선택 드롭다운 변경 시 해당 언어로 코드 블록 토글
    setCodeBlock(e) {
      const lang = e.target.value
      // 선택 후 드롭다운을 초기 상태로 리셋
      e.target.value = ''
      this.editor.chain().focus().toggleCodeBlock({ language: lang || 'plaintext' }).run()
    },
    // YouTube URL을 입력받아 에디터에 YouTube 영상 삽입
    insertYoutube() {
      const url = prompt('YouTube URL을 입력하세요:')
      if (!url) return
      this.editor.chain().focus().setYoutubeVideo({ src: url }).run()
    },
    // 외부 영상/미디어 embed URL을 입력받아 커스텀 iframe 노드로 삽입
    insertIframe() {
      const url = prompt('임베드 URL을 입력하세요:\n(예: 네이버TV, 카카오TV, Vimeo 등의 embed URL)')
      if (!url) return
      this.editor.chain().focus().setIframe({ src: url }).run()
    },
    // URL을 입력받아 선택된 텍스트에 하이퍼링크 설정 (새 탭에서 열림)
    setLink() {
      const url = prompt('URL을 입력하세요:', 'https://')
      if (!url) return
      this.editor.chain().focus().setLink({ href: url, target: '_blank' }).run()
    },
    // 숨겨진 파일 input을 클릭하여 이미지 선택 다이얼로그 열기
    triggerImage() {
      this.$refs.imageInput.click()
    },
    // 표 편집 드롭다운 열기/닫기 토글
    toggleTableMenu() {
      this.showTableMenu = !this.showTableMenu
    },
    // 3×3 기본 표를 에디터 커서 위치에 삽입
    insertTable() {
      this.editor.chain().focus().insertTable({ rows: 3, cols: 3, withHeaderRow: true }).run()
      this.showTableMenu = false
    },
    // 차트 삽입 모달에서 base64 이미지 URL을 받아 에디터에 이미지로 삽입
    onChartInsert(dataUrl) {
      this.editor.chain().focus().setImage({ src: dataUrl }).run()
      this.showChartModal = false
    },
    // 선택한 이미지 파일을 서버(imageUploadUrl)에 업로드 후 에디터에 삽입하는 비동기 메서드
    async handleImageUpload(e) {
      const file = e.target.files[0]
      if (!file) return
      const fd = new FormData()
      fd.append('file', file)
      try {
        // imageUploadUrl prop으로 전달된 API 엔드포인트에 이미지 업로드 요청
        const res = await axios.post(this.imageUploadUrl, fd)
        // 서버에서 반환된 이미지 URL을 에디터에 삽입
        this.editor.chain().focus().setImage({ src: res.data.url }).run()
      } catch {
        alert('이미지 업로드에 실패했습니다.')
      }
      // 같은 파일을 다시 업로드할 수 있도록 input 초기화
      e.target.value = ''
    }
  }
}
</script>

<style scoped>
.tiptap-wrapper {
  border: 1px solid #ddd;
  border-radius: 6px;
  overflow: hidden;
}
.toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 2px;
  padding: 8px;
  background: #f8f9fa;
  border-bottom: 1px solid #ddd;
}
.toolbar button {
  padding: 4px 10px;
  background: white;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 13px;
  cursor: pointer;
  white-space: nowrap;
}
.toolbar button:hover { background: #eee; }
.toolbar button.active { background: #e3e8ff; border-color: #5c6bc0; color: #3949ab; }
.lang-select {
  padding: 4px 8px;
  background: white;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 13px;
  cursor: pointer;
}
.lang-select.active { background: #e3e8ff; border-color: #5c6bc0; color: #3949ab; }
.divider {
  width: 1px;
  background: #ddd;
  margin: 2px 4px;
  align-self: stretch;
}
/* 표 드롭다운 */
.dropdown-wrap { position: relative; }
.dropdown-menu {
  position: absolute;
  top: calc(100% + 4px);
  left: 0;
  background: white;
  border: 1px solid #ddd;
  border-radius: 6px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.12);
  z-index: 999;
  min-width: 140px;
  padding: 4px 0;
}
.dropdown-menu button {
  display: block;
  width: 100%;
  padding: 7px 14px;
  background: none;
  border: none;
  border-radius: 0;
  text-align: left;
  font-size: 13px;
  cursor: pointer;
  white-space: nowrap;
}
.dropdown-menu button:hover { background: #f5f5f5; }
.dropdown-menu hr { margin: 4px 0; border: none; border-top: 1px solid #eee; }
.tiptap-content {
  min-height: 300px;
  padding: 16px;
  font-size: 15px;
  line-height: 1.8;
}
</style>

<style>
.tiptap-content .ProseMirror {
  outline: none;
  min-height: 280px;
}
.tiptap-content .ProseMirror p { margin: 0 0 8px 0; }
.tiptap-content .ProseMirror h1 { font-size: 24px; margin: 16px 0 8px; }
.tiptap-content .ProseMirror h2 { font-size: 20px; margin: 14px 0 6px; }
.tiptap-content .ProseMirror h3 { font-size: 17px; margin: 12px 0 4px; }
.tiptap-content .ProseMirror ul, .tiptap-content .ProseMirror ol { padding-left: 24px; margin: 8px 0; }
.tiptap-content .ProseMirror blockquote { border-left: 3px solid #ddd; margin: 8px 0; padding: 4px 12px; color: #666; }
.tiptap-content .ProseMirror pre { background: #f4f4f4; border-radius: 4px; padding: 12px; font-size: 13px; overflow-x: auto; }
.tiptap-content .ProseMirror img { max-width: 100%; height: auto; border-radius: 4px; }
.tiptap-content .ProseMirror a { color: #1565c0; text-decoration: underline; cursor: pointer; }
.tiptap-content .ProseMirror iframe { max-width: 100%; border-radius: 6px; display: block; margin: 8px 0; }
.tiptap-content .ProseMirror p.is-editor-empty:first-child::before {
  content: attr(data-placeholder);
  color: #aaa;
  pointer-events: none;
  float: left;
  height: 0;
}
/* 에디터 내 표 스타일 */
.tiptap-content .ProseMirror table { border-collapse: collapse; width: 100%; margin: 12px 0; }
.tiptap-content .ProseMirror th,
.tiptap-content .ProseMirror td { border: 1px solid #ccc; padding: 8px 10px; font-size: 14px; min-width: 60px; position: relative; }
.tiptap-content .ProseMirror th { background: #f5f5f5; font-weight: bold; }
/* 표 셀 선택 시 파란색 반투명 오버레이로 선택 영역 강조 (pointer-events 제거로 클릭 이벤트 통과) */
.tiptap-content .ProseMirror .selectedCell::after { content: ''; position: absolute; inset: 0; background: rgba(26,115,232,0.08); pointer-events: none; }
</style>
