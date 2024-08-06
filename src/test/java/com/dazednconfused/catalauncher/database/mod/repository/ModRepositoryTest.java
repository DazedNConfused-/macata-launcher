package com.dazednconfused.catalauncher.database.mod.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.dazednconfused.catalauncher.database.base.DisposableDatabase;
import com.dazednconfused.catalauncher.database.mod.dao.ModDAO;
import com.dazednconfused.catalauncher.database.mod.dao.ModH2DAOImpl;
import com.dazednconfused.catalauncher.database.mod.dao.ModfileDAO;
import com.dazednconfused.catalauncher.database.mod.dao.ModfileH2DAOImpl;
import com.dazednconfused.catalauncher.database.mod.entity.ModEntity;
import com.dazednconfused.catalauncher.database.mod.entity.ModfileEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ModRepositoryTest {

    private static final UUID uuid = UUID.randomUUID();

    private static ModRepository repository;

    @BeforeAll
    public static void setup() {
        ModDAO modDAO = new ModH2DAOImpl() {
            @Override
            public String getDatabaseName() {
                return super.getDatabaseName() + "_" + uuid;
            }
        };

        ModfileDAO modfileDAO = new ModfileH2DAOImpl() {
            @Override
            public String getDatabaseName() {
                return super.getDatabaseName() + "_" + uuid;
            }
        };

        repository = new ModH2RepositoryImpl(modDAO, modfileDAO) {
            @Override
            public String getDatabaseName() {
                return super.getDatabaseName() + "_" + uuid;
            }
        };
    }

    @AfterEach
    public void teardown() {
        ((DisposableDatabase) repository).reset();
    }

    @AfterAll
    public static void cleanup() {
        ((DisposableDatabase) repository).destroy();
    }

    @Test
    void insert_success() {

        // prepare mock data ---
        ModEntity entity = ModEntity.builder()
            .name("testName1")
            .modinfo("testModinfo1")
            .build();

        ModfileEntity childEntity1 = ModfileEntity.builder()
            .path("testPath1")
            .hash("testHash1")
            .build();
        ModfileEntity childEntity2 = ModfileEntity.builder()
            .path("testPath2")
            .hash("testHash2")
            .build();
        ModfileEntity childEntity3 = ModfileEntity.builder()
            .path("testPath3")
            .hash("testHash3")
            .build();

        entity.setModfiles(Arrays.asList(childEntity1, childEntity2, childEntity3));

        // execute test ---
        ModEntity result = repository.insert(entity);

        // verify assertions ---
        assertThat(result).isNotNull();

        assertThat(result.getId()).isNotZero();
        assertThat(result.getCreatedDate()).isNotNull();
        assertThat(result.getUpdatedDate()).isNotNull();
        assertThat(result.getCreatedDate()).isEqualTo(result.getUpdatedDate());

        assertThat(result.getModfiles()).hasSize(entity.getModfiles().size());

        ModfileEntity resultChildEntity1 = result.getModfiles().stream().filter(mf -> mf.getPath().endsWith("1")).findFirst().orElseThrow();
        assertThat(resultChildEntity1.getModId()).isEqualTo(result.getId());
        assertThat(resultChildEntity1.getPath()).isEqualTo(childEntity1.getPath());
        assertThat(resultChildEntity1.getHash()).isEqualTo(childEntity1.getHash());
        assertThat(resultChildEntity1.getCreatedDate()).isNotNull();
        assertThat(resultChildEntity1.getUpdatedDate()).isNotNull();
        assertThat(resultChildEntity1.getCreatedDate()).isEqualTo(resultChildEntity1.getUpdatedDate());

        ModfileEntity resultChildEntity2 = result.getModfiles().stream().filter(mf -> mf.getPath().endsWith("2")).findFirst().orElseThrow();
        assertThat(resultChildEntity2.getModId()).isEqualTo(result.getId());
        assertThat(resultChildEntity2.getPath()).isEqualTo(childEntity2.getPath());
        assertThat(resultChildEntity2.getHash()).isEqualTo(childEntity2.getHash());
        assertThat(resultChildEntity2.getCreatedDate()).isNotNull();
        assertThat(resultChildEntity2.getUpdatedDate()).isNotNull();
        assertThat(resultChildEntity2.getCreatedDate()).isEqualTo(resultChildEntity2.getUpdatedDate());

        ModfileEntity resultChildEntity3 = result.getModfiles().stream().filter(mf -> mf.getPath().endsWith("3")).findFirst().orElseThrow();
        assertThat(resultChildEntity3.getModId()).isEqualTo(result.getId());
        assertThat(resultChildEntity3.getPath()).isEqualTo(childEntity3.getPath());
        assertThat(resultChildEntity3.getHash()).isEqualTo(childEntity3.getHash());
        assertThat(resultChildEntity3.getCreatedDate()).isNotNull();
        assertThat(resultChildEntity3.getUpdatedDate()).isNotNull();
        assertThat(resultChildEntity3.getCreatedDate()).isEqualTo(resultChildEntity3.getUpdatedDate());
    }

    @Test
    void find_by_id_success() {

        // prepare mock data ---
        ModEntity entity = ModEntity.builder()
            .name("testName1")
            .modinfo("testModinfo1")
            .modfiles(Arrays.asList(
                ModfileEntity.builder()
                    .path("testPath1")
                    .hash("testHash1")
                    .build(),
                ModfileEntity.builder()
                    .path("testPath2")
                    .hash("testHash2")
                    .build(),
                ModfileEntity.builder()
                    .path("testPath3")
                    .hash("testHash3")
                    .build()
            ))
            .build();

        ModEntity inserted = repository.insert(entity);

        // execute test ---
        Optional<ModEntity> resultOpt = repository.findById(inserted.getId());

        // verify assertions ---
        assertThat(resultOpt).isPresent();

        ModEntity result = resultOpt.get();

        assertThat(result).usingRecursiveComparison().isEqualTo(inserted);
    }

    @Test
    void find_by_ids_success() {

        // prepare mock data ---
        ModEntity entity1 = repository.insert(ModEntity.builder()
            .name("testName1")
            .modinfo("testModinfo1")
            .modfiles(Arrays.asList(
                ModfileEntity.builder()
                    .path("testPath1_1")
                    .hash("testHash1_1")
                    .build(),
                ModfileEntity.builder()
                    .path("testPath1_2")
                    .hash("testHash1_2")
                    .build(),
                ModfileEntity.builder()
                    .path("testPath1_3")
                    .hash("testHash1_3")
                    .build()
            ))
            .build()
        );

        ModEntity entity2 = repository.insert(ModEntity.builder()
            .name("testName2")
            .modinfo("testModinfo2")
            .modfiles(Arrays.asList(
                ModfileEntity.builder()
                    .path("testPath2_1")
                    .hash("testHash2_1")
                    .build(),
                ModfileEntity.builder()
                    .path("testPath2_2")
                    .hash("testHash2_2")
                    .build(),
                ModfileEntity.builder()
                    .path("testPath2_3")
                    .hash("testHash2_3")
                    .build()
            ))
            .build()
        );

        ModEntity entity3 = repository.insert(ModEntity.builder()
            .name("testName3")
            .modinfo("testModinfo3")
            .modfiles(Arrays.asList(
                ModfileEntity.builder()
                    .path("testPath3_1")
                    .hash("testHash3_1")
                    .build(),
                ModfileEntity.builder()
                    .path("testPath3_2")
                    .hash("testHash3_2")
                    .build(),
                ModfileEntity.builder()
                    .path("testPath3_3")
                    .hash("testHash3_3")
                    .build()
            ))
            .build()
        );

        // execute test ---
        List<ModEntity> result = repository.findById(
            entity1.getId(), entity2.getId(), entity3.getId(),
            23232L // random ID that won't be found
        );

        // verify assertions ---
        assertThat(result).isNotEmpty();

        assertThat(result).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(entity1, entity2, entity3);
    }

    @Test
    void find_all_success() {

        // prepare mock data ---
        ModEntity entity1 = repository.insert(ModEntity.builder()
            .name("testName1")
            .modinfo("testModinfo1")
            .modfiles(Arrays.asList(
                ModfileEntity.builder()
                    .path("testPath1_1")
                    .hash("testHash1_1")
                    .build(),
                ModfileEntity.builder()
                    .path("testPath1_2")
                    .hash("testHash1_2")
                    .build(),
                ModfileEntity.builder()
                    .path("testPath1_3")
                    .hash("testHash1_3")
                    .build()
            ))
            .build()
        );

        ModEntity entity2 = repository.insert(ModEntity.builder()
            .name("testName2")
            .modinfo("testModinfo2")
            .modfiles(Arrays.asList(
                ModfileEntity.builder()
                    .path("testPath2_1")
                    .hash("testHash2_1")
                    .build(),
                ModfileEntity.builder()
                    .path("testPath2_2")
                    .hash("testHash2_2")
                    .build(),
                ModfileEntity.builder()
                    .path("testPath2_3")
                    .hash("testHash2_3")
                    .build()
            ))
            .build()
        );

        ModEntity entity3 = repository.insert(ModEntity.builder()
            .name("testName3")
            .modinfo("testModinfo3")
            .modfiles(Arrays.asList(
                ModfileEntity.builder()
                    .path("testPath3_1")
                    .hash("testHash3_1")
                    .build(),
                ModfileEntity.builder()
                    .path("testPath3_2")
                    .hash("testHash3_2")
                    .build(),
                ModfileEntity.builder()
                    .path("testPath3_3")
                    .hash("testHash3_3")
                    .build()
            ))
            .build()
        );

        // execute test ---
        List<ModEntity> result = repository.findAll();

        // verify assertions ---
        assertThat(result).isNotEmpty();

        assertThat(result).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(entity1, entity2, entity3);
    }

    @Test
    void count_all_success() {

        // prepare mock data ---
        repository.insert(ModEntity.builder()
            .name("testName1")
            .modinfo("testModinfo1")
            .modfiles(Arrays.asList(
                ModfileEntity.builder()
                    .path("testPath1_1")
                    .hash("testHash1_1")
                    .build(),
                ModfileEntity.builder()
                    .path("testPath1_2")
                    .hash("testHash1_2")
                    .build(),
                ModfileEntity.builder()
                    .path("testPath1_3")
                    .hash("testHash1_3")
                    .build()
            ))
            .build()
        );

        repository.insert(ModEntity.builder()
            .name("testName2")
            .modinfo("testModinfo2")
            .modfiles(Arrays.asList(
                ModfileEntity.builder()
                    .path("testPath2_1")
                    .hash("testHash2_1")
                    .build(),
                ModfileEntity.builder()
                    .path("testPath2_2")
                    .hash("testHash2_2")
                    .build(),
                ModfileEntity.builder()
                    .path("testPath2_3")
                    .hash("testHash2_3")
                    .build()
            ))
            .build()
        );

        repository.insert(ModEntity.builder()
            .name("testName3")
            .modinfo("testModinfo3")
            .modfiles(Arrays.asList(
                ModfileEntity.builder()
                    .path("testPath3_1")
                    .hash("testHash3_1")
                    .build(),
                ModfileEntity.builder()
                    .path("testPath3_2")
                    .hash("testHash3_2")
                    .build(),
                ModfileEntity.builder()
                    .path("testPath3_3")
                    .hash("testHash3_3")
                    .build()
            ))
            .build()
        );

        // execute test ---
        long result = repository.countAll();

        // verify assertions ---
        assertThat(result).isEqualTo(3);
    }

    @Test
    void update_success() {

        // prepare mock data ---
        repository.insert(ModEntity.builder()
            .name("testName1")
            .modinfo("testModinfo1")
            .modfiles(Arrays.asList(
                ModfileEntity.builder()
                    .path("testPath1_1")
                    .hash("testHash1_1")
                    .build(),
                ModfileEntity.builder()
                    .path("testPath1_2")
                    .hash("testHash1_2")
                    .build(),
                ModfileEntity.builder()
                    .path("testPath1_3")
                    .hash("testHash1_3")
                    .build()
            ))
            .build()
        );

        ModEntity entity = repository.insert(ModEntity.builder()
            .name("testName2")
            .modinfo("testModinfo2")
            .modfiles(Arrays.asList(
                ModfileEntity.builder()
                    .path("testPath2_1")
                    .hash("testHash2_1")
                    .build(),
                ModfileEntity.builder()
                    .path("testPath2_2")
                    .hash("testHash2_2")
                    .build(),
                ModfileEntity.builder()
                    .path("testPath2_3")
                    .hash("testHash2_3")
                    .build()
            ))
            .build()
        );

        repository.insert(ModEntity.builder()
            .name("testName3")
            .modinfo("testModinfo3")
            .modfiles(Arrays.asList(
                ModfileEntity.builder()
                    .path("testPath3_1")
                    .hash("testHash3_1")
                    .build(),
                ModfileEntity.builder()
                    .path("testPath3_2")
                    .hash("testHash3_2")
                    .build(),
                ModfileEntity.builder()
                    .path("testPath3_3")
                    .hash("testHash3_3")
                    .build()
            ))
            .build()
        );

        ModEntity updatedEntity2 = ModEntity.builder()
            .id(entity.getId())
            .name("updatedName2")
            .modinfo("updatedModinfo2")
            .modfiles(Collections.singletonList(
                ModfileEntity.builder()
                    .path("updatedPath2")
                    .hash("updatedHash2")
                    .build()
            ))
            .build();

        // execute test ---
        ModEntity result = repository.update(updatedEntity2);

        // verify assertions ---
        assertThat(result).isNotNull();

        assertThat(result.getId()).isEqualTo(updatedEntity2.getId());
        assertThat(result.getName()).isEqualTo(updatedEntity2.getName());
        assertThat(result.getModinfo()).isEqualTo(updatedEntity2.getModinfo());
        assertThat(result.getCreatedDate()).isEqualTo(entity.getCreatedDate());
        assertThat(result.getUpdatedDate()).isAfter(entity.getUpdatedDate());

        assertThat(result.getModfiles()).hasSize(1);

        ModfileEntity childResult = result.getModfiles().get(0);
        assertThat(childResult.getModId()).isEqualTo(updatedEntity2.getId());
        assertThat(childResult.getPath()).isEqualTo(updatedEntity2.getModfiles().get(0).getPath());
        assertThat(childResult.getHash()).isEqualTo(updatedEntity2.getModfiles().get(0).getHash());
        assertThat(childResult.getCreatedDate()).isNotNull();
        assertThat(childResult.getUpdatedDate()).isNotNull();
        assertThat(childResult.getCreatedDate()).isEqualTo(childResult.getUpdatedDate());
    }

    @Test
    void delete_success() {

        // prepare mock data ---
        repository.insert(ModEntity.builder()
            .name("testName1")
            .modinfo("testModinfo1")
            .modfiles(Arrays.asList(
                ModfileEntity.builder()
                    .path("testPath1_1")
                    .hash("testHash1_1")
                    .build(),
                ModfileEntity.builder()
                    .path("testPath1_2")
                    .hash("testHash1_2")
                    .build(),
                ModfileEntity.builder()
                    .path("testPath1_3")
                    .hash("testHash1_3")
                    .build()
            ))
            .build()
        );

        ModEntity entity = repository.insert(ModEntity.builder()
            .name("testName2")
            .modinfo("testModinfo2")
            .modfiles(Arrays.asList(
                ModfileEntity.builder()
                    .path("testPath2_1")
                    .hash("testHash2_1")
                    .build(),
                ModfileEntity.builder()
                    .path("testPath2_2")
                    .hash("testHash2_2")
                    .build(),
                ModfileEntity.builder()
                    .path("testPath2_3")
                    .hash("testHash2_3")
                    .build()
            ))
            .build()
        );

        repository.insert(ModEntity.builder()
            .name("testName3")
            .modinfo("testModinfo3")
            .modfiles(Arrays.asList(
                ModfileEntity.builder()
                    .path("testPath3_1")
                    .hash("testHash3_1")
                    .build(),
                ModfileEntity.builder()
                    .path("testPath3_2")
                    .hash("testHash3_2")
                    .build(),
                ModfileEntity.builder()
                    .path("testPath3_3")
                    .hash("testHash3_3")
                    .build()
            ))
            .build()
        );

        // execute test ---
        repository.delete(entity);
        Optional<ModEntity> result = repository.findById(entity.getId());

        // verify assertions ---
        assertThat(result).isEmpty();
    }
}