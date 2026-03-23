package com.vibe.model;

import java.util.List;

/**
 * 팀 자료실 게시글 정보를 담는 Value Object 클래스.
 * TeamArchiveMapper.java, TeamArchiveService.java에서 사용되며,
 * team 필드로 특정 팀(UserVO.java의 team)에만 공개되는 자료를 관리.
 * 전사 자료실은 ArchiveVO.java 참조.
 * 첨부 파일 목록은 ArchiveFileVO.java 리스트로 관리됨.
 */
public class TeamArchiveVO {
    /** 팀 자료실 게시글 고유 식별자 */
    private String id;
    /** 자료 제목 */
    private String title;
    /** 자료 본문 내용 (HTML 형식, Tiptap 에디터 출력) */
    private String content;
    /** 작성자 사용자 ID (UserVO.java의 id 참조) */
    private String authorId;
    /** 작성자 이름 (표시용) */
    private String authorName;
    /** 게시글 작성 일시 (문자열 형식) */
    private String createdAt;
    /** 조회수 */
    private Integer views;
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

    /** id 필드 반환 */
    public String getId() { return id; }
    /** id 필드 설정 */
    public void setId(String id) { this.id = id; }
    /** title 필드 반환 */
    public String getTitle() { return title; }
    /** title 필드 설정 */
    public void setTitle(String title) { this.title = title; }
    /** content 필드 반환 */
    public String getContent() { return content; }
    /** content 필드 설정 */
    public void setContent(String content) { this.content = content; }
    /** authorId 필드 반환 */
    public String getAuthorId() { return authorId; }
    /** authorId 필드 설정 */
    public void setAuthorId(String authorId) { this.authorId = authorId; }
    /** authorName 필드 반환 */
    public String getAuthorName() { return authorName; }
    /** authorName 필드 설정 */
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    /** createdAt 필드 반환 */
    public String getCreatedAt() { return createdAt; }
    /** createdAt 필드 설정 */
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    /** views 필드 반환 */
    public Integer getViews() { return views; }
    /** views 필드 설정 */
    public void setViews(Integer views) { this.views = views; }
    /** commentCount 필드 반환 */
    public Integer getCommentCount() { return commentCount; }
    /** commentCount 필드 설정 */
    public void setCommentCount(Integer commentCount) { this.commentCount = commentCount; }
    /** team 필드 반환 */
    public String getTeam() { return team; }
    /** team 필드 설정 */
    public void setTeam(String team) { this.team = team; }
    /** isRequired 필드 반환 */
    public Integer getIsRequired() { return isRequired; }
    /** isRequired 필드 설정 */
    public void setIsRequired(Integer isRequired) { this.isRequired = isRequired; }
    /** tags 필드 반환 */
    public String getTags() { return tags; }
    /** tags 필드 설정 */
    public void setTags(String tags) { this.tags = tags; }
    /** fileCount 필드 반환 */
    public Integer getFileCount() { return fileCount; }
    /** fileCount 필드 설정 */
    public void setFileCount(Integer fileCount) { this.fileCount = fileCount; }
    /** firstFileName 필드 반환 */
    public String getFirstFileName() { return firstFileName; }
    /** firstFileName 필드 설정 */
    public void setFirstFileName(String firstFileName) { this.firstFileName = firstFileName; }
    /** files 필드 반환 */
    public List<ArchiveFileVO> getFiles() { return files; }
    /** files 필드 설정 */
    public void setFiles(List<ArchiveFileVO> files) { this.files = files; }
}
