package com.vibe.mapper;

import com.vibe.model.ScheduleVO;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface ScheduleMapper {
    List<ScheduleVO> findAll();
    List<ScheduleVO> findByTeam(String team);
    List<ScheduleVO> findByUserId(String userId);
    ScheduleVO findById(String id);
    void insert(ScheduleVO schedule);
    void update(ScheduleVO schedule);
    void delete(String id);
}
