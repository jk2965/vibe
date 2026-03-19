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
// 파일 삭제 API 호출을 위한 axios 임포트
import axios from 'axios'

// 첨부파일 목록 표시 컴포넌트 - PostDetailCard.vue와 PostEditForm.vue에서 사용
// 다운로드 및 (canDelete=true 시) 서버에서 파일 삭제 기능 제공
export default {
  name: 'FileList',
  props: {
    // 표시할 파일 객체 배열 (id, originalName, fileSize 필드 포함)
    files: { type: Array, default: () => [] },
    // 삭제 버튼 표시 여부 (작성자/관리자일 때 true, PostDetailCard에서 canDelete prop 전달)
    canDelete: { type: Boolean, default: false }
  },
  // 파일 삭제 후 부모 컴포넌트에 삭제된 파일 ID를 emit (부모에서 로컬 목록 갱신)
  emits: ['file-deleted'],
  methods: {
    // 새 탭에서 파일 다운로드 (GET /api/archive/files/:id/download)
    download(file) {
      window.open(`http://localhost:8090/api/archive/files/${file.id}/download`, '_blank')
    },
    // DELETE /api/archive/files/:id - 서버에서 파일 삭제 후 부모에게 file-deleted 이벤트 emit
    async remove(file) {
      if (!confirm(`'${file.originalName}' 파일을 삭제하시겠습니까?`)) return
      try {
        await axios.delete(`http://localhost:8090/api/archive/files/${file.id}`, {
          // requesterId 쿼리 파라미터로 본인/권한 확인
          params: { requesterId: localStorage.getItem('userId') }
        })
        // 삭제 성공 시 부모에게 삭제된 파일 ID 전달 (PostEditForm에서 로컬 목록 갱신)
        this.$emit('file-deleted', file.id)
      } catch (e) {
        alert('파일 삭제에 실패했습니다.')
      }
    },
    // 바이트 단위 파일 크기를 사람이 읽기 쉬운 형식으로 변환 (B / KB / MB)
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
