package com.vibe.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.vibe.mapper.ArchiveFileMapper;
import com.vibe.mapper.ArchiveMapper;
import com.vibe.model.ArchiveVO;
import com.vibe.model.ArchiveFileVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * 자료실(Archive) 관련 비즈니스 로직을 처리하는 서비스 클래스.
 * ArchiveController.java에서 호출되며, ArchiveMapper.java와 ArchiveFileMapper.java를 통해 DB와 연동됨.
 * 파일 업로드/다운로드 처리를 포함하며, 게시글과 파일을 함께 관리함.
 * FileService.java와 달리 자료실 전용 파일(ArchiveFileMapper)을 직접 사용함.
 */
@Service
public class ArchiveService {

    // ArchiveMapper.java를 통해 ARCHIVE 테이블 CRUD 수행
    @Autowired
    private ArchiveMapper mapper;

    // ArchiveFileMapper.java를 통해 ARCHIVE_FILE 테이블 CRUD 수행
    @Autowired
    private ArchiveFileMapper fileMapper;

    /**
     * 자료실 게시글 목록을 페이지 단위로 조회 (페이지당 10개).
     * PageHelper를 사용해 자동 페이징 처리.
     * ArchiveMapper.java의 findAll()로 전체 목록 조회 후 PageInfo로 래핑.
     */
    public PageInfo<ArchiveVO> getList(int pageNum) {
        PageHelper.startPage(pageNum, 10);
        List<ArchiveVO> list = mapper.findAll();
        return new PageInfo<>(list);
    }

    /**
     * 자료실 게시글 상세 조회: 조회수 1 증가 후 게시글과 첨부파일 목록을 함께 반환.
     * ArchiveMapper.java의 incrementViews()로 조회수 증가.
     * ArchiveFileMapper.java의 findByBoardId()로 첨부파일 목록 조회.
     */
    public ArchiveVO getDetail(String id) {
        // 자료실 게시글 조회 시 조회수(views) 1 증가
        mapper.incrementViews(id);
        ArchiveVO archive = mapper.findById(id);
        if (archive != null) {
            // ArchiveFileMapper.java를 통해 해당 게시글의 첨부파일 목록 조회
            archive.setFiles(fileMapper.findByBoardId(id));
        }
        return archive;
    }

    /**
     * 새 자료실 게시글 작성: UUID로 고유 ID 생성, 현재 시각을 작성일로 설정 후 DB에 저장.
     * ArchiveMapper.java의 insert()로 ARCHIVE 테이블에 등록.
     */
    public ArchiveVO write(ArchiveVO archive) {
        // UUID를 사용해 유일한 게시글 ID 생성
        archive.setId(UUID.randomUUID().toString());
        // 현재 시각을 'yyyy-MM-dd HH:mm:ss' 형식으로 작성일 설정
        archive.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        mapper.insert(archive);
        return archive;
    }

    /**
     * 자료실 게시글 수정: 제목과 내용만 업데이트.
     * ArchiveMapper.java의 update()로 ARCHIVE 테이블 수정.
     */
    public void update(String id, String title, String content) {
        ArchiveVO archive = new ArchiveVO();
        archive.setId(id);
        archive.setTitle(title);
        archive.setContent(content);
        mapper.update(archive);
    }

    /**
     * 자료실 게시글 삭제: 첨부파일 먼저 삭제 후 게시글 본문 삭제.
     * ArchiveFileMapper.java의 deleteByBoardId()로 첨부파일 일괄 삭제.
     * ArchiveMapper.java의 delete()로 게시글 삭제.
     */
    public void delete(String id) {
        // 파일을 먼저 삭제하여 고아 파일(orphan file) 방지
        fileMapper.deleteByBoardId(id);
        mapper.delete(id);
    }

    /**
     * 자료실 파일 메타데이터 저장: 업로드된 파일 정보를 DB에 등록.
     * ArchiveController.java에서 파일 업로드 시 호출.
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
     * ArchiveController.java에서 파일 다운로드 시 파일 경로를 얻기 위해 호출.
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
}
