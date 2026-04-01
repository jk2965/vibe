package com.vibe.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * Q&A 질문 정보를 담는 Value Object 클래스.
 * AbstractBoardVO로부터 공통 필드(id, title, content, authorId, authorName, createdAt, views)를 상속.
 * QnaMapper.java, QnaService.java에서 사용됨.
 * answers 필드로 QnaAnswerVO.java 목록을 포함함(상세 조회 시).
 *
 * [Builder Pattern] QnaVO.builder().title("질문").authorId("user1").build()
 */
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
public class QnaVO extends AbstractBoardVO {

    /** 답변 수 (SELECT COUNT 서브쿼리로 집계) */
    private Integer answerCount;
    /** 답변 목록 (상세 조회 시 QnaAnswerVO 리스트로 채워짐) */
    private List<QnaAnswerVO> answers;
}
