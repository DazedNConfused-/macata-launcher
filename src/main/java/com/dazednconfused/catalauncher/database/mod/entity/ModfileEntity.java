package com.dazednconfused.catalauncher.database.mod.entity;

import com.dazednconfused.catalauncher.database.base.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class ModfileEntity extends BaseEntity {

    private Long modId;
    private String path;
    private String hash;

}
