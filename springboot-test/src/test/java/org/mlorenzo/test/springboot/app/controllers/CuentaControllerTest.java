package org.mlorenzo.test.springboot.app.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mlorenzo.test.springboot.app.models.entities.Cuenta;
import org.mlorenzo.test.springboot.app.models.TransaccionDto;
import org.mlorenzo.test.springboot.app.services.CuentaService;
import org.junit.jupiter.api.Test;
import org.mlorenzo.test.springboot.app.Datos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CuentaController.class)
class CuentaControllerTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    CuentaService cuentaService;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void testListar() throws Exception {
        // Given
        List<Cuenta> cuentas = Arrays.asList(Datos.crearCuenta001().orElseThrow(), Datos.crearCuenta002().orElseThrow());
        when(cuentaService.findAll()).thenReturn(cuentas);
        // When
        mvc.perform(get("/api/cuentas"))
                // Then
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].persona").value("Andrés"))
                .andExpect(jsonPath("$[1].persona").value("Jhon"))
                .andExpect(jsonPath("$[0].saldo").value("1000"))
                .andExpect(jsonPath("$[1].saldo").value("2000"))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(content().json(objectMapper.writeValueAsString(cuentas)));
        verify(cuentaService).findAll();
    }

    @Test
    void testDetalle() throws Exception {
        // Given
        when(cuentaService.findById(1L)).thenReturn(Datos.crearCuenta001().orElseThrow());
        // When
        mvc.perform(get("/api/cuentas/1"))
                // Then
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.persona").value("Andrés"))
                .andExpect(jsonPath("$.saldo").value("1000"));
        verify(cuentaService).findById(1L);
    }

    @Test
    void testGuardar() throws Exception {
        // Given
        Cuenta cuenta = new Cuenta(null, "Pepe", new BigDecimal("3000"));
        when(cuentaService.save(any())).then(invocation -> {
            // Obtiene la cuenta que se le pasa al método "save" del servicio "cuentaService"
            Cuenta c = invocation.getArgument(0);
            c.setId(3L);
            return c;
        });
        // When
        mvc.perform(post("/api/cuentas")
                // Indicamos que el request de la petición http se envía como un JSON
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cuenta)))
                //Then
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(3))) // Equivalente a la expresión "jsonPath("$.id").value(3L)"
                .andExpect(jsonPath("$.persona", is("Pepe"))) // Equivalente a la expresión "jsonPath("$.id").value("Pepe")"
                .andExpect(jsonPath("$.saldo", is(3000))); // Equivalente a la expresión "jsonPath("$.id").value("3000")"

        verify(cuentaService).save(any());
    }

    @Test
    void testTransferir() throws Exception {
        // Given
        TransaccionDto tDto = new TransaccionDto();
        tDto.setCuentaOrigenId(1L);
        tDto.setCuentaDestinoId(2L);
        tDto.setMonto(new BigDecimal("100"));
        tDto.setBancoId(1L);
        System.out.println(objectMapper.writeValueAsString(tDto));
        Map<String, Object> response = new HashMap<>();
        response.put("date", LocalDate.now().toString());
        response.put("status", "OK");
        response.put("mensaje", "Transferencia realizada con éxito!");
        response.put("transaccion", tDto);
        System.out.println(objectMapper.writeValueAsString(response));
        // When
        mvc.perform(post("/api/cuentas/transferir")
                // Indicamos que el request de la petición http se envía como un JSON
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tDto)))
                // Then
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.date").value(LocalDate.now().toString()))
                .andExpect(jsonPath("$.mensaje").value("Transferencia realizada con éxito!"))
                .andExpect(jsonPath("$.transaccion.cuentaOrigenId").value(tDto.getCuentaOrigenId()))
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }
}