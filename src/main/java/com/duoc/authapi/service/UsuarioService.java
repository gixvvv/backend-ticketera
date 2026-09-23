package com.duoc.authapi.service;

import com.duoc.authapi.dto.*;
import com.duoc.authapi.entity.Usuario;
import com.duoc.authapi.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final CognitoService cognitoService;
    private final UsuarioRepository usuarioRepository;

    /**
     * 1) Crea el usuario en Cognito (queda UNCONFIRMED hasta que
     *    verifique el código enviado a su correo).
     * 2) Guarda el perfil de negocio en MySQL, enlazado por el "sub".
     */
    @Transactional
    public UsuarioResponse registrar(RegisterRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new IllegalStateException("Ya existe un usuario registrado con ese email");
        }

        String cognitoSub = cognitoService.signUp(request);

        Usuario usuario = Usuario.builder()
                .cognitoSub(cognitoSub)
                .email(request.getEmail())
                .nombre(request.getNombre())
                .emailConfirmado(false)
                .fechaRegistro(LocalDateTime.now())
                .build();

        usuarioRepository.save(usuario);

        return mapToResponse(usuario);
    }

    /**
     * Confirma el código en Cognito y sincroniza el flag en MySQL.
     */
    @Transactional
    public void confirmar(ConfirmRequest request) {
        cognitoService.confirmSignUp(request.getEmail(), request.getCodigo());

        usuarioRepository.findByEmail(request.getEmail()).ifPresent(usuario -> {
            usuario.setEmailConfirmado(true);
            usuarioRepository.save(usuario);
        });
    }

    public LoginResponse login(LoginRequest request) {
        return cognitoService.login(request);
    }

    /**
     * Recupera el perfil del usuario autenticado a partir del "sub"
     * extraído del JWT (ver UsuarioController).
     */
    public UsuarioResponse obtenerPerfil(String cognitoSub) {
        Usuario usuario = usuarioRepository.findById(cognitoSub)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado en la base de datos local"));
        return mapToResponse(usuario);
    }

    private UsuarioResponse mapToResponse(Usuario usuario) {
        return UsuarioResponse.builder()
                .cognitoSub(usuario.getCognitoSub())
                .email(usuario.getEmail())
                .nombre(usuario.getNombre())
                .emailConfirmado(usuario.isEmailConfirmado())
                .fechaRegistro(usuario.getFechaRegistro())
                .build();
    }
}
