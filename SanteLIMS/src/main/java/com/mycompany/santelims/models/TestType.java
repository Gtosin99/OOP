package com.mycompany.santelims.models;

public class TestType {
    private int id;
    private String testName;
    private String category;
    private double price;
    private String turnaroundTime;
    private String resultFormat;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTestName() { return testName; }
    public void setTestName(String testName) { this.testName = testName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getTurnaroundTime() { return turnaroundTime; }
    public void setTurnaroundTime(String turnaroundTime) { this.turnaroundTime = turnaroundTime; }

    public String getResultFormat() { return resultFormat; }
    public void setResultFormat(String resultFormat) { this.resultFormat = resultFormat; }
}
