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
                .queryParam("settelmentDateFrom", startTime)
                .queryParam("settelmentDateTo", endTime)
                .queryParam("FuelType", "WIND")
                .toUriString();

        ResponseEntity<ElexonActualDto[]> actualResponse = restTemplate.getForEntity(actualUri, ElexonActualDto[].class);
        ElexonActualDto[] actualData = actualResponse.getBody();

        //fetch forecast data
        String forecastUri = UriComponentsBuilder.fromUriString(FORECAST_URL)
                .queryParam("publishDateTimeFrom", startTime + "T00:00:00Z")
                .queryParam("publishDateTimeTo", endTime + "T23:59:59Z")
                .toUriString();

        ResponseEntity<ElexonForcastDto[]> forecastResponse = restTemplate.getForEntity(forecastUri, ElexonForcastDto[].class);
        ElexonForcastDto[] forecastData = forecastResponse.getBody();

        //converting actual data array to a map for fast lookups(Key: starttime, value: generation)
        Map<String, Integer> actualsMap = Arrays.stream(actualData)
                .collect(Collectors.toMap(
                        ElexonActualDto::startTime,
                        ElexonActualDto::generation,
                        (existing, replacement) -> existing //if there are duplicates, keep the first
                ));

        //filter and find the best forecast for each target time
        Map<String , ElexonForcastDto> latestForecastMap = Arrays.stream(forecastData)
                .filter(forecast -> {
                    Instant publishTime = Instant.parse(forecast.publishTime());
                    Instant targetTime = Instant.parse(forecast.startTime());
                    long horizonHours = Duration.between(publishTime, targetTime).toHours();

                    //horizon must be <= 48 and >= the user's slider value
                    return horizonHours >= forecastHorizonHours && horizonHours <= 48;
                })
                .collect(Collectors.toMap(
                        ElexonForcastDto::startTime,
                        forecast -> forecast,
                        (f1, f2) -> {
                            //if mul forecast exist for the same target time, pick the one with the Latest publish time
                            Instant p1 = Instant.parse(f1.publishTime());
                            Instant p2 = Instant.parse(f2.publishTime());
                            return p1.isAfter(p2) ? f1 : f2;
                        }
                ));

        //merge actual and forecast together into our final response object
        List<WindDataPoint> combinedData = actualsMap.entrySet().stream()
                .map(entry -> {
                    String targetTime = entry.getKey();
                    Integer actualGen = entry.getValue();

                    //look up the corresponding forecast, if it dosen't exists it remains null
                    ElexonForcastDto matchedForecast = latestForecastMap.get(targetTime);
                    Integer forecastGen = (matchedForecast != null) ? matchedForecast.generation() : null;

                    return new WindDataPoint(targetTime, actualGen, forecastGen);
                })
                .sorted(Comparator.comparing(WindDataPoint::getTimestamp))
                .collect(Collectors.toList());

        return combinedData;
    }
}
