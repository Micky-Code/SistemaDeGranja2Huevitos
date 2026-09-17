# Autenticación con sesión y cookie

La API usa Spring Security, BCrypt y sesiones del servidor. La cookie `JSESSIONID` solo contiene el identificador. Los endpoints de negocio y las pruebas de excepciones requieren sesión. No se expone registro público de usuarios ni se aceptan contraseñas guardadas en texto plano.

## Primer usuario

En la configuración de ejecución del IDE, establecer estas variables de entorno antes de arrancar:

```text
BOOTSTRAP_ADMIN_ENABLED=true
BOOTSTRAP_ADMIN_USERNAME=<usuario elegido>
BOOTSTRAP_ADMIN_PASSWORD=<contraseña elegida de al menos 12 caracteres y como máximo 72 bytes UTF-8>
```

El inicializador crea el rol `ADMIN` y el usuario con hash BCrypt únicamente si la tabla de usuarios está vacía. No cambia contraseñas existentes. Deshabilitarlo después de crear el usuario en una base persistente. Las contraseñas de otros usuarios también deben generarse con el bean `PasswordEncoder`.

La configuración actual usa H2 en memoria: los usuarios se pierden al apagar la aplicación. Para conservarlos, configurar una base persistente. Las sesiones actuales también viven en memoria y se pierden al reiniciar; un despliegue con varias instancias necesitará un almacén compartido de sesiones.

## Contrato HTTP

| Método | Ruta | Respuesta |
|---|---|---|
| GET | `/api/auth/csrf` | 200: `headerName` y `token`; conservar la cookie recibida |
| POST | `/api/auth/login` | JSON `username` y `password`; 200 con `idUsuario`, `username`, `rol` |
| GET | `/api/auth/me` | 200 con los datos del usuario autenticado; 401 sin sesión |
| POST | `/api/auth/logout` | 204, invalida sesión y elimina cookie |

Antes de cualquier POST, PUT, PATCH o DELETE, enviar el token en el encabezado que devuelve `/csrf` (`X-CSRF-TOKEN`). También se exige para login y logout. Obtener un token nuevo después del login, porque el anterior se invalida. El logout lo gestiona el filtro estándar de Spring Security, configurado en `SecurityConfig`, no un método del controlador.

Credenciales incorrectas, usuario inexistente o inactivo devuelven el mismo 401. Un token CSRF ausente o inválido devuelve 403. Cuerpos inválidos devuelven 400. Los errores conservan los formatos del manejador global. Una petición GET protegida sin sesión devuelve 401; una mutación sin token CSRF válido puede devolver 403 antes de comprobar la sesión.

## Ejemplo para el frontend

```javascript
const api = 'http://localhost:8080';
async function obtenerCsrf() {
  const response = await fetch(`${api}/api/auth/csrf`, { credentials: 'include' });
  if (!response.ok) throw new Error('No se pudo obtener CSRF');
  return response.json();
}

let csrf = await obtenerCsrf();
const login = await fetch(`${api}/api/auth/login`, {
  method: 'POST', credentials: 'include',
  headers: { 'Content-Type': 'application/json', [csrf.headerName]: csrf.token },
  body: JSON.stringify({ username: usuarioIngresado, password: claveIngresada })
});
if (!login.ok) throw new Error('No se pudo iniciar sesión');
const usuario = await login.json();
csrf = await obtenerCsrf();

const me = await fetch(`${api}/api/auth/me`, { credentials: 'include' });
// Si me.status === 401, volver a la pantalla de login.

const logout = await fetch(`${api}/api/auth/logout`, {
  method: 'POST', credentials: 'include',
  headers: { [csrf.headerName]: csrf.token }
});
if (!logout.ok) throw new Error('No se pudo cerrar sesión');
// Antes del siguiente login, obtener otro token CSRF.
```

El navegador administra la cookie HttpOnly; no guardar contraseñas ni identificadores de sesión en localStorage. En Postman, habilitar su cookie jar y usar el mismo flujo CSRF.

## Configuración

| Variable | Predeterminado | Uso |
|---|---|---|
| `SESSION_TIMEOUT` | `30m` | Inactividad sin peticiones al servidor |
| `FRONTEND_ORIGINS` | `http://localhost:4200,http://localhost:5173` | Orígenes exactos separados por coma, sin barra final |
| `SESSION_COOKIE_SECURE` | `false` | Establecer `true` en producción con HTTPS |
| `SESSION_COOKIE_SAME_SITE` | `lax` | Para despliegues realmente entre sitios: `none` junto con HTTPS y Secure |

La cookie es HttpOnly y la sesión no se transmite por URL. No configurar `max-age`: es una cookie de sesión. Los navegadores pueden restaurar cookies al reabrirse, por eso el servidor controla la caducidad. Las consultas automáticas del frontend también renuevan la actividad. Ajustar el frontend para evitar polling si se busca vencer por inactividad del usuario.

Los roles se cargan al autenticar (`ROLE_ADMIN`, etc.). Para reglas futuras se puede usar `@PreAuthorize("hasRole('ADMIN')")` en los servicios. Actualmente todos los endpoints privados requieren autenticación; no se inventan permisos de ventas/inventario. Los cambios de rol o estado no revocan automáticamente sesiones ya existentes; `/me` comprueba el estado actual. Para revocación inmediata en todas las rutas se necesitará agregar gestión de sesiones de usuarios.

## Verificación

Ejecutar `./mvnw.cmd test` con Java 21. `AuthIntegrationTest` usa un servidor HTTP real y cookies para comprobar login, rotación de sesión, persistencia, CSRF, logout, rechazo de la cookie invalidada, CORS y errores de credenciales. La caducidad por inactividad está delegada al contenedor servlet y configurada en `application.properties`.

Referencias: [persistencia de autenticación](https://docs.spring.io/spring-security/reference/servlet/authentication/persistence.html) y [CSRF](https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html).
