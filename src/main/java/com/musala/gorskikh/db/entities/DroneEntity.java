package com.musala.gorskikh.db.entities;

import com.musala.gorskikh.model.DroneState;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import static jakarta.persistence.CascadeType.MERGE;
import static jakarta.persistence.EnumType.STRING;

@Data
@Setter
@Entity
@Table(name = "drones")
public class DroneEntity {

    @Id
    @Column(name = "serial_number")
    private String droneSerialNumber;

    @ManyToOne
    @JoinColumn(name = "model")
    private DroneModelEntity model;

    @Column(name = "battery_level")
    private BigDecimal batteryLevel;

    @Enumerated(STRING)
    @Column(name = "state")
    private DroneState state;

    @ManyToMany(cascade = MERGE)
    @JoinTable(
            name = "drone_medication",
            joinColumns = @JoinColumn(name = "drone_serial_number"),
            inverseJoinColumns = @JoinColumn(name = "medication_code")
    )
    private Set<MedicationEntity> medications = new HashSet<>();
}
