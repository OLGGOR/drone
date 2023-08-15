package com.musala.gorskikh.db.repositories;

import com.musala.gorskikh.db.entities.DroneEntity;
import com.musala.gorskikh.model.DroneState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface DronesRepository
        extends JpaRepository<DroneEntity, String> {

    @Query("""
            SELECT e FROM DroneEntity e
            WHERE e.state in :states AND e.batteryLevel >= :minBatteryLevel
            """)
    List<DroneEntity> findAvailable(List<DroneState> states, BigDecimal minBatteryLevel);

    @Query(value = """
            SELECT e.batteryLevel FROM DroneEntity e
            WHERE e.droneSerialNumber = :serialNumber""")
    Optional<BigDecimal> getBatteryLevel(String serialNumber);

    @Query(value = """
            SELECT e FROM DroneEntity e LEFT JOIN FETCH e.medications
            WHERE e.droneSerialNumber = :serialNumber""")
    Optional<DroneEntity> findByIdFetchMedications(String serialNumber);
}
