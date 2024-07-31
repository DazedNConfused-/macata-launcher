package com.dazednconfused.catalauncher.database.mod.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.dazednconfused.catalauncher.database.H2Database;
import com.dazednconfused.catalauncher.database.mod.entity.ModEntity;
import com.dazednconfused.catalauncher.database.mod.entity.ModfileEntity;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ModfileDAOTest {

    private static final UUID uuid = UUID.randomUUID();

    private static ModDAO modDAO = new ModH2DAOImpl();
    private static ModfileDAO dao;

    private long parentModId;

    @BeforeAll
    public static void setup() {
        modDAO = new ModH2DAOImpl() {
            @Override
            public String getDatabaseName() {
                return super.getDatabaseName() + "_" + uuid;
            }
        };
        dao = new ModfileH2DAOImpl() {
            @Override
            public String getDatabaseName() {
                return super.getDatabaseName() + "_" + uuid;
            }
        };
    }

    @BeforeEach
    public void before() {
        parentModId = modDAO.insert(ModEntity.builder()
            .name("parentTestName")
            .modinfo("parentModinfo")
            .build()
        ).getId();
    }

    @AfterEach
    public void teardown() {
        ((H2Database) dao).reset();
    }

    @AfterAll
    public static void cleanup() {
        ((H2Database) dao).destroy();
    }

    @Test
    void build_from_result_set_success() throws Exception {

        // prepare mock data ---
        Long MOCKED_ID = 1L;
        Long MOCKED_MOD_ID = 2L;
        String MOCKED_PATH = "mockedPath";
        String MOCKED_HASH = "mockedHash";
        Timestamp MOCKED_CREATED_DATE = new Timestamp(System.currentTimeMillis());
        Timestamp MOCKED_UPDATED_DATE = new Timestamp(System.currentTimeMillis());

        ResultSet MOCKED_RESULT_SET = mock(ResultSet.class);
        when(MOCKED_RESULT_SET.getLong("id")).thenReturn(MOCKED_ID);
        when(MOCKED_RESULT_SET.getLong("mod_id")).thenReturn(MOCKED_MOD_ID);
        when(MOCKED_RESULT_SET.getString("path")).thenReturn(MOCKED_PATH);
        when(MOCKED_RESULT_SET.getString("hash")).thenReturn(MOCKED_HASH);
        when(MOCKED_RESULT_SET.getTimestamp("created_date")).thenReturn(MOCKED_CREATED_DATE);
        when(MOCKED_RESULT_SET.getTimestamp("updated_date")).thenReturn(MOCKED_UPDATED_DATE);

        // execute test ---
        ModfileEntity result = dao.buildFromResultSet(MOCKED_RESULT_SET);

        // verify assertions ---
        ModfileEntity expected = ModfileEntity.builder()
            .id(MOCKED_ID)
            .modId(MOCKED_MOD_ID)
            .path(MOCKED_PATH)
            .hash(MOCKED_HASH)
            .createdDate(MOCKED_CREATED_DATE)
            .updatedDate(MOCKED_UPDATED_DATE)
            .build();

        assertThat(result).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    void insert_success() {

        // prepare mock data ---
        ModfileEntity entity = ModfileEntity.builder()
            .modId(parentModId)
            .path("testPath1")
            .hash("testHash1")
            .build();

        // execute test ---
        ModfileEntity result = dao.insert(entity);

        // verify assertions ---
        assertThat(result).isNotNull();

        assertThat(result.getId()).isNotZero();
        assertThat(result.getModId()).isEqualTo(parentModId);
        assertThat(result.getCreatedDate()).isNotNull();
        assertThat(result.getUpdatedDate()).isNotNull();

        assertThat(result.getCreatedDate()).isEqualTo(result.getUpdatedDate());
    }

    @Test
    void find_by_id_success() {

        // prepare mock data ---
        dao.insert(ModfileEntity.builder()
            .modId(parentModId)
            .path("testPath1")
            .hash("testHash1")
            .build()
        );

        ModfileEntity entity = dao.insert(ModfileEntity.builder()
            .modId(parentModId)
            .path("testPath2")
            .hash("testHash2")
            .build()
        );

        dao.insert(ModfileEntity.builder()
            .modId(parentModId)
            .path("testPath3")
            .hash("testHash3")
            .build()
        );

        // execute test ---
        Optional<ModfileEntity> resultOpt = dao.findById(entity.getId());

        // verify assertions ---
        assertThat(resultOpt).isPresent();

        ModfileEntity result = resultOpt.get();

        assertThat(result.getModId()).isEqualTo(parentModId);
        assertThat(result.getPath()).isEqualTo(entity.getPath());
        assertThat(result.getHash()).isEqualTo(entity.getHash());
    }

    @Test
    void find_by_ids_success() {

        // prepare mock data ---
        ModfileEntity entity1 = dao.insert(ModfileEntity.builder()
            .modId(parentModId)
            .path("testPath1")
            .hash("testHash1")
            .build()
        );

        ModfileEntity entity2 = dao.insert(ModfileEntity.builder()
            .modId(parentModId)
            .path("testPath2")
            .hash("testHash2")
            .build()
        );

        ModfileEntity entity3 = dao.insert(ModfileEntity.builder()
            .modId(parentModId)
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
            .modId(parentModId)
            .path("testPath1")
            .hash("testHash1")
            .build()
        );

        ModfileEntity entity2 = dao.insert(ModfileEntity.builder()
            .modId(parentModId)
            .path("testPath2")
            .hash("testHash2")
            .build()
        );

        ModfileEntity entity3 = dao.insert(ModfileEntity.builder()
            .modId(parentModId)
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
            .modId(parentModId)
            .path("testPath1")
            .hash("testHash1")
            .build()
        );

        dao.insert(ModfileEntity.builder()
            .modId(parentModId)
            .path("testPath2")
            .hash("testHash2")
            .build()
        );

        dao.insert(ModfileEntity.builder()
            .modId(parentModId)
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
            .modId(parentModId)
            .path("testPath1")
            .hash("testHash1")
            .build()
        );

        ModfileEntity entity = dao.insert(ModfileEntity.builder()
            .modId(parentModId)
            .path("testPath2")
            .hash("testHash2")
            .build()
        );

        dao.insert(ModfileEntity.builder()
            .modId(parentModId)
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

        assertThat(result.getModId()).isEqualTo(parentModId);
        assertThat(result.getPath()).isEqualTo(updatedEntity2.getPath());
        assertThat(result.getHash()).isEqualTo(updatedEntity2.getHash());
        assertThat(result.getCreatedDate()).isEqualTo(entity.getCreatedDate());
        assertThat(result.getUpdatedDate()).isAfter(entity.getUpdatedDate());
    }

    @Test
    void delete_success() {

        // prepare mock data ---
        dao.insert(ModfileEntity.builder()
            .modId(parentModId)
            .path("testPath1")
            .hash("testHash1")
            .build()
        );

        ModfileEntity entity = dao.insert(ModfileEntity.builder()
            .modId(parentModId)
            .path("testPath2")
            .hash("testHash2")
            .build()
        );

        dao.insert(ModfileEntity.builder()
            .modId(parentModId)
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

    @Test
    void find_all_by_mod_id_success() {

        // prepare mock data ---
        Long parentModId1 = modDAO.insert(ModEntity.builder()
            .name("parentTestName1")
            .modinfo("parentModinfo1")
            .build()
        ).getId();

        Long parentModId2 = modDAO.insert(ModEntity.builder()
            .name("parentTestName2")
            .modinfo("parentModinfo2")
            .build()
        ).getId();

        Long parentModId3 = modDAO.insert(ModEntity.builder()
            .name("parentTestName3")
            .modinfo("parentModinfo3")
            .build()
        ).getId();


        ModfileEntity entity1_1 = dao.insert(ModfileEntity.builder()
            .modId(parentModId1)
            .path("testPath1_1")
            .hash("testHash1_2")
            .build()
        );

        ModfileEntity entity1_2 = dao.insert(ModfileEntity.builder()
            .modId(parentModId1)
            .path("testPath1_2")
            .hash("testHash1_2")
            .build()
        );

        ModfileEntity entity1_3 = dao.insert(ModfileEntity.builder()
            .modId(parentModId1)
            .path("testPath1_3")
            .hash("testHash1_3")
            .build()
        );


        dao.insert(ModfileEntity.builder()
            .modId(parentModId2)
            .path("testPath2_1")
            .hash("testHash2_2")
            .build()
        );

        dao.insert(ModfileEntity.builder()
            .modId(parentModId2)
            .path("testPath2_2")
            .hash("testHash2_2")
            .build()
        );

        dao.insert(ModfileEntity.builder()
            .modId(parentModId2)
            .path("testPath2_3")
            .hash("testHash2_3")
            .build()
        );


        dao.insert(ModfileEntity.builder()
            .modId(parentModId3)
            .path("testPath3_1")
            .hash("testHash3_2")
            .build()
        );

        dao.insert(ModfileEntity.builder()
            .modId(parentModId3)
            .path("testPath3_2")
            .hash("testHash3_2")
            .build()
        );

        dao.insert(ModfileEntity.builder()
            .modId(parentModId3)
            .path("testPath3_3")
            .hash("testHash3_3")
            .build()
        );

        // execute test ---
        List<ModfileEntity> result = dao.findAllByModId(parentModId1);

        // verify assertions ---
        assertThat(result).containsExactlyInAnyOrder(entity1_1, entity1_2, entity1_3);
    }

    @Test
    void delete_all_by_mod_id_success() {

        // prepare mock data ---
        Long parentModId1 = modDAO.insert(ModEntity.builder()
            .name("parentTestName1")
            .modinfo("parentModinfo1")
            .build()
        ).getId();

        Long parentModId2 = modDAO.insert(ModEntity.builder()
            .name("parentTestName2")
            .modinfo("parentModinfo2")
            .build()
        ).getId();

        Long parentModId3 = modDAO.insert(ModEntity.builder()
            .name("parentTestName3")
            .modinfo("parentModinfo3")
            .build()
        ).getId();


        ModfileEntity entity1_1 = dao.insert(ModfileEntity.builder()
            .modId(parentModId1)
            .path("testPath1_1")
            .hash("testHash1_2")
            .build()
        );

        ModfileEntity entity1_2 = dao.insert(ModfileEntity.builder()
            .modId(parentModId1)
            .path("testPath1_2")
            .hash("testHash1_2")
            .build()
        );

        ModfileEntity entity1_3 = dao.insert(ModfileEntity.builder()
            .modId(parentModId1)
            .path("testPath1_3")
            .hash("testHash1_3")
            .build()
        );


        ModfileEntity entity2_1 = dao.insert(ModfileEntity.builder()
            .modId(parentModId2)
            .path("testPath2_1")
            .hash("testHash2_2")
            .build()
        );

        ModfileEntity entity2_2 = dao.insert(ModfileEntity.builder()
            .modId(parentModId2)
            .path("testPath2_2")
            .hash("testHash2_2")
            .build()
        );

        ModfileEntity entity2_3 = dao.insert(ModfileEntity.builder()
            .modId(parentModId2)
            .path("testPath2_3")
            .hash("testHash2_3")
            .build()
        );


        ModfileEntity entity3_1 = dao.insert(ModfileEntity.builder()
            .modId(parentModId3)
            .path("testPath3_1")
            .hash("testHash3_2")
            .build()
        );

        ModfileEntity entity3_2 = dao.insert(ModfileEntity.builder()
            .modId(parentModId3)
            .path("testPath3_2")
            .hash("testHash3_2")
            .build()
        );

        ModfileEntity entity3_3 = dao.insert(ModfileEntity.builder()
            .modId(parentModId3)
            .path("testPath3_3")
            .hash("testHash3_3")
            .build()
        );

        // pre-test assertions ---
        assertThat(dao.findAll()).containsExactlyInAnyOrder(
            entity1_1, entity1_2, entity1_3,
            entity2_1, entity2_2, entity2_3,
            entity3_1, entity3_2, entity3_3
        );

        // execute test ---
        int result = dao.deleteAllByModId(parentModId1);

        // verify assertions ---
        assertThat(result).isEqualTo(3);

        assertThat(dao.findAll()).containsExactlyInAnyOrder(
            entity2_1, entity2_2, entity2_3,
            entity3_1, entity3_2, entity3_3
        );
    }
}