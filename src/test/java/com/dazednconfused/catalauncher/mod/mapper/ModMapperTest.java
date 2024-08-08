package com.dazednconfused.catalauncher.mod.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.dazednconfused.catalauncher.database.mod.entity.ModEntity;
import com.dazednconfused.catalauncher.database.mod.entity.ModfileEntity;
import com.dazednconfused.catalauncher.mod.dto.ModDTO;
import com.dazednconfused.catalauncher.mod.dto.ModfileDTO;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

class ModMapperTest {

    @Test
    void to_dto_success() {

        // prepare mock data ---
        ModEntity entity = ModEntity.builder()
            .id(1L)
            .name("test")
            .createdDate(new Timestamp(System.currentTimeMillis()))
            .updatedDate(new Timestamp(System.currentTimeMillis()))
            .modfiles(new ArrayList<>())
            .build();

        ModDTO dto = ModDTO.builder()
            .id(1L)
            .name("test")
            .createdDate(entity.getCreatedDate().toLocalDateTime())
            .updatedDate(entity.getUpdatedDate().toLocalDateTime())
            .modfiles(new ArrayList<>())
            .build();

        // execute test ---
        ModDTO result = ModMapper.INSTANCE.toDTO(entity);

        // verify assertions ---
        assertThat(result).usingRecursiveComparison().isEqualTo(dto);
    }

    @Test
    void to_dto_with_children_success() {

        // prepare mock data ---
        ModfileEntity childEntity1 = ModfileEntity.builder()
            .id(1L)
            .path("testPath1")
            .hash("testHash1")
            .createdDate(new Timestamp(System.currentTimeMillis()))
            .updatedDate(new Timestamp(System.currentTimeMillis()))
            .build();

        ModfileEntity childEntity2 = ModfileEntity.builder()
            .id(2L)
            .path("testPath2")
            .hash("testHash2")
            .createdDate(new Timestamp(System.currentTimeMillis()))
            .updatedDate(new Timestamp(System.currentTimeMillis()))
            .build();

        ModfileEntity childEntity3 = ModfileEntity.builder()
            .id(3L)
            .path("testPath3")
            .hash("testHash3")
            .createdDate(new Timestamp(System.currentTimeMillis()))
            .updatedDate(new Timestamp(System.currentTimeMillis()))
            .build();

        ModEntity entity = ModEntity.builder()
            .id(1L)
            .name("test")
            .createdDate(new Timestamp(System.currentTimeMillis()))
            .updatedDate(new Timestamp(System.currentTimeMillis()))
            .modfiles(Arrays.asList(
                childEntity1, childEntity2, childEntity3
            ))
            .build();

        ModfileDTO childDto1 = ModfileDTO.builder()
            .id(1L)
            .path("testPath1")
            .hash("testHash1")
            .createdDate(childEntity1.getCreatedDate().toLocalDateTime())
            .updatedDate(childEntity1.getUpdatedDate().toLocalDateTime())
            .build();

        ModfileDTO childDto2 = ModfileDTO.builder()
            .id(2L)
            .path("testPath2")
            .hash("testHash2")
            .createdDate(childEntity2.getCreatedDate().toLocalDateTime())
            .updatedDate(childEntity2.getUpdatedDate().toLocalDateTime())
            .build();

        ModfileDTO childDto3 = ModfileDTO.builder()
            .id(3L)
            .path("testPath3")
            .hash("testHash3")
            .createdDate(childEntity3.getCreatedDate().toLocalDateTime())
            .updatedDate(childEntity3.getUpdatedDate().toLocalDateTime())
            .build();

        ModDTO dto = ModDTO.builder()
            .id(1L)
            .name("test")
            .createdDate(entity.getCreatedDate().toLocalDateTime())
            .updatedDate(entity.getUpdatedDate().toLocalDateTime())
            .modfiles(Arrays.asList(
                childDto1, childDto2, childDto3
            ))
            .build();

        // execute test ---
        ModDTO result = ModMapper.INSTANCE.toDTO(entity);

        // verify assertions ---
        assertThat(result).usingRecursiveComparison().isEqualTo(dto);
    }

    @Test
    void to_entity_success() {

        // prepare mock data ---
        ModDTO dto = ModDTO.builder()
            .id(1L)
            .name("test")
            .createdDate(LocalDateTime.now())
            .updatedDate(LocalDateTime.now())
            .modfiles(new ArrayList<>())
            .build();

        ModEntity entity = ModEntity.builder()
            .id(1L)
            .name("test")
            .createdDate(Timestamp.valueOf(dto.getCreatedDate()))
            .updatedDate(Timestamp.valueOf(dto.getUpdatedDate()))
            .modfiles(new ArrayList<>())
            .build();

        // execute test ---
        ModEntity result = ModMapper.INSTANCE.toEntity(dto);

        // verify assertions ---
        assertThat(result).usingRecursiveComparison().isEqualTo(entity);
    }

    @Test
    void to_entity_with_children_success() {

        // prepare mock data ---
        ModfileDTO childDto1 = ModfileDTO.builder()
            .id(1L)
            .path("testPath1")
            .hash("testHash1")
            .createdDate(LocalDateTime.now())
            .updatedDate(LocalDateTime.now())
            .build();

        ModfileDTO childDto2 = ModfileDTO.builder()
            .id(2L)
            .path("testPath2")
            .hash("testHash2")
            .createdDate(LocalDateTime.now())
            .updatedDate(LocalDateTime.now())
            .build();

        ModfileDTO childDto3 = ModfileDTO.builder()
            .id(3L)
            .path("testPath3")
            .hash("testHash3")
            .createdDate(LocalDateTime.now())
            .updatedDate(LocalDateTime.now())
            .build();

        ModDTO dto = ModDTO.builder()
            .id(1L)
            .name("test")
            .createdDate(LocalDateTime.now())
            .updatedDate(LocalDateTime.now())
            .modfiles(Arrays.asList(
                childDto1, childDto2, childDto3
            ))
            .build();

        ModfileEntity childEntity1 = ModfileEntity.builder()
            .id(1L)
            .path("testPath1")
            .hash("testHash1")
            .createdDate(Timestamp.valueOf(childDto1.getCreatedDate()))
            .updatedDate(Timestamp.valueOf(childDto1.getUpdatedDate()))
            .build();

        ModfileEntity childEntity2 = ModfileEntity.builder()
            .id(2L)
            .path("testPath2")
            .hash("testHash2")
            .createdDate(Timestamp.valueOf(childDto2.getCreatedDate()))
            .updatedDate(Timestamp.valueOf(childDto2.getUpdatedDate()))
            .build();

        ModfileEntity childEntity3 = ModfileEntity.builder()
            .id(3L)
            .path("testPath3")
            .hash("testHash3")
            .createdDate(Timestamp.valueOf(childDto3.getCreatedDate()))
            .updatedDate(Timestamp.valueOf(childDto3.getUpdatedDate()))
            .build();

        ModEntity entity = ModEntity.builder()
            .id(1L)
            .name("test")
            .createdDate(Timestamp.valueOf(dto.getCreatedDate()))
            .updatedDate(Timestamp.valueOf(dto.getUpdatedDate()))
            .modfiles(Arrays.asList(
                childEntity1, childEntity2, childEntity3
            ))
            .build();

        // execute test ---
        ModDTO result = ModMapper.INSTANCE.toDTO(entity);

        // verify assertions ---
        assertThat(result).usingRecursiveComparison().isEqualTo(dto);
    }
}