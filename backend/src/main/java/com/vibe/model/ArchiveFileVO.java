package com.vibe.model;

/**
 * 게시글에 첨부된 파일 정보를 담는 Value Object 클래스.
 * ArchiveFileMapper.java, FileService.java에서 사용되며,
 * BoardVO.java, NoticeVO.java, ArchiveVO.java, TeamNoticeVO.java, TeamArchiveVO.java에서
 * List<ArchiveFileVO> 형태로 참조됨.
 * boardId는 각 게시판 유형의 게시글 ID를 공통으로 가리킴.
 */
public class ArchiveFileVO {
    /** 파일 고유 식별자 */
    private String id;
    /** 파일이 속한 게시글 ID (BoardVO/NoticeVO/ArchiveVO/TeamNoticeVO/TeamArchiveVO의 id 참조) */
    private String boardId;
    /** 업로드 시 원본 파일명 (사용자에게 표시) */
    private String originalName;
    /** 서버에 저장된 실제 파일명 (UUID 등 고유값으로 변환됨) */
    private String storedName;
    /** 파일 크기 (바이트 단위) */
    private Long fileSize;
    /** 파일 업로드 일시 (문자열 형식) */
    private String createdAt;
    /** 파일을 업로드한 사용자 ID (UserVO.java의 id 참조) */
    private String uploaderId;

    /** id 필드 반환 */
    public String getId() { return id; }
    /** id 필드 설정 */
    public void setId(String id) { this.id = id; }
    /** boardId 필드 반환 */
    public String getBoardId() { return boardId; }
    /** boardId 필드 설정 */
    public void setBoardId(String boardId) { this.boardId = boardId; }
    /** originalName 필드 반환 */
    public String getOriginalName() { return originalName; }
    /** originalName 필드 설정 */
    public void setOriginalName(String originalName) { this.originalName = originalName; }
    /** storedName 필드 반환 */
    public String getStoredName() { return storedName; }
    /** storedName 필드 설정 */
    public void setStoredName(String storedName) { this.storedName = storedName; }
    /** fileSize 필드 반환 */
    public Long getFileSize() { return fileSize; }
    /** fileSize 필드 설정 */
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    /** createdAt 필드 반환 */
    public String getCreatedAt() { return createdAt; }
    /** createdAt 필드 설정 */
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    /** uploaderId 필드 반환 */
    public String getUploaderId() { return uploaderId; }
    /** uploaderId 필드 설정 */
    public void setUploaderId(String uploaderId) { this.uploaderId = uploaderId; }
}
