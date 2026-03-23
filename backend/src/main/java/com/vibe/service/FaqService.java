package com.vibe.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.vibe.mapper.ArchiveFileMapper;
import com.vibe.mapper.FaqMapper;
import com.vibe.model.ArchiveFileVO;
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
 * 첨부 파일은 archive_file 테이블을 공유하며, ArchiveFileMapper.java를 통해 처리됨.
 * 관리자(isAdmin >= 1)만 작성/수정/삭제 가능하도록 컨트롤러에서 권한 체크됨.
 */
@Service
public class FaqService {

    // FaqMapper.java를 통해 FAQ 테이블 CRUD 수행
    @Autowired
    private FaqMapper faqMapper;

    // ArchiveFileMapper.java를 통해 첨부파일 CRUD 수행 (archive_file 테이블 공유)
    @Autowired
    private ArchiveFileMapper fileMapper;

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
     * FAQ 상세 조회: 조회수 1 증가 후 해당 FAQ 데이터와 첨부파일 목록을 함께 반환.
     * FaqMapper.java의 incrementViews()로 조회수 증가.
     * FaqMapper.java의 findById()로 단건 조회 후 ArchiveFileMapper로 파일 목록 첨부.
     */
    public FaqVO getDetail(String id) {
        // FAQ 조회 시 조회수(views) 1 증가
        faqMapper.incrementViews(id);
        FaqVO faq = faqMapper.findById(id);
        if (faq != null) {
            // ArchiveFileMapper를 통해 해당 FAQ의 첨부파일 목록 조회 (archive_file 테이블 공유)
            faq.setFiles(fileMapper.findByBoardId(id));
        }
        return faq;
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
     * FAQ 삭제: 첨부파일 먼저 삭제 후 FAQ 본문 삭제 (고아 파일 방지).
     * ArchiveFileMapper.java의 deleteByBoardId()로 첨부파일 일괄 삭제.
     * FaqMapper.java의 delete()로 FAQ 테이블에서 제거.
     */
    public void delete(String id) {
        // 첨부파일 먼저 삭제 (archive_file 테이블에서 해당 FAQ ID 파일 전체 제거)
        fileMapper.deleteByBoardId(id);
        faqMapper.delete(id);
    }

    /**
     * FAQ 첨부파일 메타데이터 저장: 업로드된 파일 정보를 archive_file 테이블에 등록.
     * FaqController.java에서 파일 업로드 시 호출.
     */
    public ArchiveFileVO saveFile(String boardId, String originalName, String storedName, long fileSize, String uploaderId) {
        ArchiveFileVO file = new ArchiveFileVO();
        // UUID를 사용해 유일한 파일 ID 생성
        file.setId(UUID.randomUUID().toString());
        file.setBoardId(boardId);
        file.setOriginalName(originalName);
        // 서버에 저장된 실제 파일명 (UUID 기반으로 중복 방지)
        file.setStoredName(storedName);
        file.setFileSize(fileSize);
        file.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        file.setUploaderId(uploaderId);
        fileMapper.insert(file);
        return file;
    }

    /**
     * 파일 ID로 단건 파일 정보 조회.
     * FaqController.java에서 파일 다운로드 시 파일 경로를 얻기 위해 호출.
     */
    public ArchiveFileVO getFile(String fileId) {
        return fileMapper.findById(fileId);
    }

    /**
     * 파일 ID로 단건 파일 삭제.
     * FaqController.java에서 개별 파일 삭제 시 호출.
     */
    public void deleteFile(String fileId) {
        fileMapper.deleteById(fileId);
    }
}
