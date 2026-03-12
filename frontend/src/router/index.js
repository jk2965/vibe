import { createRouter, createWebHistory } from 'vue-router'
import Login from '../components/Login.vue'
import AttendanceRecords from '../components/AttendanceRecords.vue'

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', component: Login },
  {
    path: '/attendance',
    component: AttendanceRecords,
    beforeEnter: (to, from, next) => {
      if (localStorage.getItem('loggedIn')) next()
      else next('/login')
    }
  }
]

export default createRouter({
  history: createWebHistory(),
  routes
})
