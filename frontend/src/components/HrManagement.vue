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
// 공통 페이지 헤더 컴포넌트 (PageHeader.vue)
import PageHeader from './common/PageHeader.vue'

export default {
  name: 'HrManagement',
  components: { PageHeader },
  data() {
    return {
      // 전체 직원 목록 (GET /api/admin/users 응답)
      users: [],
      // 현재 인라인 편집 중인 직원 ID (null이면 편집 없음)
      editId: null,
      // 편집 폼 데이터 (직급, 잔여 연차, 관리자 레벨, 팀, 팀장 여부)
      editForm: { position: '', remainingVacation: 0, adminLevel: 0, team: '', isTeamLeader: false },
      // 현재 로그인한 관리자의 사용자 ID
      currentUserId: localStorage.getItem('userId'),
      // 현재 로그인한 관리자의 레벨 (0: 일반, 1: 관리자, 2: 슈퍼 관리자)
      currentAdminLevel: parseInt(localStorage.getItem('adminLevel') || '0'),
      // 현재 로그인한 사용자가 팀장인지 여부
      isCurrentTeamLeader: localStorage.getItem('isTeamLeader') === 'true',
      // 팀장 전용 모드 여부: 관리자 권한 없는 팀장만 해당 팀 인원만 조회/편집 가능
      isTeamLeaderMode: localStorage.getItem('isTeamLeader') === 'true' && parseInt(localStorage.getItem('adminLevel') || '0') < 1,
      // 현재 로그인한 팀장의 소속 팀명 (팀 편집 범위 제한에 사용)
      currentTeam: localStorage.getItem('team') || '',
      // 오류 메시지
      errorMsg: '',
      // 저장 성공 메시지
      successMsg: ''
    }
  },
  // 컴포넌트 마운트 시 직원 목록 조회
  mounted() {
    this.fetchUsers()
  },
  methods: {
    // GET /api/admin/users 호출 → AdminController.java
    // requesterId로 권한 확인 후 전체 또는 팀 범위의 직원 목록 반환
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
    // 수정 버튼 클릭: 해당 직원의 현재 값을 editForm에 복사하고 인라인 편집 시작
    startEdit(user) {
      this.editId = user.id
      this.editForm.position = user.position
      this.editForm.remainingVacation = user.remainingVacation
      this.editForm.adminLevel = user.isAdmin || 0
      this.editForm.team = user.team || ''
      // isTeamLeader DB 값(1/0)을 boolean으로 변환
      this.editForm.isTeamLeader = user.isTeamLeader === 1
      this.successMsg = ''
      this.errorMsg = ''
    },
    // 취소 버튼 클릭: 편집 모드 종료
    cancelEdit() {
      this.editId = null
    },
    // 팀 소속 편집 권한 체크
    // 슈퍼 관리자는 모든 팀, 팀장은 본인 팀 소속 일반 직원만 편집 가능
    canEditTeam(user) {
      if (this.currentAdminLevel >= 2) return true
      // 다른 팀장의 팀은 변경 불가
      if (user.isTeamLeader === 1) return false
      // 팀장은 같은 팀 소속 직원의 팀만 편집 가능
      if (this.isCurrentTeamLeader && this.currentTeam && user.team === this.currentTeam) return true
      return false
    },
    // 관리자 레벨에 따른 배지 CSS 클래스 반환
    adminBadgeClass(level) {
      if (level === 2) return 'badge-super'
      if (level === 1) return 'badge-admin'
      return 'badge-normal'
    },
    // 관리자 레벨에 따른 배지 텍스트 반환
    adminBadgeText(level) {
      if (level === 2) return '슈퍼 관리자'
      if (level === 1) return '관리자'
      return '일반'
    },
    // PUT /api/admin/users/:id 호출 → AdminController.java
    // 권한에 따라 payload 구성 후 직원 정보 수정 (직급, 잔여연차, 팀, 팀장, 관리자레벨)
    async saveUser(id) {
      this.errorMsg = ''
      // 관리자 이상만 잔여 연차 유효성 검사 수행
      if (this.currentAdminLevel >= 1) {
        const v = this.editForm.remainingVacation
        if (v === '' || v === null || isNaN(v)) { this.errorMsg = '잔여 연차를 입력하세요.'; return }
        if (v < 0) { this.errorMsg = '잔여 연차는 0일 이상이어야 합니다.'; return }
        if (v > 365) { this.errorMsg = '잔여 연차는 365일을 초과할 수 없습니다.'; return }
        // 0.5일 단위 검증 (v*2가 정수가 아니면 0.5 배수가 아님)
        if ((v * 2) % 1 !== 0) { this.errorMsg = '잔여 연차는 0.5일 단위로 입력하세요.'; return }
      }
      try {
        const targetUser = this.users.find(u => u.id === id)
        // 권한에 따라 payload에 포함할 필드 결정 (undefined인 필드는 서버에서 무시)
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
        // 저장 후 편집 모드 종료, 성공 메시지 표시, 직원 목록 재조회
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
