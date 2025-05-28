package com.itau.case_pix.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Manipula erros de validação do Bean Validation (@NotNull, @Pattern, etc. )
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException ex) {
        // Em vez de retornar um mapa complexo, vamos pegar apenas o primeiro erro
        // para manter a resposta simples como você deseja
        String mensagemErro = "Erro de validação";

        if (!ex.getBindingResult().getAllErrors().isEmpty()) {
            FieldError primeiroErro = (FieldError) ex.getBindingResult().getAllErrors().get(0);
            mensagemErro = primeiroErro.getDefaultMessage(); // Usa a mensagem personalizada da anotação
        }

        // Retorna apenas a mensagem de erro como string, sem estrutura JSON complexa
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(mensagemErro);
    }

    // Manipula exceções lançadas manualmente no código (IllegalArgumentException)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex) {
        // Retorna a mensagem de erro como string simples
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ex.getMessage());
    }

    // Manipulador genérico para outras exceções não tratadas
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGenericException(Exception ex) {
        // Para exceções não esperadas, você pode escolher retornar uma mensagem
        // genérica
        // em vez de expor detalhes técnicos
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erro interno do servidor. Por favor, contate o suporte.");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        String mensagem = "Erro de formato: verifique se os campos numéricos contêm apenas números.";

        if (ex.getMessage().contains("NumberFormatException")) {
            mensagem = "Campo numérico contém caracteres inválidos. Use apenas dígitos.";
        } else if (ex.getMessage().contains("agencia")) {
            mensagem = "O campo 'numeroAgencia' deve conter apenas números.";
        } else if (ex.getMessage().contains("conta")) {
            mensagem = "O campo 'numeroConta' deve conter apenas números.";
        }

        if (ex.getMessage().contains("Unrecognized field") ||
                ex.getMessage().contains("Unknown property")) {
            mensagem = "Campos não permitidos no JSON de entrada. Apenas os campos definidos no DTO são permitidos.";
        }

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(mensagem);
    }
}