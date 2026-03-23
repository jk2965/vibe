package com.vibe.model;

import java.util.List;

/**
 * FAQ(자주 묻는 질문) 게시글 정보를 담는 Value Object 클래스.
 * FaqMapper.java, FaqService.java에서 사용되며,
 * 관리자(UserVO.java의 isAdmin=1)만 작성·수정·삭제 가능.
 * 첨부 파일은 archive_file 테이블을 공유하며, ArchiveFileVO 목록으로 관리됨.
 */
public class FaqVO {

    /** FAQ 게시글 고유 식별자 */
    private String id;
    /** FAQ 질문 제목 */
    private String title;
    /** FAQ 답변 내용 (HTML 형식, Tiptap 에디터 출력) */
    private String content;
    /** 작성자 사용자 ID (UserVO.java의 id 참조) */
    private String authorId;
    /** 작성자 이름 (표시용) */
    private String authorName;
    /** 게시글 작성 일시 (문자열 형식) */
    private String createdAt;
    /** 조회수 */
    private Integer views;
    /** 첨부 파일 수 (목록 조회 시 서브쿼리로 집계) */
    private Integer fileCount;
    /** 첫 번째 첨부 파일 원본명 (목록에서 미리보기용) */
    private String firstFileName;
    /** 첨부 파일 목록 (FaqService.getDetail() 조회 시 채워짐, archive_file 테이블 공유) */
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
    /** fileCount 필드 반환 */
    public Integer getFileCount() { return fileCount; }
    /** fileCount 필드 설정 */
    public void setFileCount(Integer fileCount) { this.fileCount = fileCount; }
    /** firstFileName 필드 반환 */
    public String getFirstFileName() { return firstFileName; }
    /** firstFileName 필드 설정 */
    public void setFirstFileName(String firstFileName) { this.firstFileName = firstFileName; }
    /** files 필드 반환 (상세 조회 시 첨부파일 목록) */
    public List<ArchiveFileVO> getFiles() { return files; }
    /** files 필드 설정 */
    public void setFiles(List<ArchiveFileVO> files) { this.files = files; }
}
