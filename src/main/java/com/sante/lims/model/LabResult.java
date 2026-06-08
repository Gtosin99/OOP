package com.sante.lims.model;

import java.time.LocalDateTime;

public class LabResult {
    private long id;
    private long requestId;
    private String testName;
    private String filePath;
    private String fileType;
    private boolean validated;
    private LocalDateTime validatedAt;

    public LabResult(long id, long requestId, String testName, String filePath, String fileType,
                     boolean validated, LocalDateTime validatedAt) {
        this.id = id;
        this.requestId = requestId;
        this.testName = testName;
        this.filePath = filePath;
        this.fileType = fileType;
        this.validated = validated;
        this.validatedAt = validatedAt;
    }

    public long getId() {
        return id;
    }

    public long getRequestId() {
        return requestId;
    }

    public String getTestName() {
        return testName;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getFileType() {
        return fileType;
    }

    public boolean isValidated() {
        return validated;
    }

    public LocalDateTime getValidatedAt() {
        return validatedAt;
    }
}
