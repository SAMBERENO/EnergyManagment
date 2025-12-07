package com.example.energymanagment.Records;

import java.util.List;

public record CarbonData(
        String from,
        String to,
        List<GenerationMix> generationmix
) {
}
