package com.vibe.model;

/**
 * Q&A 답변 정보를 담는 Value Object 클래스.
 * QnaAnswerMapper.java, QnaService.java에서 사용됨.
 * qnaId 필드로 QnaVO.java의 질문과 연결됨.
 */
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

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getQnaId() { return qnaId; }
    public void setQnaId(String qnaId) { this.qnaId = qnaId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }
    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
