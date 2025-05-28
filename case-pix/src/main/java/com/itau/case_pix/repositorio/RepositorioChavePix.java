package com.itau.case_pix.repositorio;

import com.itau.case_pix.modelo.EntidadeChavePix;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface RepositorioChavePix extends JpaRepository<EntidadeChavePix, UUID>{

    // Buscar por tipo de chave
    List<EntidadeChavePix> findByTipoChave(String tipoChave);
    
    // Buscar por agência e conta
    List<EntidadeChavePix> findByNumeroAgenciaAndNumeroConta(Integer numeroAgencia, Integer numeroConta);
    
    // Buscar por nome do correntista (usando LIKE para busca parcial)
    @Query("SELECT c FROM EntidadeChavePix c WHERE UPPER(c.nomeCorrentista) LIKE UPPER(CONCAT('%', :nome, '%'))")
    List<EntidadeChavePix> findByNomeCorrentista(@Param("nome") String nome);
    
    // Buscar por data de inclusão (intervalo)
    List<EntidadeChavePix> findByDataHoraInclusaoChaveBetween(LocalDateTime inicio, LocalDateTime fim);
    
    // Buscar chaves ativas (não inativadas)
    @Query("SELECT c FROM EntidadeChavePix c WHERE c.dataHoraInativacaoChave IS NULL")
    List<EntidadeChavePix> findAllAtivas();
    
    // Buscar chaves inativadas
    @Query("SELECT c FROM EntidadeChavePix c WHERE c.dataHoraInativacaoChave IS NOT NULL")
    List<EntidadeChavePix> findAllInativas();
    
    // Verificar se existe chave com mesmo valor
    boolean existsByTipoChaveAndValorChave(String tipoChave, String valorChave);
    
    // Verifica se existe alguma chave para a conta com um tipoPessoa DIFERENTE do informado
    boolean existsByNumeroAgenciaAndNumeroContaAndTipoPessoaNot(Integer numeroAgencia, Integer numeroConta, String tipoPessoa);

    // Contar chaves ativas por agência e conta
    @Query("SELECT COUNT(c) FROM EntidadeChavePix c WHERE c.numeroAgencia = :agencia AND c.numeroConta = :conta AND c.dataHoraInativacaoChave IS NULL")
    long countChavesAtivasByAgenciaConta(@Param("agencia") Integer agencia, @Param("conta") Integer conta);
}