package com.dazednconfused.catalauncher.mod.mapper;

import com.dazednconfused.catalauncher.database.mod.entity.ModfileEntity;
import com.dazednconfused.catalauncher.mod.dto.ModfileDTO;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ModfileMapper {

    ModfileMapper INSTANCE = Mappers.getMapper(ModfileMapper.class);

    ModfileDTO toDTO(ModfileEntity entity);

    ModfileEntity toEntity(ModfileDTO dto);

    default LocalDateTime map(Timestamp value) {
        return value == null ? null : value.toLocalDateTime();
    }

    default Timestamp map(LocalDateTime value) {
        return value == null ? null : Timestamp.valueOf(value);
    }
}
