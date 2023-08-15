package com.musala.gorskikh.db.repositories;

import com.musala.gorskikh.db.entities.MedicationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicationsRepository extends JpaRepository<MedicationEntity, String> {

    List<MedicationEntity> findAllByCodeIn(List<String> codes);
}
