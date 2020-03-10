package com.hazelcast.cloud.example.mlclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class BtcUsdPrediction {
    @JsonProperty("forDate")
    private Long targetDate;
    @JsonProperty("price")
    private Double price;

    @Override
    public String toString() {
        return "BtcUsdPrediction{" +
                "targetDate=" + targetDate +
                ", price=" + price +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BtcUsdPrediction that = (BtcUsdPrediction) o;
        return Objects.equals(targetDate, that.targetDate) &&
                Objects.equals(price, that.price);
    }

    @Override
    public int hashCode() {

        return Objects.hash(targetDate, price);
    }

    public Long getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(Long targetDate) {
        this.targetDate = targetDate;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}
