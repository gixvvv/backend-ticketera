package com.duoc.authapi.config;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configura el cliente del SDK de AWS para hablar con Cognito
 * (SignUp, ConfirmSignUp, InitiateAuth) y expone los IDs del
 * User Pool / App Client que necesitan los services.
 *
 * Las credenciales de AWS (access key / secret key) NO se hardcodean:
 * se resuelven vía DefaultAWSCredentialsProviderChain, es decir,
 * variables de entorno AWS_ACCESS_KEY_ID / AWS_SECRET_ACCESS_KEY,
 * o el perfil configurado en ~/.aws/credentials.
 */
@Configuration
@Getter
public class AwsCognitoConfig {

    @Value("${aws.region}")
    private String region;

    @Value("${aws.user-pool-id}")
    private String userPoolId;

    @Value("${aws.client-id}")
    private String clientId;

    @Value("${aws.client-secret:}")
    private String clientSecret;

    @Bean
    public AWSCognitoIdentityProvider cognitoIdentityProvider() {
        return AWSCognitoIdentityProviderClientBuilder.standard()
                .withRegion(region)
                .withCredentials(new DefaultAWSCredentialsProviderChain())
                .build();
    }
}
