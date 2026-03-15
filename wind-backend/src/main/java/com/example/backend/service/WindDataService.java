package com.example.backend.service;

import com.example.backend.dto.ElexonActualDto;
import com.example.backend.dto.ElexonForcastDto;
import com.example.backend.model.WindDataPoint;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WindDataService {
    private final RestTemplate restTemplate;
    private static final String ACTUAL_URL = "https://data.elexon.co.uk/bmrs/api/v1/datasets/FUELHH/stream";
    private static final String FORECAST_URL = "https://data.elexon.co.uk/bmrs/api/v1/datasets/WINDFOR/stream";

    public WindDataService() {
        this.restTemplate = new RestTemplate();
    }
    public List<WindDataPoint> getCombinedWindData(String startTime, String endTime, int forecastHorizonHours){

        //fetch elexon actual data generation
        String actualUri = UriComponentsBuilder.fromUriString(ACTUAL_URL)
                .queryParam("settlementDateFrom", startTime)
                .queryParam("settlementDateTo", endTime)
                .queryParam("FuelType", "WIND")
                .toUriString();

        ResponseEntity<ElexonActualDto[]> actualResponse = restTemplate.getForEntity(actualUri, ElexonActualDto[].class);
        ElexonActualDto[] actualData = actualResponse.getBody();

        //parse the frontend string into a date, then sub 2 days
        java.time.LocalDate startTarget = java.time.LocalDate.parse(startTime);
        java.time.LocalDate adjustedPublishStart = startTarget.minusDays(2);
        //fetch forecast data
        String forecastUri = UriComponentsBuilder.fromUriString(FORECAST_URL)
                .queryParam("publishDateTimeFrom", adjustedPublishStart.toString() + "T00:00:00Z")
                .queryParam("publishDateTimeTo", endTime + "T23:59:59Z")
                .toUriString();

        ResponseEntity<ElexonForcastDto[]> forecastResponse = restTemplate.getForEntity(forecastUri, ElexonForcastDto[].class);
        ElexonForcastDto[] forecastData = forecastResponse.getBody();

        //converting actual data array to a map for fast lookups(Key: starttime, value: generation)
        Map<Instant, Integer> actualsMap = Arrays.stream(actualData)
                .collect(Collectors.toMap(
                        a -> Instant.parse(a.startTime()),
                        ElexonActualDto::generation,
                        (existing, replacement) -> existing
                ));

//        Filter and map the Forecast Data using Instant
        Map<Instant, ElexonForcastDto> latestForecastMap = Arrays.stream(forecastData)
                .filter(forecast -> {
                    Instant publishTime = Instant.parse(forecast.publishTime());
                    Instant targetTime = Instant.parse(forecast.startTime());
                    long horizonHours = Duration.between(publishTime, targetTime).toHours();

                    return horizonHours >= forecastHorizonHours && horizonHours <= 48;
                })
                .collect(Collectors.toMap(
                        f -> Instant.parse(f.startTime()),
                        forecast -> forecast,
                        (f1, f2) -> {
                            Instant p1 = Instant.parse(f1.publishTime());
                            Instant p2 = Instant.parse(f2.publishTime());
                            return p1.isAfter(p2) ? f1 : f2;
                        }
                ));

        // Merge them together! Because both maps use 'Instant', they will match perfectly.
        List<WindDataPoint> combinedData = actualsMap.entrySet().stream()
                .map(entry -> {
                    Instant targetTime = entry.getKey();
                    Integer actualGen = entry.getValue();

                    ElexonForcastDto matchedForecast = latestForecastMap.get(targetTime);
                    Integer forecastGen = (matchedForecast != null) ? matchedForecast.generation() : null;

                    // Convert the Instant back to a String so React can read it
                    return new WindDataPoint(targetTime.toString(), actualGen, forecastGen);
                })
                .sorted(Comparator.comparing(WindDataPoint::getTimestamp))
                .collect(Collectors.toList());

        return combinedData;
    }
}
