import { createRouter, createWebHistory } from 'vue-router'
import Login from '../components/Login.vue'
import Home from '../components/Home.vue'
import AttendanceRecords from '../components/AttendanceRecords.vue'

const requireAuth = (_to, _from, next) => {
  if (localStorage.getItem('loggedIn')) next()
  else next('/login')
}

const requireAdmin = (_to, _from, next) => {
  const adminLevel = parseInt(localStorage.getItem('adminLevel') || '0')
  const isTeamLeader = localStorage.getItem('isTeamLeader') === 'true'
  if (localStorage.getItem('loggedIn') && (adminLevel >= 1 || isTeamLeader)) next()
  else next('/home')
}

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', component: Login },
  { path: '/home', component: Home, beforeEnter: requireAuth },
  { path: '/attendance', component: AttendanceRecords, beforeEnter: requireAuth },
  { path: '/vacation', component: () => import('../components/Vacation.vue'), beforeEnter: requireAuth },
  { path: '/hr', component: () => import('../components/HrManagement.vue'), beforeEnter: requireAdmin },
  { path: '/calendar', component: () => import('../components/Calendar.vue'), beforeEnter: requireAuth },
  { path: '/board-hub', component: () => import('../components/BoardHub.vue'), beforeEnter: requireAuth },
  { path: '/board', component: () => import('../components/board/Board.vue'), beforeEnter: requireAuth },
  { path: '/board/write', component: () => import('../components/board/BoardWrite.vue'), beforeEnter: requireAuth },
  { path: '/board/edit/:id', component: () => import('../components/board/BoardEdit.vue'), beforeEnter: requireAuth },
  { path: '/board/:id', component: () => import('../components/board/BoardDetail.vue'), beforeEnter: requireAuth },
  { path: '/notice', component: () => import('../components/notice/Notice.vue'), beforeEnter: requireAuth },
  { path: '/notice/write', component: () => import('../components/notice/NoticeWrite.vue'), beforeEnter: requireAuth },
  { path: '/notice/edit/:id', component: () => import('../components/notice/NoticeEdit.vue'), beforeEnter: requireAuth },
  { path: '/notice/:id', component: () => import('../components/notice/NoticeDetail.vue'), beforeEnter: requireAuth },
  { path: '/team-notice', component: () => import('../components/team-notice/TeamNotice.vue'), beforeEnter: requireAuth },
  { path: '/team-notice/write', component: () => import('../components/team-notice/TeamNoticeWrite.vue'), beforeEnter: requireAuth },
  { path: '/team-notice/edit/:id', component: () => import('../components/team-notice/TeamNoticeEdit.vue'), beforeEnter: requireAuth },
  { path: '/team-notice/:id', component: () => import('../components/team-notice/TeamNoticeDetail.vue'), beforeEnter: requireAuth },
  { path: '/archive', component: () => import('../components/archive/Archive.vue'), beforeEnter: requireAuth },
  { path: '/archive/write', component: () => import('../components/archive/ArchiveWrite.vue'), beforeEnter: requireAuth },
  { path: '/archive/edit/:id', component: () => import('../components/archive/ArchiveEdit.vue'), beforeEnter: requireAuth },
  { path: '/archive/:id', component: () => import('../components/archive/ArchiveDetail.vue'), beforeEnter: requireAuth },
  { path: '/team-archive', component: () => import('../components/team-archive/TeamArchive.vue'), beforeEnter: requireAuth },
  { path: '/team-archive/write', component: () => import('../components/team-archive/TeamArchiveWrite.vue'), beforeEnter: requireAuth },
  { path: '/team-archive/edit/:id', component: () => import('../components/team-archive/TeamArchiveEdit.vue'), beforeEnter: requireAuth },
  { path: '/team-archive/:id', component: () => import('../components/team-archive/TeamArchiveDetail.vue'), beforeEnter: requireAuth },
  { path: '/faq', component: () => import('../components/faq/Faq.vue'), beforeEnter: requireAuth },
  { path: '/faq/write', component: () => import('../components/faq/FaqWrite.vue'), beforeEnter: requireAdmin },
  { path: '/faq/edit/:id', component: () => import('../components/faq/FaqEdit.vue'), beforeEnter: requireAdmin }
]

export default createRouter({
  history: createWebHistory(),
  routes
})
