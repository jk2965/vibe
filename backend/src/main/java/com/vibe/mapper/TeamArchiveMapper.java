package com.vibe.mapper;

import com.vibe.model.TeamArchiveVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TeamArchiveMapper {
    List<TeamArchiveVO> findByTeam(String team);
    TeamArchiveVO findById(String id);
    void insert(TeamArchiveVO archive);
    void update(TeamArchiveVO archive);
    void incrementViews(String id);
    void delete(String id);
}
