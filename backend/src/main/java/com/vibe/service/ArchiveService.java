package com.vibe.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.vibe.mapper.ArchiveMapper;
import com.vibe.model.ArchiveVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 전사 자료실(Archive) 비즈니스 로직 서비스.
 * AbstractBoardService의 공통 흐름을 상속.
 * 파일 처리는 FileService에 위임 (BoardService, NoticeService 등과 동일한 방식).
 */
@Service
public class ArchiveService extends AbstractBoardService<ArchiveVO> {

    @Autowired private ArchiveMapper mapper;
    @Autowired private FileService fileService;

    /** 자료실 목록 페이징 조회 (페이지당 10개) */
    public PageInfo<ArchiveVO> getList(int pageNum) {
        PageHelper.startPage(pageNum, 10);
        List<ArchiveVO> list = mapper.findAll();
        return new PageInfo<>(list);
    }

    // ── AbstractBoardService 추상 메서드 구현 ────────────────────

    @Override protected ArchiveVO doFindById(String id)        { return mapper.findById(id); }
    @Override protected void doInsert(ArchiveVO item)          { mapper.insert(item); }
    @Override protected void doDelete(String id)               { mapper.delete(id); }
    @Override protected void doIncrementViews(String id)       { mapper.incrementViews(id); }
    @Override protected void doSetRequired(String id, int v)   { mapper.setRequired(id, v); }

    @Override
    protected void doUpdate(String id, String title, String content, String tags) {
        ArchiveVO a = new ArchiveVO();
        a.setId(id); a.setTitle(title); a.setContent(content); a.setTags(tags);
        mapper.update(a);
    }

    // ── 훅 메서드 ────────────────────────────────────────────────

    @Override
    protected void beforeWrite(ArchiveVO item) {
        if (item.getIsRequired() == null) item.setIsRequired(0);
    }

    /** delete() 전: 첨부파일 먼저 삭제 */
    @Override
    protected void beforeDelete(String id) {
        fileService.deleteFilesByBoard(id);
    }

    /** getDetail() 후: 첨부파일 목록 로딩 */
    @Override
    protected void loadRelations(ArchiveVO item) {
        item.setFiles(fileService.getFilesByBoard(item.getId()));
    }
}
