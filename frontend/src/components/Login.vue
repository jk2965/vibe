<template>
  <div class="login-container">
    <div class="login-box">
      <h1>출퇴근 관리 시스템</h1>

      <div class="form-group">
        <label>아이디</label>
        <input v-model="userId" type="text" placeholder="아이디를 입력하세요" @keyup.enter="login">
      </div>
      <div class="form-group">
        <label>비밀번호</label>
        <input v-model="password" type="password" placeholder="비밀번호를 입력하세요" @keyup.enter="login" autocomplete="current-password">
      </div>

      <p v-if="errorMsg" class="error-msg">{{ errorMsg }}</p>

      <button @click="login" class="btn-login">로그인</button>
      <button @click="showRegister = true" class="btn-register">회원가입</button>
    </div>

    <!-- 회원가입 모달 -->
    <div v-if="showRegister" class="modal-overlay" @click.self="showRegister = false">
      <div class="modal-box">
        <h2>회원가입</h2>
        <div class="form-group">
          <label>아이디</label>
          <input v-model="regUserId" type="text" placeholder="로그인 아이디">
        </div>
        <div class="form-group">
          <label>이름</label>
          <input v-model="regUsername" type="text" placeholder="이름">
        </div>
        <div class="form-group">
          <label>비밀번호</label>
          <input v-model="regPassword" type="password" placeholder="비밀번호" autocomplete="new-password">
        </div>
        <p v-if="regMsg" class="error-msg">{{ regMsg }}</p>
        <button @click="register" class="btn-login">가입하기</button>
        <button @click="showRegister = false" class="btn-register">닫기</button>
      </div>
    </div>

    <!-- 회원가입 완료 팝업 -->
    <div v-if="showSuccess" class="modal-overlay">
      <div class="modal-box success-box">
        <p class="success-text">회원가입이 완료되었습니다.</p>
        <button @click="showSuccess = false" class="btn-register">닫기</button>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'Login',
  data() {
    return {
      userId: '',
      password: '',
      errorMsg: '',
      showRegister: false,
      regUserId: '',
      regUsername: '',
      regPassword: '',
      regMsg: '',
      showSuccess: false
    }
  },
  methods: {
    async login() {
      try {
        const res = await axios.post('http://localhost:8090/api/auth/login', {
          id: this.userId,
          password: this.password
        })
        if (res.data.success) {
          localStorage.setItem('loggedIn', 'true')
          localStorage.setItem('userId', this.userId)
          localStorage.setItem('username', res.data.username)
          this.$router.push('/attendance')
        }
      } catch (e) {
        this.errorMsg = e.response?.data?.message || '로그인 실패'
      }
    },
    async register() {
      try {
        const res = await axios.post('http://localhost:8090/api/auth/register', {
          id: this.regUserId,
          username: this.regUsername,
          password: this.regPassword
        })
        if (res.data.success) {
          this.showRegister = false
          this.showSuccess = true
          this.regUserId = ''
          this.regUsername = ''
          this.regPassword = ''
          this.regMsg = ''
        }
      } catch (e) {
        this.regMsg = e.response?.data?.message || '회원가입 실패'
      }
    }
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: #f0f2f5;
}
.login-box {
  background: white;
  padding: 40px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
  width: 360px;
}
h1 { text-align: center; margin-bottom: 30px; font-size: 22px; }
.form-group { margin-bottom: 16px; }
.form-group label { display: block; margin-bottom: 6px; font-weight: bold; }
.form-group input { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 6px; box-sizing: border-box; font-size: 14px; }
.btn-login { width: 100%; padding: 12px; background: #4CAF50; color: white; border: none; border-radius: 6px; font-size: 16px; cursor: pointer; margin-bottom: 8px; }
.btn-login:hover { background: #45a049; }
.btn-register { width: 100%; padding: 10px; background: white; color: #555; border: 1px solid #ddd; border-radius: 6px; font-size: 14px; cursor: pointer; }
.error-msg { color: red; font-size: 13px; margin-bottom: 10px; }
.success-msg { color: green; font-size: 13px; margin-bottom: 10px; }
.modal-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); display: flex; justify-content: center; align-items: center; }
.modal-box { background: white; padding: 30px; border-radius: 12px; width: 320px; }
.modal-box h2 { margin-bottom: 20px; }
.success-box { text-align: center; padding: 40px 30px; }
.success-text { font-size: 16px; margin-bottom: 24px; }
</style>
