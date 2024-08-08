package com.dazednconfused.catalauncher.mod.mapper;

import com.dazednconfused.catalauncher.database.mod.entity.ModEntity;
import com.dazednconfused.catalauncher.mod.dto.ModDTO;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ModMapper {

    ModMapper INSTANCE = Mappers.getMapper(ModMapper.class);

    ModDTO toDTO(ModEntity entity);

    ModEntity toEntity(ModDTO dto);

    default LocalDateTime map(Timestamp value) {
        return value == null ? null : value.toLocalDateTime();
    }

    default Timestamp map(LocalDateTime value) {
        return value == null ? null : Timestamp.valueOf(value);
    }
}
