package br.com.saveeditor.brasfoot.application.ports.out;

import java.nio.file.Path;

public interface BrasfootGameLibraryPort {
    Path load(Path sourcePath);

    ClassLoader getClassLoader();
}
