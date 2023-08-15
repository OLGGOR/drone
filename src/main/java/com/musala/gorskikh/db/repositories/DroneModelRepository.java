package com.musala.gorskikh.db.repositories;

import com.musala.gorskikh.db.entities.DroneModelEntity;
import com.musala.gorskikh.model.DroneModelEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DroneModelRepository
        extends JpaRepository<DroneModelEntity, DroneModelEnum> {

}
