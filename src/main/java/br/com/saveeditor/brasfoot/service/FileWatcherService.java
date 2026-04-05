package br.com.saveeditor.brasfoot.service;

import br.com.saveeditor.brasfoot.config.PreferencesManager;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Serviço para monitorar mudanças externas em arquivos.
 * Usa WatchService API do Java NIO.
 */
@Service
public class FileWatcherService {

    private final PreferencesManager preferencesManager;
    private WatchService watchService;
    private Thread watcherThread;
    private Path watchedFile;
    private String lastKnownHash;
    private final AtomicBoolean watching = new AtomicBoolean(false);
    private final AtomicBoolean hasLocalChanges = new AtomicBoolean(false);

    private FileChangeListener listener;

    public FileWatcherService() {
        this.preferencesManager = PreferencesManager.getInstance();
    }

    /**
     * Inicia o monitoramento de um arquivo.
     */
    public void startWatching(File file, FileChangeListener listener) {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("Arquivo inválido ou não existe");
        }

        if (!preferencesManager.isAutoRefreshEnabled()) {
            System.out.println("⚠ Auto-refresh desabilitado nas preferências");
            return;
        }

        stopWatching();

        this.watchedFile = file.toPath();
        this.listener = listener;
        this.lastKnownHash = calculateFileHash(file);
        this.watching.set(true);

        // Iniciar thread de monitoramento
        watcherThread = new Thread(this::watchLoop, "FileWatcher-Thread");
        watcherThread.setDaemon(true);
        watcherThread.start();

        System.out.println("👁 Monitorando arquivo: " + file.getName());
    }

    /**
     * Para o monitoramento.
     */
    public void stopWatching() {
        watching.set(false);

        if (watcherThread != null && watcherThread.isAlive()) {
            watcherThread.interrupt();
            try {
                watcherThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        if (watchService != null) {
            try {
                watchService.close();
            } catch (IOException e) {
                // Ignorar
            }
        }

        System.out.println("🛑 Monitoramento parado");
    }

    /**
     * Loop principal de monitoramento - usa polling com verificação de hash.
     * Mais confiável que WatchService para paths OneDrive/WSL.
     */
    private void watchLoop() {
        int pollIntervalMs = 2000; // Verificar a cada 2 segundos

        System.out.println("🔄 Polling iniciado (intervalo: " + pollIntervalMs + "ms)");

        while (watching.get() && !Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(pollIntervalMs);
            } catch (InterruptedException e) {
                break;
            }

            File file = watchedFile.toFile();

            // Verificar se arquivo foi deletado
            if (!file.exists()) {
                handleFileDeleted();
                break;
            }

            // Verificar se hash mudou
            String currentHash = calculateFileHash(file);

            if (currentHash != null && !currentHash.equals(lastKnownHash)) {
                // System.out.println("DEBUG: Hash Mismatch! Old=" + lastKnownHash + ", New=" +
                // currentHash);
                // System.out.println("📝 Mudança detectada no arquivo!");
                handleFileModified();
            }
        }

        System.out.println("🛑 Polling parado");
    }

    /**
     * Trata modificação do arquivo.
     */
    private void handleFileModified() {
        // Esperar um pouco para garantir que a escrita terminou
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            return;
        }

        File file = watchedFile.toFile();
        if (!file.exists()) {
            return;
        }

        // Calcular novo hash
        String newHash = calculateFileHash(file);

        // Verificar se realmente mudou
        if (newHash != null && !newHash.equals(lastKnownHash)) {
            System.out.println("🔄 Arquivo modificado externamente detectado");

            // Verificar se há conflito (mudanças locais não salvas)
            if (hasLocalChanges.get()) {
                if (listener != null) {
                    listener.onConflictDetected();
                }
            } else {
                // Auto-reload sem conflito
                lastKnownHash = newHash;
                if (listener != null) {
                    listener.onFileChanged();
                }
            }
        }
    }

    /**
     * Trata deleção do arquivo.
     */
    private void handleFileDeleted() {
        System.out.println("🗑 Arquivo foi deletado");
        if (listener != null) {
            listener.onFileDeleted();
        }
        stopWatching();
    }

    /**
     * Calcula o hash SHA-256 de um arquivo.
     */
    private String calculateFileHash(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }

            byte[] hashBytes = digest.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();

        } catch (Exception e) {
            System.err.println("⚠ Erro ao calcular hash: " + e.getMessage());
            return null;
        }
    }

    /**
     * Marca que há mudanças locais não salvas.
     */
    public void markLocalChanges(boolean hasChanges) {
        this.hasLocalChanges.set(hasChanges);
    }

    /**
     * Atualiza o hash conhecido após salvar.
     */
    public void updateKnownHash(File file) {
        this.lastKnownHash = calculateFileHash(file);
        this.hasLocalChanges.set(false);
        System.out.println("✅ Hash atualizado após salvamento");
    }

    /**
     * Verifica se está monitorando.
     */
    public boolean isWatching() {
        return watching.get();
    }

    /**
     * Interface para callbacks de mudanças.
     */
    public interface FileChangeListener {
        /**
         * Chamado quando o arquivo é modificado externamente (sem conflito).
         */
        void onFileChanged();

        /**
         * Chamado quando há conflito (arquivo externo mudou + mudanças locais).
         */
        void onConflictDetected();

        /**
         * Chamado quando o arquivo é deletado.
         */
        void onFileDeleted();

        /**
         * Chamado quando há erro no watcher.
         */
        void onError(Exception e);
    }

    /**
     * Estratégia de resolução de conflito.
     */
    public enum ResolutionStrategy {
        KEEP_LOCAL, // Manter mudanças locais (ignorar externas)
        LOAD_EXTERNAL, // Carregar mudanças externas (perder locais)
        SAVE_AND_RELOAD // Salvar em novo arquivo e recarregar externas
    }
}
