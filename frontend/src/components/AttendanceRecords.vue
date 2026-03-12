<template>
  <div class="attendance-container">
    <div class="header">
      <h1>출퇴근 기록 조회</h1>
      <div class="user-info">
        <span>{{ username }}님 환영합니다</span>
        <button @click="logout" class="btn-logout">로그아웃</button>
      </div>
    </div>

    <div class="record-form">
      <div class="button-group">
        <button @click="checkIn" class="check-in-btn">출근</button>
        <button @click="checkOut" class="check-out-btn">퇴근</button>
      </div>
    </div>

    <div class="records-list">
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

export default {
  name: 'AttendanceRecords',
  data() {
    return {
      records: [],
      username: localStorage.getItem('username') || '',
      userId: localStorage.getItem('userId') || ''
    }
  },
  mounted() {
    this.fetchRecords()
  },
  methods: {
    async fetchRecords() {
      try {
        const pad = n => String(n).padStart(2, '0')
        const d = new Date()
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
    localDateTime(date) {
      const pad = n => String(n).padStart(2, '0')
      return `${date.getFullYear()}-${pad(date.getMonth()+1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
    },
    async checkIn() {
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
    async checkOut() {
      try {
        const today = this.localDateTime(new Date()).split('T')[0]
        const record = this.records.find(r =>
          r.username === this.userId &&
          !r.checkOutTime &&
          r.checkInTime && r.checkInTime.startsWith(today)
        )
        if (!record) {
          alert('오늘 출근 기록이 없습니다.')
          return
        }
        await axios.put('http://localhost:8090/api/attendance', {
          id: record.id,
          checkOutTime: this.localDateTime(new Date())
        })
        this.fetchRecords()
      } catch (error) {
        console.error('Error checking out:', error)
      }
    },
    logout() {
      localStorage.removeItem('loggedIn')
      localStorage.removeItem('userId')
      localStorage.removeItem('username')
      this.$router.push('/login')
    },
    formatDateTime(dateTimeString) {
      if (!dateTimeString) return '-'
      const date = new Date(dateTimeString)
      return date.toLocaleString('ko-KR')
    }
  }
}
</script>

<style scoped>
.attendance-container { max-width: 900px; margin: 0 auto; padding: 20px; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.header h1 { margin: 0; }
.user-info { display: flex; align-items: center; gap: 16px; font-size: 15px; }
.btn-logout { padding: 8px 16px; background: #e53935; color: white; border: none; border-radius: 6px; cursor: pointer; font-size: 14px; }
.btn-logout:hover { background: #c62828; }
.record-form { margin-bottom: 30px; }
.button-group { display: flex; gap: 20px; }
.check-in-btn, .check-out-btn { padding: 12px 40px; font-size: 16px; border: none; border-radius: 6px; cursor: pointer; }
.check-in-btn { background: #4CAF50; color: white; }
.check-in-btn:hover { background: #45a049; }
.check-out-btn { background: #2196F3; color: white; }
.check-out-btn:hover { background: #1976D2; }
.records-table { width: 100%; border-collapse: collapse; }
.records-table th, .records-table td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; }
.records-table th { background: #f5f5f5; font-weight: bold; }
</style>
