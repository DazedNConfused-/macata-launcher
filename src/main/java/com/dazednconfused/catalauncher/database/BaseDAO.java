package com.dazednconfused.catalauncher.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

/**
 * Interface for basic DAO operations.
 * */
public interface BaseDAO<T> {

    /**
     * Initializes this DAO's table, if it didn't exist already.
     * */
    void initializeTable() throws DAOException;

    /**
     * Inserts the given entity into the table.
     * */
    T insert(T t) throws DAOException;

    /**
     * Updates the given entity. It must have an ID set.
     * */
    T update(T t) throws DAOException;

    /**
     * Deletes the given entity. It must have an ID set.
     * */
    default void delete(T t) throws DAOException {};

    /**
     * Finds the given entity by ID.
     * */
    Optional<T> findById(long id) throws DAOException;

    /**
     * Builds an entity {@code T} from the provided {@link ResultSet}.
     * */
    T buildFromResultSet(ResultSet rs) throws DAOException;

    /**
     * Returns the ID of the latest inserted entity from the supplied {@link PreparedStatement}.
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
}