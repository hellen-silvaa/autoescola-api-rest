package br.com.fiap3esr.autoescola3esr.exception.type;

public class IntegracaoExternaException extends RuntimeException {
    public IntegracaoExternaException(String message) {
        super(message);
    }

    public IntegracaoExternaException(String message, Throwable cause) {
        super(message, cause);
    }
}
