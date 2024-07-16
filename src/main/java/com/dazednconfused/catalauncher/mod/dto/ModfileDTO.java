package com.dazednconfused.catalauncher.mod.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ModfileDTO {

    private long id;
    private String path;
    private String hash;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

}
