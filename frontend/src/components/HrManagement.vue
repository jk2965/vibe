<template>
  <div class="hr-container">
    <PageHeader :title="isTeamLeaderMode ? '팀 인원 관리' : '인사 관리'" />

    <div class="list-card">
      <div class="card-header">
        <h2>{{ isTeamLeaderMode ? currentTeam + ' 인원 목록' : '전체 직원 목록' }}</h2>
        <span class="count">총 {{ users.length }}명</span>
      </div>
      <table class="hr-table">
        <thead>
          <tr>
            <th>아이디</th>
            <th>이름</th>
            <th>직급</th>
            <th>소속</th>
            <th v-if="currentAdminLevel >= 1">잔여 연차 (일)</th>
            <th v-if="currentAdminLevel >= 1 || isTeamLeaderMode">팀장</th>
            <th v-if="currentAdminLevel >= 1">관리자</th>
            <th>관리</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="users.length === 0">
            <td :colspan="currentAdminLevel >= 1 ? 8 : (isTeamLeaderMode ? 6 : 5)" class="empty">직원이 없습니다.</td>
          </tr>
          <tr v-for="u in users" :key="u.id">
            <td>{{ u.id }}</td>
            <td>{{ u.username }}</td>
            <!-- 직급 (팀장은 편집 불가) -->
            <td>
              <template v-if="editId === u.id && !isTeamLeaderMode">
                <select v-model="editForm.position" class="edit-select"
                        :class="editForm.adminLevel >= 1 ? 'admin-select' : ''">
                  <option value="사원">사원</option>
                  <option value="주임">주임</option>
                  <option value="대리">대리</option>
                  <option value="과장">과장</option>
                  <option value="차장">차장</option>
                  <option value="부장">부장</option>
                  <template v-if="editForm.adminLevel >= 1">
                    <option value="이사">이사</option>
                    <option value="상무">상무</option>
                    <option value="전무">전무</option>
                    <option value="부사장">부사장</option>
                    <option value="사장">사장</option>
                  </template>
                </select>
              </template>
              <span v-else>{{ u.position }}</span>
            </td>
            <!-- 소속 (슈퍼 관리자 또는 팀장이 자기 팀 인원만 편집 가능) -->
            <td>
              <select v-if="editId === u.id && canEditTeam(u)"
                      v-model="editForm.team" class="edit-select">
                <option value="">미지정</option>
                <template v-if="isCurrentTeamLeader && currentAdminLevel < 2">
                  <option :value="currentTeam">{{ currentTeam }}</option>
                </template>
                <template v-else>
                  <option value="1팀">1팀</option>
                  <option value="2팀">2팀</option>
                </template>
              </select>
              <span v-else>{{ u.team || '-' }}</span>
            </td>
            <!-- 잔여 연차 (관리자 이상만) -->
            <td v-if="currentAdminLevel >= 1">
              <input v-if="editId === u.id" v-model.number="editForm.remainingVacation"
                     type="number" step="0.5" min="0" max="365" class="edit-input" onkeydown="return false;">
              <span v-else>{{ u.remainingVacation }}</span>
            </td>
            <!-- 팀장 (슈퍼 관리자만 편집, 팀장 모드에서는 읽기 전용 표시) -->
            <td v-if="currentAdminLevel >= 1 || isTeamLeaderMode">
              <template v-if="editId === u.id && currentAdminLevel >= 2 && u.id !== currentUserId">
                <label class="toggle-label">
                  <input type="checkbox" v-model="editForm.isTeamLeader">
                  <span>{{ editForm.isTeamLeader ? '팀장' : '일반' }}</span>
                </label>
              </template>
              <span v-else :class="u.isTeamLeader === 1 ? 'badge-leader' : 'badge-normal'">
                {{ u.isTeamLeader === 1 ? '팀장' : '-' }}
              </span>
            </td>
            <!-- 관리자 권한 (슈퍼 관리자만 편집) -->
            <td v-if="currentAdminLevel >= 1">
              <template v-if="editId === u.id && currentAdminLevel >= 2 && u.id !== currentUserId">
                <select v-model.number="editForm.adminLevel" class="edit-select">
                  <option :value="0">일반</option>
                  <option :value="1">관리자</option>
                </select>
              </template>
              <span v-else :class="adminBadgeClass(u.isAdmin)">
                {{ adminBadgeText(u.isAdmin) }}
              </span>
            </td>
            <td>
              <template v-if="editId === u.id">
                <button @click="saveUser(u.id)" class="btn-save">저장</button>
                <button @click="cancelEdit" class="btn-cancel">취소</button>
              </template>
              <button v-else-if="u.isAdmin !== 2 || currentAdminLevel >= 2" @click="startEdit(u)" class="btn-edit">수정</button>
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
import PageHeader from './common/PageHeader.vue'

export default {
  name: 'HrManagement',
  components: { PageHeader },
  data() {
    return {
      users: [],
      editId: null,
      editForm: { position: '', remainingVacation: 0, adminLevel: 0, team: '', isTeamLeader: false },
      currentUserId: localStorage.getItem('userId'),
      currentAdminLevel: parseInt(localStorage.getItem('adminLevel') || '0'),
      isCurrentTeamLeader: localStorage.getItem('isTeamLeader') === 'true',
      isTeamLeaderMode: localStorage.getItem('isTeamLeader') === 'true' && parseInt(localStorage.getItem('adminLevel') || '0') < 1,
      currentTeam: localStorage.getItem('team') || '',
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
      this.editForm.position = user.position
      this.editForm.remainingVacation = user.remainingVacation
      this.editForm.adminLevel = user.isAdmin || 0
      this.editForm.team = user.team || ''
      this.editForm.isTeamLeader = user.isTeamLeader === 1
      this.successMsg = ''
      this.errorMsg = ''
    },
    cancelEdit() {
      this.editId = null
    },
    canEditTeam(user) {
      if (this.currentAdminLevel >= 2) return true
      if (user.isTeamLeader === 1) return false
      if (this.isCurrentTeamLeader && this.currentTeam && user.team === this.currentTeam) return true
      return false
    },
    adminBadgeClass(level) {
      if (level === 2) return 'badge-super'
      if (level === 1) return 'badge-admin'
      return 'badge-normal'
    },
    adminBadgeText(level) {
      if (level === 2) return '슈퍼 관리자'
      if (level === 1) return '관리자'
      return '일반'
    },
    async saveUser(id) {
      this.errorMsg = ''
      if (this.currentAdminLevel >= 1) {
        const v = this.editForm.remainingVacation
        if (v === '' || v === null || isNaN(v)) { this.errorMsg = '잔여 연차를 입력하세요.'; return }
        if (v < 0) { this.errorMsg = '잔여 연차는 0일 이상이어야 합니다.'; return }
        if (v > 365) { this.errorMsg = '잔여 연차는 365일을 초과할 수 없습니다.'; return }
        if ((v * 2) % 1 !== 0) { this.errorMsg = '잔여 연차는 0.5일 단위로 입력하세요.'; return }
      }
      try {
        const targetUser = this.users.find(u => u.id === id)
        const payload = {
          position: this.currentAdminLevel >= 1 ? this.editForm.position : undefined,
          remainingVacation: this.currentAdminLevel >= 1 ? this.editForm.remainingVacation : undefined,
          team: this.canEditTeam(targetUser) ? this.editForm.team : undefined,
          isTeamLeader: this.currentAdminLevel >= 2 ? this.editForm.isTeamLeader : undefined,
          adminLevel: this.currentAdminLevel >= 2 ? this.editForm.adminLevel : undefined
        }
        await axios.put(`http://localhost:8090/api/admin/users/${id}`, payload, {
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
.hr-container { max-width: 1100px; margin: 0 auto; padding: 24px; }

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
.toggle-label { display: flex; align-items: center; gap: 6px; cursor: pointer; font-size: 14px; }

.btn-edit { padding: 5px 14px; background: #607d8b; color: white; border: none; border-radius: 4px; cursor: pointer; font-size: 13px; }
.btn-edit:hover { background: #455a64; }
.btn-save { padding: 5px 14px; background: #4CAF50; color: white; border: none; border-radius: 4px; cursor: pointer; font-size: 13px; margin-right: 6px; }
.btn-save:hover { background: #45a049; }
.btn-cancel { padding: 5px 14px; background: #9e9e9e; color: white; border: none; border-radius: 4px; cursor: pointer; font-size: 13px; }
.btn-cancel:hover { background: #757575; }
.error-msg { color: red; font-size: 13px; margin-top: 12px; }
.success-msg { color: #2e7d32; font-size: 13px; margin-top: 12px; }

.badge-super { background: #ede7f6; color: #4527a0; padding: 3px 10px; border-radius: 12px; font-size: 12px; font-weight: bold; }
.badge-admin { background: #fff3e0; color: #e65100; padding: 3px 10px; border-radius: 12px; font-size: 12px; font-weight: bold; }
.badge-normal { background: #f5f5f5; color: #757575; padding: 3px 10px; border-radius: 12px; font-size: 12px; }
.badge-leader { background: #e8f5e9; color: #2e7d32; padding: 3px 10px; border-radius: 12px; font-size: 12px; font-weight: bold; }
</style>
