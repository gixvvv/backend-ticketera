package com.duoc.authapi.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Perfil de negocio del usuario. La contraseña NUNCA se guarda acá:
 * la administra Cognito. Esta tabla se enlaza con Cognito a través
 * del "sub" (identificador único e inmutable que entrega el User Pool).
 */
@Entity
@Table(name = "usuarios")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @Column(name = "cognito_sub", length = 36)
    private String cognitoSub;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String nombre;

    @Column(name = "email_confirmado", nullable = false)
    private boolean emailConfirmado;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;
}
