package com.dazednconfused.catalauncher.database.mod.entity;

import java.sql.Timestamp;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ModfileEntity {

    private long id;
    private String path;
    private String hash;
    private Timestamp createdDate;
    private Timestamp updatedDate;

}
