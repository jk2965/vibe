package com.vibe.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * 모든 게시판 VO의 공통 필드와 getter/setter를 정의하는 추상 클래스.
 * BoardEntity 인터페이스를 구현하여 AbstractBoardService<T extends BoardEntity>와 호환.
 *
 * 공통 필드: id, title, content, authorId, authorName, createdAt, views
 * 서브클래스: BoardVO, NoticeVO, TeamNoticeVO, ArchiveVO, TeamArchiveVO, FaqVO, QnaVO
 *
 * 서브클래스는 자신에게만 있는 필드(team, isRequired, tags, files, answers 등)만 선언하면 됨.
 *
 * [Builder Pattern] Lombok @SuperBuilder로 상속 가능한 빌더 자동 생성.
 * MyBatis ResultMap 호환을 위해 @NoArgsConstructor 유지.
 */
@SuperBuilder          // 빌더 패턴 자동 생성 — 상속 가능한 Builder 제공 (서브클래스에서 .builder() 사용 가능)
@Getter                // 모든 필드 getter 자동 생성
@Setter                // 모든 필드 setter 자동 생성
@NoArgsConstructor     // 기본 생성자 자동 생성 — MyBatis ResultMap 매핑에 필수
public abstract class AbstractBoardVO implements BoardEntity {

    /** 게시글 고유 식별자 (UUID) */
    private String id;
    /** 게시글 제목 */
    private String title;
    /** 게시글 본문 내용 (HTML 형식, Tiptap 에디터 출력) */
    private String content;
    /** 작성자 사용자 ID (UserVO의 id 참조) */
    private String authorId;
    /** 작성자 이름 (표시용) */
    private String authorName;
    /** 게시글 작성 일시 (문자열 형식: yyyy-MM-dd HH:mm:ss) */
    private String createdAt;
    /** 조회수 */
    private Integer views;
}
