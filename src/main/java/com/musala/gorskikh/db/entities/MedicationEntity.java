package com.musala.gorskikh.db.entities;

import lombok.Data;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "medications")
public class MedicationEntity {

    @Id
    @Column(name = "code")
    private String code;

    @Column(name = "name")
    private String name;

    @Column(name = "weight_gr")
    private BigDecimal weightGr;

    @Lob
    @Column(name = "image", columnDefinition="BLOB")
    private byte[] image;
}
