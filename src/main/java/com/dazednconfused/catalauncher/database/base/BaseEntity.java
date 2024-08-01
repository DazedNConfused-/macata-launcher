package com.dazednconfused.catalauncher.database.base;

import java.sql.Timestamp;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class BaseEntity {

    private Long id;
    private Timestamp createdDate;
    private Timestamp updatedDate;
}
