package com.duoc.authapi.controller;

import com.duoc.authapi.dto.UsuarioResponse;
import com.duoc.authapi.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    /**
     * Ruta protegida: Spring Security ya validó el JWT antes de llegar acá
     * (firma, expiración, issuer). @AuthenticationPrincipal Jwt te da
     * acceso directo a los claims del token, entre ellos "sub".
     */
    @GetMapping("/me")
    public ResponseEntity<UsuarioResponse> me(@AuthenticationPrincipal Jwt jwt) {
        String cognitoSub = jwt.getSubject();
        return ResponseEntity.ok(usuarioService.obtenerPerfil(cognitoSub));
    }
}
