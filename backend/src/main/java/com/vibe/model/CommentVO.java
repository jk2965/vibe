package com.vibe.model;

/**
 * 게시글 댓글(및 대댓글) 정보를 담는 Value Object 클래스.
 * CommentMapper.java, CommentService.java에서 사용되며,
 * BoardVO.java의 boardId를 통해 게시글과 연결됨.
 * parentId를 통해 대댓글(계층형 구조) 지원.
 */
public class CommentVO {

    /** 댓글 고유 식별자 */
    private String id;
    /** 댓글이 속한 게시글 ID (BoardVO.java의 id 참조) */
    private String boardId;
    /** 작성자 사용자 ID (UserVO.java의 id 참조) */
    private String authorId;
    /** 작성자 이름 (표시용) */
    private String authorName;
    /** 댓글 본문 내용 */
    private String content;
    /** 댓글 작성 일시 (문자열 형식) */
    private String createdAt;
    /** 부모 댓글 ID (대댓글인 경우 설정, 최상위 댓글은 null) */
    private String parentId;
    /** 삭제 여부 (1: 삭제됨, 0: 정상, 소프트 딜리트 방식) */
    private Integer isDeleted;

    /** id 필드 반환 */
    public String getId() { return id; }
    /** id 필드 설정 */
    public void setId(String id) { this.id = id; }
    /** boardId 필드 반환 */
    public String getBoardId() { return boardId; }
    /** boardId 필드 설정 */
    public void setBoardId(String boardId) { this.boardId = boardId; }
    /** authorId 필드 반환 */
    public String getAuthorId() { return authorId; }
    /** authorId 필드 설정 */
    public void setAuthorId(String authorId) { this.authorId = authorId; }
    /** authorName 필드 반환 */
    public String getAuthorName() { return authorName; }
    /** authorName 필드 설정 */
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    /** content 필드 반환 */
    public String getContent() { return content; }
    /** content 필드 설정 */
    public void setContent(String content) { this.content = content; }
    /** createdAt 필드 반환 */
    public String getCreatedAt() { return createdAt; }
    /** createdAt 필드 설정 */
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    /** parentId 필드 반환 */
    public String getParentId() { return parentId; }
    /** parentId 필드 설정 */
    public void setParentId(String parentId) { this.parentId = parentId; }
    /** isDeleted 필드 반환 */
    public Integer getIsDeleted() { return isDeleted; }
    /** isDeleted 필드 설정 */
    public void setIsDeleted(Integer isDeleted) { this.isDeleted = isDeleted; }
}
