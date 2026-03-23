package com.vibe.model;

import java.util.List;

/**
 * Q&A 질문 정보를 담는 Value Object 클래스.
 * QnaMapper.java, QnaService.java에서 사용됨.
 * answers 필드로 QnaAnswerVO.java 목록을 포함함(상세 조회 시).
 */
public class QnaVO {

    /** 질문 고유 식별자 (UUID) */
    private String id;
    /** 질문 제목 */
    private String title;
    /** 질문 본문 (HTML 형식, Tiptap 에디터 출력) */
    private String content;
    /** 작성자 사용자 ID */
    private String authorId;
    /** 작성자 이름 (표시용) */
    private String authorName;
    /** 작성 일시 (문자열 형식: yyyy-MM-dd HH:mm:ss) */
    private String createdAt;
    /** 조회수 */
    private Integer views;
    /** 답변 수 (SELECT COUNT 서브쿼리로 집계) */
    private Integer answerCount;
    /** 답변 목록 (상세 조회 시 QnaAnswerVO 리스트로 채워짐) */
    private List<QnaAnswerVO> answers;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }
    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public Integer getViews() { return views; }
    public void setViews(Integer views) { this.views = views; }
    public Integer getAnswerCount() { return answerCount; }
    public void setAnswerCount(Integer answerCount) { this.answerCount = answerCount; }
    public List<QnaAnswerVO> getAnswers() { return answers; }
    public void setAnswers(List<QnaAnswerVO> answers) { this.answers = answers; }
}
