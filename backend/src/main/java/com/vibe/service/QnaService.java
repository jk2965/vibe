package com.vibe.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.vibe.mapper.QnaAnswerMapper;
import com.vibe.mapper.QnaMapper;
import com.vibe.model.QnaAnswerVO;
import com.vibe.model.QnaVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Q&A 게시판 비즈니스 로직 서비스.
 * AbstractBoardService의 공통 흐름을 상속.
 * Q&A는 tags/isRequired/필독 기능이 없으므로 해당 메서드는 빈 구현 제공.
 * 답변 관련 메서드(writeAnswer, updateAnswer, deleteAnswer)는 Q&A 전용으로 유지.
 */
@Service
public class QnaService extends AbstractBoardService<QnaVO> {

    @Autowired private QnaMapper qnaMapper;
    @Autowired private QnaAnswerMapper answerMapper;

    /** Q&A 질문 목록 페이징 조회 (페이지당 10개) */
    public PageInfo<QnaVO> getList(int pageNum) {
        PageHelper.startPage(pageNum, 10);
        List<QnaVO> list = qnaMapper.findAll();
        return new PageInfo<>(list);
    }

    // ── AbstractBoardService 추상 메서드 구현 ────────────────────

    @Override protected QnaVO doFindById(String id)          { return qnaMapper.findById(id); }
    @Override protected void doInsert(QnaVO item)            { qnaMapper.insert(item); }
    @Override protected void doDelete(String id)             { qnaMapper.delete(id); }
    @Override protected void doIncrementViews(String id)     { qnaMapper.incrementViews(id); }

    /** Q&A는 tags 컬럼이 없으므로 title/content만 업데이트 */
    @Override
    protected void doUpdate(String id, String title, String content, String tags) {
        QnaVO q = new QnaVO();
        q.setId(id); q.setTitle(title); q.setContent(content);
        qnaMapper.update(q);
    }

    /** Q&A는 필독 기능 없음 — 빈 구현 */
    @Override protected void doSetRequired(String id, int v) { }

    // ── 훅 메서드 ────────────────────────────────────────────────

    /** delete() 전: 연결된 답변 먼저 삭제 (고아 답변 방지) */
    @Override
    protected void beforeDelete(String id) {
        answerMapper.deleteByQnaId(id);
    }

    /** getDetail() 후: 답변 목록 로딩 */
    @Override
    protected void loadRelations(QnaVO item) {
        item.setAnswers(answerMapper.findByQnaId(item.getId()));
    }

    // ── Q&A 전용: 답변 CRUD 메서드 ──────────────────────────────

    /** 답변 단건 조회 (권한 확인용, 조회수 증가 없음) */
    public QnaVO findById(String id) { return qnaMapper.findById(id); }

    /** 새 답변 작성: UUID 생성 + 작성일 설정 후 insert */
    public QnaAnswerVO writeAnswer(QnaAnswerVO answer) {
        answer.setId(UUID.randomUUID().toString());
        answer.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        answerMapper.insert(answer);
        return answer;
    }

    /** 답변 단건 조회 (수정/삭제 권한 확인용) */
    public QnaAnswerVO findAnswerById(String answerId) { return answerMapper.findById(answerId); }

    /** 답변 수정: 내용만 업데이트 */
    public void updateAnswer(String answerId, String content) {
        QnaAnswerVO answer = new QnaAnswerVO();
        answer.setId(answerId);
        answer.setContent(content);
        answerMapper.update(answer);
    }

    /** 답변 단건 삭제 */
    public void deleteAnswer(String answerId) { answerMapper.delete(answerId); }
}
