package com.musala.gorskikh.db.dao;

import com.musala.gorskikh.db.entities.DroneEntity;
import com.musala.gorskikh.model.*;
import lombok.NonNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface DronesDao {

    DroneDto register(@NonNull CreateDroneRequest createRequest);

    void load(@NonNull String droneSerialNumber, @NonNull List<LoadingMedication> medicationCodes);

    List<MedicationDto> getMedications(@NonNull String droneSerialNumber);

    void setState(DroneEntity drone, DroneState state);

    List<DroneDto> getAvailableDrones();

    Map<String, BigDecimal> getBatteryLevels();

    BatteryLevel getBatteryLevel(@NonNull String droneSerialNumber);
}
