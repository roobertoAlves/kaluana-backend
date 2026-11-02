package br.edu.ifsp.kaluana.pairing;

public record ValidateCodeResponseDTO(
        boolean valid,
        String mainPersonName
) {
}
