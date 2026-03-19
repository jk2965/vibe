// Vue Router의 라우터 생성 함수와 HTML5 히스토리 모드 임포트
import { createRouter, createWebHistory } from 'vue-router'
// 자주 사용되는 컴포넌트는 정적 임포트 (Login, Home, AttendanceRecords)
import Login from '../components/Login.vue'
import Home from '../components/Home.vue'
import AttendanceRecords from '../components/AttendanceRecords.vue'

// 네비게이션 가드: 로그인 여부를 localStorage에서 확인하여 미로그인 시 /login으로 리다이렉트
const requireAuth = (_to, _from, next) => {
  if (localStorage.getItem('loggedIn')) next()
  else next('/login')
}

// 네비게이션 가드: 관리자(adminLevel >= 1) 또는 팀장(isTeamLeader)만 접근 허용, 아닐 시 /home으로 리다이렉트
const requireAdmin = (_to, _from, next) => {
  const adminLevel = parseInt(localStorage.getItem('adminLevel') || '0')
  const isTeamLeader = localStorage.getItem('isTeamLeader') === 'true'
  if (localStorage.getItem('loggedIn') && (adminLevel >= 1 || isTeamLeader)) next()
  else next('/home')
}

// 전체 라우트 목록 (각 경로와 매핑되는 컴포넌트 정의)
const routes = [
  // 루트 경로는 로그인 페이지로 리다이렉트
  { path: '/', redirect: '/login' },
  // 로그인 페이지 (인증 불필요)
  { path: '/login', component: Login },
  // 홈 화면 (로그인 필요)
  { path: '/home', component: Home, beforeEnter: requireAuth },
  // 근태 기록 페이지 (로그인 필요)
  { path: '/attendance', component: AttendanceRecords, beforeEnter: requireAuth },
  // 휴가 신청 페이지 - 지연 로딩 (로그인 필요)
  { path: '/vacation', component: () => import('../components/Vacation.vue'), beforeEnter: requireAuth },
  // 인사 관리 페이지 - 관리자/팀장만 접근 가능
  { path: '/hr', component: () => import('../components/HrManagement.vue'), beforeEnter: requireAdmin },
  // 일정 캘린더 페이지 (로그인 필요)
  { path: '/calendar', component: () => import('../components/Calendar.vue'), beforeEnter: requireAuth },
  // 게시판 허브 - 모든 게시판 목록 페이지 (로그인 필요)
  { path: '/board-hub', component: () => import('../components/BoardHub.vue'), beforeEnter: requireAuth },
  // 자유 게시판 목록 (로그인 필요)
  { path: '/board', component: () => import('../components/board/Board.vue'), beforeEnter: requireAuth },
  // 자유 게시판 글쓰기 (로그인 필요) - PostWriteForm.vue 사용
  { path: '/board/write', component: () => import('../components/board/BoardWrite.vue'), beforeEnter: requireAuth },
  // 자유 게시판 글 수정 (로그인 필요) - PostEditForm.vue 사용
  { path: '/board/edit/:id', component: () => import('../components/board/BoardEdit.vue'), beforeEnter: requireAuth },
  // 자유 게시판 글 상세 (로그인 필요) - PostDetailCard.vue, CommentSection.vue 사용
  { path: '/board/:id', component: () => import('../components/board/BoardDetail.vue'), beforeEnter: requireAuth },
  // 공지사항 목록 (로그인 필요)
  { path: '/notice', component: () => import('../components/notice/Notice.vue'), beforeEnter: requireAuth },
  // 공지사항 글쓰기 (로그인 필요) - PostWriteForm.vue 사용
  { path: '/notice/write', component: () => import('../components/notice/NoticeWrite.vue'), beforeEnter: requireAuth },
  // 공지사항 글 수정 (로그인 필요) - PostEditForm.vue 사용
  { path: '/notice/edit/:id', component: () => import('../components/notice/NoticeEdit.vue'), beforeEnter: requireAuth },
  // 공지사항 글 상세 (로그인 필요) - PostDetailCard.vue 사용
  { path: '/notice/:id', component: () => import('../components/notice/NoticeDetail.vue'), beforeEnter: requireAuth },
  // 팀 공지사항 목록 (로그인 필요)
  { path: '/team-notice', component: () => import('../components/team-notice/TeamNotice.vue'), beforeEnter: requireAuth },
  // 팀 공지사항 글쓰기 (로그인 필요)
  { path: '/team-notice/write', component: () => import('../components/team-notice/TeamNoticeWrite.vue'), beforeEnter: requireAuth },
  // 팀 공지사항 글 수정 (로그인 필요)
  { path: '/team-notice/edit/:id', component: () => import('../components/team-notice/TeamNoticeEdit.vue'), beforeEnter: requireAuth },
  // 팀 공지사항 글 상세 (로그인 필요)
  { path: '/team-notice/:id', component: () => import('../components/team-notice/TeamNoticeDetail.vue'), beforeEnter: requireAuth },
  // 사내 자료실 목록 (로그인 필요)
  { path: '/archive', component: () => import('../components/archive/Archive.vue'), beforeEnter: requireAuth },
  // 자료실 글쓰기 (로그인 필요) - PostWriteForm.vue, FileUploadInput.vue 사용
  { path: '/archive/write', component: () => import('../components/archive/ArchiveWrite.vue'), beforeEnter: requireAuth },
  // 자료실 글 수정 (로그인 필요) - PostEditForm.vue, FileList.vue 사용
  { path: '/archive/edit/:id', component: () => import('../components/archive/ArchiveEdit.vue'), beforeEnter: requireAuth },
  // 자료실 글 상세 (로그인 필요) - PostDetailCard.vue, FileList.vue 사용
  { path: '/archive/:id', component: () => import('../components/archive/ArchiveDetail.vue'), beforeEnter: requireAuth },
  // 팀 자료실 목록 (로그인 필요)
  { path: '/team-archive', component: () => import('../components/team-archive/TeamArchive.vue'), beforeEnter: requireAuth },
  // 팀 자료실 글쓰기 (로그인 필요)
  { path: '/team-archive/write', component: () => import('../components/team-archive/TeamArchiveWrite.vue'), beforeEnter: requireAuth },
  // 팀 자료실 글 수정 (로그인 필요)
  { path: '/team-archive/edit/:id', component: () => import('../components/team-archive/TeamArchiveEdit.vue'), beforeEnter: requireAuth },
  // 팀 자료실 글 상세 (로그인 필요)
  { path: '/team-archive/:id', component: () => import('../components/team-archive/TeamArchiveDetail.vue'), beforeEnter: requireAuth },
  // FAQ 목록 페이지 (로그인 필요)
  { path: '/faq', component: () => import('../components/faq/Faq.vue'), beforeEnter: requireAuth },
  // FAQ 글쓰기 - 관리자/팀장만 접근 가능
  { path: '/faq/write', component: () => import('../components/faq/FaqWrite.vue'), beforeEnter: requireAdmin },
  // FAQ 글 수정 - 관리자/팀장만 접근 가능
  { path: '/faq/edit/:id', component: () => import('../components/faq/FaqEdit.vue'), beforeEnter: requireAdmin }
]

// HTML5 History 모드로 라우터 생성 후 main.js에서 use(router)로 등록됨
export default createRouter({
  history: createWebHistory(),
  routes
})
