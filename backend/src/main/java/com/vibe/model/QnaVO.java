package com.vibe.model;

import java.util.List;

/**
 * Q&A 질문 정보를 담는 Value Object 클래스.
 * AbstractBoardVO로부터 공통 필드(id, title, content, authorId, authorName, createdAt, views)를 상속.
 * QnaMapper.java, QnaService.java에서 사용됨.
 * answers 필드로 QnaAnswerVO.java 목록을 포함함(상세 조회 시).
 *
 * [Builder Pattern] QnaVO.builder().title("질문").authorId("user1").build()
 */
public class QnaVO extends AbstractBoardVO {

    /** 답변 수 (SELECT COUNT 서브쿼리로 집계) */
    private Integer answerCount;
    /** 답변 목록 (상세 조회 시 QnaAnswerVO 리스트로 채워짐) */
    private List<QnaAnswerVO> answers;

    /** answerCount 필드 반환 */
    public Integer getAnswerCount() { return answerCount; }
    /** answerCount 필드 설정 */
    public void setAnswerCount(Integer answerCount) { this.answerCount = answerCount; }
    /** answers 필드 반환 */
    public List<QnaAnswerVO> getAnswers() { return answers; }
    /** answers 필드 설정 */
    public void setAnswers(List<QnaAnswerVO> answers) { this.answers = answers; }

    /** Builder 인스턴스 반환 */
    public static Builder builder() { return new Builder(); }

    public static class Builder extends AbstractBoardVO.Builder<QnaVO, Builder> {
        private Integer answerCount;
        private List<QnaAnswerVO> answers;

        public Builder answerCount(Integer answerCount) { this.answerCount = answerCount; return this; }
        public Builder answers(List<QnaAnswerVO> answers) { this.answers = answers;       return this; }

        @Override
        public QnaVO build() {
            QnaVO vo = new QnaVO();
            applyTo(vo);
            vo.setAnswerCount(answerCount);
            vo.setAnswers(answers);
            return vo;
        }
    }
}
