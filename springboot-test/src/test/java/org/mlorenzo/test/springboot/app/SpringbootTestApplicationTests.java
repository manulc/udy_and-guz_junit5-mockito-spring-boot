package org.mlorenzo.test.springboot.app;

import org.mlorenzo.test.springboot.app.exceptions.DineroInsuficienteException;
import org.mlorenzo.test.springboot.app.models.entities.Banco;
import org.mlorenzo.test.springboot.app.models.entities.Cuenta;
import org.mlorenzo.test.springboot.app.repositories.BancoRepository;
import org.mlorenzo.test.springboot.app.repositories.CuentaRepository;
import org.mlorenzo.test.springboot.app.services.CuentaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class SpringbootTestApplicationTests {

	@MockBean
	CuentaRepository cuentaRepository;

	@MockBean
	BancoRepository bancoRepository;

	@Autowired
	CuentaService service;

	@BeforeEach
	void setUp() {
		// Se comentan estas líneas porque ahora vamos a usar anotaciones para crear los Mocks
		//cuentaRepository = mock(CuentaRepository.class);
		//bancoRepository = mock(BancoRepository.class);
		//service = new CuentaServiceImpl(cuentaRepository, bancoRepository);
	}

	@Test
	void contextLoads() {
		when(cuentaRepository.findById(1L)).thenReturn(Datos.crearCuenta001());
		when(cuentaRepository.findById(2L)).thenReturn(Datos.crearCuenta002());
		when(bancoRepository.findById(1L)).thenReturn(Datos.crearBanco());
		BigDecimal saldoOrigen = service.revisarSalto(1L);
		BigDecimal saldoDestino = service.revisarSalto(2L);
		assertEquals("1000", saldoOrigen.toPlainString());
		assertEquals("2000", saldoDestino.toPlainString());
		service.transferir(1L, 2L, 1L, new BigDecimal("100"));
		saldoOrigen = service.revisarSalto(1L);
		saldoDestino = service.revisarSalto(2L);
		assertEquals("900", saldoOrigen.toPlainString());
		assertEquals("2100", saldoDestino.toPlainString());
		int totalTransferencias = service.revisarTotalTransferencias(1L);
		assertEquals(1, totalTransferencias);
		verify(cuentaRepository, times(3)).findById(1L);
		verify(cuentaRepository, times(3)).findById(2L);
		verify(cuentaRepository, times(2)).save(any(Cuenta.class));
		verify(bancoRepository, times(2)).findById(1L);
		verify(bancoRepository).save(any(Banco.class));
		verify(cuentaRepository, times(6)).findById(anyLong());
		verify(cuentaRepository, never()).findAll();
	}

	@Test
	void contextLoads2() {
		when(cuentaRepository.findById(1L)).thenReturn(Datos.crearCuenta001());
		when(cuentaRepository.findById(2L)).thenReturn(Datos.crearCuenta002());
		when(bancoRepository.findById(1L)).thenReturn(Datos.crearBanco());
		BigDecimal saldoOrigen = service.revisarSalto(1L);
		BigDecimal saldoDestino = service.revisarSalto(2L);
		assertEquals("1000", saldoOrigen.toPlainString());
		assertEquals("2000", saldoDestino.toPlainString());
		assertThrows(DineroInsuficienteException.class, () ->
				service.transferir(1L, 2L, 1L, new BigDecimal("1200"))
		);
		saldoOrigen = service.revisarSalto(1L);
		saldoDestino = service.revisarSalto(2L);
		assertEquals("1000", saldoOrigen.toPlainString());
		assertEquals("2000", saldoDestino.toPlainString());
		int totalTransferencias = service.revisarTotalTransferencias(1L);
		assertEquals(0, totalTransferencias);
		verify(cuentaRepository, times(3)).findById(1L);
		verify(cuentaRepository, times(2)).findById(2L);
		verify(cuentaRepository, never()).save(any(Cuenta.class));
		verify(bancoRepository).findById(1L);
		verify(bancoRepository, never()).save(any(Banco.class));
		verify(cuentaRepository, times(5)).findById(anyLong());
		verify(cuentaRepository, never()).findAll();
	}

	@Test
	void contextLoads3() {
		when(cuentaRepository.findById(1L)).thenReturn(Datos.crearCuenta001());
		Cuenta cuenta1 = service.findById(1L);
		Cuenta cuenta2 = service.findById(1L);
		assertSame(cuenta1, cuenta2);
		assertTrue(cuenta1 == cuenta2);
		assertEquals("Andrés", cuenta1.getPersona());
		assertEquals("Andrés", cuenta2.getPersona());
		verify(cuentaRepository, times(2)).findById(1L);
	}

	@Test
	void testFindAll() {
		List<Cuenta> datos = Arrays.asList(Datos.crearCuenta001().orElseThrow(), Datos.crearCuenta002().orElseThrow());
		when(cuentaRepository.findAll()).thenReturn(datos);
		List<Cuenta> cuentas = service.findAll();
		assertFalse(cuentas.isEmpty());
		assertEquals(cuentas.size(), 2);
		assertTrue(cuentas.contains(Datos.crearCuenta002().orElseThrow()));
		verify(cuentaRepository).findAll();
	}

	@Test
	void testSave() {
		Cuenta cuentaPepe = new Cuenta(null, "Pepe", new BigDecimal("3000"));
		when(cuentaRepository.save(any())).then(invocation -> {
			// Obtiene la cuenta que se le pasa al método "save" del repositorio "cuentaRepository"
			Cuenta c = invocation.getArgument(0);
			c.setId(3L);
			return c;
		});
		Cuenta cuenta = service.save(cuentaPepe);
		assertEquals("Pepe", cuenta.getPersona());
		assertEquals(3L, cuenta.getId());
		assertEquals("3000", cuenta.getSaldo().toPlainString());
		verify(cuentaRepository).save(any());
	}
}
