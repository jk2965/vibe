package com.vibe.model;

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
public class FaqVO extends AbstractBoardVO {

    /** 첨부 파일 수 (목록 조회 시 서브쿼리로 집계) */
    private Integer fileCount;
    /** 첫 번째 첨부 파일 원본명 (목록에서 미리보기용) */
    private String firstFileName;
    /** 첨부 파일 목록 (FaqService.getDetail() 조회 시 채워짐, archive_file 테이블 공유) */
    private List<ArchiveFileVO> files;

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

    /** Builder 인스턴스 반환 */
    public static Builder builder() { return new Builder(); }

    public static class Builder extends AbstractBoardVO.Builder<FaqVO, Builder> {
        private Integer fileCount;
        private String firstFileName;
        private List<ArchiveFileVO> files;

        public Builder fileCount(Integer fileCount)        { this.fileCount = fileCount;       return this; }
        public Builder firstFileName(String firstFileName) { this.firstFileName = firstFileName; return this; }
        public Builder files(List<ArchiveFileVO> files)    { this.files = files;               return this; }

        @Override
        public FaqVO build() {
            FaqVO vo = new FaqVO();
            applyTo(vo);
            vo.setFileCount(fileCount);
            vo.setFirstFileName(firstFileName);
            vo.setFiles(files);
            return vo;
        }
    }
}
