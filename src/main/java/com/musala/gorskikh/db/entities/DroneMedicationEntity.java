package com.musala.gorskikh.db.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

import static jakarta.persistence.CascadeType.MERGE;

@Data
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "drone_medication")
public class DroneMedicationEntity {

    @EmbeddedId
    private DroneMedicationEntity.DroneModelPk primaryKey;

    @Column(name = "count")
    private Integer count;

    @Embeddable
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DroneModelPk implements Serializable {

        @OneToOne(cascade = MERGE)
        @JoinColumn(name = "drone_serial_number")
        private DroneEntity drone;

        @OneToOne(cascade = MERGE)
        @JoinColumn(name = "medication_code")
        private MedicationEntity medication;

    }
}
