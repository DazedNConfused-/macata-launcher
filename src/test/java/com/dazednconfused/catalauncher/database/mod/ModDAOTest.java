package com.dazednconfused.catalauncher.database.mod;

import com.dazednconfused.catalauncher.database.BaseDAO;
import com.dazednconfused.catalauncher.database.H2Database;
import com.dazednconfused.catalauncher.database.mod.entity.ModEntity;

import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class ModDAOTest {

    private static BaseDAO<ModEntity> dao;

    @BeforeAll
    public static void setup() {
        dao = new ModH2DAOImpl();
    }

    @BeforeEach
    public void before() {
        dao.initializeTable();
    }

    @AfterEach
    public void teardown() {
        ((ModH2DAOImpl) dao).wipe();
    }

    @AfterAll
    public static void cleanup() {
        ((ModH2DAOImpl) dao).destroy();
    }

    @Test
    void insert_success() {

        // prepare mock data ---
        ModEntity entity = ModEntity.builder()
            .name("testName1")
            .modinfo("testModinfo1")
            .build();

        // execute test ---
        ModEntity result = dao.insert(entity);

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
        dao.insert(ModEntity.builder()
            .name("testName1")
            .modinfo("testModinfo1")
            .build()
        );

        ModEntity entity = dao.insert(ModEntity.builder()
            .name("testName2")
            .modinfo("testModinfo2")
            .build()
        );

        dao.insert(ModEntity.builder()
            .name("testName3")
            .modinfo("testModinfo3")
            .build()
        );

        // execute test ---
        Optional<ModEntity> resultOpt = dao.findById(entity.getId());

        // verify assertions ---
        assertThat(resultOpt).isPresent();

        ModEntity result = resultOpt.get();

        assertThat(result.getName()).isEqualTo(entity.getName());
        assertThat(result.getModinfo()).isEqualTo(entity.getModinfo());
    }

    @Test
    void find_by_ids_success() {

        // prepare mock data ---
        ModEntity entity1 = dao.insert(ModEntity.builder()
            .name("testName1")
            .modinfo("testModinfo1")
            .build()
        );

        ModEntity entity2 = dao.insert(ModEntity.builder()
            .name("testName2")
            .modinfo("testModinfo2")
            .build()
        );

        ModEntity entity3 = dao.insert(ModEntity.builder()
            .name("testName3")
            .modinfo("testModinfo3")
            .build()
        );

        // execute test ---
        List<ModEntity> result = dao.findById(
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
        ModEntity entity1 = dao.insert(ModEntity.builder()
            .name("testName1")
            .modinfo("testModinfo1")
            .build()
        );

        ModEntity entity2 = dao.insert(ModEntity.builder()
            .name("testName2")
            .modinfo("testModinfo2")
            .build()
        );

        ModEntity entity3 = dao.insert(ModEntity.builder()
            .name("testName3")
            .modinfo("testModinfo3")
            .build()
        );

        // execute test ---
        List<ModEntity> result = dao.findAll();

        // verify assertions ---
        assertThat(result).isNotEmpty();

        assertThat(result).containsExactlyInAnyOrder(entity1, entity2, entity3);
    }

    @Test
    void count_all_success() {

        // prepare mock data ---
        dao.insert(ModEntity.builder()
            .name("testName1")
            .modinfo("testModinfo1")
            .build()
        );

        dao.insert(ModEntity.builder()
            .name("testName2")
            .modinfo("testModinfo2")
            .build()
        );

        dao.insert(ModEntity.builder()
            .name("testName3")
            .modinfo("testModinfo3")
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
        dao.insert(ModEntity.builder()
            .name("testName1")
            .modinfo("testModinfo1")
            .build()
        );

        ModEntity entity = dao.insert(ModEntity.builder()
            .name("testName2")
            .modinfo("testModinfo2")
            .build()
        );

        dao.insert(ModEntity.builder()
            .name("testName3")
            .modinfo("testModinfo3")
            .build()
        );

        ModEntity updatedEntity2 = ModEntity.builder()
            .id(entity.getId())
            .name("updatedName2")
            .modinfo("updatedModinfo2")
            .build();

        // execute test ---
        ModEntity result = dao.update(updatedEntity2);

        // verify assertions ---
        assertThat(result).isNotNull();

        assertThat(result.getName()).isEqualTo(updatedEntity2.getName());
        assertThat(result.getModinfo()).isEqualTo(updatedEntity2.getModinfo());
        assertThat(result.getCreatedDate()).isEqualTo(entity.getCreatedDate());
        assertThat(result.getUpdatedDate()).isAfter(entity.getUpdatedDate());
    }

    @Test
    void delete_success() {

        // prepare mock data ---
        dao.insert(ModEntity.builder()
            .name("testName1")
            .modinfo("testModinfo1")
            .build()
        );

        ModEntity entity = dao.insert(ModEntity.builder()
            .name("testName2")
            .modinfo("testModinfo2")
            .build()
        );

        dao.insert(ModEntity.builder()
            .name("testName3")
            .modinfo("testModinfo3")
            .build()
        );

        // execute test ---
        dao.delete(entity);
        Optional<ModEntity> result = dao.findById(entity.getId());
        
        // verify assertions ---
        assertThat(result).isEmpty();
    }
}