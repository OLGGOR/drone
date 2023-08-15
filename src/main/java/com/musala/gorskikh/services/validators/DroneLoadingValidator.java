package com.musala.gorskikh.services.validators;

import com.musala.gorskikh.db.entities.DroneEntity;
import com.musala.gorskikh.model.DroneState;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

import static com.musala.gorskikh.model.DroneState.IDLE;
import static com.musala.gorskikh.model.DroneState.LOADED;
import static com.musala.gorskikh.util.ErrorsUtil.illegalDroneStateError;
import static com.musala.gorskikh.util.ErrorsUtil.lessThanMinBatteryLevelError;

@Service
public class DroneLoadingValidator implements Validator<DroneEntity> {

    public final static BigDecimal MIN_BATTERY_LEVEL = BigDecimal.valueOf(25);
    public final static List<DroneState> AVAILABLE_DRONE_STATES = List.of(IDLE, LOADED);

    @Override
    public ValidationResult validate(DroneEntity drone) {
        String serialNumber = drone.getDroneSerialNumber();
        BigDecimal batteryLevel = drone.getBatteryLevel();
        DroneState droneState = drone.getState();

        if (batteryLevelLessThanMin(batteryLevel))
            return ValidationResult
                    .error(lessThanMinBatteryLevelError(serialNumber, MIN_BATTERY_LEVEL, batteryLevel));

        if (droneState != IDLE && droneState != LOADED)
            return ValidationResult
                    .error(illegalDroneStateError(serialNumber, droneState, AVAILABLE_DRONE_STATES));

        return ValidationResult.success();
    }

    private boolean batteryLevelLessThanMin(BigDecimal batteryLevel) {
        return batteryLevel.compareTo(MIN_BATTERY_LEVEL) < 0;
    }
}
