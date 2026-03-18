<template>
  <div class="calendar-container">
    <PageHeader title="일정 관리" />

    <div class="calendar-card">
      <FullCalendar :options="calendarOptions" />
    </div>

    <!-- 일정 추가/수정 모달 -->
    <div v-if="modal.show" class="modal-overlay" @click.self="closeModal">
      <div class="modal">
        <h3>{{ modal.mode === 'create' ? '일정 추가' : '일정 수정' }}</h3>

        <div v-if="modal.mode === 'edit' && !modal.isOwner" class="readonly-notice">다른 사람의 일정입니다. (조회만 가능)</div>

        <div class="form-group">
          <label>제목 *</label>
          <input v-model="modal.title" type="text" placeholder="일정 제목" class="input" :readonly="modal.mode === 'edit' && !modal.isOwner" />
        </div>

        <div class="form-row">
          <div class="form-group">
            <label>시작일 *</label>
            <input v-model="modal.startDate" type="date" class="input" :readonly="modal.mode === 'edit' && !modal.isOwner" />
          </div>
          <div class="form-group">
            <label>종료일</label>
            <input v-model="modal.endDate" type="date" class="input" :readonly="modal.mode === 'edit' && !modal.isOwner" />
          </div>
        </div>

        <div class="form-group">
          <label>메모</label>
          <textarea v-model="modal.description" placeholder="메모 (선택)" class="input textarea" rows="3" :readonly="modal.mode === 'edit' && !modal.isOwner"></textarea>
        </div>

        <div v-if="modal.mode === 'create' || modal.isOwner" class="form-group">
          <label>색상</label>
          <div class="color-row">
            <button
              v-for="c in colorOptions"
              :key="c.value"
              @click="modal.mode === 'create' || modal.isOwner ? modal.color = c.value : null"
              class="color-btn"
              :style="{ background: c.value }"
              :class="{ selected: modal.color === c.value }"
              :title="c.label"
            ></button>
          </div>
        </div>

        <div class="modal-actions">
          <button v-if="modal.mode === 'edit' && modal.isOwner" @click="deleteSchedule" class="btn-delete">삭제</button>
          <div class="right-btns">
            <button @click="closeModal" class="btn-cancel">{{ modal.mode === 'edit' && !modal.isOwner ? '닫기' : '취소' }}</button>
            <button v-if="modal.mode === 'create' || modal.isOwner" @click="saveSchedule" class="btn-save">저장</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import FullCalendar from '@fullcalendar/vue3'
import dayGridPlugin from '@fullcalendar/daygrid'
import interactionPlugin from '@fullcalendar/interaction'
import listPlugin from '@fullcalendar/list'
import koLocale from '@fullcalendar/core/locales/ko'
import axios from 'axios'
import PageHeader from './common/PageHeader.vue'

export default {
  name: 'Calendar',
  components: { FullCalendar, PageHeader },
  data() {
    return {
      events: [],
      modal: {
        show: false,
        mode: 'create',
        id: null,
        title: '',
        startDate: '',
        endDate: '',
        description: '',
        color: '#4CAF50'
      },
      colorOptions: [
        { value: '#4CAF50', label: '초록' },
        { value: '#2196F3', label: '파랑' },
        { value: '#f57c00', label: '주황' },
        { value: '#e53935', label: '빨강' },
        { value: '#7b1fa2', label: '보라' },
        { value: '#00838f', label: '청록' },
        { value: '#558b2f', label: '올리브' },
        { value: '#6d4c41', label: '갈색' }
      ]
    }
  },
  computed: {
    userId() { return localStorage.getItem('userId') || '' },
    team() { return localStorage.getItem('team') || '' },
    adminLevel() { return parseInt(localStorage.getItem('adminLevel') || '0') },
    calendarOptions() {
      return {
        plugins: [dayGridPlugin, interactionPlugin, listPlugin],
        initialView: 'dayGridMonth',
        locale: koLocale,
        headerToolbar: {
          left: 'prev,next today',
          center: 'title',
          right: 'dayGridMonth,listMonth'
        },
        events: this.events,
        dateClick: this.onDateClick,
        eventClick: this.onEventClick,
        height: 'auto',
        selectable: true,
        editable: false
      }
    }
  },
  mounted() {
    this.fetchSchedules()
  },
  methods: {
    async fetchSchedules() {
      try {
        const res = await axios.get('/api/schedule', {
          params: { adminLevel: this.adminLevel, team: this.team }
        })
        this.events = res.data.map(s => {
          let end
          if (s.endDate) {
            const d = new Date(s.endDate + 'T00:00:00')
            d.setDate(d.getDate() + 1)
            end = d.toISOString().substring(0, 10)
          }
          return {
            id: s.id,
            title: s.title,
            start: s.startDate,
            end,
            backgroundColor: s.color || '#4CAF50',
            borderColor: s.color || '#4CAF50',
            extendedProps: { description: s.description, userId: s.userId, endDate: s.endDate || '' }
          }
        })
      } catch (e) {
        console.error('일정 조회 실패:', e)
      }
    },
    onDateClick(info) {
      this.modal = {
        show: true,
        mode: 'create',
        id: null,
        title: '',
        startDate: info.dateStr,
        endDate: '',
        description: '',
        color: '#4CAF50'
      }
    },
    onEventClick(info) {
      const e = info.event
      const isOwner = e.extendedProps.userId === this.userId
      this.modal = {
        show: true,
        mode: 'edit',
        id: e.id,
        title: e.title,
        startDate: e.startStr.substring(0, 10),
        endDate: e.extendedProps.endDate,
        description: e.extendedProps.description || '',
        color: e.backgroundColor || '#4CAF50',
        isOwner
      }
    },
    async saveSchedule() {
      if (!this.modal.title.trim()) { alert('제목을 입력하세요.'); return }
      if (!this.modal.startDate) { alert('시작일을 입력하세요.'); return }

      const payload = {
        title: this.modal.title.trim(),
        startDate: this.modal.startDate,
        endDate: this.modal.endDate || null,
        description: this.modal.description,
        color: this.modal.color,
        userId: this.userId,
        team: this.team
      }

      try {
        if (this.modal.mode === 'create') {
          await axios.post('/api/schedule', payload)
        } else {
          await axios.put(`/api/schedule/${this.modal.id}`, payload, { params: { userId: this.userId } })
        }
        this.closeModal()
        this.fetchSchedules()
      } catch (e) {
        alert(e.response?.data?.message || '저장 중 오류가 발생했습니다.')
      }
    },
    async deleteSchedule() {
      if (!confirm('일정을 삭제하시겠습니까?')) return
      try {
        await axios.delete(`/api/schedule/${this.modal.id}`, { params: { userId: this.userId } })
        this.closeModal()
        this.fetchSchedules()
      } catch (e) {
        alert(e.response?.data?.message || '삭제 중 오류가 발생했습니다.')
      }
    },
    closeModal() {
      this.modal.show = false
    }
  }
}
</script>

<style scoped>
.calendar-container { max-width: 1000px; margin: 0 auto; padding: 24px; }

.calendar-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
  padding: 24px;
}

/* 모달 */
.modal-overlay {
  position: fixed; inset: 0;
  background: rgba(0,0,0,0.4);
  display: flex; align-items: center; justify-content: center;
  z-index: 1000;
}
.modal {
  background: white;
  border-radius: 12px;
  padding: 28px;
  width: 440px;
  max-width: 90vw;
  box-shadow: 0 8px 32px rgba(0,0,0,0.2);
}
.modal h3 { margin: 0 0 20px 0; font-size: 18px; }

.form-group { margin-bottom: 16px; }
.form-group label { display: block; font-size: 13px; color: #555; margin-bottom: 6px; font-weight: 500; }
.form-row { display: flex; gap: 12px; }
.form-row .form-group { flex: 1; }

.input {
  width: 100%; padding: 9px 12px; border: 1px solid #ddd;
  border-radius: 6px; font-size: 14px; box-sizing: border-box;
}
.input:focus { outline: none; border-color: #4CAF50; }
.textarea { resize: vertical; min-height: 72px; }

.color-row { display: flex; gap: 8px; flex-wrap: wrap; }
.color-btn {
  width: 28px; height: 28px; border-radius: 50%; border: 2px solid transparent;
  cursor: pointer; transition: transform 0.1s;
}
.color-btn:hover { transform: scale(1.15); }
.color-btn.selected { border-color: #333; transform: scale(1.2); }

.modal-actions { display: flex; justify-content: space-between; align-items: center; margin-top: 20px; }
.right-btns { display: flex; gap: 8px; }

.btn-save { padding: 9px 22px; background: #4CAF50; color: white; border: none; border-radius: 6px; font-size: 14px; cursor: pointer; }
.btn-save:hover { background: #388E3C; }
.btn-cancel { padding: 9px 22px; background: white; color: #555; border: 1px solid #ddd; border-radius: 6px; font-size: 14px; cursor: pointer; }
.btn-cancel:hover { background: #f5f5f5; }
.btn-delete { padding: 9px 22px; background: #e53935; color: white; border: none; border-radius: 6px; font-size: 14px; cursor: pointer; }
.btn-delete:hover { background: #c62828; }
.readonly-notice { background: #fff8e1; border: 1px solid #ffe082; border-radius: 6px; padding: 8px 12px; font-size: 13px; color: #795548; margin-bottom: 16px; }
.input[readonly] { background: #f5f5f5; color: #777; cursor: default; }
</style>
