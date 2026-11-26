package br.com.saveeditor.brasfoot.service;

import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/** Adapta o envelope de save usado por diferentes versões do Brasfoot. */
@Component
public class BrasfootSaveCompatibility {

    private static final int GZIP_MAGIC_1 = 0x1f;
    private static final int GZIP_MAGIC_2 = 0x8b;

    public DecodedSave decode(byte[] fileBytes) {
        if (!isGzip(fileBytes)) {
            return new DecodedSave(fileBytes, false);
        }

        try (GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(fileBytes));
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            gzip.transferTo(output);
            return new DecodedSave(output.toByteArray(), true);
        } catch (IOException e) {
            throw new IllegalArgumentException("Nao foi possivel descompactar o save do Brasfoot", e);
        }
    }

    public byte[] encode(byte[] payload, boolean gzip) {
        if (!gzip) {
            return payload;
        }

        try (ByteArrayOutputStream output = new ByteArrayOutputStream();
             GZIPOutputStream compressed = new GZIPOutputStream(output)) {
            compressed.write(payload);
            compressed.finish();
            return output.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Nao foi possivel compactar o save do Brasfoot", e);
        }
    }

    private boolean isGzip(byte[] bytes) {
        return bytes != null && bytes.length >= 2
                && (bytes[0] & 0xff) == GZIP_MAGIC_1
                && (bytes[1] & 0xff) == GZIP_MAGIC_2;
    }

    public record DecodedSave(byte[] payload, boolean gzip) {
    }
}
