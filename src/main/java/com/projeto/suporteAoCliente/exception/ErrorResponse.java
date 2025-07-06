package com.projeto.suporteAoCliente.exception;

import java.time.LocalDateTime;

// Essa classe representa o formato padronizado do erro
public class ErrorResponse {
    private int status; // Código HTTP(400, 404, 500 etc.)
    private String error; // Tipo de erro(Bad Request, Not found...)
    private String message; // Mensagem amigável sobre o erro
    private String path; // Caminho da requisição que gerou o erro
    private LocalDateTime timestamp; // momento do erro

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public ErrorResponse(int status, String error, String message, String path) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.timestamp = LocalDateTime.now();
    }
}
