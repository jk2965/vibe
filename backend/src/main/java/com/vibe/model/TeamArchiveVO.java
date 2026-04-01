package com.vibe.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * 팀 자료실 게시글 정보를 담는 Value Object 클래스.
 * AbstractBoardVO로부터 공통 필드(id, title, content, authorId, authorName, createdAt, views)를 상속.
 * TeamArchiveMapper.java, TeamArchiveService.java에서 사용되며,
 * team 필드로 특정 팀(UserVO.java의 team)에만 공개되는 자료를 관리.
 * 전사 자료실은 ArchiveVO.java 참조.
 * 첨부 파일 목록은 ArchiveFileVO.java 리스트로 관리됨.
 *
 * [Builder Pattern] TeamArchiveVO.builder().title("...").team("개발팀").build()
 */
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
public class TeamArchiveVO extends AbstractBoardVO {

    /** 댓글 수 (CommentVO.java 개수 집계) */
    private Integer commentCount;
    /** 자료 대상 팀 이름 (UserVO.java의 team 필드와 일치) */
    private String team;
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
