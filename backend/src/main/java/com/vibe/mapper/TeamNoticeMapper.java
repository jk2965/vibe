package com.vibe.mapper;

import com.vibe.model.TeamNoticeVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 팀별 공지사항 관련 DB 접근 인터페이스.
 * TeamNoticeMapper.xml과 연결됨. TeamNoticeService.java에서 호출됨.
 * UserMapper.java와 연계하여 팀 소속 정보 사용.
 */
@Mapper
public interface TeamNoticeMapper {
    /** 전체 팀 공지사항 목록 조회 (관리자용) */
    List<TeamNoticeVO> findAll();
    /** 특정 팀의 공지사항 목록 조회 */
    List<TeamNoticeVO> findByTeam(String team);
    /** 팀 공지사항 ID로 단건 조회 */
    TeamNoticeVO findById(String id);
    /** 새 팀 공지사항 등록 */
    void insert(TeamNoticeVO notice);
    /** 팀 공지사항 제목·내용·태그 수정 */
    void update(TeamNoticeVO notice);
    /** 조회수 1 증가 (상세 조회 시 호출) */
    void incrementViews(String id);
    /** 팀 공지사항 삭제 */
    void delete(String id);
    /** 필독 여부 설정 (1=필독, 0=해제) */
    void setRequired(@Param("id") String id, @Param("isRequired") int isRequired);
    /** 필독 팀 공지사항 목록 조회 */
    List<TeamNoticeVO> findRequired();
    /** 특정 작성자의 팀 공지사항 목록 조회 (내 게시글용) */
    List<TeamNoticeVO> findByAuthor(String authorId);
    /** 제목 기준 팀 공지사항 검색 (LIKE '%keyword%') */
    List<TeamNoticeVO> searchByTitle(String keyword);
    /** 태그 기준 팀 공지사항 검색 (tags 컬럼 부분 일치) */
    List<TeamNoticeVO> searchByTag(String keyword);
}
