# auth-api

API de **login y registro de usuario**, primera pieza del backend.

**Stack:** Java 21 · Spring Boot 3 · Spring Security (Resource Server OAuth2/JWT) ·
Spring Data JPA + Hibernate · MySQL 8.4 · Amazon Cognito · Maven · Lombok.

## Arquitectura

Cognito es el **proveedor de identidad**: emite y firma los JWT. Esta API
**no genera tokens propios**, solo:

1. Se comunica con Cognito para registrar/confirmar/loguear usuarios
   (vía AWS SDK, clase `CognitoService`).
2. Valida los JWT que Cognito emite en cada request protegido, usando
   Spring Security configurado como *Resource Server* (`SecurityConfig`).
3. Guarda el perfil de negocio del usuario en MySQL, enlazado por el
   `sub` de Cognito (entidad `Usuario`).

```
POST /auth/register  → crea usuario en Cognito + fila en MySQL (público)
POST /auth/confirm   → confirma el código enviado al email       (público)
POST /auth/login     → autentica, devuelve accessToken/idToken   (público)
GET  /users/me       → perfil del usuario autenticado            (requiere Bearer token)
```

## Requisitos previos

- JDK 21
- Maven 3.9+
- MySQL 8.4 corriendo localmente (o accesible por red)
- Una cuenta AWS con un **User Pool de Cognito** y un **App Client** creado
  (ver checklist más abajo)
- Credenciales AWS con permisos sobre Cognito (`AWS_ACCESS_KEY_ID` / `AWS_SECRET_ACCESS_KEY`)

## Checklist de configuración en AWS Cognito

1. Cognito → User pools → **Create user pool**.
   - Sign-in option: **email**.
   - Password policy según lo que exija tu rúbrica.
   - MFA: off (para simplificar).
2. Dentro del pool, crear un **App client**:
   - Sin "generate client secret" (más simple), o con secret si tu
     evaluación lo pide (el código ya soporta ambos casos).
   - En **Auth Flows**, habilitar `ALLOW_USER_PASSWORD_AUTH`.
3. Copiar `User Pool ID`, `App Client ID` y la región.

## Configuración local

1. Copia `.env.example` como `.env` (o define las variables en tu IDE /
   sistema operativo) y completa los valores reales:
   - `DB_PASSWORD`
   - `AWS_REGION`, `AWS_USER_POOL_ID`, `AWS_CLIENT_ID`, `AWS_CLIENT_SECRET`
   - `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`
2. Asegúrate de que MySQL esté corriendo. La base `auth_db` se crea sola
   (`createDatabaseIfNotExist=true` en la URL JDBC) y la tabla `usuarios`
   se crea sola gracias a `ddl-auto=update`. `schema.sql` queda solo como
   referencia por si prefieres crearla a mano.

## Ejecutar

```bash
mvn spring-boot:run
```

La API queda en `http://localhost:8080`.

## Probar con Postman

Importa `postman/auth-api.postman_collection.json`. Trae 4 requests en
orden (register → confirm → login → me) y un script que guarda el
`accessToken` del login como variable de colección automáticamente,
para que `me` lo use sin copiarlo a mano.

## Notas para el informe / defensa

- La contraseña **nunca** se persiste en MySQL: vive únicamente en Cognito.
- El enlace entre Cognito y la base de datos de negocio es el claim `sub`
  del JWT (UUID inmutable), no el email (que en teoría podría cambiar).
- `SecurityConfig` no valida el token "a mano": Spring, con la
  `issuer-uri` de Cognito, descarga automáticamente las llaves públicas
  (JWKS) y valida firma + expiración + issuer por ti.
