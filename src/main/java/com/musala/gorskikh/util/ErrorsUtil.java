package com.musala.gorskikh.util;

import com.musala.gorskikh.model.DroneState;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class ErrorsUtil {

    public static String lessThanMinBatteryLevelError(@NonNull String droneSerialNumber,
                                                      @NonNull BigDecimal minBatteryLevel,
                                                      @NonNull BigDecimal factBatteryLevel) {
        return format("Couldn't load the drone with serial number '%s', " +
                        "because level of battery is %s%% and lower than %s%%",
                droneSerialNumber, minBatteryLevel, factBatteryLevel);
    }

    public static String droneNotExistError(@NonNull String droneSerialNumber) {
        return format("There isn't drone with serial number: '%s'", droneSerialNumber);
    }

    public static String emptyMedicationsListError() {
        return "Medications list for loading cannot be empty";
    }

    public static String droneAlreadyExistError(@NonNull String droneSerialNumber) {
        return format("Drone with serial number '%s' already exists", droneSerialNumber);
    }

    public static String serialNumberIsBlankError() {
        return "Serial number of new drone cannot be blank";
    }

    public static String serialNumberIsNullError() {
        return "Serial number of new drone cannot be null";
    }

    public static String modelIsNullError() {
        return "Model of new drone cannot be null";
    }

    public static String overweightLoadingError(@NonNull String droneSerialNumber,
                                                BigDecimal freeSpace, BigDecimal medicationsWeight) {
        return format("Error while loading drone with serial number: '%s'. " +
                        "Medications total weight is %s and available free space is %s",
                droneSerialNumber, medicationsWeight, freeSpace);
    }

    public static String medicationsNotExistError(@NonNull List<String> medicationCodes) {
        return format("Medications with codes %s don't exist", String.join(",", medicationCodes));
    }

    public static String medicationsDuplicateError(@NonNull List<String> duplicateCodes) {
        return format("Gotten duplicate medications codes: %s", String.join(",", duplicateCodes));
    }

    public static String countWrongError(@NonNull Map<String, Integer> wrongCodeToCount) {
        List<String> wrongItems = wrongCodeToCount.entrySet().stream()
                .map(e -> e.getKey() + ": " + e.getValue()).toList();
        return format("Gotten wrong medicine counts for loading: %s", String.join(",", wrongItems));
    }

    public static String illegalDroneStateError(String droneSerialNumber,
                                                DroneState factState, List<DroneState> expectedStates) {
        String states = String.join(",", expectedStates.stream().map(DroneState::getValue).toList());
        return format("Illegal state of drone with serial number '%s': %s. Expected state: %s",
                droneSerialNumber, factState, states);
    }
}
