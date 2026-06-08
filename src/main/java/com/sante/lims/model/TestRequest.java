package com.sante.lims.model;

import java.time.Duration;
import java.time.LocalDateTime;

public class TestRequest {
    private long requestId;
    private String testName;
    private LocalDateTime requestDate;
    private LocalDateTime expectedCompletion;
    private String paymentStatus;
    private String sampleStatus;
    private String processingStatus;

    public TestRequest(long requestId, String testName, LocalDateTime requestDate, LocalDateTime expectedCompletion,
                       String paymentStatus, String sampleStatus, String processingStatus) {
        this.requestId = requestId;
        this.testName = testName;
        this.requestDate = requestDate;
        this.expectedCompletion = expectedCompletion;
        this.paymentStatus = paymentStatus;
        this.sampleStatus = sampleStatus;
        this.processingStatus = processingStatus;
    }

    public long getRequestId() {
        return requestId;
    }

    public String getTestName() {
        return testName;
    }

    public LocalDateTime getRequestDate() {
        return requestDate;
    }

    public LocalDateTime getExpectedCompletion() {
        return expectedCompletion;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public String getSampleStatus() {
        return sampleStatus;
    }

    public String getProcessingStatus() {
        return processingStatus;
    }

    public String getCountdownDisplay() {
        Duration remaining = Duration.between(LocalDateTime.now(), expectedCompletion);
        if (remaining.isNegative() || remaining.isZero()) {
            return "Ready";
        }

        long totalMinutes = remaining.toMinutes();
        long days = totalMinutes / (24 * 60);
        long hours = (totalMinutes % (24 * 60)) / 60;
        long minutes = totalMinutes % 60;

        return String.format("%dd %02dh %02dm", days, hours, minutes);
    }
}
