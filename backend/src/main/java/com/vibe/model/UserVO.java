package com.vibe.model;

public class UserVO {

    private String id;
    private String username;
    private String password;
    private String position;
    private Double remainingVacation;
    private Integer isAdmin;
    private String team;
    private Integer isTeamLeader;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    public Double getRemainingVacation() { return remainingVacation; }
    public void setRemainingVacation(Double remainingVacation) { this.remainingVacation = remainingVacation; }
    public Integer getIsAdmin() { return isAdmin; }
    public void setIsAdmin(Integer isAdmin) { this.isAdmin = isAdmin; }
    public String getTeam() { return team; }
    public void setTeam(String team) { this.team = team; }
    public Integer getIsTeamLeader() { return isTeamLeader; }
    public void setIsTeamLeader(Integer isTeamLeader) { this.isTeamLeader = isTeamLeader; }
}
