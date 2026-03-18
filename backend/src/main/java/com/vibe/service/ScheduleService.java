package com.vibe.service;

import com.vibe.mapper.ScheduleMapper;
import com.vibe.model.ScheduleVO;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class ScheduleService {
    private final ScheduleMapper scheduleMapper;

    public ScheduleService(ScheduleMapper scheduleMapper) {
        this.scheduleMapper = scheduleMapper;
    }

    public List<ScheduleVO> getAll() {
        return scheduleMapper.findAll();
    }

    public List<ScheduleVO> getByTeam(String team) {
        return scheduleMapper.findByTeam(team);
    }

    public List<ScheduleVO> getByUserId(String userId) {
        return scheduleMapper.findByUserId(userId);
    }

    public ScheduleVO create(ScheduleVO schedule) {
        schedule.setId(UUID.randomUUID().toString());
        schedule.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        scheduleMapper.insert(schedule);
        return schedule;
    }

    public void update(String id, ScheduleVO schedule) {
        schedule.setId(id);
        scheduleMapper.update(schedule);
    }

    public void delete(String id) {
        scheduleMapper.delete(id);
    }

    public ScheduleVO findById(String id) {
        return scheduleMapper.findById(id);
    }
}
