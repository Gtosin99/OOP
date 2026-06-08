package com.sante.lims.model;

import java.math.BigDecimal;

public class TestCatalogItem {
    private long id;
    private String name;
    private String category;
    private BigDecimal price;
    private int tatHours;
    private String resultFormat;

    public TestCatalogItem(long id, String name, String category, BigDecimal price, int tatHours, String resultFormat) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.tatHours = tatHours;
        this.resultFormat = resultFormat;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getTatHours() {
        return tatHours;
    }

    public String getResultFormat() {
        return resultFormat;
    }
}
