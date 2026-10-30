package com.bank.domain.model;

/**
 * Número de cuenta: 10 dígitos que empiezan en 53 (ahorros) o 33 (corriente).
 * Como el constructor valida el formato, no puede existir un AccountNumber inválido.
 */
public record AccountNumber(String value) {

    private static final String FORMAT = "^(53|33)\\d{8}$";
    private static final long MAX_SEQUENCE = 99_999_999L; // 8 dígitos después del prefijo

    public AccountNumber {
        if (value == null || !value.matches(FORMAT)) {
            throw new IllegalArgumentException("Número de cuenta inválido: " + value);
        }
    }

    /**
     * Arma el número con el prefijo del tipo y un consecutivo de 8 dígitos rellenado con ceros.
     * Ejemplo: ahorros + consecutivo 7 = "53" + "00000007" = "5300000007".
     */
    public static AccountNumber generate(AccountType type, long sequence) {
        if (sequence < 1 || sequence > MAX_SEQUENCE) {
            throw new IllegalArgumentException("Consecutivo fuera de rango para números de cuenta: " + sequence);
        }
        return new AccountNumber(type.numberPrefix() + String.format("%08d", sequence));
    }

    public boolean belongsTo(AccountType type) {
        return value.startsWith(type.numberPrefix());
    }
}
