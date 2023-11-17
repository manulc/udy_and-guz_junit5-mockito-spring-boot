package org.mlorenzo.test.springboot.app;

import org.mlorenzo.test.springboot.app.models.entities.Banco;
import org.mlorenzo.test.springboot.app.models.entities.Cuenta;

import java.math.BigDecimal;
import java.util.Optional;

public class Datos {
    // Usamos métodos estáticos en lugar de constantes(con "final") para conseguir que cada método de prueba utilice
    // datos independientes. Dicho de otra manera, hacemos esto para evitar que los métodos de prueba compartan datos.

    public static Optional<Cuenta> crearCuenta001() {
        return Optional.of(new Cuenta(1L, "Andrés", new BigDecimal("1000")));
    }

    public static Optional<Cuenta> crearCuenta002() {
        return Optional.of(new Cuenta(2L, "Jhon", new BigDecimal("2000")));
    }

    public static Optional<Banco> crearBanco() {
        return Optional.of(new Banco(1L, "El banco financiero", 0));
    }
}
