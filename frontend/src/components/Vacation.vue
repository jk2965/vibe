<template>
  <div class="vacation-container">
    <PageHeader title="휴가 등록" />

    <!-- 휴가 신청 폼 -->
    <div class="form-card">
      <div class="form-card-header">
        <h2>휴가 신청</h2>
        <div class="remaining-badge">
          잔여 연차 <strong>{{ remainingVacation }}</strong>일
        </div>
      </div>
      <div class="form-row">
        <div class="form-group">
          <label>휴가 유형</label>
          <select v-model="form.type" @change="onTypeChange">
            <option value="연차">연차</option>
            <option value="반차(오전)">반차 (오전)</option>
            <option value="반차(오후)">반차 (오후)</option>
            <option value="병가">병가</option>
            <option value="기타">기타</option>
          </select>
        </div>
      </div>
      <div class="form-row">
        <div class="form-group">
          <label>시작일</label>
          <input type="date" v-model="form.startDate" @change="onStartDateChange">
        </div>
        <div class="form-group">
          <label>종료일</label>
          <input type="date" v-model="form.endDate" :disabled="isHalfDay">
        </div>
      </div>
      <div class="form-group">
        <label>사유</label>
        <input type="text" v-model="form.reason" placeholder="휴가 사유를 입력하세요">
      </div>
      <p v-if="errorMsg" class="error-msg">{{ errorMsg }}</p>
      <button @click="submit" class="btn-submit">신청</button>
    </div>

    <!-- 휴가 목록 -->
    <div class="list-card">
      <h2>신청 내역</h2>
      <table class="vacation-table">
        <thead>
          <tr>
            <th>유형</th>
            <th>시작일</th>
            <th>종료일</th>
            <th>차감</th>
            <th>사유</th>
            <th>신청일시</th>
            <th>취소</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="vacations.length === 0">
            <td colspan="7" class="empty">신청한 휴가가 없습니다.</td>
          </tr>
          <tr v-for="v in vacations" :key="v.id">
            <td><span class="type-badge">{{ v.type }}</span></td>
            <td>{{ v.startDate }}</td>
            <td>{{ v.endDate }}</td>
            <td>{{ calcDeduct(v.type, v.startDate, v.endDate) }}일</td>
            <td>{{ v.reason || '-' }}</td>
            <td>{{ v.createdAt }}</td>
            <td><button @click="deleteVacation(v.id)" class="btn-delete">취소</button></td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import PageHeader from './common/PageHeader.vue'

export default {
  name: 'Vacation',
  components: { PageHeader },
  data() {
    return {
      userId: localStorage.getItem('userId') || '',
      vacations: [],
      remainingVacation: 0,
      form: {
        type: '연차',
        startDate: '',
        endDate: '',
        reason: ''
      },
      errorMsg: ''
    }
  },
  computed: {
    isHalfDay() {
      return this.form.type === '반차(오전)' || this.form.type === '반차(오후)'
    }
  },
  mounted() {
    this.fetchVacations()
    this.fetchRemaining()
  },
  methods: {
    async fetchRemaining() {
      try {
        const res = await axios.get('http://localhost:8090/api/vacation/remaining', {
          params: { userId: this.userId }
        })
        this.remainingVacation = res.data.remainingVacation
      } catch (e) {
        console.error('잔여 연차 조회 실패:', e)
      }
    },
    async fetchVacations() {
      try {
        const res = await axios.get('http://localhost:8090/api/vacation', {
          params: { userId: this.userId }
        })
        this.vacations = res.data
      } catch (e) {
        console.error('휴가 목록 조회 실패:', e)
      }
    },
    onTypeChange() {
      if (this.isHalfDay && this.form.startDate) {
        this.form.endDate = this.form.startDate
      }
    },
    onStartDateChange() {
      if (this.isHalfDay) {
        this.form.endDate = this.form.startDate
      }
    },
    calcDeduct(type, startDate, endDate) {
      if (type === '반차(오전)' || type === '반차(오후)') return 0.5
      if (type === '연차' && startDate && endDate) {
        const start = new Date(startDate)
        const end = new Date(endDate)
        return Math.floor((end - start) / (1000 * 60 * 60 * 24)) + 1
      }
      return 0
    },
    async submit() {
      this.errorMsg = ''
      if (!this.form.startDate || !this.form.endDate) {
        this.errorMsg = '시작일과 종료일을 입력하세요.'
        return
      }
      if (this.form.startDate > this.form.endDate) {
        this.errorMsg = '종료일이 시작일보다 빠를 수 없습니다.'
        return
      }
      try {
        await axios.post('http://localhost:8090/api/vacation', {
          userId: this.userId,
          type: this.form.type,
          startDate: this.form.startDate,
          endDate: this.form.endDate,
          reason: this.form.reason
        })
        this.form.startDate = ''
        this.form.endDate = ''
        this.form.reason = ''
        this.form.type = '연차'
        await this.fetchVacations()
        await this.fetchRemaining()
      } catch (e) {
        this.errorMsg = e.response?.data?.message || '휴가 신청에 실패했습니다.'
      }
    },
    async deleteVacation(id) {
      if (!confirm('휴가를 취소하시겠습니까?')) return
      try {
        await axios.delete(`http://localhost:8090/api/vacation/${id}`)
        await this.fetchVacations()
        await this.fetchRemaining()
      } catch (e) {
        alert('취소 처리에 실패했습니다.')
      }
    }
  }
}
</script>

<style scoped>
.vacation-container { max-width: 900px; margin: 0 auto; padding: 24px; }

.form-card, .list-card { background: white; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.08); padding: 24px; margin-bottom: 24px; }
.form-card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.form-card-header h2, .list-card h2 { margin: 0; font-size: 18px; }
.list-card h2 { margin: 0 0 20px 0; }

.remaining-badge {
  background: #e8f5e9; color: #2e7d32;
  padding: 8px 16px; border-radius: 20px; font-size: 14px;
}
.remaining-badge strong { font-size: 18px; margin: 0 2px; }

.form-row { display: flex; gap: 16px; }
.form-row .form-group { flex: 1; }
.form-group { margin-bottom: 16px; }
.form-group label { display: block; margin-bottom: 6px; font-weight: bold; font-size: 14px; }
.form-group input, .form-group select {
  width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 6px;
  box-sizing: border-box; font-size: 14px;
}
.form-group input:disabled { background: #f5f5f5; color: #999; cursor: not-allowed; }

.btn-submit { padding: 12px 40px; background: #4CAF50; color: white; border: none; border-radius: 6px; font-size: 15px; cursor: pointer; }
.btn-submit:hover { background: #45a049; }
.error-msg { color: red; font-size: 13px; margin-bottom: 10px; }

.vacation-table { width: 100%; border-collapse: collapse; }
.vacation-table th, .vacation-table td { padding: 12px; text-align: left; border-bottom: 1px solid #eee; font-size: 14px; }
.vacation-table th { background: #f5f5f5; font-weight: bold; }
.empty { text-align: center; color: #999; padding: 32px; }
.type-badge { background: #e3f2fd; color: #1565c0; padding: 3px 10px; border-radius: 12px; font-size: 13px; }
.btn-delete { padding: 5px 12px; background: #e53935; color: white; border: none; border-radius: 4px; cursor: pointer; font-size: 13px; }
.btn-delete:hover { background: #c62828; }
</style>
