package com.musala.gorskikh.services;

import com.musala.gorskikh.db.dao.DronesDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.concurrent.TimeUnit.MINUTES;

@Slf4j
@Service
public record Scheduler(DronesDao dronesDao) {

    @Scheduled(fixedDelay = 5, timeUnit = MINUTES)
    public void checkingBatteryLevelTask() {
        String values = dronesDao.getBatteryLevels().entrySet().stream()
                .map(e -> format("Serial number: '%s' - Battery level: %s", e.getKey(), e.getValue()))
                .reduce((s, s2) -> join("\n", s, s2))
                .orElse("[]");

        log.info("""
                Battery level of drones:
                """ + values);
    }
}
