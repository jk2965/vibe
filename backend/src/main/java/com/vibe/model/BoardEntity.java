package com.vibe.model;

public interface BoardEntity {

    /** 게시글 고유 식별자 반환 */
    String getId();
    /** 게시글 고유 식별자 설정 */
    void setId(String id);
    /** 작성자 사용자 ID 반환 */
    String getAuthorId();
    /** 게시글 제목 반환 */
    String getTitle();
    /** 게시글 본문 내용 반환 */
    String getContent();
    /** 작성일 설정 — AbstractBoardService.write() 공통 흐름에서 호출 */
    void setCreatedAt(String createdAt);
}