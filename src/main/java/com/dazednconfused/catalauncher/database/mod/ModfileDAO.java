package com.dazednconfused.catalauncher.database.mod;

import com.dazednconfused.catalauncher.database.BaseDAO;
import com.dazednconfused.catalauncher.database.mod.entity.ModfileEntity;

public interface ModfileDAO extends BaseDAO<ModfileEntity> {

    String TABLE_NAME = "modfile";
    String DATABASE_FILE = "mods";

    @Override
    default String getTableName() {
        return TABLE_NAME;
    }
}
