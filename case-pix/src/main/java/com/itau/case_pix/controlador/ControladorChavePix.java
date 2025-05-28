package com.itau.case_pix.controlador;

import com.itau.case_pix.DTO.*;
import com.itau.case_pix.modelo.EntidadeChavePix;
import com.itau.case_pix.servico.ServicoChavePix;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chaves-pix" )
public class ControladorChavePix {

    private final ServicoChavePix servicoChavePix;

    @Autowired
    public ControladorChavePix(ServicoChavePix servicoChavePix) {
        this.servicoChavePix = servicoChavePix;
    }

    // Método para inclusão de chave
    @PostMapping
    public ResponseEntity<?> incluirChave(@RequestBody EntidadeChavePix chavePix) {
        try {
            EntidadeChavePix novaChave = servicoChavePix.incluirChave(chavePix);
            RespostaInclusaoChavePixDTO resposta = new RespostaInclusaoChavePixDTO(novaChave);
            return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao incluir chave PIX: " + e.getMessage());
        }
    }

    // Método para alteração de chave
    @PutMapping("/{id}")
    public ResponseEntity<?> alterarChave(@PathVariable UUID id, @RequestBody AlteracaoChavePixDTO alteracaoDTO) {
        try {
            EntidadeChavePix chaveAlterada = servicoChavePix.alterarChave(id, alteracaoDTO);
            RespostaAlteracaoChavePixDTO resposta = new RespostaAlteracaoChavePixDTO(chaveAlterada);
            return ResponseEntity.ok(resposta);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("não encontrada")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            } else {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(e.getMessage());
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao alterar chave PIX: " + e.getMessage());
        }
    }

    // Método para inativação de chave
    @DeleteMapping("/{id}")
    public ResponseEntity<?> inativarChave(@PathVariable UUID id) {
        try {
            EntidadeChavePix chaveInativada = servicoChavePix.inativarChave(id);
            RespostaInativacaoChavePixDTO resposta = new RespostaInativacaoChavePixDTO(chaveInativada);
            return ResponseEntity.ok(resposta);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("não encontrada")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            } else if (e.getMessage().contains("já está inativada")) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(e.getMessage());
            } else {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(e.getMessage());
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao inativar chave PIX: " + e.getMessage());
        }
    }

    // Método para buscar por ID
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable UUID id) {
        try {
            Optional<EntidadeChavePix> chavePix = servicoChavePix.buscarPorId(id);
            if (chavePix.isPresent()) {
                // Verificar se a chave está inativa
                if (chavePix.get().getDataHoraInativacaoChave() != null) {
                    // Retornar erro 422 com mensagem de texto simples
                    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                        .body("Esta chave PIX está inativada desde " + 
                              chavePix.get().getDataHoraInativacaoChave());
                }
                
                // Se a chave estiver ativa, retornar normalmente
                RespostaConsultaChavePixDTO resposta = new RespostaConsultaChavePixDTO(chavePix.get());
                return ResponseEntity.ok(resposta);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Chave PIX não encontrada");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao buscar chave PIX: " + e.getMessage());
        }
    }

    // Método para buscar por tipo de chave
    @GetMapping("/tipo/{tipoChave}")
    public ResponseEntity<?> buscarPorTipoChave(@PathVariable String tipoChave) {
        try {
            List<EntidadeChavePix> chaves = servicoChavePix.buscarPorTipoChave(tipoChave);
            if (chaves.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Nenhuma chave PIX encontrada para o tipo: " + tipoChave);
            }
            
            // Filtrar apenas chaves ativas
            List<EntidadeChavePix> chavesAtivas = chaves.stream()
                    .filter(chave -> chave.getDataHoraInativacaoChave() == null)
                    .collect(Collectors.toList());
            
            if (chavesAtivas.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                        .body("Todas as chaves PIX encontradas para o tipo " + tipoChave + " estão inativadas");
            }
            
            List<RespostaConsultaChavePixDTO> respostas = chavesAtivas.stream()
                    .map(chave -> new RespostaConsultaChavePixDTO(chave))
                    .collect(Collectors.toList());
                    
            return ResponseEntity.ok(respostas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao buscar chaves PIX por tipo: " + e.getMessage());
        }
    }

    // Método para buscar por agência e conta
    @GetMapping("/conta")
    public ResponseEntity<?> buscarPorAgenciaConta(
            @RequestParam Integer agencia,
            @RequestParam Integer conta) {
        try {
            List<EntidadeChavePix> chaves = servicoChavePix.buscarPorAgenciaConta(agencia, conta);
            if (chaves.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Nenhuma chave PIX encontrada para agência " + agencia + " e conta " + conta);
            }
            
            // Filtrar apenas chaves ativas
            List<EntidadeChavePix> chavesAtivas = chaves.stream()
                    .filter(chave -> chave.getDataHoraInativacaoChave() == null)
                    .collect(Collectors.toList());
            
            if (chavesAtivas.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                        .body("Todas as chaves PIX encontradas para agência " + agencia + " e conta " + conta + " estão inativadas");
            }
            
            List<RespostaConsultaChavePixDTO> respostas = chavesAtivas.stream()
                    .map(chave -> new RespostaConsultaChavePixDTO(chave))
                    .collect(Collectors.toList());
                    
            return ResponseEntity.ok(respostas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao buscar chaves PIX por agência e conta: " + e.getMessage());
        }
    }

    // Método para buscar por nome do correntista
    @GetMapping("/correntista/{nome}")
    public ResponseEntity<?> buscarPorNomeCorrentista(@PathVariable String nome) {
        try {
            List<EntidadeChavePix> chaves = servicoChavePix.buscarPorNomeCorrentista(nome);
            if (chaves.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Nenhuma chave PIX encontrada para o correntista: " + nome);
            }
            
            // Filtrar apenas chaves ativas
            List<EntidadeChavePix> chavesAtivas = chaves.stream()
                    .filter(chave -> chave.getDataHoraInativacaoChave() == null)
                    .collect(Collectors.toList());
            
            if (chavesAtivas.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                        .body("Todas as chaves PIX encontradas para o correntista " + nome + " estão inativadas");
            }
            
            List<RespostaConsultaChavePixDTO> respostas = chavesAtivas.stream()
                    .map(chave -> new RespostaConsultaChavePixDTO(chave))
                    .collect(Collectors.toList());
                    
            return ResponseEntity.ok(respostas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao buscar chaves PIX por nome do correntista: " + e.getMessage());
        }
    }

    // Método para buscar por data de inclusão
    @GetMapping("/data")
    public ResponseEntity<?> buscarPorDataInclusao(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {
        try {
            List<EntidadeChavePix> chaves = servicoChavePix.buscarPorDataInclusao(inicio, fim);
            if (chaves.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Nenhuma chave PIX encontrada no período especificado");
            }
            
            // Filtrar apenas chaves ativas
            List<EntidadeChavePix> chavesAtivas = chaves.stream()
                    .filter(chave -> chave.getDataHoraInativacaoChave() == null)
                    .collect(Collectors.toList());
            
            if (chavesAtivas.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                        .body("Todas as chaves PIX encontradas no período especificado estão inativadas");
            }
            
            List<RespostaConsultaChavePixDTO> respostas = chavesAtivas.stream()
                    .map(chave -> new RespostaConsultaChavePixDTO(chave))
                    .collect(Collectors.toList());
                    
            return ResponseEntity.ok(respostas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao buscar chaves PIX por data de inclusão: " + e.getMessage());
        }
    }

    // Método para buscar chaves ativas
    @GetMapping("/ativas")
    public ResponseEntity<?> buscarChavesAtivas() {
        try {
            List<EntidadeChavePix> chaves = servicoChavePix.buscarChavesAtivas();
            if (chaves.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Nenhuma chave PIX ativa encontrada");
            }
            
            List<RespostaConsultaChavePixDTO> respostas = chaves.stream()
                    .map(chave -> new RespostaConsultaChavePixDTO(chave))
                    .collect(Collectors.toList());
                    
            return ResponseEntity.ok(respostas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao buscar chaves PIX ativas: " + e.getMessage());
        }
    }

    // Método para buscar chaves inativas
    @GetMapping("/inativas")
    public ResponseEntity<?> buscarChavesInativas() {
        try {
            List<EntidadeChavePix> chaves = servicoChavePix.buscarChavesInativas();
            if (chaves.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Nenhuma chave PIX inativa encontrada");
            }
            
            List<RespostaConsultaChavePixDTO> respostas = chaves.stream()
                    .map(chave -> new RespostaConsultaChavePixDTO(chave))
                    .collect(Collectors.toList());
                    
            return ResponseEntity.ok(respostas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao buscar chaves PIX inativas: " + e.getMessage());
        }
    }
}
