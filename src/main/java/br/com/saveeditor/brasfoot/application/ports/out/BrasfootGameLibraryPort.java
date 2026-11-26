package br.com.saveeditor.brasfoot.application.ports.out;

import java.nio.file.Path;

public interface BrasfootGameLibraryPort {
    Path load(Path sourcePath);

    ClassLoader getClassLoader();

    /** True only when the loaded game exposes the Real Brasfoot feature marker. */
    default boolean supportsRealBrasfootTactics() { return false; }
}
