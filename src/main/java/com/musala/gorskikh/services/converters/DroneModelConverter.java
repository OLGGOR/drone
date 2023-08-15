package com.musala.gorskikh.services.converters;

import com.musala.gorskikh.db.entities.DroneModelEntity;
import com.musala.gorskikh.model.DroneModelDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DroneModelConverter {

    DroneModelEntity dtoToEntity(DroneModelDto dto);

    DroneModelDto entityToDto(DroneModelEntity entity);
}
