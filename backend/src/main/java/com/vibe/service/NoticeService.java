package com.vibe.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.vibe.mapper.NoticeMapper;
import com.vibe.model.NoticeVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * 공지사항(Notice) 관련 비즈니스 로직을 처리하는 서비스 클래스.
 * NoticeController.java에서 호출되며, NoticeMapper.java를 통해 DB와 연동됨.
 * 파일 첨부 기능은 FileService.java에 위임함.
 * 관리자(isAdmin >= 1)만 작성/수정/삭제 가능하도록 컨트롤러에서 권한 체크됨.
 */
@Service
public class NoticeService {

    // NoticeMapper.java를 통해 NOTICE 테이블 CRUD 수행
    @Autowired
    private NoticeMapper mapper;

    // 게시글에 첨부된 파일 처리를 위해 FileService.java를 주입
    @Autowired
    private FileService fileService;

    /**
     * 공지사항 목록을 페이지 단위로 조회 (페이지당 10개).
     * PageHelper를 사용해 자동 페이징 처리.
     * NoticeMapper.java의 findAll()로 전체 목록 조회 후 PageInfo로 래핑.
     */
    public PageInfo<NoticeVO> getList(int pageNum) {
        PageHelper.startPage(pageNum, 10);
        List<NoticeVO> list = mapper.findAll();
        return new PageInfo<>(list);
    }

    /**
     * 공지사항 상세 조회: 조회수 1 증가 후 공지사항과 첨부파일 목록을 함께 반환.
     * NoticeMapper.java의 incrementViews()로 조회수 증가.
     * FileService.java의 getFilesByBoard()로 첨부파일 조회.
     */
    public NoticeVO getDetail(String id) {
        // 공지사항 조회 시 조회수(views) 1 증가
        mapper.incrementViews(id);
        NoticeVO notice = mapper.findById(id);
        if (notice != null) {
            // FileService.java를 통해 해당 공지사항의 첨부파일 목록 조회
            notice.setFiles(fileService.getFilesByBoard(id));
        }
        return notice;
    }

    /**
     * 새 공지사항 작성: UUID로 고유 ID 생성, 현재 시각을 작성일로 설정 후 DB에 저장.
     * NoticeMapper.java의 insert()로 NOTICE 테이블에 등록.
     */
    public NoticeVO write(NoticeVO notice) {
        // UUID를 사용해 유일한 공지사항 ID 생성
        notice.setId(UUID.randomUUID().toString());
        // 현재 시각을 'yyyy-MM-dd HH:mm:ss' 형식으로 작성일 설정
        notice.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        if (notice.getIsRequired() == null) notice.setIsRequired(0);
        mapper.insert(notice);
        return notice;
    }

    /**
     * 공지사항 수정: 제목과 내용만 업데이트.
     * NoticeMapper.java의 update()로 NOTICE 테이블 수정.
     */
    public void update(String id, String title, String content) {
        NoticeVO notice = new NoticeVO();
        notice.setId(id);
        notice.setTitle(title);
        notice.setContent(content);
        mapper.update(notice);
    }

    /**
     * 공지사항 삭제: 첨부파일 먼저 삭제 후 공지사항 본문 삭제.
     * FileService.java의 deleteFilesByBoard()로 첨부파일 일괄 삭제.
     * NoticeMapper.java의 delete()로 공지사항 삭제.
     */
    public void delete(String id) {
        // 파일을 먼저 삭제하여 고아 파일(orphan file) 방지
        fileService.deleteFilesByBoard(id);
        mapper.delete(id);
    }

    /**
     * 공지사항 필독 여부 설정: isRequired 값을 1(필독) 또는 0(해제)으로 업데이트.
     * NoticeMapper.java의 setRequired()로 NOTICE 테이블 수정.
     */
    public void setRequired(String id, int isRequired) {
        mapper.setRequired(id, isRequired);
    }
}
