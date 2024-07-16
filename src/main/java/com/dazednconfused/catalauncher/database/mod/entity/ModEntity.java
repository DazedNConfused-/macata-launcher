package com.dazednconfused.catalauncher.database.mod.entity;

import java.sql.Timestamp;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ModEntity {

    private long id;
    private String name;
    private Timestamp createdDate;
    private Timestamp updatedDate;

    private List<ModfileEntity> modfiles;

}
