package com.vibe.mapper;

import com.vibe.model.NoticeVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 전사 공지사항 관련 DB 접근 인터페이스.
 * NoticeMapper.xml과 연결됨. NoticeService.java에서 호출됨.
 */
@Mapper
public interface NoticeMapper {
    /** 전체 공지사항 목록 조회 (최신순, 필독 우선 정렬) */
    List<NoticeVO> findAll();
    /** 공지사항 ID로 단건 조회 */
    NoticeVO findById(String id);
    /** 새 공지사항 등록 */
    void insert(NoticeVO notice);
    /** 공지사항 제목·내용·태그 수정 */
    void update(NoticeVO notice);
    /** 조회수 1 증가 (상세 조회 시 호출) */
    void incrementViews(String id);
    /** 공지사항 삭제 */
    void delete(String id);
    /** 필독 여부 설정 (1=필독, 0=해제) */
    void setRequired(@Param("id") String id, @Param("isRequired") int isRequired);
    /** 필독 공지사항 목록 조회 */
    List<NoticeVO> findRequired();
    /** 특정 작성자의 공지사항 목록 조회 (내 게시글용) */
    List<NoticeVO> findByAuthor(String authorId);
    /** 제목 기준 공지사항 검색 (LIKE '%keyword%') */
    List<NoticeVO> searchByTitle(String keyword);
    /** 태그 기준 공지사항 검색 (tags 컬럼 부분 일치) */
    List<NoticeVO> searchByTag(String keyword);
}
