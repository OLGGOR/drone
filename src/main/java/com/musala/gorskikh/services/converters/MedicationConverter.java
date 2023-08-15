package com.musala.gorskikh.services.converters;

import com.musala.gorskikh.db.entities.MedicationEntity;
import com.musala.gorskikh.model.ImageDto;
import com.musala.gorskikh.model.MedicationDto;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.mapstruct.Mapper;

import java.util.Base64;
import java.util.List;
import java.util.Set;

import static com.musala.gorskikh.model.ImageContentType.IMAGE_JPEG;

@Mapper(componentModel = "spring")
public interface MedicationConverter {

    MedicationEntity dtoToEntity(MedicationDto dto);

    MedicationDto entityToDto(MedicationEntity entity);

    List<MedicationDto> entitiesToDtos(Set<MedicationEntity> entity);

    @SneakyThrows
    default byte[] toBytes(@NonNull ImageDto image) {
        String encodedImage = image.getContent();
        return encodedImage != null ? Base64.getDecoder().decode(encodedImage) : null;
    }

    @SneakyThrows
    default ImageDto toDto(byte[] bytes) {
        String encodedFile = Base64.getEncoder().encodeToString(bytes);

        return new ImageDto()
                .contentType(IMAGE_JPEG)
                .content(encodedFile);
    }
}
