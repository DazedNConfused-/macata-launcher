package com.dazednconfused.catalauncher.soundpack.dto;

import java.nio.file.Path;
import java.util.Optional;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SoundpackDTO {

    private String name;
    private Path path;

    public Optional<Path> getPath() {
        return Optional.ofNullable(path);
    }
}
