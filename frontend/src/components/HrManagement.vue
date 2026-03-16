<template>
  <div class="hr-container">
    <PageHeader title="인사 관리" />

    <div class="list-card">
      <div class="card-header">
        <h2>전체 직원 목록</h2>
        <span class="count">총 {{ users.length }}명</span>
      </div>
      <table class="hr-table">
        <thead>
          <tr>
            <th>아이디</th>
            <th>이름</th>
            <th>직급</th>
            <th>잔여 연차 (일)</th>
            <th>관리</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="users.length === 0">
            <td colspan="5" class="empty">직원이 없습니다.</td>
          </tr>
          <tr v-for="u in users" :key="u.id">
            <td>{{ u.id }}</td>
            <td>{{ u.username }}</td>
            <td>
              <!-- 관리자 계정 수정 시: 임원급 포함 -->
              <select v-if="editId === u.id && u.isAdmin === 1" v-model="editForm.position" class="edit-select">
                <option value="사원">사원</option>
                <option value="주임">주임</option>
                <option value="대리">대리</option>
                <option value="과장">과장</option>
                <option value="차장">차장</option>
                <option value="부장">부장</option>
                <option value="이사">이사</option>
                <option value="상무">상무</option>
                <option value="전무">전무</option>
                <option value="부사장">부사장</option>
                <option value="사장">사장</option>
              </select>
              <!-- 일반 계정 수정 시: 기본 직급 -->
              <select v-else-if="editId === u.id" v-model="editForm.position" class="edit-select">
                <option value="사원">사원</option>
                <option value="주임">주임</option>
                <option value="대리">대리</option>
                <option value="과장">과장</option>
                <option value="차장">차장</option>
                <option value="부장">부장</option>
              </select>
              <span v-else>{{ u.position }}</span>
            </td>
            <td>
              <input v-if="editId === u.id" v-model.number="editForm.remainingVacation"
                     type="number" step="0.5" min="0" max="365" class="edit-input" onkeydown="return false;">
              <span v-else>{{ u.remainingVacation }}</span>
            </td>
            <td>
              <template v-if="editId === u.id">
                <button @click="saveUser(u.id)" class="btn-save">저장</button>
                <button @click="cancelEdit" class="btn-cancel">취소</button>
              </template>
              <button v-else @click="startEdit(u)" class="btn-edit">수정</button>
            </td>
          </tr>
        </tbody>
      </table>
      <p v-if="errorMsg" class="error-msg">{{ errorMsg }}</p>
      <p v-if="successMsg" class="success-msg">{{ successMsg }}</p>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import PageHeader from './PageHeader.vue'

export default {
  name: 'HrManagement',
  components: { PageHeader },
  data() {
    return {
      users: [],
      editId: null,
      editForm: { position: '', remainingVacation: 0 },
      errorMsg: '',
      successMsg: ''
    }
  },
  mounted() {
    this.fetchUsers()
  },
  methods: {
    async fetchUsers() {
      try {
        const res = await axios.get('http://localhost:8090/api/admin/users', {
          params: { requesterId: localStorage.getItem('userId') }
        })
        this.users = res.data
      } catch (e) {
        this.errorMsg = e.response?.data?.message || '직원 목록 조회 실패'
      }
    },
    startEdit(user) {
      this.editId = user.id
      this.is_admin = user.is_admin
      this.editForm.position = user.position
      this.editForm.remainingVacation = user.remainingVacation
      this.successMsg = ''
      this.errorMsg = ''
    },
    cancelEdit() {
      this.editId = null
    },
    async saveUser(id) {
      const v = this.editForm.remainingVacation
      if (v === '' || v === null || isNaN(v)) {
        this.errorMsg = '잔여 연차를 입력하세요.'
        return
      }
      if (v < 0) {
        this.errorMsg = '잔여 연차는 0일 이상이어야 합니다.'
        return
      }
      if (v > 365) {
        this.errorMsg = '잔여 연차는 365일을 초과할 수 없습니다.'
        return
      }
      if ((v * 2) % 1 !== 0) {
        this.errorMsg = '잔여 연차는 0.5일 단위로 입력하세요.'
        return
      }
      try {
        await axios.put(`http://localhost:8090/api/admin/users/${id}`, {
          position: this.editForm.position,
          remainingVacation: this.editForm.remainingVacation
        }, {
          params: { requesterId: localStorage.getItem('userId') }
        })
        this.editId = null
        this.successMsg = '저장되었습니다.'
        await this.fetchUsers()
      } catch (e) {
        this.errorMsg = e.response?.data?.message || '저장 실패'
      }
    }
  }
}
</script>

<style scoped>
.hr-container { max-width: 900px; margin: 0 auto; padding: 24px; }

.list-card { background: white; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.08); padding: 24px; }
.card-header { display: flex; align-items: center; gap: 12px; margin-bottom: 20px; }
.card-header h2 { margin: 0; font-size: 18px; }
.count { color: #888; font-size: 14px; }

.hr-table { width: 100%; border-collapse: collapse; }
.hr-table th, .hr-table td { padding: 12px; text-align: left; border-bottom: 1px solid #eee; font-size: 14px; }
.hr-table th { background: #f5f5f5; font-weight: bold; }
.empty { text-align: center; color: #999; padding: 32px; }

.edit-select { padding: 6px; border: 1px solid #ddd; border-radius: 4px; font-size: 14px; }
.edit-input { width: 80px; padding: 6px; border: 1px solid #ddd; border-radius: 4px; font-size: 14px; }

.btn-edit { padding: 5px 14px; background: #607d8b; color: white; border: none; border-radius: 4px; cursor: pointer; font-size: 13px; }
.btn-edit:hover { background: #455a64; }
.btn-save { padding: 5px 14px; background: #4CAF50; color: white; border: none; border-radius: 4px; cursor: pointer; font-size: 13px; margin-right: 6px; }
.btn-save:hover { background: #45a049; }
.btn-cancel { padding: 5px 14px; background: #9e9e9e; color: white; border: none; border-radius: 4px; cursor: pointer; font-size: 13px; }
.btn-cancel:hover { background: #757575; }
.error-msg { color: red; font-size: 13px; margin-top: 12px; }
.success-msg { color: #2e7d32; font-size: 13px; margin-top: 12px; }
</style>
