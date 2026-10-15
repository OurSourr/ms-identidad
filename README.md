
# Microservicio de Identidad - Our Sour

## 1. Descripción

Microservicio responsable de la autenticación, autorización y gestión de identidad de los usuarios de la plataforma Our Sour.

Permite registrar cuentas, iniciar sesión, emitir tokens JWT y controlar el acceso a recursos protegidos mediante roles.

Forma parte de una arquitectura de microservicios que contempla un API Gateway como punto de entrada al sistema.

## 2. Tecnologías

- Java 25
- Spring Boot 4.1.1
- Spring Security
- Spring Data JPA / Hibernate
- JWT (JSON Web Token)
- BCrypt
- MySQL 8.4
- Maven
- JUnit 5 y Mockito

## 3. Arquitectura

El proyecto utiliza una organización por responsabilidades:

- `controller`: endpoints HTTP.
- `services`: lógica de negocio y autenticación.
- `repository`: acceso a datos mediante Spring Data JPA.
- `entity`: entidades persistentes.
- `dto`: objetos utilizados para las solicitudes y respuestas.
- `security`: configuración de seguridad y JWT.
- `exception`: gestión centralizada de excepciones.

El microservicio utiliza autenticación sin estado (stateless). Las solicitudes protegidas requieren un JWT válido.

## 4. Funcionalidades implementadas

- Registro de cuentas de usuario.
- Inicio de sesión mediante correo electrónico y contraseña.
- Almacenamiento seguro de contraseñas con BCrypt.
- Generación de tokens JWT firmados con HS256.
- Validación de tokens JWT.
- Autorización basada en roles.
- Protección de endpoints.
- Comprobación del estado activo de las cuentas.
- Manejo centralizado de errores.
- Persistencia de cuentas y roles en MySQL.

## 5. Roles

El sistema contempla los siguientes roles:

| Rol | Descripción |
| --- | --- |
| ROLE_CLIENTE | Usuario registrado de la plataforma. |
| ROLE_ADMIN | Usuario con permisos administrativos. |

## 6. Endpoints

URL base local: `http://localhost:8081`

| Método | Endpoint | Descripción |
| --- | --- | --- |
| POST | `/api/auth/registro` | Registrar una cuenta. |
| POST | `/api/auth/login` | Autenticar y obtener un JWT. |
| GET | `/api/auth/me` | Consultar la identidad autenticada. |
| GET | `/api/auth/admin-test` | Comprobar acceso administrativo. |

Los endpoints protegidos requieren el encabezado:

Authorization: Bearer TOKEN_JWT

Los endpoints de registro y login son públicos.

## 7. Base de datos

Motor: MySQL 8.4.

Base de datos utilizada:

`our_sour_identidad`

El modelo de persistencia incluye cuentas y roles.

La configuración de conexión se encuentra en:

`src/main/resources/application.properties`

## 8. Variables de entorno

El proyecto utiliza variables de entorno para evitar almacenar credenciales directamente en el código fuente.

| Variable | Descripción |
| --- | --- |
| DB_PASSWORD | Contraseña de conexión a MySQL. |
| JWT_SECRET | Clave secreta utilizada para firmar tokens JWT. |

La clave JWT debe mantenerse privada y tener suficiente entropía para HS256.

Ejemplo de configuración en PowerShell:

```powershell
$env:DB_PASSWORD = Read-Host "Contraseña de MySQL"
```

Para generar una clave JWT aleatoria:

```powershell
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$bytes = New-Object byte[] 32
$rng.GetBytes($bytes)
$env:JWT_SECRET = [Convert]::ToBase64String($bytes)
$rng.Dispose()
```

Las variables deben estar disponibles en la terminal desde la que se ejecuta la aplicación.

## 9. Ejecución local

Requisitos:

- JDK 25.
- MySQL 8.4.
- Base de datos `our_sour_identidad`.
- Variables de entorno configuradas.

Ejecutar mediante Maven Wrapper:

```powershell
.\mvnw.cmd spring-boot:run
```

El servicio inicia en:

`http://localhost:8081`

## 10. Pruebas automatizadas

El proyecto utiliza JUnit 5 y Mockito.

Incluye pruebas de:

- Inicio de sesión exitoso.
- Contraseña incorrecta.
- Cuenta inexistente.
- Cuenta desactivada.
- Registro exitoso.
- Registro con correo duplicado.
- Ausencia del rol requerido para registrar.
- Generación de JWT y validación de sus claims.
- Inicio del contexto de Spring Boot.

Ejecutar todas las pruebas:

```powershell
.\mvnw.cmd test
```

Estado de la última ejecución verificada:

**9 pruebas ejecutadas, 0 fallos, 0 errores.**

La prueba de contexto requiere una conexión disponible a la base de datos configurada.

## 11. Generación del archivo JAR

Compilar, ejecutar las pruebas y generar el artefacto:

```powershell
.\mvnw.cmd clean package
```

Archivo generado:

`target/ms-identidad-0.0.1-SNAPSHOT.jar`

Ejecutar:

```powershell
java -jar target/ms-identidad-0.0.1-SNAPSHOT.jar
```

La ejecución del JAR fue verificada correctamente en el puerto 8081.

## 12. Seguridad

- Las contraseñas se almacenan mediante BCrypt.
- Los tokens JWT utilizan firma HS256.
- La expiración configurada de los tokens es de 60 minutos.
- El sistema utiliza sesiones sin estado.
- Se verifica la autorización basada en roles.
- Se comprueba el estado de la cuenta en las solicitudes autenticadas.
- Las credenciales y la clave JWT se configuran fuera del código fuente.

## 13. Estado del proyecto

El núcleo de autenticación y autorización está implementado y probado.

El microservicio continúa en desarrollo para su integración con los demás componentes de Our Sour y el API Gateway.
