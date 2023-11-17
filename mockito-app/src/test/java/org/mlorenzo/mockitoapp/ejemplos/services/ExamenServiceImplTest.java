package org.mlorenzo.mockitoapp.ejemplos.services;

import org.mlorenzo.mockitoapp.ejemplos.Datos;
import org.mlorenzo.mockitoapp.ejemplos.models.Examen;
import org.mlorenzo.mockitoapp.ejemplos.repositories.ExamenRepository;
import org.mlorenzo.mockitoapp.ejemplos.repositories.ExamenRepositoryImpl;
import org.mlorenzo.mockitoapp.ejemplos.repositories.PreguntaRepository;
import org.mlorenzo.mockitoapp.ejemplos.repositories.PreguntaRepositoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

// Una opción para habilitar el uso de anotaciones como @Mock e @InjectMocks en esta clase
@ExtendWith(MockitoExtension.class)
class ExamenServiceImplTest {

    // Anotación para inyectar un Mock de la interfaz ExamenRepository en esta propiedad
    @Mock
    ExamenRepository examenRepository;

    // Anotación para inyectar un Mock de la implementación PreguntaRepositoryImpl en esta propiedad
    @Mock
    PreguntaRepositoryImpl preguntaRepository;

    // Anotación para crear una instancia de tipo ExamenServiceImpl en esta propiedad y para inyectar los Mocks en
    // esta instancia.
    @InjectMocks
    ExamenServiceImpl service;

    // Capturador de argumentos de tipo Long para los ids de los exámenes
    @Captor
    ArgumentCaptor<Long> captor;

    @BeforeEach
    void setUp() {
        // Para poder usar anotaciones como @Mock e @InjectMocks en esta clase.
        // Otra opción para habilitar el uso de las anotaciones @Mock e @InjectMocks en esta clase es usar la anotación
        // @ExtendWith a nivel de clase pasándole "MockitoExtension.class".
        //MockitoAnnotations.openMocks(this);
        // Se comenta estas líneas porque ahora usamos las anotaciones @Mock e @InjectMocks para crear los Mocks y
        // las instancias.
        //examenRepository = mock(ExamenRepository.class);
        //preguntaRepository = mock(PreguntaRepository.class);
        //service = new ExamenServiceImpl(examenRepository, preguntaRepository);
    }

    @Test
    void testFindExamenPorNombre() {
        when(examenRepository.findAll()).thenReturn(Datos.EXAMENES);
        Optional<Examen> oExamen = service.findExamenPorNombre("Matemáticas");
        assertTrue(oExamen.isPresent());
        assertEquals(5L, oExamen.orElseThrow().getId());
        assertEquals("Matemáticas", oExamen.orElseThrow().getNombre());
    }

    @Test
    void testFindExamenPorNombreListaVacia() {
        List<Examen> datos = Collections.emptyList();
        when(examenRepository.findAll()).thenReturn(datos);
        Optional<Examen> oExamen = service.findExamenPorNombre("Matemáticas");
        assertFalse(oExamen.isPresent());
    }

    @Test
    void testPreguntasExamen() {
        when(examenRepository.findAll()).thenReturn(Datos.EXAMENES);
        when(preguntaRepository.findPreguntasPorExamenId(5L)).thenReturn(Datos.PREGUNTAS);
        Examen examen = service.findExamenPorNombreConPreguntas("Matemáticas");
        assertEquals(5, examen.getPreguntas().size());
        assertTrue(examen.getPreguntas().contains("Aritmética"));
    }

    @Test
    void testPreguntasExamenVerify() {
        when(examenRepository.findAll()).thenReturn(Datos.EXAMENES);
        when(preguntaRepository.findPreguntasPorExamenId(5L)).thenReturn(Datos.PREGUNTAS);
        Examen examen = service.findExamenPorNombreConPreguntas("Matemáticas");
        assertEquals(5, examen.getPreguntas().size());
        assertTrue(examen.getPreguntas().contains("Aritmética"));
        verify(examenRepository).findAll();
        verify(preguntaRepository, times(2)).findPreguntasPorExamenId(anyLong());
    }

    @Test
    void testNoExisteExamenVerify() {
        when(examenRepository.findAll()).thenReturn(Datos.EXAMENES);
        Examen examen = service.findExamenPorNombreConPreguntas("Matemáticas2");
        assertNull(examen);
        verify(examenRepository).findAll();
    }

    @Test
    void testGuardarExamen() {
        Examen newExamen = Datos.EXAMEN;
        newExamen.setPreguntas(Datos.PREGUNTAS);
        when(examenRepository.guardar(any(Examen.class))).then(new Answer<Examen>() {
            Long secuencia = 8L;

            // Sobrescribimos este método de la interfaz Answer para auto-generar de forma incrmemental, empezando
            // en 8, el id del exámen.
            @Override
            public Examen answer(InvocationOnMock invocation) throws Throwable {
                // Obtenemos el primer y único argumento que le pasamos al método "guardar" del repositorio
                // "examenRepository" y que se corresponde con el exámen.
                Examen examen = invocation.getArgument(0);
                examen.setId(secuencia++);
                return examen;
            }
        });
        Examen examen = service.guardar(newExamen);
        assertNotNull(examen.getId());
        assertEquals(8L, examen.getId());
        assertEquals("Física", examen.getNombre());
        verify(examenRepository).guardar(any(Examen.class));
        verify(preguntaRepository).guardarVarias(anyList());
    }

    @Test
    void testManejoException() {
        when(examenRepository.findAll()).thenReturn(Datos.EXAMENES_ID_NULL);
        when(preguntaRepository.findPreguntasPorExamenId(isNull())).thenThrow(IllegalArgumentException.class);
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> service.findExamenPorNombreConPreguntas("Matemáticas")
        );
        assertEquals(IllegalArgumentException.class, exception.getClass());
        verify(examenRepository).findAll();
        verify(preguntaRepository).findPreguntasPorExamenId(isNull());
    }

    @Test
    void testArgumentMatchers() {
        when(examenRepository.findAll()).thenReturn(Datos.EXAMENES);
        when(preguntaRepository.findPreguntasPorExamenId(anyLong())).thenReturn(Datos.PREGUNTAS);
        service.findExamenPorNombreConPreguntas("Matemáticas");
        verify(examenRepository).findAll();
        // Verifica que se invoca 2 veces al método "findPreguntasPorExamenId" del repositorio "preguntaRepository"
        // y que se le pasa, como argumento de entrada, el valor 5L(Id del exámen "Matemáticas").
        // Usando funciones lambda, podemos realizar comprobaciones o verificaciones más precisas.
        //verify(preguntaRepository, times(2)).findPreguntasPorExamenId(argThat(arg -> arg != null && arg.equals(5L)));
        // Verificación similar a la anterior pero sin comprobar si el argumento es nulo
        verify(preguntaRepository, times(2)).findPreguntasPorExamenId(eq(5L));
    }

    @Test
    void testArgumentMatchers2() {
        when(examenRepository.findAll()).thenReturn(Datos.EXAMENES_ID_NEGATIVOS);
        when(preguntaRepository.findPreguntasPorExamenId(anyLong())).thenReturn(Datos.PREGUNTAS);
        service.findExamenPorNombreConPreguntas("Matemáticas");
        verify(examenRepository).findAll();
        // Verifica que se invoca 2 veces al método "findPreguntasPorExamenId" del repositorio "preguntaRepository"
        // y que se le pasa, como argumento de entrada, el valor 5L(Id del exámen "Matemáticas").
        // Usamos una instancia de nuestra clase "MiArgsMatchers" para realizar nuestra validación personalizada del
        // argumento de entrada que recibe este método "findPreguntasPorExamenId".
        verify(preguntaRepository, times(2)).findPreguntasPorExamenId(argThat(new MiArgsMatchers()));
    }

    // Clase para personalizar la validación de argumentos que son ids de exámenes
    static class MiArgsMatchers implements ArgumentMatcher<Long> {
        private Long argument;

        // Sobrescribimos este método para implementar nuestra validación personalizada
        @Override
        public boolean matches(Long argument) {
            this.argument = argument;
            return argument != null && argument < 0;
        }

        // Sobrescribimos este método para personalizar el mensaje de error cuando falla la validación
        @Override
        public String toString() {
            return "es para un mensaje personalizado de error que imprime mockito en caso de que falle el test, "
                    + argument + " debe ser un entero positivo";
        }
    }

    @Test
    void testArgumentCaptor() {
        when(examenRepository.findAll()).thenReturn(Datos.EXAMENES);
        service.findExamenPorNombreConPreguntas("Matemáticas");
        // Capturador de argumentos de tipo Long para los ids de los exámenes
        // Se comenta porque ahora usamos otra alternativa similar que consiste en una propiedad de la clase de tipo
        // ArgumentCaptor<Long> anotada con @Captor.
        //ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);
        // Verifica que se invoca una sóla vez al método "findPreguntasPorExamenId" del repositorio "preguntaRepository"
        // y capturamos el valor del id del exámen que se le pasa.
        verify(preguntaRepository, times(2)).findPreguntasPorExamenId(captor.capture());
        // Valida el valor del id del exámen
        assertEquals(5L, captor.getValue());
    }

    @Test
    void testDoThrow() {
        Examen examen = Datos.EXAMEN;
        examen.setPreguntas(Datos.PREGUNTAS);
        // when(preguntaRepository.guardarVarias(anyList())).thenThrow(IllegalArgumentException.class); Error porque
        // el método "guardarVarias" es de tipo void y no devuelve nada y en estos casos no se permite lanzar excepciones.
        // Usamos "doThrow" como solución al caso de error anterior, es decir, para poder simular lanzar excepciones
        // cuando el método que se invoca no devuelve nada(void).
        doThrow(IllegalArgumentException.class).when(preguntaRepository).guardarVarias(anyList());
        assertThrows(IllegalArgumentException.class, () -> service.guardar(examen));
    }

    @Test
    void testDoAnswer() {
        when(examenRepository.findAll()).thenReturn(Datos.EXAMENES);
        //when(preguntaRepository.findPreguntasPorExamenId(anyLong())).thenReturn(Datos.PREGUNTAS);
        doAnswer(invocation -> {
            // Obtenemos el primer y único argumento que le pasamos al método "findPreguntasPorExamenId" del repositorio
            // "preguntaRepository" y que se corresponde con el exámen.
            Long id = invocation.getArgument(0);
            // Si el id del exámen es 5, la simulación de la invocación al método "findPreguntasPorExamenId" devuelve
            // Datos.PREGUNTAS y, en caso contrario, devuelve una lista vacía.
            return 5L == id ? Datos.PREGUNTAS : Collections.emptyList();
        }).when(preguntaRepository).findPreguntasPorExamenId(anyLong());
        Examen examen = service.findExamenPorNombreConPreguntas("Matemáticas");
        assertEquals(5L, examen.getId());
        assertEquals("Matemáticas", examen.getNombre());
        assertEquals(5, examen.getPreguntas().size());
        assertTrue(examen.getPreguntas().contains("Geometría"));
        verify(preguntaRepository, times(2)).findPreguntasPorExamenId((anyLong()));
    }

    // Equivalente al método de pruebas "testGuardarExamen" que hay más arriba pero en este caso usando "doAnswer"
    @Test
    void testDoAnswerGuardarExamen() {
        Examen newExamen = Datos.EXAMEN;
        newExamen.setPreguntas(Datos.PREGUNTAS);
        doAnswer(new Answer<Examen>() {
            Long secuencia = 8L;
            // Sobrescribimos este método de la interfaz Answer para auto-generar de forma incrmemental,
            // empezando en 8, el id del exámen.
            @Override
            public Examen answer(InvocationOnMock invocation) throws Throwable {
                // Obtenemos el primer y único argumento que le pasamos al método "guardar" del repositorio
                // "examenRepository" y que se corresponde con el exámen.
                Examen examen = invocation.getArgument(0);
                examen.setId(secuencia++);
                return examen;
            }
        }).when(examenRepository).guardar(any(Examen.class));
        Examen examen = service.guardar(newExamen);
        assertNotNull(examen.getId());
        assertEquals(8L, examen.getId());
        assertEquals("Física", examen.getNombre());
        verify(examenRepository).guardar(any(Examen.class));
        verify(preguntaRepository).guardarVarias(anyList());
    }

    @Test
    void testDoCallRealMethod() {
        // Simulación de una invocación
        when(examenRepository.findAll()).thenReturn(Datos.EXAMENES);
        // Simulación de una invocación. Se comenta porque abajo se invoca de forma real al método
        //when(preguntaRepository.findPreguntasPorExamenId(anyLong())).thenReturn(Datos.PREGUNTAS);
        // El método "doCallRealMethod" nos permite realizar invocaciones reales en lugar de simular esas invocaciones.
        // Para usar el método "doCallRealMethod", se debe usar una implementación real, es decir, no se puede usar
        // clases abstractas o interfaces.
        // Invocación real del método "findPreguntasPorExamenId" de la implementación "preguntaRepositoryImpl".
        doCallRealMethod().when(preguntaRepository).findPreguntasPorExamenId(anyLong());
        Examen examen = service.findExamenPorNombreConPreguntas("Matemáticas");
        assertEquals(5L, examen.getId());
        assertEquals("Matemáticas", examen.getNombre());
    }

    // Los espías con Spy son híbridos entre invocaciones simuladas y reales.
    // El Spy require que se cree sobre una clase implementada, es decir, que no sea abstracta y que no sea una interfaz.
    // En este test, las invocaciones son reales.
    // También se pueden usar espías, o Spy's, usando anotaciones(ver clase "ExamenServiceImplSpyTest").
    @Test
    void testSpy() {
        ExamenRepository examenRepository = spy(ExamenRepositoryImpl.class);
        PreguntaRepository preguntaRepository = spy(PreguntaRepositoryImpl.class);
        ExamenService examenService = new ExamenServiceImpl(examenRepository, preguntaRepository);
        Examen examen = examenService.findExamenPorNombreConPreguntas("Matemáticas");
        assertEquals(5L, examen.getId());
        assertEquals("Matemáticas", examen.getNombre());
        assertEquals(5, examen.getPreguntas().size());
        assertTrue(examen.getPreguntas().contains("Aritmética"));
    }

    // Los espías, o Spy's, son híbridos entre invocaciones simuladas y reales.
    // El Spy require que se cree sobre una clase implementada, es decir, que no sea abstracta y que no sea una interfaz.
    // En este test, la invocación que se realiza sobre el espía de tipo "ExamenRepository" es real y la
    // invocación que se realiza sobre el espía "PreguntaRepository" es simulada(Se usa "doReturn-while").
    // También se pueden usar espías, o Spy's, usando anotaciones(ver clase "ExamenServiceImplSpyTest").
    @Test
    void testSpy2() {
        ExamenRepository examenRepository = spy(ExamenRepositoryImpl.class);
        PreguntaRepository preguntaRepository = spy(PreguntaRepositoryImpl.class);
        ExamenService examenService = new ExamenServiceImpl(examenRepository, preguntaRepository);
        // Si usamos "when" para simular la invocación a un método que pertenece a un espía, o Spy, realiza un
        // comportamiento extraño y dicho método se invoca de forma real en lugar de simularlo.
        // Por esta razón, cuando usamos un espía, o Spy, para simular una invocación, en lugar de usar "when", tenemos
        // que usar "doReturn-when" para lograr el comportamiento esperado.
        //when(preguntaRepository.findPreguntasPorExamenId(anyLong())).thenReturn(Datos.PREGUNTAS);
        doReturn(Datos.PREGUNTAS).when(preguntaRepository).findPreguntasPorExamenId(anyLong());
        Examen examen = examenService.findExamenPorNombreConPreguntas("Matemáticas");
        assertEquals(5L, examen.getId());
        assertEquals("Matemáticas", examen.getNombre());
        assertEquals(5, examen.getPreguntas().size());
        assertTrue(examen.getPreguntas().contains("Aritmética"));
        // Invocación real
        verify(examenRepository).findAll();
        // Invocación simulada
        verify(preguntaRepository, times(2)).findPreguntasPorExamenId(anyLong());
    }

    // Tests para verificar el orden de las invocaciones

    @Test
    void testOrderDeInvocariones() {
        when(examenRepository.findAll()).thenReturn(Datos.EXAMENES);
        service.findExamenPorNombreConPreguntas("Matemáticas");
        service.findExamenPorNombreConPreguntas("Lenguaje");
        // Verificamos el orden de invocaciones en el Mock "preguntaRepository"
        InOrder inOrder = inOrder(preguntaRepository);
        // Id del exámen "Matemáticas"
        inOrder.verify(preguntaRepository, times(2)).findPreguntasPorExamenId(5L);
        // Id del exámen "Lenguaje"
        inOrder.verify(preguntaRepository, times(2)).findPreguntasPorExamenId(6L);
    }

    // Tests para verificar el número de veces que se invoca a métodos

    @Test
    void testOrderDeInvocariones2() {
        when(examenRepository.findAll()).thenReturn(Datos.EXAMENES);
        service.findExamenPorNombreConPreguntas("Matemáticas");
        service.findExamenPorNombreConPreguntas("Lenguaje");
        // Verificamos el orden de invocaciones en los Mocks "examenRepository" y "preguntaRepository"
        InOrder inOrder = inOrder(examenRepository, preguntaRepository);
        inOrder.verify(examenRepository).findAll();
        // Id del exámen "Matemáticas"
        inOrder.verify(preguntaRepository, times(2)).findPreguntasPorExamenId(5L);
        inOrder.verify(examenRepository).findAll();
        // Id del exámen "Lenguaje"
        inOrder.verify(preguntaRepository, times(2)).findPreguntasPorExamenId(6L);
    }

    @Test
    void testNumeroDeInvocaciones() {
        when(examenRepository.findAll()).thenReturn(Datos.EXAMENES);
        service.findExamenPorNombreConPreguntas("Matemáticas");
        // Si no es especifica el número de invocaciones en el método "times", por defecto es 1
        verify(preguntaRepository, times(2)).findPreguntasPorExamenId(5l);
        // Con "atLeast" verificamos que se realiza al menos un número determinado de invocaciones
        verify(preguntaRepository, atLeast(1)).findPreguntasPorExamenId(5l);
        // El método "atLeastOnce" es equivalente al método "atLeast(1)"
        verify(preguntaRepository, atLeastOnce()).findPreguntasPorExamenId(5l);
        // Para verificar que se invoca como mucho un número determinado de veces
        verify(preguntaRepository, atMost(20)).findPreguntasPorExamenId(5l);
    }

    @Test
    void testNumeroDeInvocaciones2() {
        when(examenRepository.findAll()).thenReturn(Collections.emptyList());
        service.findExamenPorNombreConPreguntas("Matemáticas");
        // Con "never" verificamos que nunca se invocó al método
        verify(preguntaRepository, never()).findPreguntasPorExamenId(5L);
        // Verifica que no hubo interacción con "preguntaRepository"
        verifyNoInteractions(preguntaRepository);
        verify(examenRepository).findAll();
        verify(examenRepository, times(1)).findAll();
        verify(examenRepository, atLeast(1)).findAll();
        verify(examenRepository, atLeastOnce()).findAll();
        verify(examenRepository, atMost(20)).findAll();
        // El método "atMostOnce" es equivalente al método "atMost(1)"
        verify(examenRepository, atMostOnce()).findAll();
    }
}
