package com.example.energymanagment.Controller;


import com.example.energymanagment.Records.OptimalChargingWindow;
import com.example.energymanagment.Repository.DateUtils;
import com.example.energymanagment.Records.AvgWithCleanEnergy;
import com.example.energymanagment.Services.GBEnergyService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.IntStream;

@RestController
@RequestMapping("/energy")
@CrossOrigin(origins = "http://localhost:5173")
public class GBEnergyController {

    private final GBEnergyService gbEnergyService;

    public GBEnergyController(GBEnergyService gbEnergyService) {
        this.gbEnergyService = gbEnergyService;
    }


    @GetMapping("/each-day-avg")
    public List<AvgWithCleanEnergy> eachDayAvg() {
        return IntStream.range(0, 3)
                .mapToObj(day -> gbEnergyService.getAverageData(
                        DateUtils.inDays(day),
                        DateUtils.inDays(day + 1)
                ))
                .toList();
    }


    @GetMapping("/optimal-charging-window")
    public OptimalChargingWindow findOptimalChargingWindow(@RequestParam(name = "hours") @Min(1) @Max(6) int windowHours) {
        return gbEnergyService.findOptimalChargingWindow(windowHours);
    }

}