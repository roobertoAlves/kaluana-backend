package br.edu.ifsp.kaluana.auth.dto;

public record AuthResponse(String accessToken, UserResponse user) {}
