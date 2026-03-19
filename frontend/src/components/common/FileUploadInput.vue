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
// 파일 선택 및 업로드 대기 목록 관리 컴포넌트 - PostWriteForm.vue와 PostEditForm.vue에서 v-model로 사용
// 실제 서버 업로드는 하지 않고 File 객체 배열을 부모에게 전달 (부모에서 FormData로 업로드)
export default {
  name: 'FileUploadInput',
  props: {
    // 현재 선택된 File 객체 배열 (v-model 바인딩, 부모에서 관리)
    modelValue: { type: Array, default: () => [] }
  },
  // 파일 추가/제거 시 업데이트된 배열을 부모에게 emit (v-model 패턴)
  emits: ['update:modelValue'],
  methods: {
    // 파일 선택 다이얼로그에서 파일을 선택했을 때 기존 목록에 새 파일들을 추가하여 emit
    onChange(e) {
      // 기존 배열에 새로 선택된 파일들을 추가 (다중 선택 지원)
      this.$emit('update:modelValue', [...this.modelValue, ...Array.from(e.target.files)])
      // 같은 파일을 다시 선택할 수 있도록 input 값 초기화
      e.target.value = ''
    },
    // 대기 목록에서 특정 인덱스의 파일을 제거하여 부모에게 업데이트 emit
    remove(i) {
      this.$emit('update:modelValue', this.modelValue.filter((_, idx) => idx !== i))
    },
    // 바이트 단위 파일 크기를 사람이 읽기 쉬운 형식으로 변환 (B / KB / MB)
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
