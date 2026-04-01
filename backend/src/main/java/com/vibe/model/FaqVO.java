package com.vibe.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * FAQ(자주 묻는 질문) 게시글 정보를 담는 Value Object 클래스.
 * AbstractBoardVO로부터 공통 필드(id, title, content, authorId, authorName, createdAt, views)를 상속.
 * FaqMapper.java, FaqService.java에서 사용되며,
 * 관리자(UserVO.java의 isAdmin=1)만 작성·수정·삭제 가능.
 * 첨부 파일은 archive_file 테이블을 공유하며, ArchiveFileVO 목록으로 관리됨.
 *
 * [Builder Pattern] FaqVO.builder().title("질문").content("답변").build()
 */
@SuperBuilder          // 빌더 패턴 자동 생성 — AbstractBoardVO 필드 포함 체이닝 가능
@Getter                // 모든 필드 getter 자동 생성
@Setter                // 모든 필드 setter 자동 생성
@NoArgsConstructor     // 기본 생성자 자동 생성 — MyBatis ResultMap 매핑에 필수
public class FaqVO extends AbstractBoardVO {

    /** 첨부 파일 수 (목록 조회 시 서브쿼리로 집계) */
    private Integer fileCount;
    /** 첫 번째 첨부 파일 원본명 (목록에서 미리보기용) */
    private String firstFileName;
    /** 첨부 파일 목록 (FaqService.getDetail() 조회 시 채워짐, archive_file 테이블 공유) */
    private List<ArchiveFileVO> files;
}
