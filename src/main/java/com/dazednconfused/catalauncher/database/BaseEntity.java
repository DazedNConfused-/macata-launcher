package com.dazednconfused.catalauncher.database;

import java.sql.Timestamp;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class BaseEntity {
    
    private long id;
    private Timestamp createdDate;
    private Timestamp updatedDate;

}
