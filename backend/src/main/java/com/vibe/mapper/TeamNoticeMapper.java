package com.vibe.mapper;

import com.vibe.model.TeamNoticeVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TeamNoticeMapper {
    List<TeamNoticeVO> findAll();
    List<TeamNoticeVO> findByTeam(String team);
    TeamNoticeVO findById(String id);
    void insert(TeamNoticeVO notice);
    void update(TeamNoticeVO notice);
    void incrementViews(String id);
    void delete(String id);
}
