package com.duoc.authapi.service;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.model.*;
import com.duoc.authapi.config.AwsCognitoConfig;
import com.duoc.authapi.dto.LoginRequest;
import com.duoc.authapi.dto.LoginResponse;
import com.duoc.authapi.dto.RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class CognitoService {

    private final AWSCognitoIdentityProvider cognitoClient;
    private final AwsCognitoConfig awsCognitoConfig;

    /**
     * Registra al usuario en Cognito. Devuelve el "sub" (UUID único)
     * que se usará como clave primaria en la tabla MySQL.
     */
    public String signUp(RegisterRequest request) {
        SignUpRequest signUpRequest = new SignUpRequest()
                .withClientId(awsCognitoConfig.getClientId())
                .withUsername(request.getEmail())
                .withPassword(request.getPassword())
                .withUserAttributes(
                        new AttributeType().withName("email").withValue(request.getEmail()),
                        new AttributeType().withName("name").withValue(request.getNombre())
                );

        applySecretHashIfNeeded(signUpRequest::withSecretHash, request.getEmail());

        SignUpResult result = cognitoClient.signUp(signUpRequest);
        return result.getUserSub();
    }

    /**
     * Confirma el registro con el código de 6 dígitos que Cognito
     * envía automáticamente al correo del usuario.
     */
    public void confirmSignUp(String email, String codigo) {
        ConfirmSignUpRequest confirmRequest = new ConfirmSignUpRequest()
                .withClientId(awsCognitoConfig.getClientId())
                .withUsername(email)
                .withConfirmationCode(codigo);

        applySecretHashIfNeeded(confirmRequest::withSecretHash, email);

        cognitoClient.confirmSignUp(confirmRequest);
    }

    /**
     * Autentica contra Cognito con el flujo USER_PASSWORD_AUTH
     * (debe estar habilitado en el App Client) y devuelve los tokens JWT.
     */
    public LoginResponse login(LoginRequest request) {
        Map<String, String> authParams = new HashMap<>();
        authParams.put("USERNAME", request.getEmail());
        authParams.put("PASSWORD", request.getPassword());

        String secretHash = buildSecretHash(request.getEmail());
        if (secretHash != null) {
            authParams.put("SECRET_HASH", secretHash);
        }

        InitiateAuthRequest authRequest = new InitiateAuthRequest()
                .withAuthFlow(AuthFlowType.USER_PASSWORD_AUTH)
                .withClientId(awsCognitoConfig.getClientId())
                .withAuthParameters(authParams);

        InitiateAuthResult result = cognitoClient.initiateAuth(authRequest);
        AuthenticationResultType tokens = result.getAuthenticationResult();

        return new LoginResponse(
                tokens.getAccessToken(),
                tokens.getIdToken(),
                tokens.getRefreshToken(),
                tokens.getExpiresIn()
        );
    }

    // --- Soporte para App Clients configurados con "client secret" ---

    private interface SecretHashSetter {
        void set(String hash);
    }

    private void applySecretHashIfNeeded(SecretHashSetter setter, String username) {
        String hash = buildSecretHash(username);
        if (hash != null) {
            setter.set(hash);
        }
    }

    private String buildSecretHash(String username) {
        String clientSecret = awsCognitoConfig.getClientSecret();
        if (clientSecret == null || clientSecret.isBlank()) {
            return null; // App Client sin secret: no se envía SECRET_HASH
        }
        try {
            String message = username + awsCognitoConfig.getClientId();
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(clientSecret.getBytes(), "HmacSHA256"));
            byte[] rawHmac = mac.doFinal(message.getBytes());
            return Base64.getEncoder().encodeToString(rawHmac);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo calcular el SECRET_HASH de Cognito", e);
        }
    }
}
