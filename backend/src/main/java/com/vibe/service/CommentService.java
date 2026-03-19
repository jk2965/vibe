package com.vibe.service;

import com.vibe.mapper.CommentMapper;
import com.vibe.model.CommentVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * 댓글(Comment) 관련 비즈니스 로직을 처리하는 서비스 클래스.
 * CommentController.java에서 호출되며, CommentMapper.java를 통해 DB와 연동됨.
 * 자유게시판(Board)의 게시글 ID를 기준으로 댓글을 관리함.
 */
@Service
public class CommentService {

    // CommentMapper.java를 통해 COMMENT 테이블 CRUD 수행
    @Autowired
    private CommentMapper mapper;

    /**
     * 특정 게시글(boardId)에 달린 댓글 목록 조회.
     * CommentMapper.java의 findByBoardId()로 해당 게시글의 댓글만 조회.
     */
    public List<CommentVO> getByBoardId(String boardId) {
        return mapper.findByBoardId(boardId);
    }

    /**
     * 새 댓글 작성: UUID로 고유 ID 생성, 현재 시각을 작성일로 설정 후 DB에 저장.
     * CommentMapper.java의 insert()로 COMMENT 테이블에 등록.
     */
    public CommentVO write(CommentVO comment) {
        // UUID를 사용해 유일한 댓글 ID 생성
        comment.setId(UUID.randomUUID().toString());
        // 현재 시각을 'yyyy-MM-dd HH:mm:ss' 형식으로 작성일 설정
        comment.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        mapper.insert(comment);
        return comment;
    }

    /**
     * 댓글 내용 수정.
     * CommentMapper.java의 update()로 내용(content)만 업데이트.
     */
    public void update(String id, String content) {
        CommentVO comment = new CommentVO();
        comment.setId(id);
        comment.setContent(content);
        mapper.update(comment);
    }

    /**
     * 댓글 ID로 단건 조회.
     * CommentMapper.java의 findById()로 조회. 권한 검사 등에 활용됨.
     */
    public CommentVO findById(String id) {
        return mapper.findById(id);
    }

    /**
     * 댓글 소프트 삭제(soft delete): 실제 데이터를 삭제하지 않고 삭제 플래그만 설정.
     * CommentMapper.java의 softDelete()로 IS_DELETED 컬럼 등을 업데이트.
     */
    public void delete(String id) {
        mapper.softDelete(id);
    }
}
