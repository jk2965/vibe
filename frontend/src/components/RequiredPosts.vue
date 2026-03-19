<template>
  <div class="board-container">
    <PageHeader title="필독 게시글" />

    <div class="list-card">
      <div class="card-header">
        <h2>필독 게시글 목록</h2>
      </div>

      <!-- 필독 게시글 없을 때 -->
      <div v-if="!hasAny" class="empty">등록된 필독 게시글이 없습니다.</div>

      <!-- 게시판별 섹션 -->
      <template v-else>
        <template v-for="section in sections">
          <template v-if="posts[section.key] && posts[section.key].length > 0">
            <!-- 게시판 섹션 헤더 -->
            <div :key="'header-' + section.key" class="section-title">{{ section.label }}</div>

            <!-- 테이블 -->
            <table :key="'table-' + section.key" class="board-table">
              <thead>
                <tr>
                  <th class="col-no">번호</th>
                  <th class="col-title">제목</th>
                  <th class="col-author">작성자</th>
                  <th class="col-date">작성일</th>
                  <th class="col-views">조회</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="(post, index) in posts[section.key]"
                  :key="post.id"
                  class="post-row"
                  @click="$router.push(section.path + '/' + post.id)"
                >
                  <td class="col-no">{{ posts[section.key].length - index }}</td>
                  <td class="col-title title-cell">
                    <span class="required-badge">📌</span>
                    {{ post.title }}
                  </td>
                  <td class="col-author">{{ post.authorName }}</td>
                  <td class="col-date">{{ formatDate(post.createdAt) }}</td>
                  <td class="col-views">{{ post.views }}</td>
                </tr>
              </tbody>
            </table>
          </template>
        </template>
      </template>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import PageHeader from './common/PageHeader.vue'

export default {
  name: 'RequiredPosts',
  components: { PageHeader },
  data() {
    return {
      posts: {
        notice: [],
        teamNotice: [],
        board: [],
        archive: [],
        teamArchive: []
      }
    }
  },
  computed: {
    hasAny() {
      return Object.values(this.posts).some(list => list && list.length > 0)
    },
    sections() {
      return [
        { key: 'notice',      label: '📢 전체 공지사항', path: '/notice' },
        { key: 'teamNotice',  label: '👥 팀별 공지사항', path: '/team-notice' },
        { key: 'board',       label: '📋 자유 게시판',   path: '/board' },
        { key: 'archive',     label: '🗂️ 전체 자료실',   path: '/archive' },
        { key: 'teamArchive', label: '📁 팀별 자료실',   path: '/team-archive' }
      ]
    }
  },
  mounted() {
    this.fetchRequiredPosts()
  },
  methods: {
    async fetchRequiredPosts() {
      try {
        const userId = localStorage.getItem('userId') || ''
        const res = await axios.get('http://localhost:8090/api/required', { params: { userId } })
        this.posts = {
          notice:      res.data.notice      || [],
          teamNotice:  res.data.teamNotice  || [],
          board:       res.data.board       || [],
          archive:     res.data.archive     || [],
          teamArchive: res.data.teamArchive || []
        }
      } catch (e) {
        console.error('필독 게시글 조회 실패:', e)
      }
    },
    formatDate(dateStr) {
      if (!dateStr) return '-'
      return dateStr.substring(0, 10)
    }
  }
}
</script>

<style scoped>
.board-container { max-width: 900px; margin: 0 auto; padding: 24px; }

.list-card { background: white; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.08); padding: 24px; }
.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.card-header h2 { margin: 0; font-size: 18px; }

.section-title {
  font-size: 14px;
  font-weight: bold;
  color: #c62828;
  padding: 10px 4px 6px;
  border-bottom: 2px solid #fce4ec;
  margin-top: 20px;
}
.section-title:first-of-type { margin-top: 0; }

.board-table { width: 100%; border-collapse: collapse; margin-bottom: 8px; }
.board-table th, .board-table td { padding: 12px 10px; text-align: left; border-bottom: 1px solid #eee; font-size: 14px; }
.board-table th { background: #f5f5f5; font-weight: bold; }
.col-no { width: 60px; text-align: center; }
.col-author { width: 110px; }
.col-date { width: 100px; }
.col-views { width: 60px; text-align: center; }

.post-row { cursor: pointer; }
.post-row:hover { background: #f9f9f9; }
.title-cell { color: #1a1a1a; font-weight: 500; }
.required-badge { font-size: 13px; margin-right: 4px; }

.empty { text-align: center; color: #999; padding: 60px; font-size: 15px; }
</style>
