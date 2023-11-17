package org.mlorenzo.test.springboot.app.repositories;

import org.mlorenzo.test.springboot.app.models.entities.Cuenta;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integracion_jpa")
// Anotación que habilita únicamente el contexto de persistencia(repositorios, clases "Entity", etc...) de tod*o
// el contexto de Spring Boot.
@DataJpaTest
class CuentaRepositoryTest {
    // Por defecto, si un método de prueba realiza cambios en la base de datos, de forma automática se hace un Rollback
    // al finalizar la ejecución de ese test para que los datos modificados no interfieran en la ejecución del
    // siguiente test.

    @Autowired
    CuentaRepository cuentaRepository;

    @Test
    void testFindById() {
        // When
        Optional<Cuenta> cuenta = cuentaRepository.findById(2L);
        // Then
        assertTrue(cuenta.isPresent());
        assertEquals("Marie", cuenta.orElseThrow().getPersona());
    }

    @Test
    void testFindByPersona() {
        // When
        Optional<Cuenta> cuenta = cuentaRepository.findByPersona("Andrés");
        // Then
        assertTrue(cuenta.isPresent());
        assertEquals("Andrés", cuenta.orElseThrow().getPersona());
        assertEquals("1000.00", cuenta.orElseThrow().getSaldo().toPlainString());
    }

    @Test
    void testFindByPersonaThrowException() {
        // When
        Optional<Cuenta> cuenta = cuentaRepository.findByPersona("Rod");
        // Then
        // Versión simplificada de la expresión "() -> cuenta.get()"
        assertThrows(NoSuchElementException.class, cuenta::get);
        assertFalse(cuenta.isPresent());
    }

    @Test
    void testFindAll() {
        // When
        List<Cuenta> cuentas = cuentaRepository.findAll();
        // Then
        assertFalse(cuentas.isEmpty());
        assertEquals(2, cuentas.size());
    }

    @Test
    void testSave() {
        // Given
        Cuenta cuentaPepe = new Cuenta(null, "Pepe", new BigDecimal("3000"));
        // When
        Cuenta cuenta = cuentaRepository.save(cuentaPepe);
        // Then
        assertEquals("Pepe", cuenta.getPersona());
        assertEquals("3000", cuenta.getSaldo().toPlainString());
    }

    @Test
    void testUpdate() {
        // Given
        Cuenta cuentaPepe = new Cuenta(null, "Pepe", new BigDecimal("3000"));
        Cuenta cuenta = cuentaRepository.save(cuentaPepe);
        cuenta.setSaldo(new BigDecimal("3800"));
        // When
        Cuenta cuentaActualizada = cuentaRepository.save(cuenta);
        // then
        assertEquals("Pepe", cuentaActualizada.getPersona());
        assertEquals("3800", cuentaActualizada.getSaldo().toPlainString());
    }

    @Test
    void testDelete() {
        // Given
        Cuenta cuenta = cuentaRepository.findById(2L).orElseThrow();
        // When
        cuentaRepository.delete(cuenta);
        // Then
        assertThrows(NoSuchElementException.class, () ->
                cuentaRepository.findByPersona("Marie").orElseThrow()
        );
        assertEquals(1, cuentaRepository.findAll().size());
    }
}
