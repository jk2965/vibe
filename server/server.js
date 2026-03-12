const express = require('express');
const axios = require('axios');
const cors = require('cors');

const app = express();
const port = 3000;

app.use(cors());
app.use(express.json());

app.get('/api/attendance', async (req, res) => {
  try {
    const response = await axios.get('http://localhost:8081/api/attendance');
    res.json(response.data);
  } catch (error) {
    console.error('Error fetching from backend:', error);
    res.status(500).json({ error: 'Failed to fetch attendance records' });
  }
});

app.post('/api/attendance', async (req, res) => {
  try {
    const response = await axios.post('http://localhost:8081/api/attendance', req.body);
    res.json(response.data);
  } catch (error) {
    console.error('Error posting to backend:', error);
    res.status(500).json({ error: 'Failed to create attendance record' });
  }
});

app.listen(port, () => {
  console.log(`Server running at http://localhost:${port}`);
});