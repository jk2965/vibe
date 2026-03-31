package com.vibe.model;

import java.util.List;

/**
 * 자유게시판 게시글 정보를 담는 Value Object 클래스.
 * AbstractBoardVO로부터 공통 필드(id, title, content, authorId, authorName, createdAt, views)를 상속.
 * BoardMapper.java, BoardService.java에서 사용되며,
 * CommentVO.java에서 boardId로 참조됨.
 * 첨부 파일 목록은 ArchiveFileVO.java 리스트로 관리됨.
 *
 * [Builder Pattern] BoardVO.builder().title("...").authorId("...").build()
 */
public class BoardVO extends AbstractBoardVO {

    /** 댓글 수 (CommentVO.java 개수 집계) */
    private Integer commentCount;
    /** 필독 여부 (0: 일반, 1: 필독) */
    private Integer isRequired;
    /** 검색용 태그 목록 (쉼표 구분 문자열, 최대 10개) */
    private String tags;
    /** 첨부 파일 목록 (ArchiveFileVO.java 참조, ArchiveFileMapper.java에서 조회) */
    private List<ArchiveFileVO> files;

    /** commentCount 필드 반환 */
    public Integer getCommentCount() { return commentCount; }
    /** commentCount 필드 설정 */
    public void setCommentCount(Integer commentCount) { this.commentCount = commentCount; }
    /** isRequired 필드 반환 */
    public Integer getIsRequired() { return isRequired; }
    /** isRequired 필드 설정 */
    public void setIsRequired(Integer isRequired) { this.isRequired = isRequired; }
    /** tags 필드 반환 */
    public String getTags() { return tags; }
    /** tags 필드 설정 */
    public void setTags(String tags) { this.tags = tags; }
    /** files 필드 반환 */
    public List<ArchiveFileVO> getFiles() { return files; }
    /** files 필드 설정 */
    public void setFiles(List<ArchiveFileVO> files) { this.files = files; }

    /** Builder 인스턴스 반환 */
    public static Builder builder() { return new Builder(); }

    public static class Builder extends AbstractBoardVO.Builder<BoardVO, Builder> {
        private Integer commentCount;
        private Integer isRequired;
        private String tags;
        private List<ArchiveFileVO> files;

        public Builder commentCount(Integer commentCount) { this.commentCount = commentCount; return this; }
        public Builder isRequired(Integer isRequired)     { this.isRequired = isRequired;     return this; }
        public Builder tags(String tags)                  { this.tags = tags;                 return this; }
        public Builder files(List<ArchiveFileVO> files)   { this.files = files;               return this; }

        @Override
        public BoardVO build() {
            BoardVO vo = new BoardVO();
            applyTo(vo);
            vo.setCommentCount(commentCount);
            vo.setIsRequired(isRequired);
            vo.setTags(tags);
            vo.setFiles(files);
            return vo;
        }
    }
}
