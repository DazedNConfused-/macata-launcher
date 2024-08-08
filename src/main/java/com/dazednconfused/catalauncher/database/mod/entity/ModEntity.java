package com.dazednconfused.catalauncher.database.mod.entity;

import com.dazednconfused.catalauncher.database.base.BaseEntity;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class ModEntity extends BaseEntity {

    private String name;
    private String modinfo;

    private List<ModfileEntity> modfiles;

}
