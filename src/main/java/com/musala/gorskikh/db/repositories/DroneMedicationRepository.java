package com.musala.gorskikh.db.repositories;

import com.musala.gorskikh.db.entities.DroneMedicationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DroneMedicationRepository
        extends JpaRepository<DroneMedicationEntity, DroneMedicationEntity.DroneModelPk> {

    @Query("""
            SELECT e FROM DroneMedicationEntity e
            WHERE e.primaryKey.drone.droneSerialNumber = :droneSerialNumber
            AND e.primaryKey.medication.code = :medicationCode
            """)
    Optional<DroneMedicationEntity> findByDroneAndMedication(String droneSerialNumber,
                                                             String medicationCode);

}
