package com.vibe.mapper;

import com.vibe.model.NoticeVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

// NoticeMapper.xml과 연결됨 — 전사 공지사항 관련 DB 접근 인터페이스
// NoticeService.java에서 호출됨
@Mapper
public interface NoticeMapper {
    // 전체 공지사항 목록을 조회 (최신순 정렬)
    List<NoticeVO> findAll();
    // 공지사항 ID로 단일 공지사항을 조회
    NoticeVO findById(String id);
    // 새 공지사항을 DB에 등록
    void insert(NoticeVO notice);
    // 기존 공지사항 내용을 수정
    void update(NoticeVO notice);
    // 공지사항 조회수를 1 증가 (상세 조회 시 호출)
    void incrementViews(String id);
    // 공지사항을 DB에서 삭제
    void delete(String id);
    // 공지사항 필독 여부를 설정 (1=필독, 0=해제)
    void setRequired(@Param("id") String id, @Param("isRequired") int isRequired);
    // 필독 공지사항 목록을 조회
    List<NoticeVO> findRequired();
    // 특정 작성자의 공지사항 목록을 조회
    List<NoticeVO> findByAuthor(String authorId);
}
