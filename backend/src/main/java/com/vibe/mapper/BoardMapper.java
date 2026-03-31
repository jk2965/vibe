package com.vibe.mapper;

import com.vibe.model.BoardVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 자유게시판 게시글 관련 DB 접근 인터페이스.
 * BoardMapper.xml과 연결됨. BoardService.java에서 호출됨.
 */
@Mapper
public interface BoardMapper {
    /** 전체 게시글 목록 조회 (최신순, 필독 우선 정렬) */
    List<BoardVO> findAll();
    /** 게시글 ID로 단건 조회 */
    BoardVO findById(String id);
    /** 새 게시글 등록 */
    void insert(BoardVO board);
    /** 게시글 제목·내용·태그 수정 */
    void update(BoardVO board);
    /** 조회수 1 증가 (상세 조회 시 호출) */
    void incrementViews(String id);
    /** 게시글 삭제 */
    void delete(String id);
    /** 필독 여부 설정 (1=필독, 0=해제) */
    void setRequired(@Param("id") String id, @Param("isRequired") int isRequired);
    /** 필독 게시글 목록 조회 */
    List<BoardVO> findRequired();
    /** 특정 작성자의 게시글 목록 조회 (내 게시글용) */
    List<BoardVO> findByAuthor(String authorId);
    /** 제목 기준 게시글 검색 (LIKE '%keyword%') */
    List<BoardVO> searchByTitle(String keyword);
    /** 태그 기준 게시글 검색 (tags 컬럼 부분 일치) */
    List<BoardVO> searchByTag(String keyword);
}
