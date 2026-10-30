package com.bank.domain.model;

/**
 * Documentos de identificación aceptados. No incluye tarjeta de identidad (TI)
 * porque es un documento de menores de edad, y un menor no puede ser cliente.
 */
public enum IdentificationType {
    CC, // Cédula de ciudadanía
    CE, // Cédula de extranjería
    PA  // Pasaporte
}
