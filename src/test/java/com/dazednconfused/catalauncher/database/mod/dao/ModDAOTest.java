package com.dazednconfused.catalauncher.database.mod.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.dazednconfused.catalauncher.database.base.DisposableDatabase;
import com.dazednconfused.catalauncher.database.mod.entity.ModEntity;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ModDAOTest {

    private static final UUID uuid = UUID.randomUUID();
    private static ModDAO dao;

    @BeforeAll
    public static void setup() {
        dao = new ModH2DAOImpl(true){
            @Override
            public String getDatabaseName() {
                return super.getDatabaseName() + "_" + uuid;
            }
        };
    }

    @AfterEach
    public void teardown() {
        ((DisposableDatabase) dao).reset();
    }

    @AfterAll
    public static void cleanup() {
        ((DisposableDatabase) dao).destroy();
    }

    @Test
    void build_from_result_set_success() throws Exception {

        // prepare mock data ---
        Long MOCKED_ID = 1L;
        String MOCKED_NAME = "mockedName";
        String MOCKED_MOD_INFO = "mockedModInfo";
        Timestamp MOCKED_CREATED_DATE = new Timestamp(System.currentTimeMillis());
        Timestamp MOCKED_UPDATED_DATE = new Timestamp(System.currentTimeMillis());

        ResultSet MOCKED_RESULT_SET = mock(ResultSet.class);
        when(MOCKED_RESULT_SET.getLong("id")).thenReturn(MOCKED_ID);
        when(MOCKED_RESULT_SET.getString("name")).thenReturn(MOCKED_NAME);
        when(MOCKED_RESULT_SET.getString("modinfo")).thenReturn(MOCKED_MOD_INFO);
        when(MOCKED_RESULT_SET.getTimestamp("created_date")).thenReturn(MOCKED_CREATED_DATE);
        when(MOCKED_RESULT_SET.getTimestamp("updated_date")).thenReturn(MOCKED_UPDATED_DATE);

        // execute test ---
        ModEntity result = dao.buildFromResultSet(MOCKED_RESULT_SET);

        // verify assertions ---
        ModEntity expected = ModEntity.builder()
            .id(MOCKED_ID)
            .name(MOCKED_NAME)
            .modinfo(MOCKED_MOD_INFO)
            .createdDate(MOCKED_CREATED_DATE)
            .updatedDate(MOCKED_UPDATED_DATE)
            .build();

        assertThat(result).usingRecursiveComparison().isEqualTo(expected);
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