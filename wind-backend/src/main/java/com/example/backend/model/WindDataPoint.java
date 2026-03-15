package com.example.backend.model;

import lombok.Data;

@Data
public class WindDataPoint {
    private String timestamp; //target time
    private Integer actualGeneration;
    private Integer forecastGeneration;

    public WindDataPoint(String timestamp, Integer actualGeneration, Integer forecastGeneration) {
        this.actualGeneration = actualGeneration;
        this.forecastGeneration = forecastGeneration;
        this.timestamp = timestamp;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
