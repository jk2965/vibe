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
 * Q&A 게시판 비즈니스 로직 처리 서비스.
 * QnaController.java에서 호출되며, QnaMapper.java와 QnaAnswerMapper.java를 통해 DB와 연동됨.
 */
@Service
public class QnaService {

    /** Q&A 질문 DB 접근 */
    @Autowired
    private QnaMapper qnaMapper;

    /** Q&A 답변 DB 접근 */
    @Autowired
    private QnaAnswerMapper answerMapper;

    /**
     * 질문 목록을 페이지 단위로 조회 (페이지당 10개, 최신순).
     * PageHelper로 자동 페이징 처리.
     */
    public PageInfo<QnaVO> getList(int pageNum) {
        PageHelper.startPage(pageNum, 10);
        List<QnaVO> list = qnaMapper.findAll();
        return new PageInfo<>(list);
    }

    /**
     * 질문 상세 조회: 조회수 1 증가 후 질문 본문과 답변 목록을 함께 반환.
     * QnaAnswerMapper.java의 findByQnaId()로 답변 목록 조회.
     */
    public QnaVO getDetail(String id) {
        // 조회수 1 증가
        qnaMapper.incrementViews(id);
        QnaVO qna = qnaMapper.findById(id);
        if (qna != null) {
            // 해당 질문의 답변 목록을 함께 조회하여 반환
            qna.setAnswers(answerMapper.findByQnaId(id));
        }
        return qna;
    }

    /**
     * 새 질문 작성: UUID로 고유 ID 생성, 현재 시각을 작성일로 설정.
     */
    public QnaVO write(QnaVO qna) {
        qna.setId(UUID.randomUUID().toString());
        qna.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        qnaMapper.insert(qna);
        return qna;
    }

    /**
     * 질문 수정: 제목과 내용만 업데이트.
     */
    public void update(String id, String title, String content) {
        QnaVO qna = new QnaVO();
        qna.setId(id);
        qna.setTitle(title);
        qna.setContent(content);
        qnaMapper.update(qna);
    }

    /**
     * 질문 삭제: 연결된 답변을 먼저 삭제(cascade)한 후 질문 삭제.
     * 고아 답변(orphan answer) 방지를 위해 답변을 먼저 삭제함.
     */
    public void delete(String id) {
        answerMapper.deleteByQnaId(id);
        qnaMapper.delete(id);
    }

    /**
     * 질문 단건 조회 (권한 확인 등 내부 용도, 조회수 증가 없음).
     */
    public QnaVO findById(String id) {
        return qnaMapper.findById(id);
    }

    /**
     * 답변 작성: UUID로 고유 ID 생성, 현재 시각을 작성일로 설정.
     * 관리자 또는 팀장만 호출 가능 (컨트롤러에서 권한 검사).
     */
    public QnaAnswerVO writeAnswer(QnaAnswerVO answer) {
        answer.setId(UUID.randomUUID().toString());
        answer.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        answerMapper.insert(answer);
        return answer;
    }

    /**
     * 답변 단건 조회 (수정/삭제 권한 확인용).
     */
    public QnaAnswerVO findAnswerById(String answerId) {
        return answerMapper.findById(answerId);
    }

    /**
     * 답변 수정: 내용만 업데이트.
     */
    public void updateAnswer(String answerId, String content) {
        QnaAnswerVO answer = new QnaAnswerVO();
        answer.setId(answerId);
        answer.setContent(content);
        answerMapper.update(answer);
    }

    /**
     * 답변 단건 삭제.
     */
    public void deleteAnswer(String answerId) {
        answerMapper.delete(answerId);
    }
}
