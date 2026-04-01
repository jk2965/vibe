package com.vibe.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Q&A 답변 정보를 담는 Value Object 클래스.
 * QnaAnswerMapper.java, QnaService.java에서 사용됨.
 * qnaId 필드로 QnaVO.java의 질문과 연결됨.
 */
@Getter
@Setter
@NoArgsConstructor
public class QnaAnswerVO {

    /** 답변 고유 식별자 (UUID) */
    private String id;
    /** 연결된 질문 ID (QnaVO.java의 id 참조) */
    private String qnaId;
    /** 답변 본문 내용 */
    private String content;
    /** 작성자 사용자 ID */
    private String authorId;
    /** 작성자 이름 (표시용) */
    private String authorName;
    /** 작성 일시 (문자열 형식: yyyy-MM-dd HH:mm:ss) */
    private String createdAt;
}
