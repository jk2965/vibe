package com.vibe.mapper;

import com.vibe.model.ArchiveVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 전사 자료실 게시글 관련 DB 접근 인터페이스.
 * ArchiveMapper.xml과 연결됨. ArchiveService.java에서 호출됨.
 * 첨부파일은 ArchiveFileMapper.java에서 별도 처리.
 */
@Mapper
public interface ArchiveMapper {
    /** 전체 자료실 게시글 목록 조회 (최신순, 파일 수·첫 파일명 포함) */
    List<ArchiveVO> findAll();
    /** 자료실 게시글 ID로 단건 조회 */
    ArchiveVO findById(String id);
    /** 새 자료실 게시글 등록 */
    void insert(ArchiveVO archive);
    /** 자료실 게시글 제목·내용·태그 수정 */
    void update(ArchiveVO archive);
    /** 조회수 1 증가 (상세 조회 시 호출) */
    void incrementViews(String id);
    /** 자료실 게시글 삭제 (첨부파일은 ArchiveFileMapper.deleteByBoardId로 별도 삭제) */
    void delete(String id);
    /** 필독 여부 설정 (1=필독, 0=해제) */
    void setRequired(@Param("id") String id, @Param("isRequired") int isRequired);
    /** 필독 자료실 게시글 목록 조회 */
    List<ArchiveVO> findRequired();
    /** 특정 작성자의 자료실 게시글 목록 조회 (내 게시글용) */
    List<ArchiveVO> findByAuthor(String authorId);
    /** 제목 기준 자료실 게시글 검색 (LIKE '%keyword%') */
    List<ArchiveVO> searchByTitle(String keyword);
    /** 태그 기준 자료실 게시글 검색 (tags 컬럼 부분 일치) */
    List<ArchiveVO> searchByTag(String keyword);
}
