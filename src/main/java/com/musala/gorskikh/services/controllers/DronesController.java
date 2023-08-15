package com.musala.gorskikh.services.controllers;

import com.musala.gorskikh.api.DronesApi;
import com.musala.gorskikh.db.dao.DronesDao;
import com.musala.gorskikh.model.*;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpServerErrorException;

import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Slf4j
@RestController
@RequiredArgsConstructor
public class DronesController implements DronesApi {

    private final DronesDao dao;

    @Override
    public ResponseEntity<List<DroneDto>> getAvailableDrones() {
        log.debug("Received a request for getting all available drones");

        return ResponseEntity.ok(dao.getAvailableDrones());
    }

    @Override
    public ResponseEntity<BatteryLevel> getBatteryLevel(@NonNull String droneSerialNumber) {
        log.debug("Received a request for getting battery level of a specific drone: {}", droneSerialNumber);

        BatteryLevel batteryLevel = dao.getBatteryLevel(droneSerialNumber);
        return ResponseEntity.ok(batteryLevel);
    }

    @Override
    public ResponseEntity<Void> loadDrone(@NonNull LoadDroneRequest loadRequest) {
        List<LoadingMedication> medications = loadRequest.getMedications();
        String droneSerialNumber = loadRequest.getDroneSerialNumber();
        String medicationsStr = String.join(",", medications.toString());

        if (medications.isEmpty())
            throw new HttpServerErrorException(BAD_REQUEST, "List of medication codes for loading cannot be empty");

        log.info("Received a request for loading a drone {} with medications: {}", droneSerialNumber, medicationsStr);

        dao.load(droneSerialNumber, medications);

        log.info("The drone {} was loaded with medications {}", droneSerialNumber, medicationsStr);

        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> registerDrone(@NonNull CreateDroneRequest createDroneRequest) {
        log.info("Received a request for register a drone: {}", createDroneRequest);

        DroneDto registered = dao.register(createDroneRequest);
        log.info("The drone was registered: {}", registered);

        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<List<MedicationDto>> getMedicationsByDrone(@NonNull String droneSerialNumber) {
        log.debug("Received a request for getting loaded medications for the drone with serial number: {}",
                droneSerialNumber);

        List<MedicationDto> medications = dao.getMedications(droneSerialNumber);

        return ResponseEntity.ok(medications);
    }
}
