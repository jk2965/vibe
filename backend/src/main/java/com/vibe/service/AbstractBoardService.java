package com.vibe.service;

import com.vibe.model.BoardEntity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 모든 게시판 서비스의 공통 비즈니스 흐름을 정의하는 추상 클래스 (Template Method 패턴).
 * write(), delete()의 공통 흐름을 여기서 구현하고,
 * 매퍼 접근 등 게시판별 세부 구현은 추상 메서드(doXxx)로 서브클래스에 위임.
 * 서브클래스: BoardService, NoticeService, TeamNoticeService,
 *             ArchiveService, TeamArchiveService, FaqService, QnaService
 */
public abstract class AbstractBoardService<T extends BoardEntity> {

    // ── 서브클래스가 반드시 구현하는 매퍼 위임 메서드 ────────────────

    /** ID로 단건 조회 (조회수 증가 없음) */
    protected abstract T doFindById(String id);

    /** DB insert */
    protected abstract void doInsert(T item);

    /** DB update (title, content, tags) */
    protected abstract void doUpdate(String id, String title, String content, String tags);

    /** DB delete */
    protected abstract void doDelete(String id);

    /** 조회수 1 증가 */
    protected abstract void doIncrementViews(String id);

    /** 필독 여부 설정 (필독 기능이 없는 게시판은 빈 구현으로 오버라이드) */
    protected abstract void doSetRequired(String id, int value);

    // ── 훅 메서드 (필요한 서브클래스만 오버라이드) ───────────────────

    /**
     * write() 전 전처리 훅.
     * 예: isRequired 기본값 설정, 팀 유효성 검사 등
     */
    protected void beforeWrite(T item) {}

    /**
     * delete() 전 전처리 훅.
     * 예: 파일 삭제 (BoardService, ArchiveService 등),
     *     답변 삭제 (QnaService)
     */
    protected void beforeDelete(String id) {}

    /**
     * getDetail() 조회 후 연관 데이터 로딩 훅.
     * 예: 첨부파일 목록 설정 (setFiles), 답변 목록 설정 (setAnswers)
     */
    protected void loadRelations(T item) {}

    // ── 공통 흐름 구현 ────────────────────────────────────────────

    /**
     * 게시글 상세 조회: 조회수 증가 → 조회 → 연관 데이터 로딩.
     * loadRelations() 훅으로 첨부파일, 답변 등 추가 로딩 처리.
     */
    public T getDetail(String id) {
        doIncrementViews(id);
        T item = doFindById(id);
        if (item != null) loadRelations(item);
        return item;
    }

    /**
     * 게시글 작성: UUID 생성 → 작성일 설정 → beforeWrite 훅 → insert.
     * final로 선언하여 하위 클래스에서 흐름을 변경할 수 없도록 고정.
     */
    public final T write(T item) {
        item.setId(UUID.randomUUID().toString());
        item.setCreatedAt(LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        beforeWrite(item);
        doInsert(item);
        return item;
    }

    /**
     * 게시글 수정: doUpdate()로 서브클래스 매퍼에 위임.
     * QnaService처럼 tags가 없는 경우 doUpdate() 구현에서 무시하면 됨.
     */
    public void update(String id, String title, String content, String tags) {
        doUpdate(id, title, content, tags);
    }

    /**
     * 게시글 삭제: beforeDelete 훅 → doDelete().
     * final로 선언하여 beforeDelete 훅 호출 순서를 보장.
     */
    public final void delete(String id) {
        beforeDelete(id);
        doDelete(id);
    }

    /**
     * 필독 여부 설정: doSetRequired()로 서브클래스 매퍼에 위임.
     * 필독 기능이 없는 게시판(QnaService)은 doSetRequired()를 빈 구현으로 제공.
     */
    public void setRequired(String id, int value) {
        doSetRequired(id, value);
    }
}
