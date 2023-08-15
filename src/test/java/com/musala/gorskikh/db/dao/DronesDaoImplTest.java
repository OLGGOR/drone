package com.musala.gorskikh.db.dao;

import com.musala.gorskikh.db.entities.DroneEntity;
import com.musala.gorskikh.db.entities.DroneMedicationEntity;
import com.musala.gorskikh.db.entities.MedicationEntity;
import com.musala.gorskikh.db.repositories.DroneMedicationRepository;
import com.musala.gorskikh.db.repositories.DronesRepository;
import com.musala.gorskikh.model.*;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.HttpServerErrorException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static com.musala.gorskikh.model.DroneModelEnum.*;
import static com.musala.gorskikh.model.DroneState.DELIVERING;
import static com.musala.gorskikh.model.DroneState.IDLE;
import static com.musala.gorskikh.services.validators.DroneLoadingValidator.AVAILABLE_DRONE_STATES;
import static com.musala.gorskikh.services.validators.DroneLoadingValidator.MIN_BATTERY_LEVEL;
import static com.musala.gorskikh.util.ErrorsUtil.*;
import static com.musala.gorskikh.util.TestUtil.bd;
import static com.musala.gorskikh.util.TestUtil.createMedication;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@SpringBootTest
@AutoConfigureEmbeddedDatabase
class DronesDaoImplTest {

    private final DronesDao dronesDao;

    private final DronesRepository dronesRepository;
    private final DroneMedicationRepository droneMedicationRepository;

    @Autowired
    public DronesDaoImplTest(DronesRepository dronesRepository, DronesDao dronesDao,
                             DroneMedicationRepository droneMedicationRepository) {
        this.dronesRepository = dronesRepository;
        this.dronesDao = dronesDao;
        this.droneMedicationRepository = droneMedicationRepository;
    }

    @Test
    @DisplayName("Get all available drones")
    void getAvailableDrones() {
        //when
        List<DroneDto> availableDrones = dronesDao.getAvailableDrones();

        //then
        assertTrue(availableDrones.stream()
                .allMatch(d -> d.getBatteryLevel().compareTo(MIN_BATTERY_LEVEL) >= 0
                        && AVAILABLE_DRONE_STATES.contains(d.getState())));
    }

    @Test
    @DisplayName("Get battery levels of all drones")
    void getBatteryLevels() {
        Map<String, BigDecimal> expectedBatteryLevels = dronesRepository.findAll().stream()
                .collect(toMap(DroneEntity::getDroneSerialNumber, DroneEntity::getBatteryLevel));

        //when
        Map<String, BigDecimal> actualBatteryLevels = dronesDao.getBatteryLevels();

        //then
        assertEquals(expectedBatteryLevels.size(), actualBatteryLevels.size());
        assertTrue(actualBatteryLevels.entrySet().stream()
                .allMatch(e -> {
                    BigDecimal expectedLevel = expectedBatteryLevels.get(e.getKey());
                    return expectedLevel.equals(e.getValue());
                }));
    }

    @ParameterizedTest
    @MethodSource("serialNumberToBatteryLevel")
    @DisplayName("Get battery level of the specific drone by serial number")
    void getBatteryLevel(String serialNumber, BigDecimal expectedBatteryLevel) {
        if (serialNumber == null) {
            assertThrows(NullPointerException.class, () -> dronesDao.getBatteryLevel(serialNumber));
        } else if (serialNumber.isBlank()) {
            //when
            HttpServerErrorException exception =
                    assertThrows(HttpServerErrorException.class, () -> dronesDao.getBatteryLevel(serialNumber));

            //then
            assertEquals(BAD_REQUEST, exception.getStatusCode());
            assertEquals(droneNotExistError(serialNumber), exception.getStatusText());
        } else {
            //when
            BatteryLevel actualBatteryLevel = dronesDao.getBatteryLevel(serialNumber);

            //then
            assertEquals(expectedBatteryLevel, actualBatteryLevel.getLevel());
        }
    }

    @ParameterizedTest
    @MethodSource("serialNumberToMedications")
    @DisplayName("Get all medications of the specific drone by serial number")
    void getMedications(String serialNumber, List<MedicationDto> expectedMedications) {
        if (serialNumber == null) {
            assertThrows(NullPointerException.class, () -> dronesDao.getMedications(serialNumber));
        } else if (serialNumber.isBlank()) {
            //when
            HttpServerErrorException exception =
                    assertThrows(HttpServerErrorException.class, () -> dronesDao.getMedications(serialNumber));

            //then
            assertEquals(BAD_REQUEST, exception.getStatusCode());
            assertEquals(droneNotExistError(serialNumber), exception.getStatusText());
        } else {
            Map<String, MedicationDto> expectedCodeToMedication = expectedMedications.stream()
                    .collect(toMap(MedicationDto::getCode, identity()));

            //when
            List<MedicationDto> actualMedications = dronesDao.getMedications(serialNumber);

            //then
            assertEquals(expectedMedications.size(), actualMedications.size());
            assertTrue(actualMedications.stream()
                    .allMatch(actual -> {
                        MedicationDto expected = expectedCodeToMedication.get(actual.getCode());
                        return expected.getName().equals(actual.getName())
                                && expected.getWeightGr().equals(actual.getWeightGr());
                    }));
        }
    }

    @ParameterizedTest
    @MethodSource("createEmptyDroneRequest")
    @DisplayName("Register a new drone when there are empty fields")
    void register_whenEmptyData(CreateDroneRequest request, String errorMsg) {
        if (request == null) {
            assertThrows(NullPointerException.class, () -> dronesDao.register(request));
        } else {
            //when
            HttpServerErrorException exception =
                    assertThrows(HttpServerErrorException.class, () -> dronesDao.register(request));

            //then
            assertEquals(BAD_REQUEST, exception.getStatusCode());
            assertEquals(errorMsg, exception.getStatusText());
        }
    }

    @ParameterizedTest
    @MethodSource("createNotEmptyDroneRequest")
    @DisplayName("Register a new drone")
    void register(CreateDroneRequest request) {
        Optional<DroneEntity> droneFromDb = dronesRepository.findById(request.getDroneSerialNumber());
        if (droneFromDb.isPresent()) {
            //when
            HttpServerErrorException exception =
                    assertThrows(HttpServerErrorException.class, () -> dronesDao.register(request));

            //then
            assertEquals(BAD_REQUEST, exception.getStatusCode());
            assertEquals(droneAlreadyExistError(request.getDroneSerialNumber()), exception.getStatusText());
        } else {
            //when
            DroneDto registeredDrone = dronesDao.register(request);

            //then
            Optional<DroneEntity> optRegisteredDroneDb = dronesRepository.findById(request.getDroneSerialNumber());

            assertTrue(optRegisteredDroneDb.isPresent());

            DroneEntity registeredDroneDb = optRegisteredDroneDb.get();
            assertEquals(IDLE, registeredDroneDb.getState());
            assertEquals(bd(100), registeredDroneDb.getBatteryLevel());
            assertEquals(request.getModel(), registeredDroneDb.getModel().getModel());

            assertEquals(registeredDroneDb.getDroneSerialNumber(), registeredDrone.getDroneSerialNumber());
            assertEquals(registeredDroneDb.getState(), registeredDrone.getState());
            assertEquals(registeredDroneDb.getBatteryLevel(), registeredDrone.getBatteryLevel());
            assertEquals(registeredDroneDb.getModel().getModel(), registeredDrone.getModel().getModel());
        }
    }

    @ParameterizedTest
    @MethodSource("createLoadDroneRequest")
    @DisplayName("Load drone with medications by serial number")
    void load(String droneSerialNumber, List<LoadingMedication> medications) {
        Map<String, LoadingMedication> codeToLoadMedication = medications.stream()
                .collect(toMap(LoadingMedication::getMedicationCode, identity()));

        //when
        dronesDao.load(droneSerialNumber, medications);

        //then
        Optional<DroneEntity> optLoadedDrone = dronesRepository.findByIdFetchMedications(droneSerialNumber);
        assertTrue(optLoadedDrone.isPresent());

        DroneEntity loadedDrone = optLoadedDrone.get();
        for (MedicationEntity medication : loadedDrone.getMedications()) {
            Optional<DroneMedicationEntity> optDroneMedication =
                    droneMedicationRepository.findByDroneAndMedication(droneSerialNumber, medication.getCode());
            assertTrue(optDroneMedication.isPresent());

            Integer actualCount = optDroneMedication.get().getCount();
            Integer loadingCount = codeToLoadMedication.get(medication.getCode()).getCount();
            assertTrue(actualCount.compareTo(loadingCount) >= 0);
        }
    }

    @ParameterizedTest
    @MethodSource("createLoadDroneErrorRequest")
    @DisplayName("Load drone with medications by serial number when request is wrong")
    void load_whenWrongArgs(String droneSerialNumber, List<LoadingMedication> medications, String errorMsg) {
        if (droneSerialNumber == null || medications == null) {
            assertThrows(NullPointerException.class, () -> dronesDao.load(droneSerialNumber, medications));
        } else {
            //when
            HttpServerErrorException exception =
                    assertThrows(HttpServerErrorException.class, () -> dronesDao.load(droneSerialNumber, medications));

            //then
            assertEquals(BAD_REQUEST, exception.getStatusCode());
            assertEquals(errorMsg, exception.getStatusText());
        }
    }

    private static Stream<Arguments> createLoadDroneRequest() {
        return Stream.of(
                Arguments.of("serial-number_8",
                        List.of(new LoadingMedication("med_code_3", 2),
                                new LoadingMedication("med_code_2", 2),
                                new LoadingMedication("med_code_1", 2)))
        );
    }

    private static Stream<Arguments> createLoadDroneErrorRequest() {
        return Stream.of(
                Arguments.of(null, null, ""),
                Arguments.of(null, List.of(), ""),
                Arguments.of("serial-number_1", null, ""),
                Arguments.of("",
                        List.of(new LoadingMedication("med_code_2", 3)),
                        droneNotExistError("")),
                Arguments.of(" ",
                        List.of(new LoadingMedication("med_code_2", 3)),
                        droneNotExistError(" ")),
                Arguments.of("serial-number_1",
                        List.of(), emptyMedicationsListError()),
                Arguments.of("fake-number",
                        List.of(new LoadingMedication("med_code_2", 3)),
                        droneNotExistError("fake-number")),
                Arguments.of("serial-number_1",
                        List.of(new LoadingMedication("", 3)),
                        medicationsNotExistError(List.of(""))),
                Arguments.of("serial-number_1",
                        List.of(new LoadingMedication(" ", 3)),
                        medicationsNotExistError(List.of(" "))),
                Arguments.of("serial-number_1",
                        List.of(new LoadingMedication("fake_code", 3)),
                        medicationsNotExistError(List.of("fake_code"))),
                Arguments.of("serial-number_1",
                        List.of(new LoadingMedication("med_code_3", -5)),
                        countWrongError(Map.of("med_code_3", -5))),
                Arguments.of("serial-number_1",
                        List.of(new LoadingMedication("med_code_3", 0)),
                        countWrongError(Map.of("med_code_3", 0))),
                Arguments.of("serial-number_1",
                        List.of(new LoadingMedication("med_code_1", 1)),
                        overweightLoadingError("serial-number_1", bd(5), bd(10))),
                Arguments.of("serial-number_4",
                        List.of(new LoadingMedication("med_code_1", 21)),
                        illegalDroneStateError("serial-number_4", DELIVERING, AVAILABLE_DRONE_STATES),
                Arguments.of("serial-number_10",
                        List.of(new LoadingMedication("med_code_1", 51)),
                        overweightLoadingError("serial-number_4", bd(500), bd(510)))),
                Arguments.of("serial-number_10",
                        List.of(new LoadingMedication("med_code_1", 1)),
                        lessThanMinBatteryLevelError("serial-number_10", MIN_BATTERY_LEVEL, bd(24)),
                Arguments.of("serial-number_5",
                        List.of(new LoadingMedication("med_code_1", 1),
                                new LoadingMedication("med_code_1", 1),
                                new LoadingMedication("med_code_2", 1),
                                new LoadingMedication("med_code_2", 1)),
                        medicationsDuplicateError(List.of("med_code_1", "med_code_2"))))
        );
    }

    private static Stream<Arguments> createNotEmptyDroneRequest() {
        return Stream.of(
                Arguments.of(new CreateDroneRequest("serial-number_10", MIDDLEWEIGHT)),
                Arguments.of(new CreateDroneRequest("serial_number", MIDDLEWEIGHT)),
                Arguments.of(new CreateDroneRequest("serial_number", LIGHTWEIGHT)),
                Arguments.of(new CreateDroneRequest("serial_number", HEAVYWEIGHT)),
                Arguments.of(new CreateDroneRequest("serial_number", CRUISERWEIGHT))
        );
    }

    private static Stream<Arguments> createEmptyDroneRequest() {
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of(new CreateDroneRequest(null, null), modelIsNullError()),
                Arguments.of(new CreateDroneRequest(null, MIDDLEWEIGHT), serialNumberIsNullError()),
                Arguments.of(new CreateDroneRequest("serial_number", null), modelIsNullError()),
                Arguments.of(new CreateDroneRequest("", MIDDLEWEIGHT), serialNumberIsBlankError()),
                Arguments.of(new CreateDroneRequest(" ", MIDDLEWEIGHT), serialNumberIsBlankError())
        );
    }

    private static Stream<Arguments> serialNumberToBatteryLevel() {
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of("", null),
                Arguments.of("  ", null),
                Arguments.of("serial-number_1", BigDecimal.valueOf(85)),
                Arguments.of("serial-number_4", BigDecimal.valueOf(100)),
                Arguments.of("serial-number_7", BigDecimal.valueOf(80)),
                Arguments.of("serial-number_10", BigDecimal.valueOf(24))
        );
    }

    private static Stream<Arguments> serialNumberToMedications() {
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of("", null),
                Arguments.of("  ", null),
                Arguments.of("serial-number_1", List.of(
                        createMedication("med_code_2", bd(50), "Medication_2"),
                        createMedication("med_code_4", bd(45), "Medication_4")
                )),
                Arguments.of("serial-number_4", List.of()),
                Arguments.of("serial-number_8", List.of(
                        createMedication("med_code_3", bd(75), "Medication_3"),
                        createMedication("med_code_2", bd(50), "Medication_2"),
                        createMedication("med_code_1", bd(10), "Medication_1")
                ))
        );
    }
}