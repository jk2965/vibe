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
// FullCalendar Vue3 래퍼 컴포넌트 (@fullcalendar/vue3)
import FullCalendar from '@fullcalendar/vue3'
// FullCalendar 월별 그리드 뷰 플러그인 (@fullcalendar/daygrid)
import dayGridPlugin from '@fullcalendar/daygrid'
// FullCalendar 날짜 클릭/이벤트 클릭 인터랙션 플러그인 (@fullcalendar/interaction)
import interactionPlugin from '@fullcalendar/interaction'
// FullCalendar 목록(리스트) 뷰 플러그인 (@fullcalendar/list)
import listPlugin from '@fullcalendar/list'
// FullCalendar 한국어 로케일 (@fullcalendar/core/locales/ko)
import koLocale from '@fullcalendar/core/locales/ko'
import axios from 'axios'
// 공통 페이지 헤더 컴포넌트 (PageHeader.vue)
import PageHeader from './common/PageHeader.vue'

export default {
  name: 'Calendar',
  components: { FullCalendar, PageHeader },
  data() {
    return {
      // FullCalendar에 표시할 일정 이벤트 배열
      events: [],
      // 일정 추가/수정 모달 상태 객체
      modal: {
        // 모달 표시 여부
        show: false,
        // 'create'(추가) 또는 'edit'(수정) 모드
        mode: 'create',
        // 수정 시 일정 ID (create 시 null)
        id: null,
        // 일정 제목
        title: '',
        // 시작 날짜 (YYYY-MM-DD)
        startDate: '',
        // 종료 날짜 (YYYY-MM-DD, 선택 사항)
        endDate: '',
        // 메모 내용
        description: '',
        // 이벤트 색상 (기본값: 초록)
        color: '#4CAF50'
      },
      // 이벤트 색상 선택 옵션 목록
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
    // 현재 로그인한 사용자 ID (일정 소유자 확인에 사용)
    userId() { return localStorage.getItem('userId') || '' },
    // 현재 사용자의 소속 팀 (팀 필터링에 사용)
    team() { return localStorage.getItem('team') || '' },
    // 현재 사용자의 관리자 레벨 (전체 일정 조회 권한 여부 결정)
    adminLevel() { return parseInt(localStorage.getItem('adminLevel') || '0') },
    // FullCalendar 설정 옵션 객체 (반응형으로 events 배열 연동)
    calendarOptions() {
      return {
        // 사용할 플러그인 목록 (월 그리드, 인터랙션, 목록 뷰)
        plugins: [dayGridPlugin, interactionPlugin, listPlugin],
        // 초기 뷰: 월별 그리드
        initialView: 'dayGridMonth',
        // 한국어 로케일 적용
        locale: koLocale,
        // 헤더 툴바 버튼 구성
        headerToolbar: {
          left: 'prev,next today',
          center: 'title',
          right: 'dayGridMonth,listMonth'
        },
        // 표시할 이벤트 배열 (fetchSchedules에서 채움)
        events: this.events,
        // 날짜 셀 클릭 핸들러 (일정 추가 모달 열기)
        dateClick: this.onDateClick,
        // 이벤트 클릭 핸들러 (일정 상세/수정 모달 열기)
        eventClick: this.onEventClick,
        height: 'auto',
        selectable: true,
        editable: false
      }
    }
  },
  // 컴포넌트 마운트 시 일정 목록 조회
  mounted() {
    this.fetchSchedules()
  },
  methods: {
    // GET /api/schedule 호출 → ScheduleController.java
    // adminLevel과 team 파라미터로 조회 범위 결정 (슈퍼 관리자: 전체, 팀원: 같은 팀)
    // FullCalendar 이벤트 형식으로 변환하여 events 배열에 저장
    async fetchSchedules() {
      try {
        const res = await axios.get('/api/schedule', {
          params: { adminLevel: this.adminLevel, team: this.team }
        })
        // FullCalendar는 종료일이 exclusive이므로 endDate에 하루 추가
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
            // extendedProps에 추가 정보 저장 (이벤트 클릭 시 모달에서 사용)
            extendedProps: { description: s.description, userId: s.userId, endDate: s.endDate || '' }
          }
        })
      } catch (e) {
        console.error('일정 조회 실패:', e)
      }
    },
    // FullCalendar 날짜 셀 클릭 이벤트 핸들러
    // 클릭한 날짜를 시작일로 설정하고 '추가' 모드 모달 오픈
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
    // FullCalendar 이벤트 클릭 핸들러
    // 클릭한 이벤트 데이터를 모달에 채우고 '수정' 모드 모달 오픈
    // isOwner 여부로 수정/삭제 가능 여부 결정 (다른 사람 일정은 조회만 가능)
    onEventClick(info) {
      const e = info.event
      // 현재 로그인 사용자가 이 일정의 작성자인지 확인
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
    // 일정 저장: 모드에 따라 POST(추가) 또는 PUT(수정) 호출
    // POST /api/schedule 호출 → ScheduleController.java (일정 추가)
    // PUT /api/schedule/:id 호출 → ScheduleController.java (일정 수정)
    async saveSchedule() {
      if (!this.modal.title.trim()) { alert('제목을 입력하세요.'); return }
      if (!this.modal.startDate) { alert('시작일을 입력하세요.'); return }

      // 일정 저장 payload (userId와 team 포함)
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
        // 저장 후 모달 닫고 일정 목록 재조회
        this.closeModal()
        this.fetchSchedules()
      } catch (e) {
        alert(e.response?.data?.message || '저장 중 오류가 발생했습니다.')
      }
    },
    // DELETE /api/schedule/:id 호출 → ScheduleController.java
    // 삭제 확인 후 일정 삭제, 성공 시 모달 닫고 목록 재조회
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
    // 모달 닫기
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
