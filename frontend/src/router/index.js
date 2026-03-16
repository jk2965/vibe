import { createRouter, createWebHistory } from 'vue-router'
import Login from '../components/Login.vue'
import Home from '../components/Home.vue'
import AttendanceRecords from '../components/AttendanceRecords.vue'

const requireAuth = (_to, _from, next) => {
  if (localStorage.getItem('loggedIn')) next()
  else next('/login')
}

const requireAdmin = (_to, _from, next) => {
  if (localStorage.getItem('loggedIn') && localStorage.getItem('isAdmin') === 'true') next()
  else next('/home')
}

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', component: Login },
  { path: '/home', component: Home, beforeEnter: requireAuth },
  { path: '/attendance', component: AttendanceRecords, beforeEnter: requireAuth },
  { path: '/vacation', component: () => import('../components/Vacation.vue'), beforeEnter: requireAuth },
  { path: '/hr', component: () => import('../components/HrManagement.vue'), beforeEnter: requireAdmin },
  { path: '/board', component: () => import('../components/Board.vue'), beforeEnter: requireAuth },
  { path: '/board/write', component: () => import('../components/BoardWrite.vue'), beforeEnter: requireAuth },
  { path: '/board/edit/:id', component: () => import('../components/BoardEdit.vue'), beforeEnter: requireAuth },
  { path: '/board/:id', component: () => import('../components/BoardDetail.vue'), beforeEnter: requireAuth }
]

export default createRouter({
  history: createWebHistory(),
  routes
})
