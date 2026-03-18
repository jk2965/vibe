<template>
  <div v-if="files && files.length > 0" class="file-section">
    <h4 class="file-title">첨부파일 ({{ files.length }})</h4>
    <ul class="file-list">
      <li v-for="file in files" :key="file.id" class="file-item">
        <span class="file-icon">📎</span>
        <span class="file-name">{{ file.originalName }}</span>
        <span class="file-size">({{ formatSize(file.fileSize) }})</span>
        <button @click="download(file)" class="btn-download">다운로드</button>
        <button v-if="canDelete" @click="remove(file)" class="btn-del">삭제</button>
      </li>
    </ul>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'FileList',
  props: {
    files: { type: Array, default: () => [] },
    canDelete: { type: Boolean, default: false }
  },
  emits: ['file-deleted'],
  methods: {
    download(file) {
      window.open(`http://localhost:8090/api/archive/files/${file.id}/download`, '_blank')
    },
    async remove(file) {
      if (!confirm(`'${file.originalName}' 파일을 삭제하시겠습니까?`)) return
      try {
        await axios.delete(`http://localhost:8090/api/archive/files/${file.id}`, {
          params: { requesterId: localStorage.getItem('userId') }
        })
        this.$emit('file-deleted', file.id)
      } catch (e) {
        alert('파일 삭제에 실패했습니다.')
      }
    },
    formatSize(bytes) {
      if (!bytes) return ''
      if (bytes < 1024) return bytes + 'B'
      if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + 'KB'
      return (bytes / (1024 * 1024)).toFixed(1) + 'MB'
    }
  }
}
</script>

<style scoped>
.file-section { background: #f8f9fa; border-radius: 8px; padding: 16px; margin-bottom: 24px; }
.file-title { margin: 0 0 12px 0; font-size: 14px; font-weight: bold; color: #555; }
.file-list { list-style: none; padding: 0; margin: 0; }
.file-item { display: flex; align-items: center; gap: 8px; padding: 8px 0; border-bottom: 1px solid #eee; }
.file-item:last-child { border-bottom: none; }
.file-icon { font-size: 15px; flex-shrink: 0; }
.file-name { flex: 1; font-size: 14px; word-break: break-all; }
.file-size { font-size: 12px; color: #888; white-space: nowrap; }
.btn-download { padding: 4px 12px; background: #1565c0; color: white; border: none; border-radius: 4px; font-size: 13px; cursor: pointer; white-space: nowrap; }
.btn-download:hover { background: #0d47a1; }
.btn-del { padding: 4px 10px; background: white; color: #e53935; border: 1px solid #e53935; border-radius: 4px; font-size: 13px; cursor: pointer; white-space: nowrap; }
.btn-del:hover { background: #ffebee; }
</style>
