package com.dazednconfused.catalauncher.database.migration;

import static org.assertj.core.api.Assertions.assertThat;

import com.dazednconfused.catalauncher.helper.result.Result;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sun.misc.Unsafe;

class MigrateableH2DatabaseTest {

    private static final String DB_BASE_MIGRATION_FILENAME_YEAR = "2024";
    private static final String DB_BASE_MIGRATION_FILENAME_MONTH = "07";
    private static final String DB_BASE_MIGRATION_FILENAME_DAY = "30";

    private static final String DB_BASE_MIGRATION_FILENAME = String.format(
        "%s%s%s_base.sql",
        DB_BASE_MIGRATION_FILENAME_YEAR,
        DB_BASE_MIGRATION_FILENAME_MONTH,
        DB_BASE_MIGRATION_FILENAME_DAY
    );

    private TestDatabase db;

    @BeforeAll
    public static void setup() {
    }

    @BeforeEach
    public void before() {
    }

    @AfterEach
    public void teardown() {
        if (db != null) {
            db.destroy();
        }
        db = null; // guarantee fresh TestDatabase is created on each individual test
    }

    @AfterAll
    public static void cleanup() {
    }

    @Test
    void get_database_migration_files_success() {

        // prepare mock data ---
        db = new TestDatabase();

        // execute test ---
        Result<Throwable, List<String>> result = db.getDatabaseMigrationFiles();

        // verify assertions ---
        assertThat(result).isNotNull(); // assert non-null result

        assertThat(result.toEither().isRight()).isTrue(); // assert that Result is Success

        List<String> migrations = result.toEither().get().getResult().orElseThrow();

        assertThat(migrations).isNotEmpty(); // assert that Result's Success is not empty

        assertThat(migrations).containsOnlyOnce(DB_BASE_MIGRATION_FILENAME); // assert that Result's Success contains, at the very least, the base migration
    }

    @Test
    void get_database_migration_files_dated_after_success() throws ParseException {

        // prepare mock data ---
        db = new TestDatabase() {
            @Override
            public String getDatabaseMigrationsResourcePath() {
                return  MigrateableH2Database.DATABASE_MIGRATIONS_DEFAULT_RESOURCE_ROOT_PATH;
            }
        };

        Date MOCKED_DATE = new SimpleDateFormat("yyyyMMdd").parse(
            DB_BASE_MIGRATION_FILENAME_YEAR + DB_BASE_MIGRATION_FILENAME_MONTH + DB_BASE_MIGRATION_FILENAME_DAY
        );

        // execute test ---
        List<String> result = db.getDatabaseMigrationFilesDatedAfter(MOCKED_DATE);

        // verify assertions ---
        assertThat(result).isNotNull();

        assertThat(result).hasSize(5);

        String VALID_MIGRATION_FILE_1 = "20990101_valid_migration_1.sql";
        String VALID_MIGRATION_FILE_2 = "20990101_valid_migration_2.sql";
        String VALID_MIGRATION_FILE_3 = "20990102_valid_migration_3.sql";
        String VALID_MIGRATION_FILE_4 = "20990201_valid_migration_4.sql";
        String VALID_MIGRATION_FILE_5 = "20990202_valid_migration_5.sql";

        assertThat(result).containsAll(Stream.of(
                VALID_MIGRATION_FILE_1,
                VALID_MIGRATION_FILE_2,
                VALID_MIGRATION_FILE_3,
                VALID_MIGRATION_FILE_4,
                VALID_MIGRATION_FILE_5)
            .collect(Collectors.toList())
        );
    }

    @Test
    void execute_sql_resource_success() {

        // prepare mock data ---
        db = TestDatabase.getInstanceBypassingConstructor();
        assertThat(db).isNotNull();

        // pre-test assertions ---
        assertThat(db.doesTableExist("sample")).isFalse();

        // execute test ---
        Result<Throwable, Boolean> result = db.executeSqlResource("db/scripts/sample.sql");

        // verify assertions ---
        assertThat(result).isNotNull(); // assert non-null result

        assertThat(result.toEither().isRight()).isTrue(); // assert that Result is Success

        assertThat(result.toEither().get().getResult().isEmpty()).isFalse(); // assert that Result's Success is not empty
        assertThat(result.toEither().get().getResult().get()).isFalse();

        assertThat(db.doesTableExist("sample")).isTrue();
    }

    @Test
    void apply_base_and_migrations_success() throws NoSuchFieldException, IllegalAccessException, InstantiationException {

        // prepare mock data ---
        db = TestDatabase.getInstanceBypassingConstructor();
        assertThat(db).isNotNull();
        db.initMigrationTableManually();

        // pre-test assertions ---
        assertThat(db.doesTableExist("sample")).isFalse();
        assertThat(db.doesTableExist("sample2")).isFalse();
        assertThat(db.doesTableExist("sample3")).isFalse();

        // execute test ---
        db.applyAllPendingMigrations();

        // verify assertions ---
        assertThat(db.doesTableExist("sample")).isTrue();
        assertThat(db.doesTableExist("sample2")).isTrue();
        assertThat(db.doesTableExist("sample3")).isTrue();

        String latestAppliedMigration = db.getLatestAppliedMigration().orElse(null);
        assertThat(latestAppliedMigration).isNotNull();
        assertThat(latestAppliedMigration).isEqualTo("20240801_second_migration.sql");
    }

    @Test
    void apply_only_migrations_success() {

        // prepare mock data ---
        db = TestDatabase.getInstanceBypassingConstructor();
        assertThat(db).isNotNull();
        db.initMigrationTableManually();

        db.applyMigration("20240730_base.sql");

        // pre-test assertions ---
        assertThat(db.doesTableExist("sample")).isTrue();
        assertThat(db.doesTableExist("sample2")).isFalse();
        assertThat(db.doesTableExist("sample3")).isFalse();

        // prepare mock data ---
        TestDatabase db = new TestDatabase();

        // execute test ---
        db.applyAllPendingMigrations();

        // verify assertions ---
        assertThat(db.doesTableExist("sample2")).isTrue();
        assertThat(db.doesTableExist("sample3")).isTrue();

        String latestAppliedMigration = db.getLatestAppliedMigration().orElse(null);
        assertThat(latestAppliedMigration).isNotNull();
        assertThat(latestAppliedMigration).isEqualTo("20240801_second_migration.sql");
    }

    @Test
    void apply_partial_migrations_success() {

        // prepare mock data ---
        db = TestDatabase.getInstanceBypassingConstructor();
        assertThat(db).isNotNull();
        db.initMigrationTableManually();

        db.applyMigration("20240730_base.sql");
        db.applyMigration("20240731_first_migration.sql");

        // pre-test assertions ---
        assertThat(db.doesTableExist("sample")).isTrue();
        assertThat(db.doesTableExist("sample2")).isTrue();
        assertThat(db.doesTableExist("sample3")).isFalse();

        // execute test ---
        db.applyAllPendingMigrations();

        // verify assertions ---
        assertThat(db.doesTableExist("sample3")).isTrue();

        String latestAppliedMigration = db.getLatestAppliedMigration().orElse(null);
        assertThat(latestAppliedMigration).isNotNull();
        assertThat(latestAppliedMigration).isEqualTo("20240801_second_migration.sql");
    }

    @Test
    void apply_no_migrations_success() {

        // prepare mock data ---
        db = TestDatabase.getInstanceBypassingConstructor();
        assertThat(db).isNotNull();
        db.initMigrationTableManually();

        db.applyMigration("20240730_base.sql");
        db.applyMigration("20240731_first_migration.sql");
        db.applyMigration("20240801_second_migration.sql");

        // pre-test assertions ---
        assertThat(db.doesTableExist("sample")).isTrue();
        assertThat(db.doesTableExist("sample2")).isTrue();
        assertThat(db.doesTableExist("sample3")).isTrue();

        // execute test ---
        db.applyAllPendingMigrations();

        // verify assertions ---
        String latestAppliedMigration = db.getLatestAppliedMigration().orElse(null);
        assertThat(latestAppliedMigration).isNotNull();
        assertThat(latestAppliedMigration).isEqualTo("20240801_second_migration.sql");
    }

    /**
     * Mock database used to test basic functionality of the {@link MigrateableH2Database} interface.
     * */
    private static class TestDatabase extends MigrateableH2Database {

        public static String MOCK_DATABASE_NAME = "migrateableH2TestDatabase";

        public TestDatabase() {
            super();
        }

        @Override
        public String getDatabaseName() {
            return MOCK_DATABASE_NAME;
        }

        @Override
        public String getDatabaseMigrationsResourcePath() {
            return MigrateableH2Database.DATABASE_MIGRATIONS_DEFAULT_RESOURCE_ROOT_PATH + "test/";
        }

        /**
         * Manually initiates the migration engine (normally accessible only by the Supeclass' constructor)
         * */
        public void initMigrationTableManually() {
            try {
                // Step 1: Get the Class object of the superclass
                Class<?> superClass = this.getClass().getSuperclass();

                // Step 2: Retrieve the Method object representing the private method
                Method method = superClass.getDeclaredMethod("initMigrationTable");

                // Step 3: Set the method accessible
                method.setAccessible(true);

                // Step 4: Invoke the method on an instance of the subclass
                method.invoke(this);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

        /**
         * Gets a testing instance of this DB, bypassing the constructor.
         * */
        public static TestDatabase getInstanceBypassingConstructor() {
            try {
                Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
                unsafeField.setAccessible(true);
                Unsafe unsafe = (Unsafe) unsafeField.get(null);

                // create an instance of the class without calling the constructor
                return (TestDatabase) unsafe.allocateInstance(TestDatabase.class);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                return null;
            }
        }
    }
}