package br.edu.ifsp.kaluana.contact;

import java.util.UUID;

public record TrustedContactResponseDTO(
        UUID id,
        String name,
        ContactMethod method,
        String value
) {
    public static TrustedContactResponseDTO from(TrustedContact contact) {
        return new TrustedContactResponseDTO(contact.getId(), contact.getName(), contact.getMethod(), contact.getValue());
    }
}
