package com.granja.dos.huevitos;

import static org.junit.jupiter.api.Assertions.*;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.granja.dos.huevitos.models.Rol;
import com.granja.dos.huevitos.models.Usuario;
import com.granja.dos.huevitos.repository.RolRepository;
import com.granja.dos.huevitos.repository.UsuarioRepository;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"app.bootstrap.enabled=false", "spring.datasource.url=jdbc:h2:mem:auth-test;INIT=CREATE SCHEMA IF NOT EXISTS AVICOLA",
                "server.servlet.session.cookie.secure=false"})
class AuthIntegrationTest {
    @LocalServerPort int port;
    @Autowired UsuarioRepository usuarios;
    @Autowired RolRepository roles;
    @Autowired PasswordEncoder encoder;
    @Autowired ObjectMapper mapper;
    private HttpClient client;
    private CookieManager cookies;
    private static final String LOGIN = "{\"username\":\"tester\",\"password\":\"Password-test-123\"}";

    @BeforeEach
    void preparar() {
        usuarios.deleteAll();
        roles.deleteAll();
        var rol = new Rol();
        rol.setNombre("ADMIN");
        rol.setCreat(LocalDateTime.now());
        roles.save(rol);
        var usuario = new Usuario();
        usuario.setUsername("tester");
        usuario.setPassword(encoder.encode("Password-test-123"));
        usuario.setRol(rol);
        usuario.setCreat(LocalDateTime.now());
        usuarios.save(usuario);
        cookies = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        client = HttpClient.newBuilder().cookieHandler(cookies).build();
    }

    private URI uri(String path) { return URI.create("http://localhost:" + port + path); }
    private HttpResponse<String> get(String path) throws Exception {
        return client.send(HttpRequest.newBuilder(uri(path)).GET().build(), HttpResponse.BodyHandlers.ofString());
    }
    private HttpResponse<String> post(String path, String json, String token) throws Exception {
        var builder = HttpRequest.newBuilder(uri(path)).header("Content-Type", "application/json");
        if (token != null) builder.header("X-CSRF-TOKEN", token);
        return client.send(builder.POST(HttpRequest.BodyPublishers.ofString(json)).build(), HttpResponse.BodyHandlers.ofString());
    }
    private String csrf() throws Exception {
        var response = get("/api/auth/csrf");
        assertEquals(200, response.statusCode());
        return mapper.readTree(response.body()).get("token").asText();
    }
    private String sessionId() {
        return cookies.getCookieStore().getCookies().stream().filter(c -> c.getName().equals("JSESSIONID"))
                .findFirst().orElseThrow().getValue();
    }

    @Test
    void flujoCompletoConCookieRealYRotacion() throws Exception {
        assertEquals(200, get("/login").statusCode());
        assertEquals(401, get("/menu").statusCode());
        assertEquals(401, get("/api/auth/me").statusCode());
        String token = csrf();
        String anonymousId = sessionId();
        var login = post("/api/auth/login", LOGIN, token);
        assertEquals(200, login.statusCode(), login.body());
        assertNotEquals(anonymousId, sessionId());
        assertTrue(login.headers().allValues("set-cookie").stream()
                .anyMatch(c -> c.contains("HttpOnly") && c.contains("SameSite=Lax")));
        var body = mapper.readTree(login.body());
        assertEquals("tester", body.get("username").asText());
        assertEquals("ADMIN", body.get("rol").asText());
        assertFalse(body.has("password"));
        assertEquals(200, get("/api/auth/me").statusCode());
        var menu = get("/menu");
        assertEquals(200, menu.statusCode());
        assertTrue(menu.body().contains("tester"));
        assertTrue(menu.body().contains("Producción"));
        assertEquals(403, post("/api/auth/logout", "", token).statusCode());
        String authenticatedId = sessionId();
        assertEquals(204, post("/api/auth/logout", "", csrf()).statusCode());
        assertEquals(401, get("/api/auth/me").statusCode());
        assertEquals(401, get("/menu").statusCode());
        var stale = HttpClient.newHttpClient().send(HttpRequest.newBuilder(uri("/api/auth/me"))
                .header("Cookie", "JSESSIONID=" + authenticatedId).GET().build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(401, stale.statusCode());
    }

    @Test
    void rechazaLoginSinCsrf() throws Exception {
        assertEquals(403, post("/api/auth/login", LOGIN, null).statusCode());
        assertEquals(401, get("/api/auth/me").statusCode());
    }

    @Test
    void credencialesIncorrectasYUsuarioInexistenteRespondenIgual() throws Exception {
        String token = csrf();
        var wrong = post("/api/auth/login", LOGIN.replace("Password-test-123", "incorrecta"), token);
        var unknown = post("/api/auth/login", LOGIN.replace("tester", "inexistente"), token);
        assertEquals(401, wrong.statusCode());
        assertEquals(401, unknown.statusCode());
        assertEquals(mapper.readTree(wrong.body()).get("mensaje"), mapper.readTree(unknown.body()).get("mensaje"));
        assertEquals(401, get("/api/auth/me").statusCode());
    }

    @Test
    void usuarioInactivoNoIniciaSesion() throws Exception {
        var usuario = usuarios.findByUsername("tester").orElseThrow();
        usuario.setEstado(false);
        usuarios.save(usuario);
        assertEquals(401, post("/api/auth/login", LOGIN, csrf()).statusCode());
        assertEquals(401, get("/api/auth/me").statusCode());
    }

    @Test
    void loginValidaJsonYCampos() throws Exception {
        String token = csrf();
        assertEquals(400, post("/api/auth/login", "{}", token).statusCode());
        assertEquals(400, post("/api/auth/login", "{", token).statusCode());
    }

    @Test
    void endpointsDeNegocioRequierenSesion() throws Exception {
        var response = get("/api/pruebas/excepciones/no-encontrado");
        assertEquals(401, response.statusCode());
        assertEquals(401, mapper.readTree(response.body()).get("status").asInt());
    }

    @Test
    void corsPermiteSoloOrigenConfigurado() throws Exception {
        for (String origin : new String[] {"http://localhost:5173", "https://no-permitido.example"}) {
            var request = HttpRequest.newBuilder(uri("/api/auth/login"))
                    .header("Origin", origin).header("Access-Control-Request-Method", "POST")
                    .header("Access-Control-Request-Headers", "content-type,x-csrf-token")
                    .method("OPTIONS", HttpRequest.BodyPublishers.noBody()).build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (origin.contains("localhost")) {
                assertEquals(200, response.statusCode());
                assertEquals(origin, response.headers().firstValue("access-control-allow-origin").orElseThrow());
                assertEquals("true", response.headers().firstValue("access-control-allow-credentials").orElseThrow());
            } else {
                assertEquals(403, response.statusCode());
            }
        }
    }
}
