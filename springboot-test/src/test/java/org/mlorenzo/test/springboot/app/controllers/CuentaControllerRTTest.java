package org.mlorenzo.test.springboot.app.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mlorenzo.test.springboot.app.models.entities.Cuenta;
import org.mlorenzo.test.springboot.app.models.TransaccionDto;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

// "spring.jpa.hibernate.ddl-auto=update" -> Para que Hibernate cree las tablas a partir de las entidades pero sin
// ejecutar el script "import.sql" automáticamente. La idea es ejecutar ese script en cada test que lo necesite y no
// una sóla vez para todos los test.
@Tag("integracion_rt")
@SpringBootTest(webEnvironment = RANDOM_PORT, properties = "spring.jpa.hibernate.ddl-auto=update")
class CuentaControllerRTTest {

    @Autowired
    TestRestTemplate client;

    @Autowired
    ObjectMapper objectMapper;

    @AfterEach
    void tearDown(@Autowired JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("TRUNCATE TABLE cuentas RESTART IDENTITY" );
    }

    @Sql("/import.sql")
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
        ResponseEntity<String> responseEntity = client.postForEntity("/api/cuentas/transferir",
                dto, String.class);
        String json = responseEntity.getBody();
        System.out.println(json);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, responseEntity.getHeaders().getContentType());
        assertNotNull(json);
        assertTrue(json.contains("Transferencia realizada con éxito!"));
        assertTrue(json.contains("\"cuentaOrigenId\":1,\"cuentaDestinoId\":2,\"monto\":100,\"bancoId\":1"));
        // Verifica que el cuerpo de la respuesta sea el JSON del objeto "response"
        assertEquals(objectMapper.writeValueAsString(response), json);
        // También podemos obtener un JsonNode a partir de un JSON en formato String para realizar verificaciones
        JsonNode jsonNode = objectMapper.readTree(json);
        assertEquals("Transferencia realizada con éxito!", jsonNode.path("mensaje").asText());
        assertEquals(LocalDate.now().toString(), jsonNode.path("date").asText());
        assertEquals("100", jsonNode.path("transaccion").path("monto").asText());
        assertEquals(1L, jsonNode.path("transaccion").path("cuentaOrigenId").asLong());
    }

    @Sql("/import.sql")
    @Test
    void testDetalle() {
        ResponseEntity<Cuenta> responseEntity = client.getForEntity("/api/cuentas/1", Cuenta.class);
        Cuenta cuenta = responseEntity.getBody();
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, responseEntity.getHeaders().getContentType());
        assertNotNull(cuenta);
        assertEquals("Andrés", cuenta.getPersona());
        assertEquals(1L, cuenta.getId());
        assertEquals("1000.00", cuenta.getSaldo().toPlainString());
        assertEquals(new Cuenta(1L, "Andrés", new BigDecimal("1000.00")), cuenta);
    }

    @Sql("/import.sql")
    @Test
    void testListar() throws JsonProcessingException {
        ResponseEntity<Cuenta[]> responseEntity = client.getForEntity("/api/cuentas", Cuenta[].class);
        List<Cuenta> cuentas = Arrays.asList(responseEntity.getBody());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, responseEntity.getHeaders().getContentType());
        assertNotNull(cuentas);
        assertEquals(2, cuentas.size());
        assertEquals("Andrés", cuentas.get(0).getPersona());
        assertEquals(1L, cuentas.get(0).getId());
        assertEquals("1000.00", cuentas.get(0).getSaldo().toPlainString());
        assertEquals("Marie", cuentas.get(1).getPersona());
        assertEquals(2L, cuentas.get(1).getId());
        assertEquals("2000.00", cuentas.get(1).getSaldo().toPlainString());
        // También podemos obtener un JsonNode a partir de un JSON en formato String para realizar verificaciones
        JsonNode jsonNode = objectMapper.readTree(objectMapper.writeValueAsString(cuentas));
        assertEquals("Andrés", jsonNode.get(0).path("persona").asText());
        assertEquals(1L, jsonNode.get(0).path("id").asLong());
        assertEquals("1000.0", jsonNode.get(0).path("saldo").asText());
        assertEquals("Marie", jsonNode.get(1).path("persona").asText());
        assertEquals(2L, jsonNode.get(1).path("id").asLong());
        assertEquals("2000.0", jsonNode.get(1).path("saldo").asText());
    }

    @Test
    void testGuardar() {
        Cuenta cuenta = new Cuenta(null, "Pepa", new BigDecimal("3800"));
        ResponseEntity<Cuenta> responseEntity = client.postForEntity("/api/cuentas", cuenta, Cuenta.class);
        Cuenta cuentaCreada = responseEntity.getBody();
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, responseEntity.getHeaders().getContentType());
        assertNotNull(cuentaCreada);
        assertEquals("Pepa", cuentaCreada.getPersona());
        assertEquals(1L, cuentaCreada.getId());
        assertEquals("3800", cuentaCreada.getSaldo().toPlainString());
    }

    // Usando el método "delete" del cliente
    @Sql("/import.sql")
    @Test
    void testEliminar() {
        ResponseEntity<Cuenta[]> responseEntity = client.getForEntity("/api/cuentas", Cuenta[].class);
        List<Cuenta> cuentas = Arrays.asList(responseEntity.getBody());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, responseEntity.getHeaders().getContentType());
        assertNotNull(cuentas);
        assertEquals(2, cuentas.size());
        client.delete("/api/cuentas/2");
        responseEntity = client.getForEntity("/api/cuentas", Cuenta[].class);
        cuentas = Arrays.asList(responseEntity.getBody());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, responseEntity.getHeaders().getContentType());
        assertNotNull(cuentas);
        assertEquals(1, cuentas.size());
        ResponseEntity<Cuenta> responseEntityDetalle = client.getForEntity("/api/cuentas/2", Cuenta.class);
        assertEquals(HttpStatus.NOT_FOUND, responseEntityDetalle.getStatusCode());
        assertFalse(responseEntityDetalle.hasBody());
    }

    // Usando el método "exchange" del cliente
    @Sql("/import.sql")
    @Test
    void testEliminar2() {
        ResponseEntity<Cuenta[]> responseEntity = client.getForEntity("/api/cuentas", Cuenta[].class);
        List<Cuenta> cuentas = Arrays.asList(responseEntity.getBody());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, responseEntity.getHeaders().getContentType());
        assertNotNull(cuentas);
        assertEquals(2, cuentas.size());
        // Una forma; pasándo directamente el PathVariable en la uri
        //ResponseEntity<Void> exchange = client.exchange(crearUrl("/api/cuentas/2"), HttpMethod.DELETE, null, Void.class);
        // Otra forma; creando un HashMap para los PathVariables
        Map<String, Object> pathVariables = new HashMap<>();
        pathVariables.put("cuentaId", 2);
        ResponseEntity<Void> exchange = client.exchange("/api/cuentas/{cuentaId}", HttpMethod.DELETE,
                null, Void.class, pathVariables);
        assertEquals(HttpStatus.NO_CONTENT, exchange.getStatusCode());
        assertFalse(exchange.hasBody());
        responseEntity = client.getForEntity("/api/cuentas", Cuenta[].class);
        cuentas = Arrays.asList(responseEntity.getBody());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, responseEntity.getHeaders().getContentType());
        assertNotNull(cuentas);
        assertEquals(1, cuentas.size());
        ResponseEntity<Cuenta> responseEntityDetalle = client.getForEntity("/api/cuentas/2", Cuenta.class);
        assertEquals(HttpStatus.NOT_FOUND, responseEntityDetalle.getStatusCode());
        assertFalse(responseEntityDetalle.hasBody());
    }

}