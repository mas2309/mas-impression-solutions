package com.mas.impression.infrastructure.persistence;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Small shared helper for the flat-file stores under {@code infrastructure.persistence}.
 */
final class DataFiles {

    private DataFiles() {
    }

    static void ensureParentDirectoryExists(Path file) throws IOException {
        Path parent = file.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
    }
}
