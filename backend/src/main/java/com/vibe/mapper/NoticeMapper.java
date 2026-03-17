package com.vibe.mapper;

import com.vibe.model.NoticeVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface NoticeMapper {
    List<NoticeVO> findAll();
    NoticeVO findById(String id);
    void insert(NoticeVO notice);
    void update(NoticeVO notice);
    void incrementViews(String id);
    void delete(String id);
}
