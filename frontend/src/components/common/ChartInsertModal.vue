<template>
  <!-- 차트 삽입 모달 오버레이 -->
  <div class="chart-modal-overlay" @click.self="$emit('close')">
    <div class="chart-modal">
      <h3>차트 삽입</h3>

      <!-- 차트 유형 선택 -->
      <div class="form-row">
        <label>차트 유형</label>
        <div class="chart-type-btns">
          <button
            v-for="t in chartTypes"
            :key="t.value"
            type="button"
            :class="['type-btn', { active: chartType === t.value }]"
            @click="chartType = t.value; updatePreview()"
          >{{ t.label }}</button>
        </div>
      </div>

      <!-- 차트 제목 -->
      <div class="form-row">
        <label>차트 제목 <span class="opt">(선택)</span></label>
        <input v-model="chartTitle" type="text" placeholder="예: 월별 매출" @input="updatePreview" />
      </div>

      <!-- 레이블 입력 -->
      <div class="form-row">
        <label>레이블 <span class="hint">(쉼표로 구분)</span></label>
        <input v-model="labelsInput" type="text" placeholder="예: 1월, 2월, 3월" @input="updatePreview" />
      </div>

      <!-- 데이터 입력 -->
      <div class="form-row">
        <label>데이터 값 <span class="hint">(쉼표로 구분)</span></label>
        <input v-model="dataInput" type="text" placeholder="예: 120, 200, 150" @input="updatePreview" />
      </div>

      <!-- 색상 선택 -->
      <div class="form-row">
        <label>색상 테마</label>
        <div class="color-btns">
          <button
            v-for="theme in colorThemes"
            :key="theme.name"
            type="button"
            :class="['color-btn', { active: selectedTheme === theme.name }]"
            :style="{ background: theme.colors[0] }"
            @click="selectedTheme = theme.name; updatePreview()"
            :title="theme.name"
          ></button>
        </div>
      </div>

      <!-- 미리보기 캔버스 -->
      <div class="preview-area">
        <canvas ref="previewCanvas" width="480" height="240"></canvas>
      </div>

      <!-- 버튼 행 -->
      <div class="modal-actions">
        <button type="button" class="btn-cancel" @click="$emit('close')">취소</button>
        <button type="button" class="btn-insert" @click="insertChart">삽입</button>
      </div>
    </div>
  </div>
</template>

<script>
// 차트 렌더링 라이브러리
import { Chart, registerables } from 'chart.js'
Chart.register(...registerables)

export default {
  name: 'ChartInsertModal',
  emits: ['close', 'insert'],
  data() {
    return {
      // 현재 선택된 차트 유형
      chartType: 'bar',
      // 차트 유형 목록
      chartTypes: [
        { value: 'bar',  label: '막대' },
        { value: 'line', label: '선'   },
        { value: 'pie',  label: '원형' },
        { value: 'doughnut', label: '도넛' },
      ],
      // 차트 제목
      chartTitle: '',
      // 레이블 입력 문자열 (쉼표 구분)
      labelsInput: '1월, 2월, 3월, 4월, 5월',
      // 데이터 값 입력 문자열 (쉼표 구분)
      dataInput: '120, 200, 150, 180, 90',
      // 선택된 색상 테마 이름
      selectedTheme: 'blue',
      // 색상 테마 목록
      colorThemes: [
        { name: 'blue',   colors: ['#1a73e8','#4285f4','#66b2ff','#99ccff','#cce5ff'] },
        { name: 'green',  colors: ['#2e7d32','#43a047','#66bb6a','#a5d6a7','#c8e6c9'] },
        { name: 'red',    colors: ['#c62828','#e53935','#ef5350','#ef9a9a','#ffcdd2'] },
        { name: 'purple', colors: ['#6a1b9a','#8e24aa','#ab47bc','#ce93d8','#e1bee7'] },
        { name: 'orange', colors: ['#e65100','#f57c00','#ff9800','#ffcc80','#ffe0b2'] },
      ],
      // Chart.js 인스턴스 (미리보기용)
      chartInstance: null,
    }
  },
  mounted() {
    // 모달 열릴 때 미리보기 초기 렌더링
    this.$nextTick(() => this.updatePreview())
  },
  beforeUnmount() {
    // 메모리 누수 방지: Chart.js 인스턴스 파괴
    if (this.chartInstance) this.chartInstance.destroy()
  },
  methods: {
    // 입력값을 파싱하여 차트 미리보기 업데이트
    updatePreview() {
      const labels = this.labelsInput.split(',').map(s => s.trim()).filter(Boolean)
      const values = this.dataInput.split(',').map(s => parseFloat(s.trim())).filter(v => !isNaN(v))
      const theme = this.colorThemes.find(t => t.name === this.selectedTheme) || this.colorThemes[0]

      // 기존 차트 인스턴스 파괴 후 새로 생성
      if (this.chartInstance) {
        this.chartInstance.destroy()
        this.chartInstance = null
      }

      const ctx = this.$refs.previewCanvas.getContext('2d')
      // 파이/도넛 차트는 각 데이터 포인트에 개별 색상 적용
      const isPieOrDoughnut = this.chartType === 'pie' || this.chartType === 'doughnut'
      const backgroundColors = isPieOrDoughnut
        ? values.map((_, i) => theme.colors[i % theme.colors.length])
        : theme.colors[0]

      this.chartInstance = new Chart(ctx, {
        type: this.chartType,
        data: {
          labels,
          datasets: [{
            label: this.chartTitle || '데이터',
            data: values,
            backgroundColor: backgroundColors,
            borderColor: isPieOrDoughnut ? '#fff' : theme.colors[1] || theme.colors[0],
            borderWidth: isPieOrDoughnut ? 2 : 1,
            fill: this.chartType === 'line' ? false : undefined,
            tension: this.chartType === 'line' ? 0.3 : undefined,
          }]
        },
        options: {
          responsive: false,
          plugins: {
            title: {
              display: !!this.chartTitle,
              text: this.chartTitle,
              font: { size: 14 }
            },
            legend: { position: 'bottom' }
          },
          scales: isPieOrDoughnut ? {} : {
            y: { beginAtZero: true }
          }
        }
      })
    },

    // 현재 캔버스를 base64 PNG 이미지로 변환하여 부모(에디터)에 전달
    insertChart() {
      if (!this.chartInstance) return
      const dataUrl = this.$refs.previewCanvas.toDataURL('image/png')
      // 부모 컴포넌트(TiptapEditor)에 base64 이미지 URL 전달
      this.$emit('insert', dataUrl)
    }
  }
}
</script>

<style scoped>
/* 모달 오버레이: 화면 전체를 반투명 배경으로 덮음 */
.chart-modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

/* 모달 본체 */
.chart-modal {
  background: white;
  border-radius: 10px;
  padding: 24px;
  width: 540px;
  max-width: 95vw;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
}
.chart-modal h3 {
  margin: 0 0 18px;
  font-size: 16px;
  color: #222;
}

/* 폼 행 */
.form-row {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 14px;
}
.form-row label {
  font-size: 13px;
  font-weight: 600;
  color: #444;
}
.form-row input {
  padding: 7px 10px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 13px;
  outline: none;
}
.form-row input:focus { border-color: #1a73e8; }
.opt, .hint { font-weight: 400; color: #999; font-size: 12px; }

/* 차트 유형 버튼 */
.chart-type-btns { display: flex; gap: 6px; }
.type-btn {
  padding: 5px 14px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 13px;
  background: white;
  cursor: pointer;
}
.type-btn.active { background: #e3e8ff; border-color: #5c6bc0; color: #3949ab; }

/* 색상 테마 버튼 */
.color-btns { display: flex; gap: 8px; }
.color-btn {
  width: 26px;
  height: 26px;
  border-radius: 50%;
  border: 2px solid transparent;
  cursor: pointer;
}
.color-btn.active { border-color: #222; }

/* 미리보기 캔버스 */
.preview-area {
  background: #f8f9fa;
  border: 1px solid #eee;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 12px;
  margin-bottom: 16px;
  overflow: hidden;
}
.preview-area canvas { max-width: 100%; border-radius: 4px; }

/* 하단 버튼 */
.modal-actions { display: flex; justify-content: flex-end; gap: 8px; }
.btn-cancel {
  padding: 7px 18px;
  border: 1px solid #ddd;
  border-radius: 6px;
  background: white;
  font-size: 13px;
  cursor: pointer;
}
.btn-insert {
  padding: 7px 18px;
  border: none;
  border-radius: 6px;
  background: #1a73e8;
  color: white;
  font-size: 13px;
  cursor: pointer;
}
.btn-insert:hover { background: #1558b0; }
</style>
