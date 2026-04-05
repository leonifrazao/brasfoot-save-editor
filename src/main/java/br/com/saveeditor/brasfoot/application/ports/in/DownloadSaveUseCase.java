package br.com.saveeditor.brasfoot.application.ports.in;

public interface DownloadSaveUseCase {
    byte[] download(String sessionId);
}