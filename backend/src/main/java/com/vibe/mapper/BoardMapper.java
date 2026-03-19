package com.vibe.mapper;

import com.vibe.model.BoardVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

// BoardMapper.xml과 연결됨 — 자유게시판 게시글 관련 DB 접근 인터페이스
// BoardService.java에서 호출됨
@Mapper
public interface BoardMapper {
    // 전체 게시글 목록을 조회 (최신순 정렬)
    List<BoardVO> findAll();
    // 게시글 ID로 단일 게시글을 조회
    BoardVO findById(String id);
    // 새 게시글을 DB에 등록
    void insert(BoardVO board);
    // 기존 게시글 내용을 수정
    void update(BoardVO board);
    // 게시글 조회수를 1 증가 (게시글 상세 조회 시 호출)
    void incrementViews(String id);
    // 게시글을 DB에서 삭제
    void delete(String id);
    // 게시글 필독 여부를 설정 (1=필독, 0=해제)
    void setRequired(@Param("id") String id, @Param("isRequired") int isRequired);
    // 필독 게시글 목록을 조회
    List<BoardVO> findRequired();
}
