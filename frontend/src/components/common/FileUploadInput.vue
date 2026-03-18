<template>
  <div class="upload-wrap">
    <div class="upload-row">
      <input type="file" ref="input" multiple @change="onChange" class="hidden-input">
      <button type="button" @click="$refs.input.click()" class="btn-select">파일 선택</button>
      <span class="hint">여러 파일 선택 가능</span>
    </div>
    <ul v-if="modelValue.length > 0" class="pending-list">
      <li v-for="(f, i) in modelValue" :key="i" class="pending-item">
        <span class="file-icon">📎</span>
        <span class="file-name">{{ f.name }}</span>
        <span class="file-size">({{ formatSize(f.size) }})</span>
        <button type="button" @click="remove(i)" class="btn-remove">✕</button>
      </li>
    </ul>
  </div>
</template>

<script>
export default {
  name: 'FileUploadInput',
  props: {
    modelValue: { type: Array, default: () => [] }
  },
  emits: ['update:modelValue'],
  methods: {
    onChange(e) {
      this.$emit('update:modelValue', [...this.modelValue, ...Array.from(e.target.files)])
      e.target.value = ''
    },
    remove(i) {
      this.$emit('update:modelValue', this.modelValue.filter((_, idx) => idx !== i))
    },
    formatSize(bytes) {
      if (bytes < 1024) return bytes + 'B'
      if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + 'KB'
      return (bytes / (1024 * 1024)).toFixed(1) + 'MB'
    }
  }
}
</script>

<style scoped>
.upload-wrap { }
.upload-row { display: flex; align-items: center; gap: 12px; }
.hidden-input { display: none; }
.btn-select { padding: 8px 18px; background: #f5f5f5; border: 1px solid #ddd; border-radius: 6px; font-size: 14px; cursor: pointer; }
.btn-select:hover { background: #eee; }
.hint { font-size: 12px; color: #aaa; }
.pending-list { list-style: none; padding: 0; margin: 10px 0 0 0; }
.pending-item { display: flex; align-items: center; gap: 8px; padding: 6px 10px; background: #f9f9f9; border-radius: 6px; margin-bottom: 6px; }
.file-icon { font-size: 14px; }
.file-name { flex: 1; font-size: 14px; word-break: break-all; }
.file-size { font-size: 12px; color: #888; white-space: nowrap; }
.btn-remove { background: none; border: none; color: #e53935; font-size: 15px; cursor: pointer; padding: 0 4px; line-height: 1; }
</style>
