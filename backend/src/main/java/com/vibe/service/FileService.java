package com.vibe.service;

import com.vibe.mapper.ArchiveFileMapper;
import com.vibe.model.ArchiveFileVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * 공통 파일 첨부 기능을 처리하는 서비스 클래스.
 * BoardService.java, NoticeService.java, TeamNoticeService.java에서 파일 처리 시 호출됨.
 * ArchiveFileMapper.java를 공유 파일 저장소로 사용하여 여러 게시판의 첨부파일을 통합 관리함.
 * 자료실(ArchiveService.java)은 별도로 ArchiveFileMapper를 직접 사용하여 파일을 관리함.
 */
@Service
public class FileService {

    // ArchiveFileMapper.java를 통해 ARCHIVE_FILE 테이블에서 파일 정보 CRUD 수행
    @Autowired
    private ArchiveFileMapper fileMapper;

    /**
     * 특정 게시글(boardId)에 첨부된 파일 목록 조회.
     * BoardService.java, NoticeService.java, TeamNoticeService.java의 getDetail()에서 호출.
     * ArchiveFileMapper.java의 findByBoardId()로 조회.
     */
    public List<ArchiveFileVO> getFilesByBoard(String boardId) {
        return fileMapper.findByBoardId(boardId);
    }

    /**
     * 파일 메타데이터 저장: 업로드된 파일 정보를 DB에 등록.
     * 각 게시판 컨트롤러에서 파일 업로드 시 호출.
     * ArchiveFileMapper.java의 insert()로 ARCHIVE_FILE 테이블에 등록.
     */
    public ArchiveFileVO saveFile(String boardId, String originalName, String storedName, long fileSize, String uploaderId) {
        ArchiveFileVO file = new ArchiveFileVO();
        // UUID를 사용해 유일한 파일 ID 생성
        file.setId(UUID.randomUUID().toString());
        file.setBoardId(boardId);
        file.setOriginalName(originalName);
        // 서버에 저장된 실제 파일명(UUID 기반으로 중복 방지)
        file.setStoredName(storedName);
        file.setFileSize(fileSize);
        file.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        file.setUploaderId(uploaderId);
        fileMapper.insert(file);
        return file;
    }

    /**
     * 파일 ID로 단건 파일 정보 조회.
     * 파일 다운로드 시 저장 경로(storedName)를 얻기 위해 컨트롤러에서 호출.
     * ArchiveFileMapper.java의 findById()로 조회.
     */
    public ArchiveFileVO getFile(String fileId) {
        return fileMapper.findById(fileId);
    }

    /**
     * 파일 ID로 단건 파일 삭제.
     * ArchiveFileMapper.java의 deleteById()로 해당 파일 레코드만 삭제.
     */
    public void deleteFile(String fileId) {
        fileMapper.deleteById(fileId);
    }

    /**
     * 특정 게시글(boardId)에 첨부된 파일 전체 삭제.
     * BoardService.java, NoticeService.java, TeamNoticeService.java의 delete()에서 게시글 삭제 전에 호출.
     * ArchiveFileMapper.java의 deleteByBoardId()로 해당 게시글의 모든 파일 레코드 일괄 삭제.
     */
    public void deleteFilesByBoard(String boardId) {
        fileMapper.deleteByBoardId(boardId);
    }
}
