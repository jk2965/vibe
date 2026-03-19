<template>
  <div class="attendance-container">
    <PageHeader title="출퇴근 기록 조회" />

    <div class="form-card">
      <h2>출퇴근 기록</h2>
      <div class="current-time">{{ currentTime }}</div>
      <div class="button-group">
        <button @click="checkIn" :disabled="todayCheckedIn" class="check-in-btn" :class="{ disabled: todayCheckedIn }">출근</button>
        <button @click="checkOut" :disabled="!hasUnfinished" class="check-out-btn" :class="{ disabled: !hasUnfinished }">퇴근</button>
      </div>
      <p v-if="unfinishedDate" class="unfinished-notice">⚠ {{ unfinishedDate }} 미퇴근 기록이 있습니다.</p>
    </div>

    <div class="list-card">
      <h2>기록 목록</h2>
      <table class="records-table">
        <thead>
          <tr>
            <th>이름</th>
            <th>출근 시간</th>
            <th>퇴근 시간</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="records.length === 0">
            <td colspan="3" class="empty">기록이 없습니다.</td>
          </tr>
          <tr v-for="(record, index) in records" :key="index">
            <td>{{ username }}</td>
            <td>{{ formatDateTime(record.checkInTime) }}</td>
            <td>{{ formatDateTime(record.checkOutTime) }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
// 공통 페이지 헤더 컴포넌트 (PageHeader.vue)
import PageHeader from './common/PageHeader.vue'

export default {
  name: 'AttendanceRecords',
  components: { PageHeader },
  data() {
    return {
      // 출퇴근 기록 목록 (최근 7일)
      records: [],
      // 현재 로그인한 사용자 이름 (테이블 '이름' 컬럼 표시용)
      username: localStorage.getItem('username') || '',
      // 현재 로그인한 사용자 ID (API 호출 시 사용)
      userId: localStorage.getItem('userId') || '',
      // 현재 시각 문자열 (1초마다 갱신)
      currentTime: '',
      // setInterval 타이머 참조 (컴포넌트 해제 시 clearInterval에 사용)
      timer: null
    }
  },
  computed: {
    // 오늘 날짜 문자열 'YYYY-MM-DD' 형식 (출근 여부 판단에 사용)
    today() {
      const pad = n => String(n).padStart(2, '0')
      const d = new Date()
      return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}`
    },
    // 오늘 이미 출근 기록이 있는지 여부 (출근 버튼 비활성화 조건)
    todayCheckedIn() {
      return this.records.some(r => r.checkInTime && r.checkInTime.startsWith(this.today))
    },
    // 미퇴근 기록이 있는지 여부 (퇴근 버튼 활성화 조건)
    hasUnfinished() {
      return this.records.some(r => r.checkInTime && !r.checkOutTime)
    },
    // 미퇴근 기록의 날짜 반환 (오늘 날짜면 null, 이전 날짜면 해당 날짜 문자열)
    unfinishedDate() {
      const record = this.records.find(r => r.checkInTime && !r.checkOutTime)
      if (!record) return null
      const date = record.checkInTime.split('T')[0]
      return date === this.today ? null : date
    }
  },
  // 컴포넌트 마운트 시 기록 조회, 현재 시각 표시 시작 (1초 간격)
  mounted() {
    this.fetchRecords()
    this.updateTime()
    this.timer = setInterval(this.updateTime, 1000)
  },
  // 컴포넌트 해제 시 타이머 정지 (메모리 누수 방지)
  beforeUnmount() {
    clearInterval(this.timer)
  },
  methods: {
    // 현재 시각을 한국어 형식으로 갱신 (1초마다 호출)
    updateTime() {
      this.currentTime = new Date().toLocaleString('ko-KR', {
        year: 'numeric', month: '2-digit', day: '2-digit',
        hour: '2-digit', minute: '2-digit', second: '2-digit',
        weekday: 'short'
      })
    },
    // GET /api/attendance 호출 → AttendanceController.java
    // 최근 7일간의 출퇴근 기록 조회 (username=userId, fromDate 파라미터)
    async fetchRecords() {
      try {
        const pad = n => String(n).padStart(2, '0')
        const d = new Date()
        // 7일 전 날짜 계산
        d.setDate(d.getDate() - 7)
        const fromDate = `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}T00:00:00`
        const response = await axios.get('http://localhost:8090/api/attendance', {
          params: { username: this.userId, fromDate }
        })
        this.records = response.data
      } catch (error) {
        console.error('Error fetching records:', error)
      }
    },
    // Date 객체를 'YYYY-MM-DDTHH:MM:SS' 형식의 로컬 시각 문자열로 변환 (서버 전송용)
    localDateTime(date) {
      const pad = n => String(n).padStart(2, '0')
      return `${date.getFullYear()}-${pad(date.getMonth()+1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
    },
    // POST /api/attendance 호출 → AttendanceController.java
    // 출근 기록 생성 (checkOutTime: null), 성공 시 기록 목록 새로고침
    async checkIn() {
      if (this.todayCheckedIn) return
      try {
        await axios.post('http://localhost:8090/api/attendance', {
          username: this.userId,
          checkInTime: this.localDateTime(new Date()),
          checkOutTime: null
        })
        this.fetchRecords()
      } catch (error) {
        console.error('Error checking in:', error)
      }
    },
    // PUT /api/attendance 호출 → AttendanceController.java
    // 미퇴근 기록의 checkOutTime을 현재 시각으로 업데이트
    // 이전 날짜 미퇴근인 경우 확인 팝업 표시
    async checkOut() {
      // 미퇴근 기록(checkInTime 있고 checkOutTime 없는 것) 찾기
      const record = this.records.find(r => r.checkInTime && !r.checkOutTime)
      if (!record) return
      const date = record.checkInTime.split('T')[0]
      // 이전 날짜 미퇴근 처리 시 사용자에게 확인
      if (date !== this.today) {
        if (!confirm(`${date} 날짜의 미퇴근 기록을 지금 퇴근 처리하시겠습니까?`)) return
      }
      try {
        await axios.put('http://localhost:8090/api/attendance', {
          id: record.id,
          checkOutTime: this.localDateTime(new Date())
        })
        this.fetchRecords()
      } catch (error) {
        console.error('Error checking out:', error)
      }
    },
    // 날짜+시간 문자열을 한국어 로케일 형식으로 변환 (테이블 표시용)
    formatDateTime(dateTimeString) {
      if (!dateTimeString) return '-'
      const date = new Date(dateTimeString)
      return date.toLocaleString('ko-KR')
    }
  }
}
</script>

<style scoped>
.attendance-container { max-width: 900px; margin: 0 auto; padding: 24px; }

.form-card, .list-card { background: white; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.08); padding: 24px; margin-bottom: 24px; }
.form-card h2, .list-card h2 { margin: 0 0 20px 0; font-size: 18px; }

.current-time { font-size: 28px; font-weight: bold; color: #333; margin-bottom: 20px; }
.button-group { display: flex; gap: 20px; }
.check-in-btn, .check-out-btn { padding: 12px 40px; font-size: 16px; border: none; border-radius: 6px; cursor: pointer; }
.check-in-btn { background: #4CAF50; color: white; }
.check-in-btn:hover:not(.disabled) { background: #45a049; }
.check-out-btn { background: #2196F3; color: white; }
.check-out-btn:hover:not(.disabled) { background: #1976D2; }
.check-in-btn.disabled, .check-out-btn.disabled { background: #ccc; color: #999; cursor: not-allowed; }
.unfinished-notice { margin-top: 12px; color: #e65100; font-size: 13px; }

.records-table { width: 100%; border-collapse: collapse; }
.records-table th, .records-table td { padding: 12px; text-align: left; border-bottom: 1px solid #eee; font-size: 14px; }
.records-table th { background: #f5f5f5; font-weight: bold; }
.empty { text-align: center; color: #999; padding: 32px; }
</style>
