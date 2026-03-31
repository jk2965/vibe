package com.vibe.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.vibe.mapper.TeamArchiveMapper;
import com.vibe.model.TeamArchiveVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 팀 자료실(TeamArchive) 비즈니스 로직 서비스.
 * AbstractBoardService의 공통 흐름을 상속.
 * 팀별 목록 조회는 getListByTeam()으로 별도 제공.
 * 파일 처리는 FileService에 위임 (다른 게시판 서비스와 동일한 방식).
 */
@Service
public class TeamArchiveService extends AbstractBoardService<TeamArchiveVO> {

    @Autowired private TeamArchiveMapper mapper;
    @Autowired private FileService fileService;

    /** 특정 팀의 자료실 목록 페이징 조회 */
    public PageInfo<TeamArchiveVO> getListByTeam(String team, int pageNum) {
        PageHelper.startPage(pageNum, 10);
        List<TeamArchiveVO> list = mapper.findByTeam(team);
        return new PageInfo<>(list);
    }

    // ── AbstractBoardService 추상 메서드 구현 ────────────────────

    @Override protected TeamArchiveVO doFindById(String id)      { return mapper.findById(id); }
    @Override protected void doInsert(TeamArchiveVO item)        { mapper.insert(item); }
    @Override protected void doDelete(String id)                 { mapper.delete(id); }
    @Override protected void doIncrementViews(String id)         { mapper.incrementViews(id); }
    @Override protected void doSetRequired(String id, int v)     { mapper.setRequired(id, v); }

    @Override
    protected void doUpdate(String id, String title, String content, String tags) {
        TeamArchiveVO a = new TeamArchiveVO();
        a.setId(id); a.setTitle(title); a.setContent(content); a.setTags(tags);
        mapper.update(a);
    }

    // ── 훅 메서드 ────────────────────────────────────────────────

    @Override
    protected void beforeWrite(TeamArchiveVO item) {
        if (item.getIsRequired() == null) item.setIsRequired(0);
    }

    @Override
    protected void beforeDelete(String id) {
        fileService.deleteFilesByBoard(id);
    }

    @Override
    protected void loadRelations(TeamArchiveVO item) {
        item.setFiles(fileService.getFilesByBoard(item.getId()));
    }
}
