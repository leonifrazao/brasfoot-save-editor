package br.com.saveeditor.brasfoot.model;

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

    public NavegacaoState(Object objetoRaiz, Object dataAfQ, String caminhoArquivoOriginal) {
        this.objetoRaiz = objetoRaiz;
        this.dataAfQ = dataAfQ;
        this.caminhoArquivoOriginal = caminhoArquivoOriginal;
    }

}