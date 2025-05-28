package com.itau.case_pix.DTO;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.itau.case_pix.modelo.EntidadeChavePix;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RespostaAlteracaoChavePixDTO {
    private UUID id;
    private String tipoChave;
    private String valorChave;
    private String tipoPessoa;
    private String tipoConta;
    private Integer numeroAgencia;
    private Integer numeroConta;
    private String nomeCorrentista;
    private String sobrenomeCorrentista;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataHoraInclusaoChave;
    
    // Construtor
    public RespostaAlteracaoChavePixDTO(EntidadeChavePix chavePix) {
        this.id = chavePix.getId();
        this.tipoChave = chavePix.getTipoChave();
        this.valorChave = chavePix.getValorChave();
        this.tipoPessoa = chavePix.getTipoPessoa();
        this.tipoConta = chavePix.getTipoConta();
        this.numeroAgencia = chavePix.getNumeroAgencia();
        this.numeroConta = chavePix.getNumeroConta();
        this.nomeCorrentista = chavePix.getNomeCorrentista();
        this.sobrenomeCorrentista = chavePix.getSobrenomeCorrentista();
        this.dataHoraInclusaoChave = chavePix.getDataHoraInclusaoChave();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTipoChave() {
        return tipoChave;
    }

    public void setTipoChave(String tipoChave) {
        this.tipoChave = tipoChave;
    }

    public String getValorChave() {
        return valorChave;
    }

    public void setValorChave(String valorChave) {
        this.valorChave = valorChave;
    }

    public String getTipoPessoa() {
        return tipoPessoa;
    }

    public void setTipoPessoa(String tipoPessoa) {
        this.tipoPessoa = tipoPessoa;
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

    public LocalDateTime getDataHoraInclusaoChave() {
        return dataHoraInclusaoChave;
    }

    public void setDataHoraInclusaoChave(LocalDateTime dataHoraInclusaoChave) {
        this.dataHoraInclusaoChave = dataHoraInclusaoChave;
    }
    
}
