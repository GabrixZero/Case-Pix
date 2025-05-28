package com.itau.case_pix.modelo;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "PixItauDb")
public class EntidadeChavePix {

    @Id
    @Column(name = "id", columnDefinition = "RAW(16)")
    private UUID id;

    @NotNull(message = "O tipo de chave é obrigatório.")
    @Pattern(regexp = "(?i)^(celular|e[-]?mail|cpf|cnpj|aleatoria)$", message = "Tipo de chave inválido. Valores permitidos: celular, email/e-mail, cpf, cnpj, aleatoria")
    @Column(name = "tipo_chave", length = 9, nullable = false)
    private String tipoChave;

    @NotNull
    @Size(max = 77, message = "Email muito grande, o limite é de 77 caracteres")
    @Column(name = "valor_chave", length = 77, nullable = false)
    private String valorChave;

    @NotNull(message = "O tipo de pessoa é obrigatório.")
    @Pattern(regexp = "(?i)^(f[ií]sica|jur[ií]dica)$", message = "Tipo de pessoa deve ser 'fisica' ou 'juridica'.")
    @Column(name = "tipo_pessoa", length = 8, nullable = false)
    private String tipoPessoa;

    @NotNull(message = "O tipo de conta é obrigatório.")
    @Pattern(regexp = "(?i)^(corrente|poupan[çc]a|POUPAN[ÇC]A)$", message = "Tipo de conta deve ser 'corrente' ou 'poupanca'.")
    @Column(name = "tipo_conta", length = 10, nullable = false)
    private String tipoConta;

    @NotNull(message = "O número da agência é obrigatório.")
    @Min(value = 1, message = "Número da agência inválido.")
    @Max(value = 9999, message = "Número da agência não pode exceder 4 dígitos.")
    @Column(name = "numero_agencia", nullable = false)
    private Integer numeroAgencia;

    @NotNull(message = "O número da conta é obrigatório.")
    @Min(value = 1, message = "Número da conta inválido.")
    @Max(value = 99999999, message = "Número da conta não pode exceder 8 dígitos.")
    @Column(name = "numero_conta", nullable = false)
    private Integer numeroConta;

    @NotNull(message = "O nome do correntista é obrigatório.")
    @Size(max = 30, message = "Nome do correntista muito grande, o limite é de 30 caracteres")
    @Column(name = "nome_correntista", length = 30, nullable = false)
    private String nomeCorrentista;

    @Size(max = 45, message = "Sobrenome do correntista muito grande, o limite é de 45 caracteres")
    @Column(name = "sobrenome_correntista", length = 45)
    private String sobrenomeCorrentista;

    @Column(name = "data_hora_inclusao_chave")
    private LocalDateTime dataHoraInclusaoChave;

    @Column(name = "data_hora_inativacao_chave")
    private LocalDateTime dataHoraInativacaoChave;

    // Construtores
    public EntidadeChavePix() {
    }

    public EntidadeChavePix(UUID id, String tipoChave, String valorChave, String tipoPessoa, String tipoConta,
            Integer numeroAgencia, Integer numeroConta, String nomeCorrentista,
            String sobrenomeCorrentista) {
        this.id = id;
        this.tipoChave = tipoChave;
        this.valorChave = valorChave;
        this.tipoPessoa = tipoPessoa;
        this.tipoConta = tipoConta;
        this.numeroAgencia = numeroAgencia;
        this.numeroConta = numeroConta;
        this.nomeCorrentista = nomeCorrentista;
        this.sobrenomeCorrentista = sobrenomeCorrentista;
        this.dataHoraInclusaoChave = LocalDateTime.now();
    }

    // Getters e Setters
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

    public LocalDateTime getDataHoraInativacaoChave() {
        return dataHoraInativacaoChave;
    }

    public void setDataHoraInativacaoChave(LocalDateTime dataHoraInativacaoChave) {
        this.dataHoraInativacaoChave = dataHoraInativacaoChave;
    }

    // Método para inativar a chave
    public void inativar() {
        this.dataHoraInativacaoChave = LocalDateTime.now();
    }

    // Método para verificar se a chave está ativa
    public boolean isAtiva() {
        return this.dataHoraInativacaoChave == null;
    }

    @Override
    public String toString() {
        return "ChavePix{" +
                "id=" + id +
                ", tipoChave='" + tipoChave + '\'' +
                ", valorChave='" + valorChave + '\'' +
                ", tipoConta='" + tipoConta + '\'' +
                ", numeroAgencia=" + numeroAgencia +
                ", numeroConta=" + numeroConta +
                ", nomeCorrentista='" + nomeCorrentista + '\'' +
                ", sobrenomeCorrentista='" + sobrenomeCorrentista + '\'' +
                ", dataHoraInclusaoChave=" + dataHoraInclusaoChave +
                ", dataHoraInativacaoChave=" + dataHoraInativacaoChave +
                '}';
    }
}
