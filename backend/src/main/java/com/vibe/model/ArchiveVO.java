package com.vibe.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * 전사 자료실 게시글 정보를 담는 Value Object 클래스.
 * AbstractBoardVO로부터 공통 필드(id, title, content, authorId, authorName, createdAt, views)를 상속.
 * ArchiveMapper.java, ArchiveService.java에서 사용되며,
 * 첨부 파일 목록은 ArchiveFileVO.java 리스트로 관리됨 (ArchiveFileMapper.java 참조).
 * 팀별 자료실은 TeamArchiveVO.java 참조.
 *
 * [Builder Pattern] ArchiveVO.builder().title("...").tags("Spring,Java").build()
 */
@SuperBuilder          // 빌더 패턴 자동 생성 — AbstractBoardVO 필드 포함 체이닝 가능
@Getter                // 모든 필드 getter 자동 생성
@Setter                // 모든 필드 setter 자동 생성
@NoArgsConstructor     // 기본 생성자 자동 생성 — MyBatis ResultMap 매핑에 필수
public class ArchiveVO extends AbstractBoardVO {

    /** 댓글 수 (CommentVO.java 개수 집계) */
    private Integer commentCount;
    /** 필독 여부 (0: 일반, 1: 필독) */
    private Integer isRequired;
    /** 검색용 태그 목록 (쉼표 구분 문자열, 최대 10개) */
    private String tags;
    /** 첨부 파일 수 (목록 조회 시 서브쿼리로 집계) */
    private Integer fileCount;
    /** 첫 번째 첨부 파일 원본명 (목록에서 미리보기용) */
    private String firstFileName;
    /** 첨부 파일 목록 (ArchiveFileVO.java 참조, ArchiveFileMapper.java에서 조회) */
    private List<ArchiveFileVO> files;
}
