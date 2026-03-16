package com.vibe.mapper;

import com.vibe.model.BoardVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface BoardMapper {
    List<BoardVO> findAll();
    BoardVO findById(String id);
    void insert(BoardVO board);
    void update(BoardVO board);
    void incrementViews(String id);
    void delete(String id);
}
