package com.example.energymanagment.Records;

import java.util.List;

public record AvgWithCleanEnergy(
        String from,
        String to,
        List<GenerationMix> generationmix,
        double cleanenergy
) {
}