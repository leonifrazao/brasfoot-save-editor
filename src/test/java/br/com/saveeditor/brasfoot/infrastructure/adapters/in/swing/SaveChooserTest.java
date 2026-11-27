package br.com.saveeditor.brasfoot.infrastructure.adapters.in.swing;

import br.com.saveeditor.brasfoot.application.ports.out.BrasfootGameLibraryPort;
import br.com.saveeditor.brasfoot.presentation.presenter.BrasfootPresenter;
import br.com.saveeditor.brasfoot.presentation.view.BrasfootDesktopView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SaveChooserTest {

    @TempDir
    private Path tempDirectory;
    @Mock
    private BrasfootGameLibraryPort gameLibraryPort;
    @Mock
    private BrasfootDesktopView view;
    @InjectMocks
    private BrasfootPresenter presenter;

    @BeforeEach
    void attachView() {
        presenter.attach(view);
    }

    @Test
    void opensSavAndFiltersSaveFiles() throws Exception {
        Path saveDirectory = Files.createDirectory(tempDirectory.resolve("sav"));
        Path save = Files.createFile(saveDirectory.resolve("meu-save.S22"));
        Path metadata = Files.createFile(saveDirectory.resolve("meu-save.info"));
        selectLibrary(tempDirectory.resolve("Brasfoot.exe"));

        JFileChooser chooser = createChooser();
        FileFilter filter = chooser.getFileFilter();
        assertEquals(saveDirectory.toFile(), chooser.getCurrentDirectory());
        assertFalse(chooser.isAcceptAllFileFilterUsed());
        assertTrue(filter.accept(saveDirectory.toFile()));
        assertTrue(filter.accept(save.toFile()));
        assertFalse(filter.accept(metadata.toFile()));
    }

    @Test
    void updatesDirectoryWhenAnotherLibraryLoads() throws Exception {
        Path firstDirectory = Files.createDirectory(tempDirectory.resolve("Primeira instalação"));
        Path secondDirectory = Files.createDirectory(tempDirectory.resolve("Segunda instalação"));
        Path firstSaveDirectory = Files.createDirectory(firstDirectory.resolve("sav"));

        selectLibrary(firstDirectory.resolve("Brasfoot.exe"));
        assertEquals(firstSaveDirectory.toFile(), createChooser().getCurrentDirectory());

        selectLibrary(secondDirectory.resolve("Brasfoot.jar"));
        assertEquals(secondDirectory.toFile(), createChooser().getCurrentDirectory());

        Path secondSaveDirectory = Files.createDirectory(secondDirectory.resolve("sav"));
        assertEquals(secondSaveDirectory.toFile(), createChooser().getCurrentDirectory());
        Files.delete(secondSaveDirectory);
        Files.createFile(secondSaveDirectory);
        assertEquals(secondDirectory.toFile(), createChooser().getCurrentDirectory());
    }

    @Test
    void keepsLastLoadedInstallationWhenLibrarySelectionFails() {
        Path invalidLibrary = tempDirectory.resolve("outra-instalação").resolve("Brasfoot.jar");
        selectLibrary(tempDirectory.resolve("Brasfoot.exe"));
        when(gameLibraryPort.load(invalidLibrary)).thenThrow(new IllegalArgumentException("Arquivo inválido"));

        presenter.selectBrasfootLibrary(invalidLibrary);

        assertEquals(tempDirectory.toFile(), createChooser().getCurrentDirectory());
    }

    @Test
    void usesDirectoryOfTheLoadedLibraryForARelativeSelection() throws Exception {
        Path saveDirectory = Files.createDirectory(tempDirectory.resolve("sav"));
        Path selectedLibrary = Path.of("Brasfoot.jar");
        when(gameLibraryPort.load(selectedLibrary)).thenReturn(tempDirectory.resolve(selectedLibrary));

        presenter.selectBrasfootLibrary(selectedLibrary);

        assertEquals(saveDirectory.toFile(), createChooser().getCurrentDirectory());
    }

    @Test
    void usesDefaultDirectoryBeforeAnyLibraryLoads() {
        assertEquals(FileSystemView.getFileSystemView().getDefaultDirectory(), createChooser().getCurrentDirectory());
    }

    @Test
    void usesDefaultDirectoryAfterInstallationIsRemoved() throws Exception {
        Path installationDirectory = Files.createDirectory(tempDirectory.resolve("Brasfoot"));
        selectLibrary(installationDirectory.resolve("Brasfoot.jar"));
        assertEquals(installationDirectory.toFile(), createChooser().getCurrentDirectory());

        Files.delete(installationDirectory);

        assertEquals(FileSystemView.getFileSystemView().getDefaultDirectory(), createChooser().getCurrentDirectory());
    }

    @ParameterizedTest
    @EnumSource(value = PosixFilePermission.class, names = {"OWNER_READ", "OWNER_EXECUTE"})
    void fallsBackWhenSavLacksReadOrSearchPermission(PosixFilePermission permission) throws Exception {
        assumeTrue(Files.getFileStore(tempDirectory).supportsFileAttributeView("posix"));
        Path saveDirectory = Files.createDirectory(tempDirectory.resolve("sav"));
        Set<PosixFilePermission> originalPermissions = Files.getPosixFilePermissions(saveDirectory);

        try {
            Files.setPosixFilePermissions(saveDirectory, Set.of(permission));
            assumeFalse(Files.isReadable(saveDirectory) && Files.isExecutable(saveDirectory));
            selectLibrary(tempDirectory.resolve("Brasfoot.jar"));

            assertEquals(tempDirectory.toFile(), createChooser().getCurrentDirectory());
        } finally {
            Files.setPosixFilePermissions(saveDirectory, originalPermissions);
        }
    }

    @Test
    void usesDefaultDirectoryWhileInstallationIsInaccessible() throws Exception {
        assumeTrue(Files.getFileStore(tempDirectory).supportsFileAttributeView("posix"));
        Path installationDirectory = Files.createDirectory(tempDirectory.resolve("Brasfoot"));
        Path saveDirectory = Files.createDirectory(installationDirectory.resolve("sav"));
        Set<PosixFilePermission> originalPermissions = Files.getPosixFilePermissions(installationDirectory);
        selectLibrary(installationDirectory.resolve("Brasfoot.jar"));

        try {
            Files.setPosixFilePermissions(installationDirectory, Set.of());
            assumeFalse(Files.isExecutable(installationDirectory));

            assertEquals(FileSystemView.getFileSystemView().getDefaultDirectory(), createChooser().getCurrentDirectory());
        } finally {
            Files.setPosixFilePermissions(installationDirectory, originalPermissions);
        }

        assertEquals(saveDirectory.toFile(), createChooser().getCurrentDirectory());
    }

    private void selectLibrary(Path library) {
        when(gameLibraryPort.load(library)).thenReturn(library);
        presenter.selectBrasfootLibrary(library);
    }

    private JFileChooser createChooser() {
        return SwingMainWindow.createSaveChooser(presenter.getInstallationDirectory());
    }
}
