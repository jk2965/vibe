package com.vibe.mapper;

import com.vibe.model.ArchiveFileVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

// ArchiveFileMapper.xml과 연결됨 — 자료실 첨부파일 관련 DB 접근 인터페이스
// ArchiveService.java 및 FileService.java에서 호출됨 / ArchiveMapper.java와 연계하여 사용
@Mapper
public interface ArchiveFileMapper {
    // 새 첨부파일 정보를 DB에 등록 (게시글 등록·수정 시 호출)
    void insert(ArchiveFileVO file);
    // 특정 자료실 게시글(boardId)에 첨부된 파일 목록을 조회
    List<ArchiveFileVO> findByBoardId(String boardId);
    // 첨부파일 ID로 단일 첨부파일 정보를 조회 (파일 다운로드 시 호출)
    ArchiveFileVO findById(String id);
    // 특정 자료실 게시글(boardId)에 연결된 첨부파일 전체를 삭제 (게시글 삭제 시 연쇄 삭제)
    void deleteByBoardId(String boardId);
    // 첨부파일 ID로 단일 첨부파일을 삭제 (개별 파일 삭제 시 호출)
    void deleteById(String id);
}
