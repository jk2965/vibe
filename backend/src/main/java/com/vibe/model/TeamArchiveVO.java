package com.vibe.model;

import java.util.List;

public class TeamArchiveVO {
    private String id;
    private String title;
    private String content;
    private String authorId;
    private String authorName;
    private String createdAt;
    private Integer views;
    private Integer commentCount;
    private String team;
    private List<ArchiveFileVO> files;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }
    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public Integer getViews() { return views; }
    public void setViews(Integer views) { this.views = views; }
    public Integer getCommentCount() { return commentCount; }
    public void setCommentCount(Integer commentCount) { this.commentCount = commentCount; }
    public String getTeam() { return team; }
    public void setTeam(String team) { this.team = team; }
    public List<ArchiveFileVO> getFiles() { return files; }
    public void setFiles(List<ArchiveFileVO> files) { this.files = files; }
}
