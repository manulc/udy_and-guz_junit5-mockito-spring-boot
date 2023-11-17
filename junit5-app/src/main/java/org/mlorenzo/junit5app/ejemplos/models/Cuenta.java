package org.mlorenzo.junit5app.ejemplos.models;

import org.mlorenzo.junit5app.ejemplos.exceptions.DineroInsuficienteException;

import java.math.BigDecimal;

public class Cuenta {
    private String persona;
    private BigDecimal saldo;
    private Banco banco;

    public Cuenta(String persona, BigDecimal saldo) {
        this.persona = persona;
        this.saldo = saldo;
    }

    public String getPersona() {
        return persona;
    }

    public void setPersona(String persona) {
        this.persona = persona;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public void setSaldo(BigDecimal saldo) {
        this.saldo = saldo;
    }

    public Banco getBanco() {
        return banco;
    }

    public void setBanco(Banco banco) {
        this.banco = banco;
    }

    public void debito(BigDecimal monto) {
        // Los objetos de tipo BigDecimal son inmutables. Por esta razón, cada modificación que se haga sobre un objeto BigDecimal, devuelve otro objeto BigDecimal con el resultado de esa modificación
        BigDecimal nuevoSaldo = this.saldo.subtract(monto);
        if(nuevoSaldo.compareTo(BigDecimal.ZERO) < 0)
            throw new DineroInsuficienteException("Dinero insuficiente");
        this.saldo = nuevoSaldo;
    }

    public void credito(BigDecimal monto) {
        // Los objetos de tipo BigDecimal son inmutables. Por esta razón, cada modificación que se haga sobre un objeto BigDecimal, devuelve otro objeto BigDecimal con el resultado de esa modificación
        this.saldo = this.saldo.add(monto);
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Cuenta))
            return false;
        Cuenta c = (Cuenta) obj;
        if(this.persona == null || this.saldo == null)
            return false;
        return this.persona.equals(c.getPersona()) && this.saldo.equals(c.getSaldo());
    }
}
