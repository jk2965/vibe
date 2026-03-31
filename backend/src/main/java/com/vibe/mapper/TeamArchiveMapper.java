package com.vibe.mapper;

import com.vibe.model.TeamArchiveVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 팀별 자료실 게시글 관련 DB 접근 인터페이스.
 * TeamArchiveMapper.xml과 연결됨. TeamArchiveService.java에서 호출됨.
 * UserMapper.java와 연계하여 팀 소속 정보 사용.
 */
@Mapper
public interface TeamArchiveMapper {
    /** 특정 팀의 자료실 게시글 목록 조회 (파일 수·첫 파일명 포함) */
    List<TeamArchiveVO> findByTeam(String team);
    /** 팀 자료실 게시글 ID로 단건 조회 */
    TeamArchiveVO findById(String id);
    /** 새 팀 자료실 게시글 등록 */
    void insert(TeamArchiveVO archive);
    /** 팀 자료실 게시글 제목·내용·태그 수정 */
    void update(TeamArchiveVO archive);
    /** 조회수 1 증가 (상세 조회 시 호출) */
    void incrementViews(String id);
    /** 팀 자료실 게시글 삭제 */
    void delete(String id);
    /** 필독 여부 설정 (1=필독, 0=해제) */
    void setRequired(@Param("id") String id, @Param("isRequired") int isRequired);
    /** 필독 팀 자료실 게시글 목록 조회 */
    List<TeamArchiveVO> findRequired();
    /** 특정 작성자의 팀 자료실 게시글 목록 조회 (내 게시글용) */
    List<TeamArchiveVO> findByAuthor(String authorId);
    /** 제목 기준 팀 자료실 게시글 검색 (LIKE '%keyword%') */
    List<TeamArchiveVO> searchByTitle(String keyword);
    /** 태그 기준 팀 자료실 게시글 검색 (tags 컬럼 부분 일치) */
    List<TeamArchiveVO> searchByTag(String keyword);
}
