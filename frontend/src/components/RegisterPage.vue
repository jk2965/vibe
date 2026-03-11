<template>
  <div class="register-card">
    <!-- 성공 화면 -->
    <div v-if="isSuccess" class="success-screen">
      <div class="success-icon">✓</div>
      <h2>회원가입 완료!</h2>
      <p>{{ successData.name }}님, 환영합니다!</p>
      <p class="success-email">{{ successData.email }}</p>
      <button class="btn-primary" @click="resetForm">다시 가입하기</button>
    </div>

    <!-- 회원가입 폼 -->
    <template v-else>
      <div class="card-header">
        <h1>회원가입</h1>
        <p>계정을 만들어 서비스를 이용하세요</p>
      </div>

      <form @submit.prevent="handleSubmit" class="register-form" novalidate>
        <!-- 이름 -->
        <div class="form-group" :class="{ error: errors.name, success: touched.name && !errors.name }">
          <label for="name">이름</label>
          <input
            id="name"
            v-model="form.name"
            type="text"
            placeholder="이름을 입력하세요"
            @blur="validateField('name')"
            @input="clearError('name')"
          />
          <span v-if="errors.name" class="error-msg">{{ errors.name }}</span>
        </div>

        <!-- 이메일 -->
        <div class="form-group" :class="{ error: errors.email, success: touched.email && !errors.email && !emailChecking }">
          <label for="email">이메일</label>
          <div class="input-with-btn">
            <input
              id="email"
              v-model="form.email"
              type="email"
              placeholder="이메일을 입력하세요"
              @blur="validateField('email')"
              @input="onEmailInput"
            />
            <button
              type="button"
              class="btn-check"
              :disabled="!form.email || emailChecking || !!errors.email"
              @click="checkEmail"
            >
              {{ emailChecking ? '확인 중...' : '중복 확인' }}
            </button>
          </div>
          <span v-if="errors.email" class="error-msg">{{ errors.email }}</span>
          <span v-if="emailAvailable === true" class="success-msg">사용 가능한 이메일입니다</span>
          <span v-if="emailAvailable === false && !errors.email" class="error-msg">이미 사용 중인 이메일입니다</span>
        </div>

        <!-- 비밀번호 -->
        <div class="form-group" :class="{ error: errors.password, success: touched.password && !errors.password }">
          <label for="password">비밀번호</label>
          <div class="input-with-icon">
            <input
              id="password"
              v-model="form.password"
              :type="showPassword ? 'text' : 'password'"
              placeholder="비밀번호를 입력하세요 (최소 8자)"
              @blur="validateField('password')"
              @input="clearError('password')"
            />
            <button type="button" class="toggle-pw" @click="showPassword = !showPassword">
              {{ showPassword ? '숨기기' : '보기' }}
            </button>
          </div>
          <div v-if="form.password" class="strength-bar">
            <div
              class="strength-fill"
              :class="passwordStrength.class"
              :style="{ width: passwordStrength.width }"
            ></div>
          </div>
          <span v-if="form.password" class="strength-label" :class="passwordStrength.class">
            {{ passwordStrength.label }}
          </span>
          <span v-if="errors.password" class="error-msg">{{ errors.password }}</span>
        </div>

        <!-- 비밀번호 확인 -->
        <div class="form-group" :class="{ error: errors.confirmPassword, success: touched.confirmPassword && !errors.confirmPassword }">
          <label for="confirmPassword">비밀번호 확인</label>
          <input
            id="confirmPassword"
            v-model="form.confirmPassword"
            :type="showPassword ? 'text' : 'password'"
            placeholder="비밀번호를 다시 입력하세요"
            @blur="validateField('confirmPassword')"
            @input="clearError('confirmPassword')"
          />
          <span v-if="errors.confirmPassword" class="error-msg">{{ errors.confirmPassword }}</span>
        </div>

        <!-- 전화번호 -->
        <div class="form-group" :class="{ error: errors.phone, success: touched.phone && !errors.phone }">
          <label for="phone">전화번호</label>
          <input
            id="phone"
            v-model="form.phone"
            type="tel"
            placeholder="숫자만 입력하세요 (예: 01012345678)"
            @blur="validateField('phone')"
            @input="onPhoneInput"
            maxlength="11"
          />
          <span v-if="errors.phone" class="error-msg">{{ errors.phone }}</span>
        </div>

        <!-- 전체 에러 -->
        <div v-if="serverError" class="server-error">
          {{ serverError }}
        </div>

        <button
          type="submit"
          class="btn-primary btn-submit"
          :disabled="isSubmitting"
        >
          {{ isSubmitting ? '처리 중...' : '회원가입' }}
        </button>
      </form>

      <p class="login-link">
        이미 계정이 있으신가요? <a href="#">로그인</a>
      </p>
    </template>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'RegisterPage',

  data() {
    return {
      form: {
        name: '',
        email: '',
        password: '',
        confirmPassword: '',
        phone: ''
      },
      errors: {},
      touched: {},
      showPassword: false,
      emailChecking: false,
      emailAvailable: null,
      isSubmitting: false,
      serverError: '',
      isSuccess: false,
      successData: null
    }
  },

  computed: {
    passwordStrength() {
      const pw = this.form.password
      if (!pw) return { class: '', width: '0%', label: '' }

      let score = 0
      if (pw.length >= 8) score++
      if (pw.length >= 12) score++
      if (/[A-Z]/.test(pw)) score++
      if (/[0-9]/.test(pw)) score++
      if (/[^A-Za-z0-9]/.test(pw)) score++

      if (score <= 1) return { class: 'weak', width: '25%', label: '매우 약함' }
      if (score === 2) return { class: 'fair', width: '50%', label: '보통' }
      if (score === 3) return { class: 'good', width: '75%', label: '강함' }
      return { class: 'strong', width: '100%', label: '매우 강함' }
    }
  },

  methods: {
    clearError(field) {
      delete this.errors[field]
    },

    onEmailInput() {
      this.emailAvailable = null
      this.clearError('email')
    },

    onPhoneInput() {
      this.form.phone = this.form.phone.replace(/\D/g, '')
      this.clearError('phone')
    },

    validateField(field) {
      this.touched[field] = true
      const val = this.form[field]

      if (field === 'name') {
        if (!val) this.errors.name = '이름을 입력해주세요'
        else if (val.length < 2) this.errors.name = '이름은 2자 이상이어야 합니다'
        else delete this.errors.name
      }

      if (field === 'email') {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
        if (!val) this.errors.email = '이메일을 입력해주세요'
        else if (!emailRegex.test(val)) this.errors.email = '올바른 이메일 형식이 아닙니다'
        else delete this.errors.email
      }

      if (field === 'password') {
        if (!val) this.errors.password = '비밀번호를 입력해주세요'
        else if (val.length < 8) this.errors.password = '비밀번호는 최소 8자 이상이어야 합니다'
        else delete this.errors.password
      }

      if (field === 'confirmPassword') {
        if (!val) this.errors.confirmPassword = '비밀번호를 다시 입력해주세요'
        else if (val !== this.form.password) this.errors.confirmPassword = '비밀번호가 일치하지 않습니다'
        else delete this.errors.confirmPassword
      }

      if (field === 'phone') {
        const phoneRegex = /^\d{10,11}$/
        if (!val) this.errors.phone = '전화번호를 입력해주세요'
        else if (!phoneRegex.test(val)) this.errors.phone = '전화번호는 10~11자리 숫자여야 합니다'
        else delete this.errors.phone
      }
    },

    async checkEmail() {
      this.validateField('email')
      if (this.errors.email) return

      this.emailChecking = true
      try {
        const res = await axios.get('/api/users/check-email', {
          params: { email: this.form.email }
        })
        this.emailAvailable = res.data.available
        if (!res.data.available) {
          this.errors.email = '이미 사용 중인 이메일입니다'
        }
      } catch {
        this.serverError = '이메일 확인 중 오류가 발생했습니다'
      } finally {
        this.emailChecking = false
      }
    },

    validateAll() {
      ['name', 'email', 'password', 'confirmPassword', 'phone'].forEach(f => this.validateField(f))
      return Object.keys(this.errors).length === 0
    },

    async handleSubmit() {
      this.serverError = ''
      if (!this.validateAll()) return

      if (this.emailAvailable === null) {
        this.serverError = '이메일 중복 확인을 해주세요'
        return
      }
      if (this.emailAvailable === false) {
        this.errors.email = '이미 사용 중인 이메일입니다'
        return
      }

      this.isSubmitting = true
      try {
        const res = await axios.post('/api/users/register', {
          name: this.form.name,
          email: this.form.email,
          password: this.form.password,
          phone: this.form.phone
        })
        this.successData = res.data
        this.isSuccess = true
      } catch (err) {
        if (err.response?.data?.fieldErrors) {
          const fieldErrors = err.response.data.fieldErrors
          Object.keys(fieldErrors).forEach(key => {
            this.errors[key] = fieldErrors[key]
          })
        } else if (err.response?.data?.error) {
          this.serverError = err.response.data.error
        } else {
          this.serverError = '회원가입 중 오류가 발생했습니다. 다시 시도해주세요.'
        }
      } finally {
        this.isSubmitting = false
      }
    },

    resetForm() {
      this.form = { name: '', email: '', password: '', confirmPassword: '', phone: '' }
      this.errors = {}
      this.touched = {}
      this.emailAvailable = null
      this.serverError = ''
      this.isSuccess = false
      this.successData = null
    }
  }
}
</script>

<style scoped>
.register-card {
  background: white;
  border-radius: 16px;
  padding: 40px;
  width: 100%;
  max-width: 480px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

.card-header {
  text-align: center;
  margin-bottom: 32px;
}

.card-header h1 {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a2e;
  margin-bottom: 8px;
}

.card-header p {
  color: #666;
  font-size: 14px;
}

.form-group {
  margin-bottom: 20px;
}

label {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 6px;
}

input {
  width: 100%;
  padding: 12px 16px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 15px;
  transition: border-color 0.2s, box-shadow 0.2s;
  outline: none;
  color: #333;
}

input:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.15);
}

.form-group.error input {
  border-color: #e53e3e;
}

.form-group.success input {
  border-color: #38a169;
}

.input-with-btn {
  display: flex;
  gap: 8px;
}

.input-with-btn input {
  flex: 1;
}

.input-with-icon {
  position: relative;
}

.input-with-icon input {
  padding-right: 64px;
}

.toggle-pw {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: #667eea;
  cursor: pointer;
  font-size: 13px;
  font-weight: 600;
  padding: 4px;
}

.btn-check {
  padding: 12px 16px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  white-space: nowrap;
  transition: background 0.2s;
}

.btn-check:hover:not(:disabled) {
  background: #5a6fd6;
}

.btn-check:disabled {
  background: #b0b8d4;
  cursor: not-allowed;
}

.error-msg {
  display: block;
  color: #e53e3e;
  font-size: 12px;
  margin-top: 4px;
}

.success-msg {
  display: block;
  color: #38a169;
  font-size: 12px;
  margin-top: 4px;
}

.strength-bar {
  height: 4px;
  background: #e0e0e0;
  border-radius: 2px;
  margin-top: 6px;
  overflow: hidden;
}

.strength-fill {
  height: 100%;
  border-radius: 2px;
  transition: width 0.3s, background 0.3s;
}

.strength-label {
  font-size: 12px;
  margin-top: 2px;
  display: block;
}

.weak { background: #e53e3e; color: #e53e3e; }
.fair { background: #ed8936; color: #ed8936; }
.good { background: #ecc94b; color: #b7791f; }
.strong { background: #38a169; color: #38a169; }

.server-error {
  background: #fff5f5;
  border: 1px solid #fed7d7;
  color: #c53030;
  padding: 12px 16px;
  border-radius: 8px;
  font-size: 14px;
  margin-bottom: 16px;
}

.btn-primary {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
  transition: opacity 0.2s, transform 0.1s;
}

.btn-primary:hover:not(:disabled) {
  opacity: 0.92;
  transform: translateY(-1px);
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.btn-submit {
  margin-top: 8px;
}

.login-link {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
  color: #666;
}

.login-link a {
  color: #667eea;
  font-weight: 600;
  text-decoration: none;
}

.login-link a:hover {
  text-decoration: underline;
}

/* 성공 화면 */
.success-screen {
  text-align: center;
  padding: 20px 0;
}

.success-icon {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #38a169, #48bb78);
  color: white;
  font-size: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 24px;
}

.success-screen h2 {
  font-size: 24px;
  color: #1a1a2e;
  margin-bottom: 8px;
}

.success-screen p {
  color: #555;
  font-size: 16px;
  margin-bottom: 4px;
}

.success-email {
  color: #667eea;
  font-weight: 600;
  margin-bottom: 28px !important;
}
</style>
