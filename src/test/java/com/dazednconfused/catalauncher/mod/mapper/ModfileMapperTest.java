package com.dazednconfused.catalauncher.mod.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.dazednconfused.catalauncher.database.mod.entity.ModfileEntity;
import com.dazednconfused.catalauncher.mod.dto.ModfileDTO;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

public class ModfileMapperTest {

    @Test
    void to_dto_success() {

        // prepare mock data ---
        ModfileEntity entity = ModfileEntity.builder()
            .id(1)
            .path("testPath1")
            .hash("testHash1")
            .createdDate(new Timestamp(System.currentTimeMillis()))
            .updatedDate(new Timestamp(System.currentTimeMillis()))
            .build();

        ModfileDTO dto = ModfileDTO.builder()
            .id(1)
            .path("testPath1")
            .hash("testHash1")
            .createdDate(entity.getCreatedDate().toLocalDateTime())
            .updatedDate(entity.getUpdatedDate().toLocalDateTime())
            .build();

        // execute test ---
        ModfileDTO result = ModfileMapper.INSTANCE.toDTO(entity);

        // verify assertions ---
        assertThat(result).usingRecursiveComparison().isEqualTo(dto);
    }

    @Test
    void to_entity_success() {

        // prepare mock data ---
        ModfileDTO dto = ModfileDTO.builder()
            .id(1)
            .path("testPath1")
            .hash("testHash1")
            .createdDate(LocalDateTime.now())
            .updatedDate(LocalDateTime.now())
            .build();

        ModfileEntity entity = ModfileEntity.builder()
            .id(1)
            .path("testPath1")
            .hash("testHash1")
            .createdDate(Timestamp.valueOf(dto.getCreatedDate()))
            .updatedDate(Timestamp.valueOf(dto.getUpdatedDate()))
            .build();


        // execute test ---
        ModfileEntity result = ModfileMapper.INSTANCE.toEntity(dto);

        // verify assertions ---
        assertThat(result).usingRecursiveComparison().isEqualTo(entity);
    }
}