package com.dazednconfused.catalauncher.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Interface for basic DAO operations.
 * */
public interface BaseDAO<T extends BaseEntity> {

    Logger LOGGER = LoggerFactory.getLogger(BaseDAO.class);

    /**
     * Initializes this DAO's table, if it didn't exist already.
     * */
    default void initializeTable() throws DAOException {
        LOGGER.info("Initializing table [{}]...", getTableName());

        Map.Entry<Connection, PreparedStatement> initializationTuple = this.getTableCreationStatement();

        try (Connection conn = initializationTuple.getKey(); PreparedStatement pstmt = initializationTuple.getValue()) {
            pstmt.execute();
            LOGGER.debug("[{}] table initialized successfully", getTableName());
        } catch (SQLException e) {
            LOGGER.error("An error occurred while creating table [{}]", getTableName(), e);
            throw new DAOException(e);
        }

        LOGGER.info("Table initialization completed");
    }

    /**
     * Inserts the given {@link BaseEntity} into the table.
     * */
    T insert(T t) throws DAOException;

    /**
     * Updates the given {@link BaseEntity}. It must have an ID set.
     * */
    T update(T t) throws DAOException;

    /**
     * Deletes the given {@link BaseEntity}. It must have an ID set.
     * */
    default void delete(T t) throws DAOException {
        Optional<T> originalEntity = this.findById(t.getId());

        if (originalEntity.isEmpty()) {
            throw new DAOException("No entity with id [" + t.getId() + "] found");
        }

        LOGGER.debug("Deleting Entity [{}]...", originalEntity.get());

        String sql = "DELETE FROM " + getTableName() + " WHERE id = ? ";

        try (Connection conn = this.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, t.getId());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("An error occurred while deleting entity [{}]", t, e);
            throw new DAOException(e);
        }
    };

    /**
     * Finds the given {@link BaseEntity} by ID.
     * */
    default Optional<T> findById(long id) throws DAOException {
        LOGGER.debug("Finding Entity with ID [{}]...", id);

        String sql = "SELECT * FROM " + getTableName() + " WHERE id = ?";
        try (Connection conn = this.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Optional.of(this.buildFromResultSet(rs));
            }
        } catch (SQLException e) {
            LOGGER.error("An error occurred while retrieving entity with ID [{}]", id, e);
            throw new DAOException(e);
        }

        return Optional.empty();
    }

    /**
     * Builds a {@link BaseEntity} {@code T} from the provided {@link ResultSet}.
     * */
    T buildFromResultSet(ResultSet rs) throws DAOException;

    /**
     * Returns the ID of the latest inserted {@link BaseEntity} from the supplied {@link PreparedStatement}.
     *
     * @apiNote {@link PreparedStatement} must have been built using {@link Statement#RETURN_GENERATED_KEYS} for this method
     *          to succeed.
     * */
    default Optional<Long> getLatestGeneratedId(PreparedStatement pstmt) throws DAOException {
        try {
            ResultSet rs = pstmt.getGeneratedKeys();

            if (rs.next()) {
                long insertedId = rs.getLong(1);
                return Optional.of(insertedId);
            }

            return Optional.empty();
        } catch (SQLException e) {
            throw new DAOException(e);
        }
    }

    /**
     * The DAO's table under management's name.
     * */
    String getTableName();

    /**
     * The DAO's table creation {@link PreparedStatement}, alongside its {@link Connection}, to be used during the initialization
     * process.
     * */
    Map.Entry<Connection, PreparedStatement> getTableCreationStatement() throws DAOException;

    /**
     * Opens a connection to this DAO's database.
     *
     * @apiNote Callers of this method are in charge of properly handling and disposing of this connection upon use.
     * */
    Connection getConnection();
}