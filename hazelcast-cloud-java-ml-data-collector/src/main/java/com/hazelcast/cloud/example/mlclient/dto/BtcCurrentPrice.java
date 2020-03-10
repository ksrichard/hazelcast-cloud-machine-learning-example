package com.hazelcast.cloud.example.mlclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.Objects;


public class BtcCurrentPrice {

    @JsonProperty("time")
    private BtcCurrentPriceTime time;

    @JsonProperty("bpi")
    private Map<String, BtcCurrentPriceBpi> rates;

    @Override
    public String toString() {
        return "BtcCurrentPrice{" +
                "time=" + time +
                ", rates=" + rates +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BtcCurrentPrice that = (BtcCurrentPrice) o;
        return Objects.equals(time, that.time) &&
                Objects.equals(rates, that.rates);
    }

    @Override
    public int hashCode() {

        return Objects.hash(time, rates);
    }

    public BtcCurrentPriceTime getTime() {
        return time;
    }

    public void setTime(BtcCurrentPriceTime time) {
        this.time = time;
    }

    public Map<String, BtcCurrentPriceBpi> getRates() {
        return rates;
    }

    public void setRates(Map<String, BtcCurrentPriceBpi> rates) {
        this.rates = rates;
    }
}
