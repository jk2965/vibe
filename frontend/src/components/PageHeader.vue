<template>
  <div class="page-header">
    <h1>{{ title }}</h1>
    <div class="header-actions">
      <span class="username">{{ username }} {{ position }}님</span>
      <button @click="$router.push('/home')" class="btn-home">홈으로</button>
      <button @click="logout" class="btn-logout">로그아웃</button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'PageHeader',
  props: {
    title: {
      type: String,
      required: true
    }
  },
  computed: {
    username() {
      return localStorage.getItem('username') || ''
    },
    position() {
      return localStorage.getItem('position') || ''
    }
  },
  methods: {
    logout() {
      localStorage.removeItem('loggedIn')
      localStorage.removeItem('userId')
      localStorage.removeItem('username')
      localStorage.removeItem('position')
      this.$router.push('/login')
    }
  }
}
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}
.page-header h1 { margin: 0; }
.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 15px;
}
.username { color: #555; }
.btn-home {
  padding: 8px 16px;
  background: #607d8b;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
}
.btn-home:hover { background: #455a64; }
.btn-logout {
  padding: 8px 16px;
  background: #e53935;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
}
.btn-logout:hover { background: #c62828; }
</style>
