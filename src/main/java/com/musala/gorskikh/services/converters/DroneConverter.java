package com.musala.gorskikh.services.converters;

import com.musala.gorskikh.db.entities.DroneEntity;
import com.musala.gorskikh.model.DroneDto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = DroneModelConverter.class)
public interface DroneConverter {

    DroneDto entityToDto(DroneEntity entity);

    List<DroneDto> entitiesToDtos(List<DroneEntity> entity);
}
