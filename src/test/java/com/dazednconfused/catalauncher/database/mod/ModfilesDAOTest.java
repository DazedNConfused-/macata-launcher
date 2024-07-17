package com.dazednconfused.catalauncher.database.mod;

import com.dazednconfused.catalauncher.database.BaseDAO;
import com.dazednconfused.catalauncher.database.mod.entity.ModfileEntity;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;

public class ModfilesDAOTest {

    private static BaseDAO<ModfileEntity> dao;

    @BeforeAll
    public static void setup() {
        dao = new ModfilesH2DAOImpl();
    }

    @AfterEach
    public void teardown() {
        ((ModfilesH2DAOImpl) dao).wipe();
    }

    @AfterAll
    public static void cleanup() {
        ((ModfilesH2DAOImpl) dao).destroy();
    }
}