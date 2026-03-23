package com.vibe.mapper;

import com.vibe.model.QnaAnswerVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Q&A 답변 관련 DB 접근 인터페이스.
 * QnaAnswerMapper.xml과 연결됨. QnaService.java에서 호출됨.
 */
@Mapper
public interface QnaAnswerMapper {
    /** 특정 질문(qnaId)에 달린 답변 목록 조회 (작성일 오름차순) */
    List<QnaAnswerVO> findByQnaId(String qnaId);
    /** 답변 ID로 단건 조회 */
    QnaAnswerVO findById(String id);
    /** 새 답변 등록 */
    void insert(QnaAnswerVO answer);
    /** 답변 내용 수정 */
    void update(QnaAnswerVO answer);
    /** 답변 삭제 */
    void delete(String id);
    /** 특정 질문의 모든 답변 삭제 (질문 삭제 시 cascade용) */
    void deleteByQnaId(String qnaId);
}
