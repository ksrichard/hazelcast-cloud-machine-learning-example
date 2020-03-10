package com.hazelcast.cloud.example.mlclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.Objects;

public class BtcCurrentPriceTime {

    @JsonProperty("updatedISO")
    private Date updatedIso;

    @Override
    public String toString() {
        return "BtcCurrentPriceTime{" +
                "updatedIso=" + updatedIso +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BtcCurrentPriceTime that = (BtcCurrentPriceTime) o;
        return Objects.equals(updatedIso, that.updatedIso);
    }

    @Override
    public int hashCode() {

        return Objects.hash(updatedIso);
    }

    public Date getUpdatedIso() {
        return updatedIso;
    }

    public void setUpdatedIso(Date updatedIso) {
        this.updatedIso = updatedIso;
    }
}
