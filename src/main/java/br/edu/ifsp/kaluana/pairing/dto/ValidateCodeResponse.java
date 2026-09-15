package br.edu.ifsp.kaluana.pairing.dto;

public record ValidateCodeResponse(
        boolean valid,
        String mainPersonName
) {
}
