package com.vibe.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.vibe.mapper.BoardMapper;
import com.vibe.model.BoardVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * 자유게시판(Board) 관련 비즈니스 로직을 처리하는 서비스 클래스.
 * BoardController.java에서 호출되며, BoardMapper.java를 통해 DB와 연동됨.
 * 파일 첨부 기능은 FileService.java에 위임함.
 */
@Service
public class BoardService {

    // BoardMapper.java를 통해 BOARD 테이블 CRUD 수행
    @Autowired
    private BoardMapper mapper;

    // 게시글에 첨부된 파일 처리를 위해 FileService.java를 주입
    @Autowired
    private FileService fileService;

    /**
     * 게시글 목록을 페이지 단위로 조회 (페이지당 10개).
     * PageHelper를 사용해 자동 페이징 처리.
     * BoardMapper.java의 findAll()로 전체 목록 조회 후 PageInfo로 래핑.
     */
    public PageInfo<BoardVO> getList(int pageNum) {
        PageHelper.startPage(pageNum, 10);
        List<BoardVO> list = mapper.findAll();
        return new PageInfo<>(list);
    }

    /**
     * 게시글 상세 조회: 조회수 1 증가 후 게시글과 첨부파일 목록을 함께 반환.
     * BoardMapper.java의 incrementViews()로 조회수 증가.
     * FileService.java의 getFilesByBoard()로 첨부파일 조회.
     */
    public BoardVO getDetail(String id) {
        // 게시글 조회 시 조회수(views) 1 증가
        mapper.incrementViews(id);
        BoardVO board = mapper.findById(id);
        if (board != null) {
            // FileService.java를 통해 해당 게시글의 첨부파일 목록 조회
            board.setFiles(fileService.getFilesByBoard(id));
        }
        return board;
    }

    /**
     * 새 게시글 작성: UUID로 고유 ID 생성, 현재 시각을 작성일로 설정 후 DB에 저장.
     * BoardMapper.java의 insert()로 BOARD 테이블에 등록.
     */
    public BoardVO write(BoardVO board) {
        // UUID를 사용해 유일한 게시글 ID 생성
        board.setId(UUID.randomUUID().toString());
        // 현재 시각을 'yyyy-MM-dd HH:mm:ss' 형식으로 작성일 설정
        board.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        // isRequired가 null이면 0(일반)으로 초기화
        if (board.getIsRequired() == null) board.setIsRequired(0);
        mapper.insert(board);
        return board;
    }

    /**
     * 게시글 수정: 제목과 내용만 업데이트.
     * BoardMapper.java의 update()로 BOARD 테이블 수정.
     */
    public void update(String id, String title, String content, String tags) {
        BoardVO board = new BoardVO();
        board.setId(id);
        board.setTitle(title);
        board.setContent(content);
        board.setTags(tags);
        mapper.update(board);
    }

    /**
     * 게시글 삭제: 첨부파일 먼저 삭제 후 게시글 본문 삭제.
     * FileService.java의 deleteFilesByBoard()로 첨부파일 일괄 삭제.
     * BoardMapper.java의 delete()로 게시글 삭제.
     */
    public void delete(String id) {
        // 파일을 먼저 삭제하여 고아 파일(orphan file) 방지
        fileService.deleteFilesByBoard(id);
        mapper.delete(id);
    }

    /**
     * 게시글 필독 여부 설정: isRequired 값을 1(필독) 또는 0(해제)으로 업데이트.
     * BoardMapper.java의 setRequired()로 BOARD 테이블 수정.
     */
    public void setRequired(String id, int isRequired) {
        mapper.setRequired(id, isRequired);
    }
}
