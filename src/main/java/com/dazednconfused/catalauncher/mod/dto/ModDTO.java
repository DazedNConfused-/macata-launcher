package com.dazednconfused.catalauncher.mod.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ModDTO {

    private Long id;
    private String name;
    private String modinfo;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    private List<ModfileDTO> modfiles;

}
