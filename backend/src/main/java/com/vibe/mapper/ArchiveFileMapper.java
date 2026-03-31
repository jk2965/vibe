package com.vibe.mapper;

import com.vibe.model.ArchiveFileVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 게시글 첨부파일 관련 DB 접근 인터페이스.
 * ArchiveFileMapper.xml과 연결됨. FileService.java에서 호출됨.
 * 자유게시판·공지사항·자료실 등 모든 게시판의 첨부파일을 통합 관리.
 */
@Mapper
public interface ArchiveFileMapper {
    /** 첨부파일 정보 등록 (게시글 첨부파일 업로드 시 호출) */
    void insert(ArchiveFileVO file);
    /** 특정 게시글(boardId)에 첨부된 파일 목록 조회 */
    List<ArchiveFileVO> findByBoardId(String boardId);
    /** 파일 ID로 단건 조회 (다운로드 시 storedName 조회용) */
    ArchiveFileVO findById(String id);
    /** 특정 게시글의 첨부파일 전체 삭제 (게시글 삭제 시 cascade 삭제용) */
    void deleteByBoardId(String boardId);
    /** 파일 ID로 단건 삭제 (개별 파일 삭제 시 호출) */
    void deleteById(String id);
}
