package com.dazednconfused.catalauncher.database.mod.dao;

import com.dazednconfused.catalauncher.database.base.BaseDAO;
import com.dazednconfused.catalauncher.database.mod.entity.ModEntity;

public interface ModDAO extends BaseDAO<ModEntity> {

    String TABLE_NAME = "mod";
    String DATABASE_FILE = "mods";

    @Override
    default String getTableName() {
        return TABLE_NAME;
    }
}
