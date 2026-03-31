package com.vibe.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.vibe.mapper.FaqMapper;
import com.vibe.model.FaqVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * FAQ 게시판 비즈니스 로직 서비스.
 * AbstractBoardService의 공통 흐름을 상속.
 * FAQ는 tags 컬럼이 없으므로 doUpdate()에서 tags 파라미터를 무시.
 * 파일 처리는 FileService에 위임 (다른 게시판 서비스와 동일한 방식).
 */
@Service
public class FaqService extends AbstractBoardService<FaqVO> {

    @Autowired private FaqMapper faqMapper;
    @Autowired private FileService fileService;

    /** FAQ 목록 페이징 조회 (페이지당 10개) */
    public PageInfo<FaqVO> getList(int pageNum) {
        PageHelper.startPage(pageNum, 10);
        List<FaqVO> list = faqMapper.findAll();
        return new PageInfo<>(list);
    }

    // ── AbstractBoardService 추상 메서드 구현 ────────────────────

    @Override protected FaqVO doFindById(String id)          { return faqMapper.findById(id); }
    @Override protected void doInsert(FaqVO item)            { faqMapper.insert(item); }
    @Override protected void doDelete(String id)             { faqMapper.delete(id); }
    @Override protected void doIncrementViews(String id)     { faqMapper.incrementViews(id); }
    /** FAQ는 필독 기능 없음 — 빈 구현 */
    @Override protected void doSetRequired(String id, int v) { }

    /** FAQ는 tags 컬럼이 없으므로 title/content만 업데이트 */
    @Override
    protected void doUpdate(String id, String title, String content, String tags) {
        FaqVO f = new FaqVO();
        f.setId(id); f.setTitle(title); f.setContent(content);
        faqMapper.update(f);
    }

    // ── 훅 메서드 ────────────────────────────────────────────────

    @Override
    protected void beforeDelete(String id) {
        fileService.deleteFilesByBoard(id);
    }

    @Override
    protected void loadRelations(FaqVO item) {
        item.setFiles(fileService.getFilesByBoard(item.getId()));
    }
}
