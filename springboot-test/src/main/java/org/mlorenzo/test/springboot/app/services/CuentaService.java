package org.mlorenzo.test.springboot.app.services;

import org.mlorenzo.test.springboot.app.models.entities.Cuenta;

import java.math.BigDecimal;
import java.util.List;

public interface CuentaService {
    List<Cuenta> findAll();
    Cuenta findById(Long id);
    Cuenta save(Cuenta cuenta);
    void deleteById(Long id);
    int revisarTotalTransferencias(Long bancoId);
    BigDecimal revisarSalto(Long id);
    void transferir(Long numCuentaOrigen, Long numCuentaDestino, Long bancoId, BigDecimal monto);
}
