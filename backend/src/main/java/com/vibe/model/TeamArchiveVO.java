package com.vibe.model;

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

    /** Builder 인스턴스 반환 */
    public static Builder builder() { return new Builder(); }

    public static class Builder extends AbstractBoardVO.Builder<TeamArchiveVO, Builder> {
        private Integer commentCount;
        private String team;
        private Integer isRequired;
        private String tags;
        private Integer fileCount;
        private String firstFileName;
        private List<ArchiveFileVO> files;

        public Builder commentCount(Integer commentCount)   { this.commentCount = commentCount; return this; }
        public Builder team(String team)                    { this.team = team;                 return this; }
        public Builder isRequired(Integer isRequired)       { this.isRequired = isRequired;     return this; }
        public Builder tags(String tags)                    { this.tags = tags;                 return this; }
        public Builder fileCount(Integer fileCount)         { this.fileCount = fileCount;       return this; }
        public Builder firstFileName(String firstFileName)  { this.firstFileName = firstFileName; return this; }
        public Builder files(List<ArchiveFileVO> files)     { this.files = files;               return this; }

        @Override
        public TeamArchiveVO build() {
            TeamArchiveVO vo = new TeamArchiveVO();
            applyTo(vo);
            vo.setCommentCount(commentCount);
            vo.setTeam(team);
            vo.setIsRequired(isRequired);
            vo.setTags(tags);
            vo.setFileCount(fileCount);
            vo.setFirstFileName(firstFileName);
            vo.setFiles(files);
            return vo;
        }
    }
}
