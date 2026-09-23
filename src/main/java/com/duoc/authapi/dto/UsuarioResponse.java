package com.duoc.authapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponse {

    private String cognitoSub;
    private String email;
    private String nombre;
    private boolean emailConfirmado;
    private LocalDateTime fechaRegistro;
}
