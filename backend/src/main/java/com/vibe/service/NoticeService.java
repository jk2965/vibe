package com.vibe.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.vibe.mapper.NoticeMapper;
import com.vibe.model.NoticeVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 전사 공지사항(Notice) 비즈니스 로직 서비스.
 * AbstractBoardService의 공통 흐름을 상속하며,
 * 관리자/팀장만 작성 가능한 권한 검사는 NoticeController에서 처리.
 */
@Service
public class NoticeService extends AbstractBoardService<NoticeVO> {

    @Autowired private NoticeMapper mapper;
    @Autowired private FileService fileService;

    /** 공지사항 목록 페이징 조회 (페이지당 10개) */
    public PageInfo<NoticeVO> getList(int pageNum) {
        PageHelper.startPage(pageNum, 10);
        List<NoticeVO> list = mapper.findAll();
        return new PageInfo<>(list);
    }

    // ── AbstractBoardService 추상 메서드 구현 ────────────────────

    @Override protected NoticeVO doFindById(String id)        { return mapper.findById(id); }
    @Override protected void doInsert(NoticeVO item)          { mapper.insert(item); }
    @Override protected void doDelete(String id)              { mapper.delete(id); }
    @Override protected void doIncrementViews(String id)      { mapper.incrementViews(id); }
    @Override protected void doSetRequired(String id, int v)  { mapper.setRequired(id, v); }

    @Override
    protected void doUpdate(String id, String title, String content, String tags) {
        NoticeVO n = new NoticeVO();
        n.setId(id); n.setTitle(title); n.setContent(content); n.setTags(tags);
        mapper.update(n);
    }

    // ── 훅 메서드 ────────────────────────────────────────────────

    /** write() 전: isRequired 기본값 설정 */
    @Override
    protected void beforeWrite(NoticeVO item) {
        if (item.getIsRequired() == null) item.setIsRequired(0);
    }

    /** delete() 전: 첨부파일 먼저 삭제 (고아 파일 방지) */
    @Override
    protected void beforeDelete(String id) {
        fileService.deleteFilesByBoard(id);
    }

    /** getDetail() 후: 첨부파일 목록 로딩 */
    @Override
    protected void loadRelations(NoticeVO item) {
        item.setFiles(fileService.getFilesByBoard(item.getId()));
    }
}
