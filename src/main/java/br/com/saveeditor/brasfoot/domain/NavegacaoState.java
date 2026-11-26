package br.com.saveeditor.brasfoot.domain;

import lombok.Getter;

/**
 * Armazena todo o estado da sessão de edição atual.
 */
public class NavegacaoState {
    // Getters e Setters
    @Getter
    private final Object objetoRaiz;
    @Getter
    private final Object dataAfQ;

    @Getter
    private String caminhoArquivoOriginal;
    @Getter
    private final boolean compressedSave;

    public NavegacaoState(Object objetoRaiz, Object dataAfQ, String caminhoArquivoOriginal) {
        this(objetoRaiz, dataAfQ, caminhoArquivoOriginal, false);
    }

    public NavegacaoState(Object objetoRaiz, Object dataAfQ, String caminhoArquivoOriginal, boolean compressedSave) {
        this.objetoRaiz = objetoRaiz;
        this.dataAfQ = dataAfQ;
        this.caminhoArquivoOriginal = caminhoArquivoOriginal;
        this.compressedSave = compressedSave;
    }

}
