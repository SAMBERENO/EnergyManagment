package com.example.energymanagment.Repository;

import java.time.LocalDate;

public final class DateUtils {

    public static LocalDate today() {
        return LocalDate.now();
    }

    public static LocalDate inDays(int days) {
        return LocalDate.now().plusDays(days);
    }

}