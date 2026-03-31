package com.vibe.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.vibe.mapper.BoardMapper;
import com.vibe.model.BoardVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 자유게시판(Board) 비즈니스 로직 서비스.
 * AbstractBoardService의 공통 흐름(write, getDetail, update, delete, setRequired)을 상속.
 * 게시판별 세부 구현(매퍼 호출, 파일 로딩)을 doXxx/훅 메서드로 제공.
 */
@Service
public class BoardService extends AbstractBoardService<BoardVO> {

    @Autowired private BoardMapper mapper;
    // 첨부파일 처리를 FileService에 위임
    @Autowired private FileService fileService;

    /** 자유게시판 목록 페이징 조회 (페이지당 10개) */
    public PageInfo<BoardVO> getList(int pageNum) {
        PageHelper.startPage(pageNum, 10);
        List<BoardVO> list = mapper.findAll();
        return new PageInfo<>(list);
    }

    // ── AbstractBoardService 추상 메서드 구현 ────────────────────

    @Override protected BoardVO doFindById(String id)        { return mapper.findById(id); }
    @Override protected void doInsert(BoardVO item)          { mapper.insert(item); }
    @Override protected void doDelete(String id)             { mapper.delete(id); }
    @Override protected void doIncrementViews(String id)     { mapper.incrementViews(id); }
    @Override protected void doSetRequired(String id, int v) { mapper.setRequired(id, v); }

    @Override
    protected void doUpdate(String id, String title, String content, String tags) {
        BoardVO b = new BoardVO();
        b.setId(id); b.setTitle(title); b.setContent(content); b.setTags(tags);
        mapper.update(b);
    }

    // ── 훅 메서드 ────────────────────────────────────────────────

    /** write() 전: isRequired 기본값 설정 */
    @Override
    protected void beforeWrite(BoardVO item) {
        if (item.getIsRequired() == null) item.setIsRequired(0);
    }

    /** delete() 전: 첨부파일 먼저 삭제 (고아 파일 방지) */
    @Override
    protected void beforeDelete(String id) {
        fileService.deleteFilesByBoard(id);
    }

    /** getDetail() 후: 첨부파일 목록 로딩 */
    @Override
    protected void loadRelations(BoardVO item) {
        item.setFiles(fileService.getFilesByBoard(item.getId()));
    }
}
