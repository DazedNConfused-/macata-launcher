package com.dazednconfused.catalauncher.database.mod.repository;

import com.dazednconfused.catalauncher.database.DAOException;
import com.dazednconfused.catalauncher.database.H2Database;
import com.dazednconfused.catalauncher.database.mod.dao.ModDAO;
import com.dazednconfused.catalauncher.database.mod.dao.ModH2DAOImpl;
import com.dazednconfused.catalauncher.database.mod.dao.ModfileDAO;
import com.dazednconfused.catalauncher.database.mod.dao.ModfileH2DAOImpl;
import com.dazednconfused.catalauncher.database.mod.entity.ModEntity;
import com.dazednconfused.catalauncher.database.mod.entity.ModfileEntity;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModH2RepositoryImpl extends H2Database implements ModRepository {

    public static final String MODS_TABLE_NAME = "mod";

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

        LOGGER.debug("Inserting ModfileEntity(s): [{}]", entity.getModfiles());
        List<ModfileEntity> modfiles = entity.getModfiles().stream().map(modfileDAO::insert).collect(Collectors.toList());
        result.setModfiles(modfiles);

        return result;
    }

    @Override
    public ModEntity update(ModEntity entity) throws DAOException {
        LOGGER.debug("Updating ModEntity: [{}]", entity);
        ModEntity result = this.modDAO.update(entity);

        LOGGER.debug("Deleting ModfileEntity(s) associated to modID [{}]...", entity.getId());
        int deletedChildEntities = this.modfileDAO.deleteAllByModId(entity.getId());
        LOGGER.debug("Deleted [{}] ModfileEntity(s) associated to modId [{}]", deletedChildEntities, entity.getId());

        LOGGER.debug("Reinserting ModfileEntity(s) associated to modID [{}]...", entity.getId());
        List<ModfileEntity> insertedChildEntities = entity.getModfiles().stream().map(modfileDAO::insert).collect(Collectors.toList());
        LOGGER.debug("Inserted [{}] ModfileEntity(s) associated to modId [{}]", insertedChildEntities.size(), entity.getId());

        result.setModfiles(insertedChildEntities);

        return result;
    }

    @Override
    public void delete(ModEntity entity) throws DAOException {
        ModRepository.super.delete(entity);
    }

    @Override
    public Optional<ModEntity> findById(long id) throws DAOException {
        return ModRepository.super.findById(id);
    }

    @Override
    public List<ModEntity> findById(long... ids) throws DAOException {
        return ModRepository.super.findById(ids);
    }

    @Override
    public List<ModEntity> findAll() throws DAOException {
        return ModRepository.super.findAll();
    }
}