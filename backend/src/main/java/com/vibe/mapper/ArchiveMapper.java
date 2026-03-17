package com.vibe.mapper;

import com.vibe.model.ArchiveVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ArchiveMapper {
    List<ArchiveVO> findAll();
    ArchiveVO findById(String id);
    void insert(ArchiveVO archive);
    void update(ArchiveVO archive);
    void incrementViews(String id);
    void delete(String id);
}
