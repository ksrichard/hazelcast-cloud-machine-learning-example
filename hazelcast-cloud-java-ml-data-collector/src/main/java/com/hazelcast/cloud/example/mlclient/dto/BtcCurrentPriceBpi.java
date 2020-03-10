package com.hazelcast.cloud.example.mlclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class BtcCurrentPriceBpi {
    @JsonProperty("rate_float")
    private Double rateFloat;

    @Override
    public String toString() {
        return "BtcCurrentPriceBpi{" +
                "rateFloat=" + rateFloat +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BtcCurrentPriceBpi that = (BtcCurrentPriceBpi) o;
        return Objects.equals(rateFloat, that.rateFloat);
    }

    @Override
    public int hashCode() {

        return Objects.hash(rateFloat);
    }

    public Double getRateFloat() {
        return rateFloat;
    }

    public void setRateFloat(Double rateFloat) {
        this.rateFloat = rateFloat;
    }
}
