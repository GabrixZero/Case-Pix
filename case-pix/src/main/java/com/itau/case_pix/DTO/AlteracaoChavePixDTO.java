package com.itau.case_pix.DTO;

import java.util.UUID;

public class AlteracaoChavePixDTO {
    private UUID id;
    private String tipoConta;
    private Integer numeroAgencia;
    private Integer numeroConta;
    private String nomeCorrentista;
    private String sobrenomeCorrentista;
    
    // Getters e Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTipoConta() {
        return tipoConta;
    }
    
    public void setTipoConta(String tipoConta) {
        this.tipoConta = tipoConta;
    }
    
    public Integer getNumeroAgencia() {
        return numeroAgencia;
    }
    
    public void setNumeroAgencia(Integer numeroAgencia) {
        this.numeroAgencia = numeroAgencia;
    }
    
    public Integer getNumeroConta() {
        return numeroConta;
    }
    
    public void setNumeroConta(Integer numeroConta) {
        this.numeroConta = numeroConta;
    }
    
    public String getNomeCorrentista() {
        return nomeCorrentista;
    }
    
    public void setNomeCorrentista(String nomeCorrentista) {
        this.nomeCorrentista = nomeCorrentista;
    }
    
    public String getSobrenomeCorrentista() {
        return sobrenomeCorrentista;
    }
    
    public void setSobrenomeCorrentista(String sobrenomeCorrentista) {
        this.sobrenomeCorrentista = sobrenomeCorrentista;
    }
}
