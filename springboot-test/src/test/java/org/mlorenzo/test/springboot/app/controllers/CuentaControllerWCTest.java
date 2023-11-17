package org.mlorenzo.test.springboot.app.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mlorenzo.test.springboot.app.models.entities.Cuenta;
import org.mlorenzo.test.springboot.app.models.TransaccionDto;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.*;

// "spring.jpa.properties.hibernate.hbm2ddl.import_files=" -> Una forma para prevenir que Hibernate ejecute el script
// "import.sql" automáticamente. La idea es ejecutar ese script antes de cada test y no una sóla vez para todos los test.
@Tag("integracion_wc")
// Ejecuta automáticamente el script "import.sql" antes de cada test
@Sql(value = "/import.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@SpringBootTest(webEnvironment = RANDOM_PORT, properties = "spring.jpa.properties.hibernate.hbm2ddl.import_files=")
class CuentaControllerWCTest {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    ObjectMapper objectMapper;

    @AfterEach
    void tearDown(@Autowired JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("TRUNCATE TABLE cuentas RESTART IDENTITY" );
    }

    @Test
    void testTransferir() throws JsonProcessingException {
        TransaccionDto dto = new TransaccionDto();
        dto.setCuentaOrigenId(1L);
        dto.setCuentaDestinoId(2L);
        dto.setBancoId(1L);
        dto.setMonto(new BigDecimal("100"));
        Map<String, Object> response = new HashMap<>();
        response.put("date", LocalDate.now().toString());
        response.put("status", "OK");
        response.put("mensaje", "Transferencia realizada con éxito!");
        response.put("transaccion", dto);
        webTestClient.post().uri("/api/cuentas/transferir")
                .bodyValue(dto)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                // Por defecto, si no se especifica un tipo de dato, el cuerpo de la respuesta se obtiene como un array
                // de bytes.
                .expectBody()
                // Podemos realizar las verificaciones usando el método "consumeWith" con una función lambda
                .consumeWith(resp -> {
                    try {
                        JsonNode json = objectMapper.readTree(resp.getResponseBody());
                        assertEquals("Transferencia realizada con éxito!", json.path("mensaje").asText());
                        assertEquals(1L, json.path("transaccion").path("cuentaOrigenId").asLong());
                        assertEquals(LocalDate.now().toString(), json.path("date").asText());
                        assertEquals("100", json.path("transaccion").path("monto").asText());
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                })
                // O bien, podemos realizar las verificaciones de forma nativa usando métodos "jsonPath"
                .jsonPath("$.mensaje").isNotEmpty()
                // Varias formas de validar la propiedad "mensaje" del JSON
                .jsonPath("$.mensaje").value(is("Transferencia realizada con éxito!"))
                .jsonPath("$.mensaje").value(valor -> assertEquals("Transferencia realizada con éxito!", valor))
                .jsonPath("$.mensaje").isEqualTo("Transferencia realizada con éxito!")
                .jsonPath("$.transaccion.cuentaOrigenId").isEqualTo(dto.getCuentaOrigenId())
                .jsonPath("$.date").isEqualTo(LocalDate.now().toString())
                // Verifica que el cuerpo de la respuesta sea el JSON del objeto "respuesta"
                .json(objectMapper.writeValueAsString(response));
    }

    @Test
    void testDetalle() throws JsonProcessingException {
        Cuenta cuenta = new Cuenta(1L, "Andrés", new BigDecimal("1000"));
        webTestClient.get().uri("/api/cuentas/1")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                // Por defecto, si no se especifica un tipo de dato, el cuerpo de la respuesta se obtiene como un array de bytes
                .expectBody()
                .jsonPath("$.persona").isEqualTo("Andrés")
                .jsonPath("$.saldo").isEqualTo(1000)
                // Verifica que el cuerpo de la respuesta sea el JSON del objeto "cuenta"
                .json(objectMapper.writeValueAsString(cuenta));
    }

    @Test
    void testDetalle2() {
        webTestClient.get().uri("/api/cuentas/2")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                // Por defecto, si no se especifica un tipo de dato, el cuerpo de la respuesta se obtiene como un array
                // de bytes. En esta caso, el cuerpo de la respuesta se obtiene como un objeto de tipo Cuenta.
                .expectBody(Cuenta.class)
                // Nota: El método "jsonPath" sólo funciona cuando el cuerpo de la respuesta se obtiene como un array de bytes.
                // No funciona el método "jsonPath" porque el cuerpo de la respuesta se obtiene como un objeto de tipo Cuenta y no como una array de bytes.
                //.jsonPath("$.persona").isEqualTo("Andrés")
                .consumeWith(resp -> {
                    Cuenta cuenta = resp.getResponseBody();
                    assertNotNull(cuenta);
                    assertEquals("Marie", cuenta.getPersona());
                    assertEquals("2000.00", cuenta.getSaldo().toPlainString());
                });
    }

    @Test
    void testListar() {
        webTestClient.get().uri("/api/cuentas")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                // Por defecto, si no se especifica un tipo de dato, el cuerpo de la respuesta se obtiene como un array
                // de bytes.
                .expectBody()
                .jsonPath("$[0].persona").isEqualTo("Andrés")
                .jsonPath("$[0].id").isEqualTo(1)
                .jsonPath("$[0].saldo").isEqualTo(1000)
                .jsonPath("$[1].persona").isEqualTo("Marie")
                .jsonPath("$[1].id").isEqualTo(2)
                .jsonPath("$[1].saldo").isEqualTo(2000)
                .jsonPath("$").isArray()
                .jsonPath("$").value(hasSize(2));
    }

    @Test
    void testListar2() {
        webTestClient.get().uri("/api/cuentas")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                // Por defecto, si no se especifica un tipo de dato, el cuerpo de la respuesta se obtiene como un array
                // de bytes. En esta caso, el cuerpo de la respuesta se obtiene como un listado de objetos de tipo Cuenta.
                .expectBodyList(Cuenta.class)
                // Nota: El método "jsonPath" sólo funciona cuando el cuerpo de la respuesta se obtiene como un array de bytes.
                // No funciona el método "jsonPath" porque el cuerpo de la respuesta se obtiene como un listado de
                // objetos de tipo Cuenta y no como una array de bytes.
                //.jsonPath("$[0].persona").isEqualTo("Andrés")
                .consumeWith(resp -> {
                   List<Cuenta> cuentas = resp.getResponseBody();
                   assertNotNull(cuentas);
                   assertEquals(2, cuentas.size());
                   assertEquals("Andrés", cuentas.get(0).getPersona());
                   assertEquals(1L, cuentas.get(0).getId());
                   assertEquals(1000, cuentas.get(0).getSaldo().intValue());
                   assertEquals("Marie", cuentas.get(1).getPersona());
                   assertEquals(2L, cuentas.get(1).getId());
                   assertEquals(2000, cuentas.get(1).getSaldo().intValue());
                })
                // Distintas formas de validar el tamaño de la lista
                .hasSize(2)
                .value(hasSize(2));
    }

    @Test
    void testGuardar() {
        Cuenta cuenta = new Cuenta(null, "Pepe", new BigDecimal("3000"));
        webTestClient.post().uri("/api/cuentas")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(cuenta)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                // Por defecto, si no se especifica un tipo de dato, el cuerpo de la respuesta se obtiene como
                // un array de bytes.
                .expectBody()
                .jsonPath("$.id").isEqualTo(3)
                .jsonPath("$.persona").isEqualTo("Pepe")
                .jsonPath("$.persona").value(is("Pepe"))
                .jsonPath("$.saldo").isEqualTo(3000);
    }

    @Test
    void testGuardar2() {
        Cuenta cuenta = new Cuenta(null, "Pepa", new BigDecimal("3500"));
        webTestClient.post().uri("/api/cuentas")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(cuenta)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                // Por defecto, si no se especifica un tipo de dato, el cuerpo de la respuesta se obtiene como un array
                // de bytes. En esta caso, el cuerpo de la respuesta se obtiene como un objeto de tipo Cuenta.
                .expectBody(Cuenta.class)
                // Nota: El método "jsonPath" sólo funciona cuando el cuerpo de la respuesta se obtiene como un array
                // de bytes.
                // No funciona el método "jsonPath" porque el cuerpo de la respuesta se obtiene como un objeto de tipo
                // Cuenta y no como una array de bytes.
                //.jsonPath("$.id").isEqualTo(3)
                .consumeWith(resp -> {
                    Cuenta c = resp.getResponseBody();
                    assertNotNull(c);
                    assertEquals(3L, c.getId());
                    assertEquals("Pepa", c.getPersona());
                    assertEquals("3500", c.getSaldo().toPlainString());
                });
    }

    @Test
    void testEliminar() {
        webTestClient.get().uri("/api/cuentas")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Cuenta.class)
                .hasSize(2);
        webTestClient.delete().uri("/api/cuentas/2")
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();
        webTestClient.get().uri("/api/cuentas")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Cuenta.class)
                .hasSize(1);
        webTestClient.get().uri("/api/cuentas/2")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody().isEmpty();
    }
}