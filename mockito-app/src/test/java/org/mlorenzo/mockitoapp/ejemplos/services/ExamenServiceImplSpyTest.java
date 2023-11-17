package org.mlorenzo.mockitoapp.ejemplos.services;

import org.mlorenzo.mockitoapp.ejemplos.Datos;
import org.mlorenzo.mockitoapp.ejemplos.models.Examen;
import org.mlorenzo.mockitoapp.ejemplos.repositories.ExamenRepositoryImpl;
import org.mlorenzo.mockitoapp.ejemplos.repositories.PreguntaRepositoryImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Una opción para habilitar el uso de anotaciones como @Mock e @InjectMocks en esta clase
@ExtendWith(MockitoExtension.class)
class ExamenServiceImplSpyTest {
    // Uso de espías, o Spy's, usando anotaciones

    // Los espías,o Spy's, son híbridos entre invocaciones simuladas y reales
    // El Spy require que se cree sobre una clase implementada, es decir, que no sea abstracta y que no sea una interfaz

    // Anotación para inyectar un Spy de la implementación ExamenRepositoryImpl en esta propiedad
    @Spy
    ExamenRepositoryImpl examenRepositoryImpl;

    // Anotación para inyectar un Spy de la implementación PreguntaRepositoryImpl en esta propiedad
    @Spy
    PreguntaRepositoryImpl preguntaRepositoryImpl;

    // Anotación para crear una instancia de tipo ExamenServiceImpl en esta propiedad y para inyectar los Spy's
    // en esta instancia.
    @InjectMocks
    ExamenServiceImpl service;

    // En este test, la invocación que se realiza sobre el espía de tipo "ExamenRepository" es real y la invocación que
    // se realiza sobre el espía "PreguntaRepository" es simulada(Se usa "doReturn-while").
    @Test
    void testSpy2() {
        // Si usamos "when" para simular la invocación a un método que pertenece a un espía, o Spy, realiza un
        // comportamiento extraño y dicho método se invoca de forma real en lugar de simularlo.
        // Por esta razón, cuando usamos un espía, o Spy, para simular una invocación, en lugar de usar "when",
        // tenemos que usar "doReturn-when" para lograr el comportamiento esperado.
        //when(preguntaRepository.findPreguntasPorExamenId(anyLong())).thenReturn(Datos.PREGUNTAS);
        doReturn(Datos.PREGUNTAS).when(preguntaRepositoryImpl).findPreguntasPorExamenId(anyLong());
        Examen examen = service.findExamenPorNombreConPreguntas("Matemáticas");
        assertEquals(5L, examen.getId());
        assertEquals("Matemáticas", examen.getNombre());
        assertEquals(5, examen.getPreguntas().size());
        assertTrue(examen.getPreguntas().contains("Aritmética"));
        // Invocación real
        verify(examenRepositoryImpl).findAll();
        // Invocación simulada
        verify(preguntaRepositoryImpl, times(2)).findPreguntasPorExamenId(anyLong());
    }
}