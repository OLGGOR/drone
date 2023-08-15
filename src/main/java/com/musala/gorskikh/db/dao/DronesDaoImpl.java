package com.musala.gorskikh.db.dao;

import com.musala.gorskikh.db.entities.DroneEntity;
import com.musala.gorskikh.db.entities.DroneMedicationEntity;
import com.musala.gorskikh.db.entities.DroneMedicationEntity.DroneModelPk;
import com.musala.gorskikh.db.entities.DroneModelEntity;
import com.musala.gorskikh.db.entities.MedicationEntity;
import com.musala.gorskikh.db.repositories.DroneMedicationRepository;
import com.musala.gorskikh.db.repositories.DroneModelRepository;
import com.musala.gorskikh.db.repositories.DronesRepository;
import com.musala.gorskikh.db.repositories.MedicationsRepository;
import com.musala.gorskikh.model.*;
import com.musala.gorskikh.services.converters.DroneConverter;
import com.musala.gorskikh.services.converters.MedicationConverter;
import com.musala.gorskikh.services.validators.ValidationResult;
import com.musala.gorskikh.services.validators.Validator;
import lombok.NonNull;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpServerErrorException;

import java.math.BigDecimal;
import java.util.*;

import static com.musala.gorskikh.model.DroneState.*;
import static com.musala.gorskikh.services.validators.DroneLoadingValidator.AVAILABLE_DRONE_STATES;
import static com.musala.gorskikh.services.validators.DroneLoadingValidator.MIN_BATTERY_LEVEL;
import static com.musala.gorskikh.util.ErrorsUtil.*;
import static java.math.BigDecimal.ZERO;
import static java.util.Optional.ofNullable;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

@Service
public class DronesDaoImpl implements DronesDao {

    private final DronesRepository dronesRepository;
    private final MedicationsRepository medicationsRepository;
    private final DroneModelRepository droneModelRepository;
    private final DroneMedicationRepository droneMedicationRepository;

    private final DroneConverter droneConverter;
    private final MedicationConverter medicationConverter;

    private final Validator<DroneEntity> validator;
    private final DronesDao self;

    public DronesDaoImpl(DronesRepository dronesRepository, MedicationsRepository medicationsRepository,
                         DroneModelRepository droneModelRepository, DroneMedicationRepository droneMedicationRepository, DroneConverter droneConverter, MedicationConverter medicationConverter,
                         Validator<DroneEntity> validator, @Lazy DronesDao self) {
        this.dronesRepository = dronesRepository;
        this.medicationsRepository = medicationsRepository;
        this.droneModelRepository = droneModelRepository;
        this.droneMedicationRepository = droneMedicationRepository;
        this.droneConverter = droneConverter;
        this.medicationConverter = medicationConverter;
        this.validator = validator;
        this.self = self;
    }

    @Transactional
    @Override
    public DroneDto register(@NonNull CreateDroneRequest createRequest) {
        DroneModelEnum model = ofNullable(createRequest.getModel())
                .orElseThrow(() -> new HttpServerErrorException(BAD_REQUEST, modelIsNullError()));
        String serialNumber = ofNullable(createRequest.getDroneSerialNumber())
                .orElseThrow(() -> new HttpServerErrorException(BAD_REQUEST, serialNumberIsNullError()));

        if (serialNumber.isBlank())
            throw new HttpServerErrorException(BAD_REQUEST, serialNumberIsBlankError());

        DroneModelEntity droneModel = droneModelRepository.findById(model)
                .orElseThrow(() -> new HttpServerErrorException(BAD_REQUEST, "Unknown model of drone: " + model));

        Optional<DroneEntity> droneById = dronesRepository.findById(serialNumber);
        if (droneById.isPresent())
            throw new HttpServerErrorException(BAD_REQUEST, droneAlreadyExistError(serialNumber));

        DroneEntity droneEntity = createDrone(createRequest, droneModel);
        return droneConverter.entityToDto(dronesRepository.save(droneEntity));
    }

    @Transactional
    @Override
    public void load(@NonNull String droneSerialNumber, @NonNull List<LoadingMedication> medications) {
        if (medications.isEmpty())
            throw new HttpServerErrorException(BAD_REQUEST, emptyMedicationsListError());

        DroneEntity drone = dronesRepository.findByIdFetchMedications(droneSerialNumber)
                .orElseThrow(() -> new HttpServerErrorException(BAD_REQUEST, droneNotExistError(droneSerialNumber)));

        ValidationResult validationResult = validator.validate(drone);
        if (!validationResult.isValid())
            throw new HttpServerErrorException(BAD_REQUEST, validationResult.getErrorMsg());

        Map<MedicationEntity, Integer> medicationCount = getMedicationEntities(medications);

        BigDecimal freeSpace = getFreeSpace(drone);
        BigDecimal medicationsWeight = getWeight(medicationCount);

        if (freeSpace.compareTo(medicationsWeight) < 0)
            throw new HttpServerErrorException(BAD_REQUEST,
                    overweightLoadingError(droneSerialNumber, freeSpace, medicationsWeight));

        loadMedications(drone, medicationCount);
    }

    @Transactional(propagation = REQUIRES_NEW)
    @Override
    public void setState(DroneEntity drone, DroneState state) {
        drone.setState(state);
        dronesRepository.save(drone);
    }

    @Transactional(readOnly = true)
    @Override
    public List<MedicationDto> getMedications(@NonNull String droneSerialNumber) {
        DroneEntity drone = dronesRepository.findByIdFetchMedications(droneSerialNumber)
                .orElseThrow(() -> new HttpServerErrorException(BAD_REQUEST, droneNotExistError(droneSerialNumber)));

        return medicationConverter.entitiesToDtos(drone.getMedications());
    }

    @Transactional(readOnly = true)
    @Override
    public List<DroneDto> getAvailableDrones() {
        List<DroneEntity> drones =
                dronesRepository.findAvailable(AVAILABLE_DRONE_STATES, MIN_BATTERY_LEVEL);
        return droneConverter.entitiesToDtos(drones);
    }

    @Transactional(readOnly = true)
    @Override
    public Map<String, BigDecimal> getBatteryLevels() {
        return dronesRepository.findAll().stream()
                .collect(toMap(DroneEntity::getDroneSerialNumber, DroneEntity::getBatteryLevel));
    }

    @Transactional(readOnly = true)
    @Override
    public BatteryLevel getBatteryLevel(@NonNull String droneSerialNumber) {
        BigDecimal batteryLevel = dronesRepository.getBatteryLevel(droneSerialNumber)
                .orElseThrow(() -> new HttpServerErrorException(BAD_REQUEST, droneNotExistError(droneSerialNumber)));

        return new BatteryLevel()
                .level(batteryLevel);
    }

    public BigDecimal getFreeSpace(DroneEntity drone) {
        BigDecimal totalWeight = ZERO;

        for (MedicationEntity medication : drone.getMedications()) {
            BigDecimal count = droneMedicationRepository
                    .findByDroneAndMedication(drone.getDroneSerialNumber(), medication.getCode())
                    .map(dm -> BigDecimal.valueOf(dm.getCount())).orElse(ZERO);

            totalWeight = totalWeight.add(medication.getWeightGr().multiply(count));
        }

        return drone.getModel().getWeightLimit().subtract(totalWeight);
    }

    private BigDecimal getWeight(Map<MedicationEntity, Integer> medicationCount) {
        return medicationCount.entrySet().stream()
                .map(e -> e.getKey().getWeightGr()
                        .multiply(BigDecimal.valueOf(e.getValue())))
                .reduce(ZERO, BigDecimal::add);
    }

    private static DroneEntity createDrone(CreateDroneRequest request, DroneModelEntity droneModel) {
        DroneEntity droneEntity = new DroneEntity();
        droneEntity.setModel(droneModel);
        droneEntity.setBatteryLevel(BigDecimal.valueOf(100L));
        droneEntity.setState(IDLE);
        droneEntity.setDroneSerialNumber(request.getDroneSerialNumber());
        return droneEntity;
    }

    private void loadMedications(DroneEntity drone, Map<MedicationEntity, Integer> medicationCount) {
        DroneState previousState = drone.getState();

        try {
            self.setState(drone, LOADING);
            List<DroneMedicationEntity> droneMedicationEntities = new ArrayList<>();

            for (MedicationEntity medication : medicationCount.keySet()) {
                DroneModelPk droneModel = new DroneModelPk(drone, medication);
                Integer newMedicationCount = medicationCount.get(medication);
                int count = droneMedicationRepository.findById(droneModel)
                        .map(dm -> dm.getCount() + newMedicationCount).orElse(newMedicationCount);
                droneMedicationEntities.add(new DroneMedicationEntity(droneModel, count));
            }

            droneMedicationRepository.saveAll(droneMedicationEntities);

            self.setState(drone, LOADED);
        } catch (Exception ex) {
            self.setState(drone, previousState);
            throw ex;
        }
    }

    private List<String> getAbsentCodes(List<String> searchMedicationCodes, List<MedicationEntity> medicationsFromDb) {
        List<String> factCodes = medicationsFromDb.stream()
                .map(MedicationEntity::getCode).toList();

        return searchMedicationCodes.stream()
                .filter(c -> !factCodes.contains(c)).toList();
    }

    private Map<MedicationEntity, Integer> getMedicationEntities(List<LoadingMedication> medications) {
        List<String> duplicateCodes = medications.stream()
                .collect(groupingBy(LoadingMedication::getMedicationCode, counting())).entrySet().stream()
                .filter(e -> e.getValue() > 1)
                .map(Map.Entry::getKey).toList();

        if (!duplicateCodes.isEmpty())
            throw new HttpServerErrorException(BAD_REQUEST, medicationsDuplicateError(duplicateCodes));

        Map<String, Integer> codeToCount = medications.stream()
                .collect(toMap(LoadingMedication::getMedicationCode, LoadingMedication::getCount));
        Map<String, Integer> wrongCodeToCount = getWrongCounts(codeToCount);
        if (!wrongCodeToCount.isEmpty())
            throw new HttpServerErrorException(BAD_REQUEST, countWrongError(wrongCodeToCount));


        List<String> codes = codeToCount.keySet().stream().toList();

        Map<MedicationEntity, Integer> medicationCount = medicationsRepository.findAllByCodeIn(codes).stream()
                .collect(toMap(identity(), m -> codeToCount.get(m.getCode())));

        if (medicationCount.size() < codeToCount.size()) {
            List<MedicationEntity> dbCodes = medicationCount.keySet().stream().toList();
            List<String> absentCodes = getAbsentCodes(codes, dbCodes);
            throw new HttpServerErrorException(BAD_REQUEST, medicationsNotExistError(absentCodes));
        }

        return medicationCount;
    }

    private Map<String, Integer> getWrongCounts(Map<String, Integer> codeToCount) {
        return codeToCount.entrySet().stream()
                .filter(c -> c.getValue() <= 0)
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
