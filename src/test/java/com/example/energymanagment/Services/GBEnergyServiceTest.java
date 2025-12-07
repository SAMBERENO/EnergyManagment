package com.example.energymanagment.Services;

import com.example.energymanagment.Records.AvgWithCleanEnergy;
import com.example.energymanagment.Records.CarbonData;
import com.example.energymanagment.Records.EnergyData;
import com.example.energymanagment.Records.GenerationMix;
import com.example.energymanagment.Records.OptimalChargingWindow;
import com.example.energymanagment.web.CarbonIntensityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class GBEnergyServiceTest {

    private CarbonIntensityService carbonIntensityService;
    private GBEnergyService gbEnergyService;

    @BeforeEach
    void setUp() {
        carbonIntensityService = Mockito.mock(CarbonIntensityService.class);
        gbEnergyService = new GBEnergyService(carbonIntensityService);
    }

    @Test
    void getAverageData_shouldCalculateAveragesAndCleanEnergy() {


        LocalDate from = LocalDate.of(2025, 12, 5);
        LocalDate to   = from.plusDays(1);


        EnergyData energyData = buildEnergyDataForAverage();
        when(carbonIntensityService.getEnergyData(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(energyData);


        AvgWithCleanEnergy result = gbEnergyService.getAverageData(from, to);


        String expectedFrom = from.atStartOfDay().atOffset(ZoneOffset.UTC).toString();
        String expectedTo   = to.atStartOfDay().atOffset(ZoneOffset.UTC).toString();

        assertEquals(expectedFrom, result.from());
        assertEquals(expectedTo, result.to());


        GenerationMix wind = result.generationmix().stream()
                .filter(gm -> gm.fuel().equals("wind"))
                .findFirst()
                .orElseThrow();

        GenerationMix gas = result.generationmix().stream()
                .filter(gm -> gm.fuel().equals("gas"))
                .findFirst()
                .orElseThrow();

        assertEquals(40.0, wind.perc(), 0.0001);
        assertEquals(60.0, gas.perc(), 0.0001);


        assertEquals(40.0, result.cleanenergy(), 0.0001);
    }

    @Test
    void findOptimalChargingWindow_shouldReturnBestWindow() {
        // given
        int windowHours = 2;

        List<CarbonData> intervals = buildIntervalsForWindowTest();
        EnergyData energyData = new EnergyData(intervals);

        when(carbonIntensityService.getEnergyData(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(energyData);

        OptimalChargingWindow result = gbEnergyService.findOptimalChargingWindow(windowHours);

        CarbonData startInterval = intervals.get(2);
        CarbonData endInterval   = intervals.get(5);

        assertEquals(startInterval.from(), result.startTime());
        assertEquals(endInterval.to(), result.endTime());
        assertEquals(70.0, result.cleanEnergyPercentage(), 0.0001);
    }

    private EnergyData buildEnergyDataForAverage() {
        LocalDate baseDay = LocalDate.of(2025, 12, 5);
        OffsetDateTime start = baseDay.atStartOfDay().atOffset(ZoneOffset.UTC);

        List<CarbonData> intervals = List.of(
                carbonInterval(start,       40.0, 60.0),
                carbonInterval(start.plusMinutes(30), 40.0, 60.0),
                carbonInterval(start.plusMinutes(60), 40.0, 60.0),
                carbonInterval(start.plusMinutes(90), 40.0, 60.0)
        );

        return new EnergyData(intervals);
    }

    private List<CarbonData> buildIntervalsForWindowTest() {
        LocalDate baseDay = LocalDate.of(2025, 12, 5);
        OffsetDateTime start = baseDay.atStartOfDay().atOffset(ZoneOffset.UTC);

        return List.of(
                carbonInterval(start,               10.0, 90.0),
                carbonInterval(start.plusMinutes(30), 10.0, 90.0),
                carbonInterval(start.plusMinutes(60), 50.0, 50.0),
                carbonInterval(start.plusMinutes(90), 50.0, 50.0),
                carbonInterval(start.plusMinutes(120), 90.0, 10.0),
                carbonInterval(start.plusMinutes(150), 90.0, 10.0)
        );
    }

    private CarbonData carbonInterval(OffsetDateTime from, double windPerc, double gasPerc) {
        String fromStr = from.toString();
        String toStr   = from.plusMinutes(30).toString();

        List<GenerationMix> mix = List.of(
                new GenerationMix("wind", windPerc),
                new GenerationMix("gas", gasPerc)
        );

        return new CarbonData(fromStr, toStr, mix);
    }
}
