package br.com.saveeditor.brasfoot.application.services;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.function.Supplier;

final class AtomicSaveFiles {
    private AtomicSaveFiles() {}

    static void write(Path destination, Supplier<byte[]> payload, Path sourceInfo) throws IOException {
        publish(destination, payload, sourceInfo, () -> {});
    }

    static void replace(Path destination, Supplier<byte[]> payload, Runnable backup) throws IOException {
        publish(destination, payload, null, backup);
    }

    private static void publish(Path destination, Supplier<byte[]> payload, Path sourceInfo, Runnable beforeReplace)
            throws IOException {
        Path saveTemp = Files.createTempFile(destination.getParent(), ".brasfoot-save-", ".tmp");
        Path infoTemp = null;
        Path publishedInfo = null;
        boolean published = false;
        try {
            writeAndFlush(saveTemp, payload.get());
            if (sourceInfo != null && !Files.notExists(sourceInfo)) {
                infoTemp = Files.createTempFile(destination.getParent(), ".brasfoot-info-", ".tmp");
                writeAndFlush(infoTemp, Files.readAllBytes(sourceInfo));
                Path destinationInfo = metadataPath(destination);
                Files.move(infoTemp, destinationInfo, StandardCopyOption.ATOMIC_MOVE);
                publishedInfo = destinationInfo;
            }
            beforeReplace.run();
            Files.move(saveTemp, destination, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            published = true;
        } finally {
            deleteIfPresent(saveTemp);
            deleteIfPresent(infoTemp);
            if (!published) deleteIfPresent(publishedInfo);
        }
    }

    private static void writeAndFlush(Path file, byte[] data) throws IOException {
        try (FileChannel channel = FileChannel.open(file, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            ByteBuffer bytes = ByteBuffer.wrap(data);
            while (bytes.hasRemaining()) channel.write(bytes);
            channel.force(true);
        }
    }

    static Path metadataPath(Path savePath) {
        String filename = savePath.getFileName().toString();
        int extension = filename.lastIndexOf('.');
        String basename = extension > 0 ? filename.substring(0, extension) : filename;
        return savePath.resolveSibling(basename + ".info");
    }

    private static void deleteIfPresent(Path file) {
        if (file == null) return;
        try {
            Files.deleteIfExists(file);
        } catch (IOException ignored) {
            // The original error remains visible to the caller.
        }
    }
}
