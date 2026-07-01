package br.com.saveeditor.brasfoot.service;

import br.com.saveeditor.brasfoot.application.ports.out.BrasfootGameLibraryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;
import java.util.jar.JarFile;

@Service
public class BrasfootGameLibraryService implements BrasfootGameLibraryPort {

    private static final Logger log = LoggerFactory.getLogger(BrasfootGameLibraryService.class);
    private static final String DEFAULT_VALIDATION_CLASS = "best.f";

    private final ClassLoader applicationClassLoader;
    private volatile ClassLoader classLoader;
    private URLClassLoader installedClassLoader;

    public BrasfootGameLibraryService() {
        this.applicationClassLoader = Thread.currentThread().getContextClassLoader();
        this.classLoader = applicationClassLoader;
    }

    @Override
    public synchronized Path load(Path sourcePath) {
        Path source = sourcePath.toAbsolutePath().normalize();
        validateSource(source);
        configureLibrary(source);
        return source;
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    private void validateSource(Path source) {
        if (!Files.isRegularFile(source)) {
            throw new IllegalArgumentException("Arquivo do Brasfoot nao encontrado: " + source);
        }

        String filename = source.getFileName().toString().toLowerCase(Locale.ROOT);
        if (!filename.endsWith(".exe") && !filename.endsWith(".jar")) {
            throw new IllegalArgumentException("Selecione um arquivo .exe ou .jar do Brasfoot.");
        }
    }

    private void configureLibrary(Path library) {
        URLClassLoader loader = createClassLoader(library);
        try {
            validateLibrary(library, loader);
            closeInstalledClassLoader();
            installedClassLoader = loader;
            classLoader = loader;
            Thread.currentThread().setContextClassLoader(loader);
            log.info("Brasfoot game library loaded from {}", library);
        } catch (RuntimeException e) {
            closeQuietly(loader);
            throw e;
        }
    }

    private void validateLibrary(Path library) {
        URLClassLoader loader = createClassLoader(library);
        try {
            validateLibrary(library, loader);
        } finally {
            closeQuietly(loader);
        }
    }

    private void validateLibrary(Path library, ClassLoader loader) {
        String className = findValidationClass(library)
                .orElseThrow(() -> new IllegalArgumentException("Arquivo selecionado nao contem classes do Brasfoot no pacote best."));
        try {
            Class.forName(className, false, loader);
        } catch (ClassNotFoundException | LinkageError e) {
            throw new IllegalArgumentException("Nao foi possivel carregar classes do Brasfoot a partir de " + library, e);
        }
    }

    private Optional<String> findValidationClass(Path library) {
        try (JarFile jar = new JarFile(library.toFile())) {
            if (jar.getEntry("best/f.class") != null) {
                return Optional.of(DEFAULT_VALIDATION_CLASS);
            }

            return jar.stream()
                    .map(entry -> entry.getName())
                    .filter(name -> name.startsWith("best/") && name.endsWith(".class"))
                    .findFirst()
                    .map(name -> name.substring(0, name.length() - ".class".length()).replace('/', '.'));
        } catch (IOException e) {
            throw new IllegalArgumentException("Arquivo selecionado nao parece ser um JAR/EXE Java valido: " + library, e);
        }
    }

    private URLClassLoader createClassLoader(Path library) {
        try {
            URL url = library.toUri().toURL();
            return new URLClassLoader(new URL[] { url }, applicationClassLoader);
        } catch (IOException e) {
            throw new IllegalArgumentException("Caminho invalido para biblioteca do Brasfoot: " + library, e);
        }
    }

    private void closeInstalledClassLoader() {
        if (installedClassLoader == null) {
            return;
        }

        closeQuietly(installedClassLoader);
        installedClassLoader = null;
        classLoader = applicationClassLoader;
        Thread.currentThread().setContextClassLoader(applicationClassLoader);
    }

    private void closeQuietly(URLClassLoader loader) {
        try {
            loader.close();
        } catch (IOException e) {
            log.debug("Could not close Brasfoot game classloader", e);
        }
    }
}
