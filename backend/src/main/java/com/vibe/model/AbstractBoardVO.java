package com.vibe.model;

/**
 * 모든 게시판 VO의 공통 필드와 getter/setter를 정의하는 추상 클래스.
 * BoardEntity 인터페이스를 구현하여 AbstractBoardService<T extends BoardEntity>와 호환.
 *
 * 공통 필드: id, title, content, authorId, authorName, createdAt, views
 * 서브클래스: BoardVO, NoticeVO, TeamNoticeVO, ArchiveVO, TeamArchiveVO, FaqVO, QnaVO
 *
 * 서브클래스는 자신에게만 있는 필드(team, isRequired, tags, files, answers 등)만 선언하면 됨.
 *
 * [Builder Pattern]
 * 내부 추상 Builder<T, B>를 제공하여 서브클래스가 공통 필드 설정 메서드를 재사용할 수 있도록 함.
 * 서브클래스 Builder는 AbstractBoardVO.Builder를 상속하고 build()만 구현하면 됨.
 * MyBatis ResultMap 호환을 위해 no-arg constructor와 setter는 그대로 유지.
 */
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

    @Override public String getId()                      { return id; }
    @Override public void setId(String id)               { this.id = id; }
    @Override public String getTitle()                   { return title; }
    public void setTitle(String title)                   { this.title = title; }
    @Override public String getContent()                 { return content; }
    public void setContent(String content)               { this.content = content; }
    @Override public String getAuthorId()                { return authorId; }
    public void setAuthorId(String authorId)             { this.authorId = authorId; }
    public String getAuthorName()                        { return authorName; }
    public void setAuthorName(String authorName)         { this.authorName = authorName; }
    public String getCreatedAt()                         { return createdAt; }
    @Override public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public Integer getViews()                            { return views; }
    public void setViews(Integer views)                  { this.views = views; }

    // ── Builder 베이스 (CRTP: Curiously Recurring Template Pattern) ──────────
    //
    // T: 생성할 VO 타입 (예: BoardVO)
    // B: 서브클래스 Builder 자신의 타입 — 메서드 체이닝 시 서브클래스 타입 유지에 필요
    //
    // 서브클래스 사용 예:
    //   public static class Builder extends AbstractBoardVO.Builder<BoardVO, Builder> {
    //       public BoardVO build() { BoardVO vo = new BoardVO(); applyTo(vo); return vo; }
    //   }

    protected static abstract class Builder<T extends AbstractBoardVO, B extends Builder<T, B>> {

        private String id;
        private String title;
        private String content;
        private String authorId;
        private String authorName;
        private String createdAt;
        private Integer views;

        /** unchecked cast를 한 곳에 격리 — 각 setter 메서드에서 사용 */
        @SuppressWarnings("unchecked")
        private B self() { return (B) this; }

        public B id(String id)                 { this.id = id;               return self(); }
        public B title(String title)           { this.title = title;         return self(); }
        public B content(String content)       { this.content = content;     return self(); }
        public B authorId(String authorId)     { this.authorId = authorId;   return self(); }
        public B authorName(String authorName) { this.authorName = authorName; return self(); }
        public B createdAt(String createdAt)   { this.createdAt = createdAt; return self(); }
        public B views(Integer views)          { this.views = views;         return self(); }

        /** 서브클래스 build()에서 호출 — 공통 필드를 생성된 vo에 주입 */
        protected void applyTo(T vo) {
            vo.setId(id);
            vo.setTitle(title);
            vo.setContent(content);
            vo.setAuthorId(authorId);
            vo.setAuthorName(authorName);
            vo.setCreatedAt(createdAt);
            vo.setViews(views);
        }

        /** 서브클래스가 반드시 구현 — 구체 VO 인스턴스를 생성하고 applyTo()를 호출 */
        public abstract T build();
    }
}
