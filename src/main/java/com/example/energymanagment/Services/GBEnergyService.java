package com.example.energymanagment.Services;

import com.example.energymanagment.Records.*;
import com.example.energymanagment.Repository.DateUtils;
import com.example.energymanagment.web.CarbonIntensityService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GBEnergyService {

    private final CarbonIntensityService carbonIntensityService;
    private final List<String> fuels = List.of("biomass", "nuclear", "hydro", "wind", "solar");

    public GBEnergyService(CarbonIntensityService carbonIntensityService) {
        this.carbonIntensityService = carbonIntensityService;
    }


    public AvgWithCleanEnergy getAverageData(LocalDate from, LocalDate to) {
        EnergyData energyData = getData(from, to);
        Map<String, DoubleSummaryStatistics> stats =
                energyData.data().stream()
                        .flatMap(cd -> cd.generationmix().stream())
                        .collect(Collectors.groupingBy(
                                GenerationMix::fuel,
                                Collectors.summarizingDouble(GenerationMix::perc)
                        ));
        List<GenerationMix> averages = stats.entrySet().stream()
                .map(e -> new GenerationMix(e.getKey(), e.getValue().getAverage()))
                .toList();
        List<String> cleanFuels = fuels;

        double cleanEnergyperc = averages.stream()
                .filter(e -> cleanFuels.contains(e.fuel()))
                .mapToDouble(GenerationMix::perc)
                .sum();

        return new AvgWithCleanEnergy(
                from.atStartOfDay().atOffset(ZoneOffset.UTC).toString(),
                to.atStartOfDay().atOffset(ZoneOffset.UTC).toString(),
                averages,
                cleanEnergyperc
        );

    }


    public OptimalChargingWindow findOptimalChargingWindow(int windowHours) {
        EnergyData energyData = carbonIntensityService.getEnergyData(
                DateUtils.today(),
                DateUtils.inDays(3));

        List<CarbonData> intervals = energyData.data();

        int intervalsInWindow = windowHours * 2;

        double maxCleanEnergy = 0;
        int bestStartIndex = 0;

        for (int i = 0; i <= intervals.size() - intervalsInWindow; i++) {
            List<CarbonData> window = intervals.subList(i, i + intervalsInWindow);
            double avgCleanEnergy = calculateCleanEnergyPercentage(window);

            if (avgCleanEnergy > maxCleanEnergy) {
                maxCleanEnergy = avgCleanEnergy;
                bestStartIndex = i;
            }
        }

        CarbonData startInterval = intervals.get(bestStartIndex);
        CarbonData endInterval = intervals.get(bestStartIndex + intervalsInWindow - 1);

        return new OptimalChargingWindow(
                startInterval.from(),
                endInterval.to(),
                maxCleanEnergy
        );
    }


    private EnergyData getData(LocalDate from, LocalDate to) {
        EnergyData energyData = carbonIntensityService.getEnergyData(
                DateUtils.today(),
                DateUtils.inDays(3)
        );

        OffsetDateTime fromDate = from.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime toDate = to.atStartOfDay().atOffset(ZoneOffset.UTC);

        List<CarbonData> data = energyData.data().stream()
                .filter(e -> {
                    OffsetDateTime dataFrom = OffsetDateTime.parse(e.from());
                    OffsetDateTime dataTo = OffsetDateTime.parse(e.to());
                    return !dataFrom.isBefore(fromDate)
                            && !dataTo.isAfter(toDate);
                })
                .toList();

        return new EnergyData(data);
    }


    private double calculateCleanEnergyPercentage(List<CarbonData> intervals) {
        List<String> cleanFuels = fuels;
        return intervals.stream()
                .flatMap(cd -> cd.generationmix().stream())
                .filter(gm -> cleanFuels.contains(gm.fuel()))
                .mapToDouble(GenerationMix::perc)
                .average()
                .orElse(0.0);
    }

}