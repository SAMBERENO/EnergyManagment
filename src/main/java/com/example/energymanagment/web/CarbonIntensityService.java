package com.example.energymanagment.web;

import com.example.energymanagment.Records.EnergyData;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;

@Service
public class CarbonIntensityService {

    private final RestClient restClient;

    public CarbonIntensityService(RestClient restClient) {
        this.restClient = restClient;
    }

    public EnergyData getEnergyData(final LocalDate from, final LocalDate to) {
        try {
            return restClient
                    .get()
                    .uri("https://api.carbonintensity.org.uk/generation/{from}/{to}", from, to)
                    .retrieve()
                    .body(EnergyData.class);
        } catch (Exception e) {
            throw new RuntimeException("Could not get data from carbon intensity service", e);
        }
    }

}