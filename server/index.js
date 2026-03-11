const express = require('express');
const cors = require('cors');
const axios = require('axios');

const app = express();
const PORT = 3000;
const SPRING_BASE_URL = 'http://localhost:8080';

app.use(cors());
app.use(express.json());

// 요청 로깅 미들웨어
app.use((req, res, next) => {
  console.log(`[${new Date().toISOString()}] ${req.method} ${req.path}`);
  next();
});

// 회원가입 프록시
app.post('/api/users/register', async (req, res) => {
  try {
    const response = await axios.post(`${SPRING_BASE_URL}/api/users/register`, req.body);
    res.status(response.status).json(response.data);
  } catch (error) {
    if (error.response) {
      res.status(error.response.status).json(error.response.data);
    } else {
      console.error('Spring 서버 연결 오류:', error.message);
      res.status(503).json({ error: '서버에 연결할 수 없습니다. 잠시 후 다시 시도해주세요.' });
    }
  }
});

// 이메일 중복 확인 프록시
app.get('/api/users/check-email', async (req, res) => {
  try {
    const response = await axios.get(`${SPRING_BASE_URL}/api/users/check-email`, {
      params: req.query
    });
    res.status(response.status).json(response.data);
  } catch (error) {
    if (error.response) {
      res.status(error.response.status).json(error.response.data);
    } else {
      console.error('Spring 서버 연결 오류:', error.message);
      res.status(503).json({ error: '서버에 연결할 수 없습니다.' });
    }
  }
});

// 헬스 체크
app.get('/health', (req, res) => {
  res.json({ status: 'ok', timestamp: new Date().toISOString() });
});

app.listen(PORT, () => {
  console.log(`Node.js BFF 서버가 포트 ${PORT}에서 실행 중입니다`);
  console.log(`Spring 백엔드: ${SPRING_BASE_URL}`);
});
