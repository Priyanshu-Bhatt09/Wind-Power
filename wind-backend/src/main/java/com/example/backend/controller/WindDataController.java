package com.example.backend.controller;

import com.example.backend.model.WindDataPoint;
import com.example.backend.service.WindDataService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wind-data")
@CrossOrigin(origins = "*")
public class WindDataController {
    private final WindDataService windService;
    public WindDataController(WindDataService windService) {
        this.windService = windService;
    }
    @GetMapping
    public List<WindDataPoint> getWindData(@RequestParam String startTime, @RequestParam String endTime,
                                           @RequestParam(defaultValue = 4) int forecastHorizonHours) {
        return windService.getCombinedWindData(startTime, endTime, forecastHorizonHours);
    }
}
