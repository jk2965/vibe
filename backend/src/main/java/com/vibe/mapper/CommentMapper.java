package com.vibe.mapper;

import com.vibe.model.CommentVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 게시글 댓글(및 대댓글) 관련 DB 접근 인터페이스.
 * CommentMapper.xml과 연결됨. CommentService.java에서 호출됨.
 */
@Mapper
public interface CommentMapper {
    /** 특정 게시글(boardId)에 달린 댓글 목록 조회 (소프트 삭제된 댓글 포함) */
    List<CommentVO> findByBoardId(String boardId);
    /** 댓글 ID로 단건 조회 */
    CommentVO findById(String id);
    /** 새 댓글 등록 */
    void insert(CommentVO comment);
    /** 댓글 내용 수정 */
    void update(CommentVO comment);
    /** 댓글 소프트 삭제 (is_deleted 플래그 설정, 실제 데이터는 유지) */
    void softDelete(String id);
}
