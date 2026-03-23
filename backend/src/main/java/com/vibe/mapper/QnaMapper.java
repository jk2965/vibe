package com.vibe.mapper;

import com.vibe.model.QnaVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Q&A 질문 관련 DB 접근 인터페이스.
 * QnaMapper.xml과 연결됨. QnaService.java에서 호출됨.
 */
@Mapper
public interface QnaMapper {
    /** 전체 질문 목록 조회 (최신순, 답변 수 포함) */
    List<QnaVO> findAll();
    /** 질문 ID로 단건 조회 */
    QnaVO findById(String id);
    /** 새 질문 등록 */
    void insert(QnaVO qna);
    /** 질문 제목/내용 수정 */
    void update(QnaVO qna);
    /** 질문 삭제 */
    void delete(String id);
    /** 조회수 1 증가 */
    void incrementViews(String id);
    /** 작성자별 질문 목록 조회 (내 게시글용) */
    List<QnaVO> findByAuthor(String authorId);
}
