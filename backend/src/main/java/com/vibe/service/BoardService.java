package com.vibe.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.vibe.mapper.BoardMapper;
import com.vibe.model.BoardVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class BoardService {

    @Autowired
    private BoardMapper mapper;

    public PageInfo<BoardVO> getList(int pageNum) {
        PageHelper.startPage(pageNum, 10);
        List<BoardVO> list = mapper.findAll();
        return new PageInfo<>(list);
    }

    public BoardVO getDetail(String id) {
        mapper.incrementViews(id);
        return mapper.findById(id);
    }

    public BoardVO write(BoardVO board) {
        board.setId(UUID.randomUUID().toString());
        board.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        mapper.insert(board);
        return board;
    }

    public void update(String id, String title, String content) {
        BoardVO board = new BoardVO();
        board.setId(id);
        board.setTitle(title);
        board.setContent(content);
        mapper.update(board);
    }

    public void delete(String id) {
        mapper.delete(id);
    }
}
