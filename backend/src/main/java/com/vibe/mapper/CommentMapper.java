package com.vibe.mapper;

import com.vibe.model.CommentVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

// CommentMapper.xml과 연결됨 — 게시글 댓글 관련 DB 접근 인터페이스
// CommentService.java에서 호출됨
@Mapper
public interface CommentMapper {
    // 특정 게시글(boardId)에 달린 댓글 목록을 조회 (삭제된 댓글 포함)
    List<CommentVO> findByBoardId(String boardId);
    // 댓글 ID로 단일 댓글을 조회
    CommentVO findById(String id);
    // 새 댓글을 DB에 등록
    void insert(CommentVO comment);
    // 기존 댓글 내용을 수정
    void update(CommentVO comment);
    // 댓글을 소프트 삭제 처리 (실제 삭제가 아닌 deleted 플래그 설정)
    void softDelete(String id);
}
