package com.example.normalizer.config;

public class GitLabConfig {
    private String gitlabProjectId;
    private String gitlabPersonalAccessToken;
    private String repoBranch;
    private String gitlabUploadFolderPath;

    public String getGitlabProjectId() { return gitlabProjectId; }
    public void setGitlabProjectId(String gitlabProjectId) { this.gitlabProjectId = gitlabProjectId; }

    public String getGitlabPersonalAccessToken() { return gitlabPersonalAccessToken; }
    public void setGitlabPersonalAccessToken(String gitlabPersonalAccessToken) { this.gitlabPersonalAccessToken = gitlabPersonalAccessToken; }

    public String getRepoBranch() { return repoBranch; }
    public void setRepoBranch(String repoBranch) { this.repoBranch = repoBranch; }

    public String getGitlabUploadFolderPath() { return gitlabUploadFolderPath; }
    public void setGitlabUploadFolderPath(String gitlabUploadFolderPath) { this.gitlabUploadFolderPath = gitlabUploadFolderPath; }
}
