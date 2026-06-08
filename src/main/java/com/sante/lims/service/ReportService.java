package com.sante.lims.service;

import com.sante.lims.model.LabResult;
import com.sante.lims.model.TestRequest;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReportService {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public Path exportRequestHistoryCsv(List<TestRequest> requests, long customerId) throws IOException {
        Path reportsDir = Path.of("reports");
        Files.createDirectories(reportsDir);

        Path file = reportsDir.resolve("request-history-customer-" + customerId + ".csv");
        try (BufferedWriter writer = Files.newBufferedWriter(file)) {
            writer.write("Request ID,Test Name,Request Date,Expected Completion,Payment Status,Sample Status,Processing Status");
            writer.newLine();
            for (TestRequest request : requests) {
                writer.write(String.format("%d,%s,%s,%s,%s,%s,%s",
                        request.getRequestId(),
                        sanitize(request.getTestName()),
                        request.getRequestDate().format(DATE_FORMAT),
                        request.getExpectedCompletion().format(DATE_FORMAT),
                        request.getPaymentStatus(),
                        request.getSampleStatus(),
                        request.getProcessingStatus()
                ));
                writer.newLine();
            }
        }
        return file;
    }

    public Path exportResultHistoryCsv(List<LabResult> results, long customerId) throws IOException {
        Path reportsDir = Path.of("reports");
        Files.createDirectories(reportsDir);

        Path file = reportsDir.resolve("result-history-customer-" + customerId + ".csv");
        try (BufferedWriter writer = Files.newBufferedWriter(file)) {
            writer.write("Result ID,Request ID,Test Name,File Type,Payment Status,Access,Validated At,File Path");
            writer.newLine();
            for (LabResult result : results) {
                String access = result.isPaid() ? "Available" : "Payment Required";
                String filePath = result.isPaid() ? sanitize(result.getFilePath()) : "";
                writer.write(String.format("%d,%d,%s,%s,%s,%s,%s,%s",
                        result.getId(),
                        result.getRequestId(),
                        sanitize(result.getTestName()),
                        result.getFileType(),
                        result.getPaymentStatus(),
                        access,
                        result.getValidatedAt() == null ? "" : result.getValidatedAt().format(DATE_FORMAT),
                        filePath
                ));
                writer.newLine();
            }
        }
        return file;
    }

    private String sanitize(String value) {
        if (value == null) {
            return "";
        }
        return '"' + value.replace("\"", "\"\"") + '"';
    }
}
