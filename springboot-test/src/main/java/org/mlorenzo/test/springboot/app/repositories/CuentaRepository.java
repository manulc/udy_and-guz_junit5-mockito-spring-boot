package org.mlorenzo.test.springboot.app.repositories;

import org.mlorenzo.test.springboot.app.models.entities.Cuenta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CuentaRepository extends JpaRepository<Cuenta, Long> {
    Optional<Cuenta> findByPersona(String persona);
}
