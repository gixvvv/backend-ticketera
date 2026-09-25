package com.duoc.authapi.controller;

import com.duoc.authapi.dto.UsuarioResponse;
import com.duoc.authapi.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    /**
     * Obtiene el perfil del usuario autenticado.
     *
     * Los datos personales se obtienen desde MySQL.
     * Los roles se obtienen desde el claim "cognito:groups"
     * presente en el Access Token de Cognito.
     */
    @GetMapping("/me")
    public ResponseEntity<UsuarioResponse> me(
            @AuthenticationPrincipal Jwt jwt) {

        String cognitoSub = jwt.getSubject();

        // Perfil almacenado en MySQL
        UsuarioResponse usuario =
                usuarioService.obtenerPerfil(cognitoSub);

        // Roles/grupos enviados por Cognito
        List<String> roles =
                jwt.getClaimAsStringList("cognito:groups");

        if (roles == null) {
            roles = Collections.emptyList();
        }

        // Normalizamos a mayúsculas
        roles = roles.stream()
                .map(String::toUpperCase)
                .toList();

        usuario.setRoles(roles);

        return ResponseEntity.ok(usuario);
    }
}