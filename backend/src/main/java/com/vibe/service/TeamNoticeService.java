package com.vibe.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.vibe.mapper.TeamNoticeMapper;
import com.vibe.model.TeamNoticeVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 팀 공지사항(TeamNotice) 비즈니스 로직 서비스.
 * AbstractBoardService의 공통 흐름을 상속.
 * 팀별 목록 조회는 getListByTeam()으로 별도 제공 (team 파라미터 필요).
 */
@Service
public class TeamNoticeService extends AbstractBoardService<TeamNoticeVO> {

    @Autowired private TeamNoticeMapper mapper;
    @Autowired private FileService fileService;

    /** 특정 팀의 공지사항 목록 페이징 조회 */
    public PageInfo<TeamNoticeVO> getListByTeam(String team, int pageNum) {
        PageHelper.startPage(pageNum, 10);
        List<TeamNoticeVO> list = mapper.findByTeam(team);
        return new PageInfo<>(list);
    }

    // ── AbstractBoardService 추상 메서드 구현 ────────────────────

    @Override protected TeamNoticeVO doFindById(String id)       { return mapper.findById(id); }
    @Override protected void doInsert(TeamNoticeVO item)         { mapper.insert(item); }
    @Override protected void doDelete(String id)                 { mapper.delete(id); }
    @Override protected void doIncrementViews(String id)         { mapper.incrementViews(id); }
    @Override protected void doSetRequired(String id, int v)     { mapper.setRequired(id, v); }

    @Override
    protected void doUpdate(String id, String title, String content, String tags) {
        TeamNoticeVO n = new TeamNoticeVO();
        n.setId(id); n.setTitle(title); n.setContent(content); n.setTags(tags);
        mapper.update(n);
    }

    // ── 훅 메서드 ────────────────────────────────────────────────

    /** write() 전: isRequired 기본값 설정 */
    @Override
    protected void beforeWrite(TeamNoticeVO item) {
        if (item.getIsRequired() == null) item.setIsRequired(0);
    }

    /** delete() 전: 첨부파일 먼저 삭제 (고아 파일 방지) */
    @Override
    protected void beforeDelete(String id) {
        fileService.deleteFilesByBoard(id);
    }

    /** getDetail() 후: 첨부파일 목록 로딩 */
    @Override
    protected void loadRelations(TeamNoticeVO item) {
        item.setFiles(fileService.getFilesByBoard(item.getId()));
    }
}
