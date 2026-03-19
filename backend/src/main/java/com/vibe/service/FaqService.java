package com.vibe.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.vibe.mapper.FaqMapper;
import com.vibe.model.FaqVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * FAQ 게시판 관련 비즈니스 로직을 처리하는 서비스 클래스.
 * FaqController.java에서 호출되며, FaqMapper.java를 통해 DB와 연동됨.
 * 파일 첨부 기능이 없으며 텍스트 기반의 Q&A 데이터만 관리함.
 * 관리자(isAdmin >= 1)만 작성/수정/삭제 가능하도록 컨트롤러에서 권한 체크됨.
 */
@Service
public class FaqService {

    // FaqMapper.java를 통해 FAQ 테이블 CRUD 수행
    @Autowired
    private FaqMapper faqMapper;

    /**
     * FAQ 목록을 페이지 단위로 조회 (페이지당 10개).
     * PageHelper를 사용해 자동 페이징 처리.
     * FaqMapper.java의 findAll()로 전체 목록 조회 후 PageInfo로 래핑.
     */
    public PageInfo<FaqVO> getList(int pageNum) {
        PageHelper.startPage(pageNum, 10);
        List<FaqVO> list = faqMapper.findAll();
        return new PageInfo<>(list);
    }

    /**
     * FAQ 상세 조회: 조회수 1 증가 후 해당 FAQ 데이터 반환.
     * FaqMapper.java의 incrementViews()로 조회수 증가.
     * FaqMapper.java의 findById()로 단건 조회.
     */
    public FaqVO getDetail(String id) {
        // FAQ 조회 시 조회수(views) 1 증가
        faqMapper.incrementViews(id);
        return faqMapper.findById(id);
    }

    /**
     * 새 FAQ 작성: UUID로 고유 ID 생성, 현재 시각을 작성일로 설정 후 DB에 저장.
     * FaqMapper.java의 insert()로 FAQ 테이블에 등록.
     */
    public FaqVO write(FaqVO faq) {
        // UUID를 사용해 유일한 FAQ ID 생성
        faq.setId(UUID.randomUUID().toString());
        // 현재 시각을 'yyyy-MM-dd HH:mm:ss' 형식으로 작성일 설정
        faq.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        faqMapper.insert(faq);
        return faq;
    }

    /**
     * FAQ 수정: 제목과 내용만 업데이트.
     * FaqMapper.java의 update()로 FAQ 테이블 수정.
     */
    public void update(String id, String title, String content) {
        FaqVO faq = new FaqVO();
        faq.setId(id);
        faq.setTitle(title);
        faq.setContent(content);
        faqMapper.update(faq);
    }

    /**
     * FAQ 삭제: 파일 첨부가 없으므로 바로 DB에서 삭제.
     * FaqMapper.java의 delete()로 FAQ 테이블에서 제거.
     */
    public void delete(String id) {
        faqMapper.delete(id);
    }
}
