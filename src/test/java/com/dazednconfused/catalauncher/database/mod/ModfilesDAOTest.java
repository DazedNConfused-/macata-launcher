package com.dazednconfused.catalauncher.database.mod;

import static org.assertj.core.api.Assertions.assertThat;

import com.dazednconfused.catalauncher.database.BaseDAO;
import com.dazednconfused.catalauncher.database.mod.entity.ModfileEntity;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ModfilesDAOTest {

    private static BaseDAO<ModfileEntity> dao;

    @BeforeAll
    public static void setup() {
        dao = new ModfilesH2DAOImpl();
    }

    @BeforeEach
    public void before() {
        dao.initializeTable();
    }

    @AfterEach
    public void teardown() {
        ((ModfilesH2DAOImpl) dao).wipe();
    }

    @AfterAll
    public static void cleanup() {
        ((ModfilesH2DAOImpl) dao).destroy();
    }

    @Test
    void insert_success() {

        // prepare mock data ---
        ModfileEntity entity = ModfileEntity.builder()
            .path("testPath1")
            .hash("testHash1")
            .build();

        // execute test ---
        ModfileEntity result = dao.insert(entity);

        // verify assertions ---
        assertThat(result).isNotNull();

        assertThat(result.getId()).isNotZero();
        assertThat(result.getCreatedDate()).isNotNull();
        assertThat(result.getUpdatedDate()).isNotNull();

        assertThat(result.getCreatedDate()).isEqualTo(result.getUpdatedDate());
    }

    @Test
    void find_by_id_success() {

        // prepare mock data ---
        dao.insert(ModfileEntity.builder()
            .path("testPath1")
            .hash("testHash1")
            .build()
        );

        ModfileEntity entity = dao.insert(ModfileEntity.builder()
            .path("testPath2")
            .hash("testHash2")
            .build()
        );

        dao.insert(ModfileEntity.builder()
            .path("testPath3")
            .hash("testHash3")
            .build()
        );

        // execute test ---
        Optional<ModfileEntity> resultOpt = dao.findById(entity.getId());

        // verify assertions ---
        assertThat(resultOpt).isPresent();

        ModfileEntity result = resultOpt.get();

        assertThat(result.getPath()).isEqualTo(entity.getPath());
        assertThat(result.getHash()).isEqualTo(entity.getHash());
    }

    @Test
    void find_by_ids_success() {

        // prepare mock data ---
        ModfileEntity entity1 = dao.insert(ModfileEntity.builder()
            .path("testPath1")
            .hash("testHash1")
            .build()
        );

        ModfileEntity entity2 = dao.insert(ModfileEntity.builder()
            .path("testPath2")
            .hash("testHash2")
            .build()
        );

        ModfileEntity entity3 = dao.insert(ModfileEntity.builder()
            .path("testPath3")
            .hash("testHash3")
            .build()
        );

        // execute test ---
        List<ModfileEntity> result = dao.findById(
            entity1.getId(), entity2.getId(), entity3.getId(),
            23232L // random ID that won't be found
        );

        // verify assertions ---
        assertThat(result).isNotEmpty();

        assertThat(result).containsExactlyInAnyOrder(entity1, entity2, entity3);
    }

    @Test
    void find_all_success() {

        // prepare mock data ---
        ModfileEntity entity1 = dao.insert(ModfileEntity.builder()
            .path("testPath1")
            .hash("testHash1")
            .build()
        );

        ModfileEntity entity2 = dao.insert(ModfileEntity.builder()
            .path("testPath2")
            .hash("testHash2")
            .build()
        );

        ModfileEntity entity3 = dao.insert(ModfileEntity.builder()
            .path("testPath3")
            .hash("testHash3")
            .build()
        );

        // execute test ---
        List<ModfileEntity> result = dao.findAll();

        // verify assertions ---
        assertThat(result).isNotEmpty();

        assertThat(result).containsExactlyInAnyOrder(entity1, entity2, entity3);
    }

    @Test
    void count_all_success() {

        // prepare mock data ---
        dao.insert(ModfileEntity.builder()
            .path("testPath1")
            .hash("testHash1")
            .build()
        );

        dao.insert(ModfileEntity.builder()
            .path("testPath2")
            .hash("testHash2")
            .build()
        );

        dao.insert(ModfileEntity.builder()
            .path("testPath3")
            .hash("testHash3")
            .build()
        );

        // execute test ---
        long result = dao.countAll();

        // verify assertions ---
        assertThat(result).isEqualTo(3);
    }

    @Test
    void update_success() {

        // prepare mock data ---
        dao.insert(ModfileEntity.builder()
            .path("testPath1")
            .hash("testHash1")
            .build()
        );

        ModfileEntity entity = dao.insert(ModfileEntity.builder()
            .path("testPath2")
            .hash("testHash2")
            .build()
        );

        dao.insert(ModfileEntity.builder()
            .path("testPath3")
            .hash("testHash3")
            .build()
        );

        ModfileEntity updatedEntity2 = ModfileEntity.builder()
            .id(entity.getId())
            .path("updatedTestPath2")
            .hash("updatedTestHash2")
            .build();

        // execute test ---
        ModfileEntity result = dao.update(updatedEntity2);

        // verify assertions ---
        assertThat(result).isNotNull();

        assertThat(result.getPath()).isEqualTo(updatedEntity2.getPath());
        assertThat(result.getHash()).isEqualTo(updatedEntity2.getHash());
        assertThat(result.getCreatedDate()).isEqualTo(entity.getCreatedDate());
        assertThat(result.getUpdatedDate()).isAfter(entity.getUpdatedDate());
    }

    @Test
    void delete_success() {

        // prepare mock data ---
        dao.insert(ModfileEntity.builder()
            .path("testPath1")
            .hash("testHash1")
            .build()
        );

        ModfileEntity entity = dao.insert(ModfileEntity.builder()
            .path("testPath2")
            .hash("testHash2")
            .build()
        );

        dao.insert(ModfileEntity.builder()
            .path("testPath3")
            .hash("testHash3")
            .build()
        );

        // execute test ---
        dao.delete(entity);
        Optional<ModfileEntity> result = dao.findById(entity.getId());


        // verify assertions ---
        assertThat(result).isEmpty();
    }
}