package com.vibe.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.vibe.mapper.ArchiveFileMapper;
import com.vibe.mapper.TeamArchiveMapper;
import com.vibe.model.TeamArchiveVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * 팀 자료실(TeamArchive) 관련 비즈니스 로직을 처리하는 서비스 클래스.
 * TeamArchiveController.java에서 호출되며, TeamArchiveMapper.java와 ArchiveFileMapper.java를 통해 DB와 연동됨.
 * 팀별로 자료실을 분리하여 관리하며, 파일 첨부는 ArchiveFileMapper.java를 직접 사용함.
 * ArchiveService.java와 파일 저장 구조가 동일하나 게시글 테이블(TEAM_ARCHIVE)이 별도로 존재함.
 */
@Service
public class TeamArchiveService {

    // TeamArchiveMapper.java를 통해 TEAM_ARCHIVE 테이블 CRUD 수행
    @Autowired
    private TeamArchiveMapper mapper;

    // ArchiveFileMapper.java를 통해 ARCHIVE_FILE 테이블에서 첨부파일 CRUD 수행
    @Autowired
    private ArchiveFileMapper fileMapper;

    /**
     * 특정 팀의 자료실 게시글 목록을 페이지 단위로 조회 (페이지당 10개).
     * PageHelper를 사용해 자동 페이징 처리.
     * TeamArchiveMapper.java의 findByTeam()으로 해당 팀의 자료만 조회.
     */
    public PageInfo<TeamArchiveVO> getListByTeam(String team, int pageNum) {
        PageHelper.startPage(pageNum, 10);
        // 팀명(team)을 기준으로 해당 팀의 자료만 필터링
        List<TeamArchiveVO> list = mapper.findByTeam(team);
        return new PageInfo<>(list);
    }

    /**
     * 팀 자료실 게시글 상세 조회: 조회수 1 증가 후 게시글과 첨부파일 목록을 함께 반환.
     * TeamArchiveMapper.java의 incrementViews()로 조회수 증가.
     * ArchiveFileMapper.java의 findByBoardId()로 첨부파일 목록 조회.
     */
    public TeamArchiveVO getDetail(String id) {
        // 팀 자료실 게시글 조회 시 조회수(views) 1 증가
        mapper.incrementViews(id);
        TeamArchiveVO archive = mapper.findById(id);
        if (archive != null) {
            // ArchiveFileMapper.java를 통해 해당 게시글의 첨부파일 목록 조회
            archive.setFiles(fileMapper.findByBoardId(id));
        }
        return archive;
    }

    /**
     * 새 팀 자료실 게시글 작성: UUID로 고유 ID 생성, 현재 시각을 작성일로 설정 후 DB에 저장.
     * TeamArchiveMapper.java의 insert()로 TEAM_ARCHIVE 테이블에 등록.
     */
    public TeamArchiveVO write(TeamArchiveVO archive) {
        // UUID를 사용해 유일한 팀 자료실 게시글 ID 생성
        archive.setId(UUID.randomUUID().toString());
        // 현재 시각을 'yyyy-MM-dd HH:mm:ss' 형식으로 작성일 설정
        archive.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        mapper.insert(archive);
        return archive;
    }

    /**
     * 팀 자료실 게시글 수정: 제목과 내용만 업데이트.
     * TeamArchiveMapper.java의 update()로 TEAM_ARCHIVE 테이블 수정.
     */
    public void update(String id, String title, String content) {
        TeamArchiveVO archive = new TeamArchiveVO();
        archive.setId(id);
        archive.setTitle(title);
        archive.setContent(content);
        mapper.update(archive);
    }

    /**
     * 팀 자료실 게시글 삭제: 첨부파일 먼저 삭제 후 게시글 본문 삭제.
     * ArchiveFileMapper.java의 deleteByBoardId()로 첨부파일 일괄 삭제.
     * TeamArchiveMapper.java의 delete()로 게시글 삭제.
     */
    public void delete(String id) {
        // 파일을 먼저 삭제하여 고아 파일(orphan file) 방지
        fileMapper.deleteByBoardId(id);
        mapper.delete(id);
    }
}
