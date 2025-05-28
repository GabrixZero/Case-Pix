package com.itau.case_pix.servico;

import com.itau.case_pix.DTO.AlteracaoChavePixDTO;

import com.itau.case_pix.modelo.EntidadeChavePix;
import com.itau.case_pix.repositorio.RepositorioChavePix;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class ServicoImplementacaoChavePix implements ServicoChavePix {

    private final RepositorioChavePix repositorioChavePix;

    @Autowired
    public ServicoImplementacaoChavePix(RepositorioChavePix repositorioChavePix) {
        this.repositorioChavePix = repositorioChavePix;
    }

    @Override
    @Transactional
    public EntidadeChavePix incluirChave(EntidadeChavePix chavePix) {

        normalizarDados(chavePix);

        // Validar a chave antes de incluir
        if (!validarChave(chavePix)) {
            throw new IllegalArgumentException("Dados da chave PIX inválidos");
        }

        // Verificar se já existe chave com mesmo valor
        if (repositorioChavePix.existsByTipoChaveAndValorChave(chavePix.getTipoChave(), chavePix.getValorChave())) {
            throw new IllegalArgumentException("Já existe uma chave PIX com este valor");
        }

        if (repositorioChavePix.existsByNumeroAgenciaAndNumeroContaAndTipoPessoaNot(
                chavePix.getNumeroAgencia(), chavePix.getNumeroConta(), chavePix.getTipoPessoa())) {
            throw new IllegalArgumentException(
                    "Inconsistência: Esta conta (agência/número) já está registrada com um tipo de pessoa (física/jurídica) diferente.");
        }

        // Verificar limite de chaves por conta
        if (!validarLimiteChavesPorConta(chavePix.getNumeroAgencia(), chavePix.getNumeroConta(),
                chavePix.getTipoPessoa())) {
            throw new IllegalArgumentException("Limite de chaves PIX por conta excedido");
        }

        // Gerar UUID para a nova chave se não foi informado
        if (chavePix.getId() == null) {
            chavePix.setId(UUID.randomUUID());
        }

        // Definir data e hora atual para inclusão
        chavePix.setDataHoraInclusaoChave(LocalDateTime.now());

        // Salvar a chave no repositório
        return repositorioChavePix.save(chavePix);
    }

    @Override
    @Transactional
    public EntidadeChavePix alterarChave(UUID id, AlteracaoChavePixDTO alteracaoDTO) {
        // Buscar a chave existente
        EntidadeChavePix chaveExistente = repositorioChavePix.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Chave PIX não encontrada"));

        // Verificar se a chave está inativada
        if (chaveExistente.getDataHoraInativacaoChave() != null) {
            throw new IllegalArgumentException("Não é permitido alterar chaves inativadas");
        }

        // Normalizar dados do DTO
        if (alteracaoDTO.getTipoConta() != null) {
            String tipoConta = alteracaoDTO.getTipoConta().toLowerCase()
                    .replace("ç", "c");
            alteracaoDTO.setTipoConta(tipoConta);
        }

        // Verificar se algum campo foi alterado
        boolean algumCampoAlterado = false;

        if (alteracaoDTO.getTipoConta() != null &&
                !alteracaoDTO.getTipoConta().equals(chaveExistente.getTipoConta())) {
            algumCampoAlterado = true;
        }

        if (alteracaoDTO.getNumeroAgencia() != null &&
                !alteracaoDTO.getNumeroAgencia().equals(chaveExistente.getNumeroAgencia())) {
            algumCampoAlterado = true;
        }

        if (alteracaoDTO.getNumeroConta() != null &&
                !alteracaoDTO.getNumeroConta().equals(chaveExistente.getNumeroConta())) {
            algumCampoAlterado = true;
        }

        if (alteracaoDTO.getNomeCorrentista() != null &&
                !alteracaoDTO.getNomeCorrentista().equals(chaveExistente.getNomeCorrentista())) {
            algumCampoAlterado = true;
        }

        if (alteracaoDTO.getSobrenomeCorrentista() != null &&
                !alteracaoDTO.getSobrenomeCorrentista().equals(chaveExistente.getSobrenomeCorrentista())) {
            algumCampoAlterado = true;
        }

        // Se nenhum campo foi alterado, lançar exceção
        if (!algumCampoAlterado) {
            throw new IllegalArgumentException("Nenhum campo foi alterado. A operação foi cancelada.");
        }

        // Validar os dados do DTO
        if (alteracaoDTO.getTipoConta() != null &&
                !alteracaoDTO.getTipoConta().equals("corrente") &&
                !alteracaoDTO.getTipoConta().equals("poupanca")) {
            throw new IllegalArgumentException("Tipo de conta inválido");
        }

        // Validar número de agência
        if (alteracaoDTO.getNumeroAgencia() != null &&
                (alteracaoDTO.getNumeroAgencia() <= 0 ||
                        String.valueOf(alteracaoDTO.getNumeroAgencia()).length() > 4)) {
            throw new IllegalArgumentException("Número da agência inválido");
        }

        // Validar número de conta
        if (alteracaoDTO.getNumeroConta() != null &&
                (alteracaoDTO.getNumeroConta() <= 0 ||
                        String.valueOf(alteracaoDTO.getNumeroConta()).length() > 8)) {
            throw new IllegalArgumentException("Número da conta inválido");
        }

        // Validar nome do correntista
        if (alteracaoDTO.getNomeCorrentista() != null &&
                (alteracaoDTO.getNomeCorrentista().isEmpty() ||
                        alteracaoDTO.getNomeCorrentista().length() > 30)) {
            throw new IllegalArgumentException("Nome do correntista inválido");
        }

        // Validar sobrenome do correntista
        if (alteracaoDTO.getSobrenomeCorrentista() != null &&
                alteracaoDTO.getSobrenomeCorrentista().length() > 45) {
            throw new IllegalArgumentException("Sobrenome do correntista inválido");
        }

        // Validar consistência do tipoPessoa se agência ou conta foram alteradas
        boolean agenciaOuContaMudou = (alteracaoDTO.getNumeroAgencia() != null
                && !chaveExistente.getNumeroAgencia().equals(alteracaoDTO.getNumeroAgencia())) ||
                (alteracaoDTO.getNumeroConta() != null
                        && !chaveExistente.getNumeroConta().equals(alteracaoDTO.getNumeroConta()));

        if (agenciaOuContaMudou) {
            // Verifica se a *nova* conta/agência já existe com tipoPessoa diferente do
            // tipoPessoa *original* da chave
            Integer novaAgencia = alteracaoDTO.getNumeroAgencia() != null ? alteracaoDTO.getNumeroAgencia()
                    : chaveExistente.getNumeroAgencia();
            Integer novaConta = alteracaoDTO.getNumeroConta() != null ? alteracaoDTO.getNumeroConta()
                    : chaveExistente.getNumeroConta();

            if (repositorioChavePix.existsByNumeroAgenciaAndNumeroContaAndTipoPessoaNot(
                    novaAgencia, novaConta, chaveExistente.getTipoPessoa())) {
                throw new IllegalArgumentException(
                        "Inconsistência: A nova combinação de agência/conta já está registrada com um tipo de pessoa (física/jurídica) diferente.");
            }
        }

        // Atualizar apenas os campos fornecidos no DTO
        if (alteracaoDTO.getTipoConta() != null) {
            chaveExistente.setTipoConta(alteracaoDTO.getTipoConta());
        }

        if (alteracaoDTO.getNumeroAgencia() != null) {
            chaveExistente.setNumeroAgencia(alteracaoDTO.getNumeroAgencia());
        }

        if (alteracaoDTO.getNumeroConta() != null) {
            chaveExistente.setNumeroConta(alteracaoDTO.getNumeroConta());
        }

        if (alteracaoDTO.getNomeCorrentista() != null) {
            chaveExistente.setNomeCorrentista(alteracaoDTO.getNomeCorrentista());
        }

        if (alteracaoDTO.getSobrenomeCorrentista() != null) {
            chaveExistente.setSobrenomeCorrentista(alteracaoDTO.getSobrenomeCorrentista());
        }

        // Salvar as alterações
        return repositorioChavePix.save(chaveExistente);
    }

    @Override
    public Optional<EntidadeChavePix> buscarPorId(UUID id) {
        return repositorioChavePix.findById(id);
    }

    @Override
    public List<EntidadeChavePix> buscarPorTipoChave(String tipoChave) {
        return repositorioChavePix.findByTipoChave(tipoChave);
    }

    @Override
    public List<EntidadeChavePix> buscarPorAgenciaConta(Integer numeroAgencia, Integer numeroConta) {
        return repositorioChavePix.findByNumeroAgenciaAndNumeroConta(numeroAgencia, numeroConta);
    }

    @Override
    public List<EntidadeChavePix> buscarPorNomeCorrentista(String nome) {
        return repositorioChavePix.findByNomeCorrentista(nome);
    }

    @Override
    public List<EntidadeChavePix> buscarPorDataInclusao(LocalDateTime inicio, LocalDateTime fim) {
        return repositorioChavePix.findByDataHoraInclusaoChaveBetween(inicio, fim);
    }

    @Override
    public List<EntidadeChavePix> buscarChavesAtivas() {
        return repositorioChavePix.findAllAtivas();
    }

    @Override
    public List<EntidadeChavePix> buscarChavesInativas() {
        return repositorioChavePix.findAllInativas();
    }

    @Override
    @Transactional
    public EntidadeChavePix inativarChave(UUID id) {
        // Buscar a chave existente
        EntidadeChavePix chaveExistente = repositorioChavePix.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Chave PIX não encontrada"));

        // Verificar se a chave já está inativada
        if (chaveExistente.getDataHoraInativacaoChave() != null) {
            throw new IllegalArgumentException("Chave PIX já está inativada");
        }

        // Inativar a chave
        chaveExistente.setDataHoraInativacaoChave(LocalDateTime.now());

        // Salvar as alterações
        return repositorioChavePix.save(chaveExistente);
    }

    @Override
    public boolean validarChave(EntidadeChavePix chavePix) {
        // Validar tipo de chave
        if (chavePix.getTipoChave() == null || !validarTipoChave(chavePix.getTipoChave())) {
            return false;
        }

        // Validar valor da chave conforme seu tipo
        if (chavePix.getValorChave() == null || !validarValorChave(chavePix.getTipoChave(), chavePix.getValorChave())) {
            return false;
        }

        // Validar tipo de pessoa
        if (chavePix.getTipoPessoa() == null ||
                (!chavePix.getTipoPessoa().equals("fisica") && !chavePix.getTipoPessoa().equals("juridica"))) {
            return false;
        }

        // Validar tipo de conta
        if (chavePix.getTipoConta() == null ||
                (!chavePix.getTipoConta().equals("corrente") && !chavePix.getTipoConta().equals("poupanca"))) {
            return false;
        }

        // Validar número de agência
        if (chavePix.getNumeroAgencia() == null ||
                chavePix.getNumeroAgencia() <= 0 ||
                String.valueOf(chavePix.getNumeroAgencia()).length() > 4) {
            return false;
        }

        // Validar número de conta
        if (chavePix.getNumeroConta() == null ||
                chavePix.getNumeroConta() <= 0 ||
                String.valueOf(chavePix.getNumeroConta()).length() > 8) {
            return false;
        }

        // Validar nome do correntista
        if (chavePix.getNomeCorrentista() == null ||
                chavePix.getNomeCorrentista().isEmpty() ||
                chavePix.getNomeCorrentista().length() > 30) {
            return false;
        }

        // Validar sobrenome do correntista (opcional)
        if (chavePix.getSobrenomeCorrentista() != null &&
                chavePix.getSobrenomeCorrentista().length() > 45) {
            return false;
        }

        return true;
    }

    private void normalizarDados(EntidadeChavePix chavePix) {
        if (chavePix.getTipoChave() != null) {
            String tipoChave = chavePix.getTipoChave().toLowerCase().replace("-", "");
            chavePix.setTipoChave(tipoChave);
        }

        if (chavePix.getTipoPessoa() != null) {
            String tipoPessoa = chavePix.getTipoPessoa().toLowerCase()
                    .replace("í", "i").replace("é", "e").replace("ç", "c");
            chavePix.setTipoPessoa(tipoPessoa);
        }

        if (chavePix.getTipoConta() != null) {
            String tipoConta = chavePix.getTipoConta().toLowerCase()
                    .replace("ç", "c");
            chavePix.setTipoConta(tipoConta);
        }
    }

    @Override
    public boolean validarTipoChave(String tipoChave) {
        return tipoChave.equals("celular") ||
                tipoChave.equals("email") ||
                tipoChave.equals("cpf") ||
                tipoChave.equals("cnpj") ||
                tipoChave.equals("aleatoria");
    }

    @Override
    public boolean validarValorChave(String tipoChave, String valorChave) {
        if (valorChave == null || valorChave.isEmpty()) {
            return false;
        }

        switch (tipoChave) {
            case "celular":
                return validarCelular(valorChave);
            case "email":
                return validarEmail(valorChave);
            case "cpf":
                return validarCPF(valorChave);
            case "cnpj":
                return validarCNPJ(valorChave);
            case "aleatoria":
                return validarChaveAleatoria(valorChave);
            default:
                return false;
        }
    }

    @Override
    public boolean validarLimiteChavesPorConta(Integer numeroAgencia, Integer numeroConta, String tipoPessoa) {
        long quantidadeChaves = repositorioChavePix.countChavesAtivasByAgenciaConta(numeroAgencia, numeroConta);

        // Verificar limite conforme tipo de pessoa
        if ("fisica".equals(tipoPessoa) && quantidadeChaves >= 5) {
            return false; // Excede limite para PF
        } else if ("juridica".equals(tipoPessoa) && quantidadeChaves >= 20) {
            return false; // Excede limite para PJ
        }

        return true;
    }

    // Métodos de validação específicos para cada tipo de chave

    private boolean validarCelular(String celular) {
        // Regex para validar formato: +{1-3 dígitos}{2-3 dígitos}{8-9 dígitos}
        // Grupos: (código país)(DDD)(número)
        String regex = "^\\+(\\d{1,3})(\\d{2,3})(\\d{8,9})$";

        if (celular == null || !celular.matches(regex)) {
            return false;
        }

        return true;
    }

    private boolean validarEmail(String email) {
        // Regra: contém "@", tamanho máximo de 77 caracteres
        if (email == null || !email.contains("@") || email.length() > 77) {
            return false;
        }

        // Regex de formato básico já existente
        String regex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(regex);
        if (!pattern.matcher(email).matches()) {
            return false;
        }
        return true;
    }

    private boolean validarCPF(String cpf) {
        if (cpf == null) {
            return false;
        }

        cpf = cpf.replaceAll("\\D", ""); // Remove não-números
        if (cpf.length() != 11 || !cpf.matches("^\\d+$")) { // Garante 11 dígitos numéricos
            return false;
        }

        // Verificar se todos os dígitos são iguais (caso inválido)
        boolean todosDigitosIguais = true;
        for (int i = 1; i < cpf.length(); i++) {
            if (cpf.charAt(i) != cpf.charAt(0)) {
                todosDigitosIguais = false;
                break;
            }
        }
        if (todosDigitosIguais) {
            return false;
        }

        // Calcular e verificar primeiro dígito verificador
        int soma = 0;
        for (int i = 0; i < 9; i++) {
            soma += (cpf.charAt(i) - '0') * (10 - i);
        }
        int resto = soma % 11;
        int dv1 = (resto < 2) ? 0 : 11 - resto;
        if ((cpf.charAt(9) - '0') != dv1) {
            return false;
        }

        // Calcular e verificar segundo dígito verificador
        soma = 0;
        for (int i = 0; i < 10; i++) {
            soma += (cpf.charAt(i) - '0') * (11 - i);
        }
        resto = soma % 11;
        int dv2 = (resto < 2) ? 0 : 11 - resto;
        if ((cpf.charAt(10) - '0') != dv2) {
            return false;
        }

        return true;
    }

    private boolean validarCNPJ(String cnpj) {
        if (cnpj == null) {
            return false;
        }

        cnpj = cnpj.replaceAll("\\D", ""); // Remove não-números
        if (cnpj.length() != 14 || !cnpj.matches("^\\d+$")) { // Garante 14 dígitos numéricos
            return false;
        }

        // Verificar se todos os dígitos são iguais (caso inválido)
        boolean todosDigitosIguais = true;
        for (int i = 1; i < cnpj.length(); i++) {
            if (cnpj.charAt(i) != cnpj.charAt(0)) {
                todosDigitosIguais = false;
                break;
            }
        }
        if (todosDigitosIguais) {
            return false;
        }

        // Calcular e verificar primeiro dígito verificador
        int[] multiplicadores1 = { 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2 };
        int soma = 0;
        for (int i = 0; i < 12; i++) {
            soma += (cnpj.charAt(i) - '0') * multiplicadores1[i];
        }
        int resto = soma % 11;
        int dv1 = (resto < 2) ? 0 : 11 - resto;
        if ((cnpj.charAt(12) - '0') != dv1) {
            return false;
        }

        // Calcular e verificar segundo dígito verificador
        int[] multiplicadores2 = { 6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2 };
        soma = 0;
        for (int i = 0; i < 13; i++) {
            soma += (cnpj.charAt(i) - '0') * multiplicadores2[i];
        }
        resto = soma % 11;
        int dv2 = (resto < 2) ? 0 : 11 - resto;
        if ((cnpj.charAt(13) - '0') != dv2) {
            return false;
        }

        return true;
    }

    private boolean validarChaveAleatoria(String chaveAleatoria) {
        // Regra: Exatamente 36 caracteres alfanuméricos, sem pontuação.
        if (chaveAleatoria == null || chaveAleatoria.length() != 36) { // Tamanho exato
            return false;
        }
        // Verifica se contém apenas caracteres alfanuméricos
        if (!chaveAleatoria.matches("^[a-zA-Z0-9]+$")) {
            return false;
        }
        return true;
    }
}