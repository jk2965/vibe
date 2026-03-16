package com.vibe.mapper;

import com.vibe.model.CommentVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {
    List<CommentVO> findByBoardId(String boardId);
    CommentVO findById(String id);
    void insert(CommentVO comment);
    void update(CommentVO comment);
    void delete(String id);
}
