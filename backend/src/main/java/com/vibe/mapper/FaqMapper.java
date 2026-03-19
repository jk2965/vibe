package com.vibe.mapper;

import com.vibe.model.FaqVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

// FaqMapper.xml과 연결됨 — FAQ 게시판 관련 DB 접근 인터페이스
// FaqService.java에서 호출됨
@Mapper
public interface FaqMapper {
    // 전체 FAQ 목록을 조회 (최신순 정렬)
    List<FaqVO> findAll();
    // FAQ ID로 단일 FAQ 항목을 조회
    FaqVO findById(String id);
    // 새 FAQ 항목을 DB에 등록
    void insert(FaqVO faq);
    // 기존 FAQ 항목의 내용을 수정
    void update(FaqVO faq);
    // FAQ 조회수를 1 증가 (상세 조회 시 호출)
    void incrementViews(String id);
    // FAQ 항목을 DB에서 삭제
    void delete(String id);
}
