package com.vibe.service;

import com.vibe.mapper.CommentMapper;
import com.vibe.model.CommentVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class CommentService {

    @Autowired
    private CommentMapper mapper;

    public List<CommentVO> getByBoardId(String boardId) {
        return mapper.findByBoardId(boardId);
    }

    public CommentVO write(CommentVO comment) {
        comment.setId(UUID.randomUUID().toString());
        comment.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        mapper.insert(comment);
        return comment;
    }

    public void update(String id, String content) {
        CommentVO comment = new CommentVO();
        comment.setId(id);
        comment.setContent(content);
        mapper.update(comment);
    }

    public CommentVO findById(String id) {
        return mapper.findById(id);
    }

    public void delete(String id) {
        mapper.softDelete(id);
    }
}
