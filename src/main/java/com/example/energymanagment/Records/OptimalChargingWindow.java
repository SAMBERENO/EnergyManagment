package com.example.energymanagment.Records;

public record OptimalChargingWindow(
        String startTime,
        String endTime,
        double cleanEnergyPercentage
) {
}