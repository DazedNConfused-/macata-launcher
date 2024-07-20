package com.dazednconfused.catalauncher.database.mod.repository;

import com.dazednconfused.catalauncher.database.DAOException;
import com.dazednconfused.catalauncher.database.H2Database;
import com.dazednconfused.catalauncher.database.mod.dao.ModDAO;
import com.dazednconfused.catalauncher.database.mod.dao.ModH2DAOImpl;
import com.dazednconfused.catalauncher.database.mod.dao.ModfileDAO;
import com.dazednconfused.catalauncher.database.mod.dao.ModfileH2DAOImpl;
import com.dazednconfused.catalauncher.database.mod.entity.ModEntity;
import com.dazednconfused.catalauncher.database.mod.entity.ModfileEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModH2RepositoryImpl extends H2Database implements ModRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModH2RepositoryImpl.class);

    private static final String DATABASE_FILE = "mods";

    private final ModDAO modDAO;
    private final ModfileDAO modfileDAO;

    public ModH2RepositoryImpl() {
        this.modDAO = new ModH2DAOImpl();
        this.modfileDAO = new ModfileH2DAOImpl();
    }

    @Override
    public String getDatabaseName() {
        return DATABASE_FILE;
    }

    @Override
    public void initializeTable() throws DAOException {
        this.modDAO.initializeTable();
        this.modfileDAO.initializeTable();
    }

    @Override
    public ModEntity insert(ModEntity entity) throws DAOException {
        LOGGER.debug("Inserting ModEntity: [{}]", entity);

        ModEntity result = this.modDAO.insert(entity);

        result.setModfiles(
            this.insertChildEntities(result.getId(), entity.getModfiles())
        );

        return result;
    }

    @Override
    public ModEntity update(ModEntity entity) throws DAOException {
        LOGGER.debug("Updating ModEntity: [{}]", entity);
        ModEntity result = this.modDAO.update(entity);

        this.deleteChildEntitiesFor(entity.getId());
        result.setModfiles(
            this.insertChildEntities(result.getId(), entity.getModfiles())
        );

        return result;
    }

    @Override
    public void delete(ModEntity entity) throws DAOException {
        this.deleteChildEntitiesFor(entity.getId());

        LOGGER.debug("Deleting ModEntity with ID [{}]...", entity.getId());
        this.modDAO.delete(entity);
    }

    /**
     * Inserts all given {@link ModfileEntity}(ies) associated to the provided {@code modId}.
     * */
    private List<ModfileEntity> insertChildEntities(long modId, List<ModfileEntity> entities) throws DAOException {
        LOGGER.debug("Inserting [{}] ModfileEntity(s) associated to modId [{}]", entities.size(), modId);

        List<ModfileEntity> result = entities.stream()
            .peek(e -> e.setModId(modId)) // set/overwrite with entity ID
            .map(modfileDAO::insert)
            .collect(Collectors.toList());

        LOGGER.debug("Inserted [{}] ModfileEntity(s) associated to modId [{}]", result.size(), modId);

        return result;
    }

    /**
     * Deletes all {@link ModfileEntity}(ies) associated to the given {@code modId}.
     * */
    private void deleteChildEntitiesFor(long modId) {
        LOGGER.debug("Deleting ModfileEntity(s) associated to modID [{}]...",modId);

        int deletedChildEntities = this.modfileDAO.deleteAllByModId(modId);

        LOGGER.debug("Deleted [{}] ModfileEntity(s) associated to modId [{}]", deletedChildEntities, modId);
    }

    @Override
    public Optional<ModEntity> findById(long id) throws DAOException {
        LOGGER.debug("Finding ModEntity with ID [{}]...", id);

        Optional<ModEntity> result = modDAO.findById(id);

        result.ifPresent(entity -> {
            LOGGER.debug("Finding child ModfileEntity(s) associated to modID [{}]...", entity.getId());
            entity.setModfiles(this.modfileDAO.findAllByModId(id));
        });

        return result;
    }

    @Override
    public List<ModEntity> findById(long... ids) throws DAOException {
        LOGGER.debug("Finding ModEntity(ies) with IDs [{}]...", ids);

        return Arrays.stream(ids).mapToObj(this::findById)
            .map(opt -> opt.orElse(null))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    @Override
    public List<ModEntity> findAll() throws DAOException {
        LOGGER.debug("Finding all ModEntity(ies) for [{}]...", getTableName());

        List<ModEntity> result = this.modDAO.findAll();
        result.forEach(entity -> entity.setModfiles(this.modfileDAO.findAllByModId(entity.getId())));

        return result;
    }
}