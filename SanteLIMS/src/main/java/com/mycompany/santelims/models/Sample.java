package com.mycompany.santelims.models;

import java.time.LocalDate;

public class Sample {
    private int id;
    private String patientName;
    private String testName;
    private LocalDate collectionDate;
    private String status;
    private String result;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getTestName() { return testName; }
    public void setTestName(String testName) { this.testName = testName; }

    public LocalDate getCollectionDate() { return collectionDate; }
    public void setCollectionDate(LocalDate collectionDate) { this.collectionDate = collectionDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
}