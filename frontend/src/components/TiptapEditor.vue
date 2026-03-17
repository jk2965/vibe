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
      <button type="button" @click="editor.chain().focus().toggleCodeBlock().run()" :class="{ active: editor.isActive('codeBlock') }" title="코드">코드</button>
      <span class="divider"></span>
      <button type="button" @click="triggerImage" title="이미지 삽입">이미지</button>
      <input ref="imageInput" type="file" accept="image/*" style="display:none" @change="handleImageUpload">
      <span class="divider"></span>
      <button type="button" @click="editor.chain().focus().unsetAllMarks().clearNodes().run()" title="서식 초기화">초기화</button>
    </div>
    <editor-content :editor="editor" class="tiptap-content" />
  </div>
</template>

<script>
import { Editor, EditorContent } from '@tiptap/vue-3'
import StarterKit from '@tiptap/starter-kit'
import Image from '@tiptap/extension-image'
import Underline from '@tiptap/extension-underline'
import axios from 'axios'

export default {
  name: 'TiptapEditor',
  components: { EditorContent },
  props: {
    modelValue: { type: String, default: '' },
    imageUploadUrl: { type: String, required: true }
  },
  emits: ['update:modelValue'],
  data() {
    return { editor: null }
  },
  watch: {
    modelValue(val) {
      if (this.editor && this.editor.getHTML() !== val) {
        this.editor.commands.setContent(val, false)
      }
    }
  },
  mounted() {
    this.editor = new Editor({
      content: this.modelValue,
      extensions: [StarterKit, Image, Underline],
      onUpdate: () => {
        this.$emit('update:modelValue', this.editor.getHTML())
      }
    })
  },
  beforeUnmount() {
    this.editor.destroy()
  },
  methods: {
    triggerImage() {
      this.$refs.imageInput.click()
    },
    async handleImageUpload(e) {
      const file = e.target.files[0]
      if (!file) return
      const fd = new FormData()
      fd.append('file', file)
      try {
        const res = await axios.post(this.imageUploadUrl, fd)
        this.editor.chain().focus().setImage({ src: res.data.url }).run()
      } catch {
        alert('이미지 업로드에 실패했습니다.')
      }
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
.divider {
  width: 1px;
  background: #ddd;
  margin: 2px 4px;
  align-self: stretch;
}
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
.tiptap-content .ProseMirror p.is-editor-empty:first-child::before {
  content: attr(data-placeholder);
  color: #aaa;
  pointer-events: none;
  float: left;
  height: 0;
}
</style>
