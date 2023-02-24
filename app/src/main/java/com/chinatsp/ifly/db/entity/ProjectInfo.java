package com.chinatsp.ifly.db.entity;

import java.io.Serializable;

public class ProjectInfo implements Serializable {
    private String projectId;
    private String currentVersion;

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getCurrentVersion() {
        return currentVersion;
    }

    public void setCurrentVersion(String currentVersion) {
        this.currentVersion = currentVersion;
    }

    @Override
    public String toString() {
        return "ProjectInfo{" +
                "projectId='" + projectId + '\'' +
                ", currentVersion='" + currentVersion + '\'' +
                '}';
    }
}
