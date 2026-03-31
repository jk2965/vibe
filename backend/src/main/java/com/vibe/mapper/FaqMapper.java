package com.vibe.mapper;

import com.vibe.model.FaqVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * FAQ 게시판 관련 DB 접근 인터페이스.
 * FaqMapper.xml과 연결됨. FaqService.java에서 호출됨.
 */
@Mapper
public interface FaqMapper {
    /** 전체 FAQ 목록 조회 (최신순 정렬) */
    List<FaqVO> findAll();
    /** FAQ ID로 단건 조회 */
    FaqVO findById(String id);
    /** 새 FAQ 등록 */
    void insert(FaqVO faq);
    /** FAQ 질문·답변 내용 수정 */
    void update(FaqVO faq);
    /** 조회수 1 증가 (상세 조회 시 호출) */
    void incrementViews(String id);
    /** FAQ 삭제 */
    void delete(String id);
}
