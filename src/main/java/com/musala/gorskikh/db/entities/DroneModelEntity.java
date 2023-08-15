package com.musala.gorskikh.db.entities;

import com.musala.gorskikh.model.DroneModelEnum;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Setter;

import java.math.BigDecimal;

import static jakarta.persistence.EnumType.STRING;

@Data
@Setter
@Entity
@Table(name = "drone_model")
public class DroneModelEntity {

    @Id
    @Enumerated(STRING)
    @Column(name = "model")
    private DroneModelEnum model;

    @Column(name = "weight_limit_gr")
    private BigDecimal weightLimit;
}
