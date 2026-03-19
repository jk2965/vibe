package com.vibe.mapper;

import com.vibe.model.ArchiveVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

// ArchiveMapper.xml과 연결됨 — 전사 자료실 게시글 관련 DB 접근 인터페이스
// ArchiveService.java에서 호출됨 / 첨부파일은 ArchiveFileMapper.java에서 별도 처리
@Mapper
public interface ArchiveMapper {
    // 전체 자료실 게시글 목록을 조회 (최신순 정렬)
    List<ArchiveVO> findAll();
    // 자료실 게시글 ID로 단일 게시글을 조회
    ArchiveVO findById(String id);
    // 새 자료실 게시글을 DB에 등록
    void insert(ArchiveVO archive);
    // 기존 자료실 게시글 내용을 수정
    void update(ArchiveVO archive);
    // 자료실 게시글 조회수를 1 증가 (상세 조회 시 호출)
    void incrementViews(String id);
    // 자료실 게시글을 DB에서 삭제 (첨부파일은 ArchiveFileMapper.deleteByBoardId로 별도 삭제)
    void delete(String id);
}
