<template>
  <!-- 게시판 관련 페이지에서 왼쪽에 고정 표시되는 사이드바 -->
  <div class="sidebar">
    <div class="sidebar-header">
      <span class="sidebar-icon">📌</span>
      <span class="sidebar-title">게시판</span>
    </div>

    <!-- 글쓰기 버튼: 클릭 시 게시판 선택 팝업 열기 -->
    <div class="sidebar-write-wrap">
      <button class="btn-write" @click="openWriteModal">✏️ 글쓰기</button>
    </div>

    <!-- 게시글 검색 영역: 제목 또는 태그 기준으로 전체 게시판 통합 검색 -->
    <div class="sidebar-search-wrap">
      <!-- 검색 타입 선택: 제목 검색 / 태그 검색 -->
      <div class="search-type-tabs">
        <button
          class="type-tab"
          :class="{ active: searchType === 'title' }"
          @click="searchType = 'title'"
        >제목</button>
        <button
          class="type-tab"
          :class="{ active: searchType === 'tag' }"
          @click="searchType = 'tag'"
        >태그</button>
      </div>
      <!-- 검색어 입력 및 실행 (Enter 키 또는 검색 버튼으로 실행) -->
      <div class="search-input-row">
        <input
          v-model="searchKeyword"
          type="text"
          class="search-input"
          :placeholder="searchType === 'title' ? '제목 검색...' : '태그 검색...'"
          @keydown.enter="doSearch"
        />
        <button class="btn-search" @click="doSearch">🔍</button>
      </div>
    </div>

    <nav class="sidebar-nav">
      <router-link to="/required" class="sidebar-item required-item" :class="{ active: isActive('/required') }">
        <span class="item-icon">📌</span>
        <span class="item-label">필독 게시글</span>
      </router-link>
      <router-link to="/my-posts" class="sidebar-item my-posts-item" :class="{ active: isActive('/my-posts') }">
        <span class="item-icon">✍️</span>
        <span class="item-label">내 게시글</span>
      </router-link>
      <router-link to="/notice" class="sidebar-item" :class="{ active: isActive('/notice') }">
        <span class="item-icon">📢</span>
        <span class="item-label">전체 공지사항</span>
      </router-link>
      <router-link v-if="canAccessTeam" to="/team-notice" class="sidebar-item" :class="{ active: isActive('/team-notice') }">
        <span class="item-icon">👥</span>
        <span class="item-label">팀별 공지사항</span>
      </router-link>
      <router-link to="/board" class="sidebar-item" :class="{ active: isActive('/board') }">
        <span class="item-icon">📋</span>
        <span class="item-label">자유 게시판</span>
      </router-link>
      <router-link to="/archive" class="sidebar-item" :class="{ active: isActive('/archive') }">
        <span class="item-icon">🗂️</span>
        <span class="item-label">전체 자료실</span>
      </router-link>
      <router-link v-if="canAccessTeam" to="/team-archive" class="sidebar-item" :class="{ active: isActive('/team-archive') }">
        <span class="item-icon">📁</span>
        <span class="item-label">팀별 자료실</span>
      </router-link>
      <router-link to="/faq" class="sidebar-item" :class="{ active: isActive('/faq') }">
        <span class="item-icon">❓</span>
        <span class="item-label">FAQ</span>
      </router-link>
    </nav>

    <!-- 게시판 선택 팝업 모달 -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal">
        <!-- 모달 헤더 -->
        <div class="modal-header">
          <span class="modal-title">게시판 선택</span>
          <button class="btn-close" @click="closeModal">✕</button>
        </div>

        <!-- 게시판 목록: 사용자 권한에 따라 쓰기 가능한 게시판만 표시 -->
        <div class="modal-body">
          <label v-for="board in writableBoards" :key="board.path" class="board-option">
            <input type="radio" :value="board.path" v-model="selectedBoard" />
            <span class="board-icon">{{ board.icon }}</span>
            <span class="board-name">{{ board.label }}</span>
          </label>

          <!-- 쓸 수 있는 게시판이 없는 경우 -->
          <p v-if="writableBoards.length === 0" class="no-board">
            글쓰기 권한이 있는 게시판이 없습니다.
          </p>
        </div>

        <!-- 모달 하단 버튼 -->
        <div class="modal-footer">
          <button class="btn-cancel" @click="closeModal">닫기</button>
          <button class="btn-confirm" :disabled="!selectedBoard" @click="goWrite">확인</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Sidebar',
  data() {
    return {
      // 팝업 표시 여부
      showModal: false,
      // 선택한 게시판 글쓰기 경로
      selectedBoard: '',
      // 검색 타입: 'title'(제목 검색) 또는 'tag'(태그 검색)
      searchType: 'title',
      // 검색창에 입력 중인 검색어
      searchKeyword: ''
    }
  },
  computed: {
    // localStorage에서 권한 정보 읽기
    adminLevel() {
      return parseInt(localStorage.getItem('adminLevel') || '0')
    },
    team() {
      return localStorage.getItem('team') || ''
    },
    isTeamLeader() {
      return localStorage.getItem('isTeamLeader') === 'true'
    },
    // 팀별 게시판 접근 권한: 마스터 관리자 또는 팀 소속 사용자
    canAccessTeam() {
      return this.adminLevel >= 2 || !!this.team
    },
    // 관리자 또는 팀장 여부 (공지사항, 자료실 글쓰기 권한)
    canWrite() {
      return this.adminLevel >= 1 || this.isTeamLeader
    },
    // 현재 사용자가 글쓰기 가능한 게시판 목록 (권한에 따라 필터링)
    writableBoards() {
      const boards = []

      // 자유 게시판: 모든 사용자 글쓰기 가능
      boards.push({ label: '자유 게시판', icon: '📋', path: '/board/write' })

      // 전체 공지사항: 관리자 또는 팀장만 가능
      if (this.canWrite) {
        boards.push({ label: '전체 공지사항', icon: '📢', path: '/notice/write' })
      }

      // 팀별 공지사항: 팀 소속이고 관리자 또는 팀장만 가능
      if (this.canAccessTeam && this.canWrite) {
        boards.push({ label: '팀별 공지사항', icon: '👥', path: '/team-notice/write' })
      }

      // 전체 자료실: 관리자 또는 팀장만 가능
      if (this.canWrite) {
        boards.push({ label: '전체 자료실', icon: '🗂️', path: '/archive/write' })
      }

      // 팀별 자료실: 팀 소속이고 관리자 또는 팀장만 가능
      if (this.canAccessTeam && this.canWrite) {
        boards.push({ label: '팀별 자료실', icon: '📁', path: '/team-archive/write' })
      }

      // FAQ: 관리자(adminLevel >= 1)만 가능
      if (this.adminLevel >= 1) {
        boards.push({ label: 'FAQ', icon: '❓', path: '/faq/write' })
      }

      return boards
    }
  },
  methods: {
    // 현재 경로가 해당 메뉴 경로로 시작하는지 확인
    isActive(path) {
      return this.$route.path.startsWith(path)
    },
    // 글쓰기 팝업 열기
    openWriteModal() {
      // 현재 보고 있는 게시판을 기본값으로 선택
      const current = this.writableBoards.find(b => this.$route.path.startsWith(b.path.replace('/write', '')))
      this.selectedBoard = current ? current.path : (this.writableBoards[0]?.path || '')
      this.showModal = true
    },
    // 팝업 닫기
    closeModal() {
      this.showModal = false
      this.selectedBoard = ''
    },
    // 선택한 게시판 글쓰기 페이지로 이동
    goWrite() {
      if (!this.selectedBoard) return
      this.$router.push(this.selectedBoard)
      this.closeModal()
    },
    // 검색어가 있을 때 /search?type={searchType}&keyword={searchKeyword} 로 이동
    // SearchResult.vue에서 쿼리 파라미터를 읽어 API 호출 후 결과 표시
    doSearch() {
      const kw = this.searchKeyword.trim()
      if (!kw) return
      this.$router.push({ path: '/search', query: { type: this.searchType, keyword: kw } })
    }
  }
}
</script>

<style scoped>
.sidebar {
  width: 210px;
  min-width: 210px;
  background: #fff;
  border-right: 1px solid #e0e0e0;
  min-height: 100vh;
  padding: 20px 0;
  box-shadow: 2px 0 8px rgba(0,0,0,0.04);
}

.sidebar-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 20px 14px;
  border-bottom: 1px solid #eee;
}
.sidebar-icon { font-size: 18px; }
.sidebar-title { font-size: 15px; font-weight: bold; color: #333; }

/* 글쓰기 버튼 영역 */
.sidebar-write-wrap {
  padding: 12px 16px 10px;
}
.btn-write {
  width: 100%;
  padding: 9px 0;
  background: #1a73e8;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 13.5px;
  font-weight: bold;
  cursor: pointer;
  transition: background 0.15s;
}
.btn-write:hover { background: #1558b0; }

/* 검색 영역 */
.sidebar-search-wrap {
  padding: 8px 16px 12px;
  border-bottom: 1px solid #eee;
}

/* 검색 타입 탭 버튼 (제목 / 태그) */
.search-type-tabs {
  display: flex;
  gap: 4px;
  margin-bottom: 6px;
}
.type-tab {
  flex: 1;
  padding: 5px 0;
  background: #f5f5f5;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 12px;
  color: #666;
  cursor: pointer;
  transition: background 0.12s, color 0.12s;
}
.type-tab:hover { background: #e8f0fe; color: #1a73e8; }
/* 선택된 탭 강조 */
.type-tab.active { background: #1a73e8; color: white; border-color: #1a73e8; font-weight: bold; }

/* 검색 입력 + 버튼 행 */
.search-input-row {
  display: flex;
  gap: 4px;
}
.search-input {
  flex: 1;
  padding: 6px 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 12px;
  outline: none;
  min-width: 0;
}
.search-input:focus { border-color: #1a73e8; }
.btn-search {
  padding: 6px 9px;
  background: #f5f5f5;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 13px;
  cursor: pointer;
  flex-shrink: 0;
}
.btn-search:hover { background: #e8f0fe; }

.sidebar-nav { display: flex; flex-direction: column; padding-top: 6px; }

.sidebar-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 11px 20px;
  text-decoration: none;
  color: #555;
  font-size: 13.5px;
  transition: background 0.15s, color 0.15s;
  border-left: 3px solid transparent;
}
.sidebar-item:hover { background: #f5f5f5; color: #222; }
.sidebar-item.active {
  background: #e8f0fe;
  color: #1a73e8;
  font-weight: bold;
  border-left-color: #1a73e8;
}
.required-item.active { background: #fce4ec; color: #c62828; border-left-color: #c62828; }
.required-item:not(.active):hover { background: #fce4ec30; }
.my-posts-item.active { background: #e8f5e9; color: #2e7d32; border-left-color: #2e7d32; }
.my-posts-item:not(.active):hover { background: #e8f5e930; }
.item-icon { font-size: 16px; flex-shrink: 0; }
.item-label { font-size: 13.5px; }

/* 모달 오버레이 */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

/* 모달 박스 */
.modal {
  background: white;
  border-radius: 10px;
  width: 360px;
  max-width: 90vw;
  box-shadow: 0 8px 32px rgba(0,0,0,0.18);
  overflow: hidden;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #eee;
}
.modal-title { font-size: 16px; font-weight: bold; color: #222; }
.btn-close {
  background: none;
  border: none;
  font-size: 16px;
  cursor: pointer;
  color: #888;
  line-height: 1;
  padding: 2px 6px;
}
.btn-close:hover { color: #333; }

.modal-body {
  padding: 16px 20px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  max-height: 360px;
  overflow-y: auto;
}

/* 게시판 선택 라디오 옵션 */
.board-option {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.12s;
}
.board-option:hover { background: #f5f5f5; }
.board-option input[type="radio"] { accent-color: #1a73e8; width: 16px; height: 16px; cursor: pointer; }
.board-icon { font-size: 17px; }
.board-name { font-size: 14px; color: #333; }

.no-board { color: #888; font-size: 14px; text-align: center; padding: 20px 0; }

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding: 14px 20px;
  border-top: 1px solid #eee;
}
.btn-cancel {
  padding: 8px 20px;
  background: #f5f5f5;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
}
.btn-cancel:hover { background: #eee; }
.btn-confirm {
  padding: 8px 20px;
  background: #1a73e8;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: bold;
  cursor: pointer;
  transition: background 0.15s;
}
.btn-confirm:hover:not(:disabled) { background: #1558b0; }
.btn-confirm:disabled { background: #b0c4e8; cursor: not-allowed; }
</style>
