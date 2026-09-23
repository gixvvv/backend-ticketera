-- Script referencial. No es obligatorio ejecutarlo a mano:
-- con spring.jpa.hibernate.ddl-auto=update, Hibernate crea/actualiza
-- la tabla automáticamente al levantar la app. Úsalo si prefieres
-- controlar el DDL manualmente o mostrarlo en tu informe.

CREATE DATABASE IF NOT EXISTS auth_db
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE auth_db;

CREATE TABLE IF NOT EXISTS usuarios (
    cognito_sub      VARCHAR(36)  NOT NULL PRIMARY KEY,
    email            VARCHAR(255) NOT NULL UNIQUE,
    nombre           VARCHAR(255) NOT NULL,
    email_confirmado BOOLEAN      NOT NULL DEFAULT FALSE,
    fecha_registro   DATETIME     NOT NULL
);
