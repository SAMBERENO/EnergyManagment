package com.example.energymanagment.Records;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public record EnergyData(
        List<CarbonData> data
) {
}
