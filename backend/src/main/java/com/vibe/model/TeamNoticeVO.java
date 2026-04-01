package com.vibe.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * 팀 공지사항 게시글 정보를 담는 Value Object 클래스.
 * AbstractBoardVO로부터 공통 필드(id, title, content, authorId, authorName, createdAt, views)를 상속.
 * TeamNoticeMapper.java, TeamNoticeService.java에서 사용되며,
 * team 필드로 특정 팀(UserVO.java의 team)에만 공개되는 공지를 관리.
 * 전사 공지는 NoticeVO.java 참조.
 * 첨부 파일 목록은 ArchiveFileVO.java 리스트로 관리됨.
 *
 * [Builder Pattern] TeamNoticeVO.builder().title("...").team("개발팀").build()
 */
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
public class TeamNoticeVO extends AbstractBoardVO {

    /** 댓글 수 (CommentVO.java 개수 집계) */
    private Integer commentCount;
    /** 공지 대상 팀 이름 (UserVO.java의 team 필드와 일치) */
    private String team;
    /** 필독 여부 (0: 일반, 1: 필독) */
    private Integer isRequired;
    /** 검색용 태그 목록 (쉼표 구분 문자열, 최대 10개) */
    private String tags;
    /** 첨부 파일 목록 (ArchiveFileVO.java 참조, ArchiveFileMapper.java에서 조회) */
    private List<ArchiveFileVO> files;
}
