package com.itau.case_pix.servico;

import com.itau.case_pix.DTO.AlteracaoChavePixDTO;
import com.itau.case_pix.modelo.EntidadeChavePix;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServicoChavePix {
    
    // Métodos para inclusão de chave
    EntidadeChavePix incluirChave(EntidadeChavePix chavePix);

    //Método para alteração de chave com DTO
    EntidadeChavePix alterarChave(UUID id, AlteracaoChavePixDTO alteracaoDTO);
    
    // Métodos para consulta de chaves
    Optional<EntidadeChavePix> buscarPorId(UUID id);
    List<EntidadeChavePix> buscarPorTipoChave(String tipoChave);
    List<EntidadeChavePix> buscarPorAgenciaConta(Integer numeroAgencia, Integer numeroConta);
    List<EntidadeChavePix> buscarPorNomeCorrentista(String nome);
    List<EntidadeChavePix> buscarPorDataInclusao(LocalDateTime inicio, LocalDateTime fim);
    List<EntidadeChavePix> buscarChavesAtivas();
    List<EntidadeChavePix> buscarChavesInativas();
    
    // Método para inativação de chave
    EntidadeChavePix inativarChave(UUID id);
    
    // Métodos de validação
    boolean validarChave(EntidadeChavePix chavePix);
    boolean validarTipoChave(String tipoChave);
    boolean validarValorChave(String tipoChave, String valorChave);
    boolean validarLimiteChavesPorConta(Integer numeroAgencia, Integer numeroConta, String tipoConta);

}

