package org.mlorenzo.junit5app.ejemplos.models;

import org.mlorenzo.junit5app.ejemplos.exceptions.DineroInsuficienteException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

// Por defecto, JUnit 5 crea una instancia de una clase Test por cada método de prueba que haya en esa clase.
// Podemos hacer que Junit 5 cree una única instancia de esa clase para todos los métodos de prueba configurando la
// anotación @TestInstance, pero se considera una mala práctica y no se recomienda porque hay que mantener un estado de
// la instancia y no se garantiza el orden de ejecución de los métodos de prueba.
// Opción por defecto -> TestInstance.Lifecycle.PER_METHOD
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CuentaTest {
    Cuenta cuenta;
    TestInfo testInfo;
    TestReporter testReporter;

    // Los argumentos de entrada de tipo "TestInfo"(para obtener información de cada test) y "TestReporter"(para guardar
    // información en el log de JUnit) se inyectan automáticamente.
    // Para que este método se ejecute antes de ejecutarse cada test.
    @BeforeEach
    void initMetodoTest(TestInfo testInfo, TestReporter testReporter) {
        System.out.println("iniciando el método de prueba");
        this.cuenta = new Cuenta("Andres", new BigDecimal("1000.12345"));
        // Para usar las instancias de tipo "TestInfo" y "TestReporter" en otros métodos de esta clase "CuentaTest"
        this.testInfo = testInfo;
        this.testReporter = testReporter;
        // Se publica en el log de JUnit
        testReporter.publishEntry("ejecutando: " + testInfo.getDisplayName() + " " + testInfo.getTestMethod()
                .orElse(null).getName() + " con las etiquetas " + testInfo.getTags());
    }

    // Para que este método se ejecute después de ejecutarse cada test
    @AfterEach
    void tearDown() {
        System.out.println("finalizando el método de prueba");
    }

    // Método "static"(De la clase) si @TestInstance(TestInstance.Lifecycle.PER_METHOD) -> Opción por defecto.
    // Este método pertenece a la clase de los tests y no a las instancias(Es decir, es común a todas las instancias).
    // y se ejecuta una sóla vez antes de los tests y de los métodos anotados con @BeforeEach.
    @BeforeAll
    static void beforeAll() {
        System.out.println("inicializando el test");
    }

    // Método no "static"(De la instancia) si @TestInstance(TestInstance.Lifecycle.PER_CLASS).
    // Este método se ejecuta una sóla vez antes de los tests y de los métodos anotados con @BeforeEach.
    /*@BeforeAll
    void beforeAll() {
        System.out.println("inicializando el test");
    }*/

    // Método "static"(De la clase) si @TestInstance(TestInstance.Lifecycle.PER_METHOD) -> Opción por defecto.
    // Este método pertenece a la clase de los tests y no a las instancias(Es decir, es común a todas las instancias)
    // y se ejecuta una sóla vez después de los tests y de los métodos anotados con @AfterEach.
    @AfterAll
    static void afterAll() {
        System.out.println("finalizando el test");
    }

    // Método no "static"(De la instancia) si @TestInstance(TestInstance.Lifecycle.PER_CLASS).
    // Este método se ejecuta una sóla vez después de los tests y de los métodos anotados con @AfterEach.
    /*@AfterAll
    void afterAll() {
        System.out.println("finalizando el test");
    }*/

    // Etiqueta que nos permite seleccionar y ejecutar un conjunto determinado de tests.En este caso, esta etiqueta se
    // aplica a todos los test que se incluyen en esta clase interna.
    @Tag("cuenta")
    // Nos permite usar clases anidadas o internas para organizar los tests
    @Nested
    // Muestra este texto en lugar del nombre de la clase interna
    @DisplayName("probando atributos de la cuenta corriente")
    class CuentaNombreSaldoTest {

        @Test
        // Muestra este texto en lugar del nombre del método del test
        @DisplayName("el nombre")
        void testNombreCuenta() {
            // Se publica en el log de JUnit
            testReporter.publishEntry(testInfo.getTags().toString());
            if(testInfo.getTags().contains("cuenta")) {
                // Se publica en el log de JUnit
                testReporter.publishEntry("hacer algo con la etiqueta cuenta");
            }
            String esperado = "Andres";
            String real = cuenta.getPersona();
            // Opcionalmente, a los "asserts" podemos añadir mensajes descriptivos que se muestran en caso de fallo
            assertNotNull(real, () -> "la cuenta no puede ser nula");
            assertEquals(esperado, real, () -> "el nombre de la cuenta no es el esperado: se esperaba " + esperado
                    + " sin embargo fue " + real);
            assertTrue(real.equals("Andres"), () -> "nombre cuenta esperada debe ser igual a la real");
        }

        @Test
        // Muestra este texto en lugar del nombre del método del test
        @DisplayName("el saldo, que no sea null , que sea mayor que cero y que el valor sea el esperado")
        void testSaldoCuenta() {
            assertNotNull(cuenta.getSaldo());
            assertEquals(1000.12345, cuenta.getSaldo().doubleValue());
            assertFalse(cuenta.getSaldo().compareTo(BigDecimal.ZERO) < 0);
            assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
        }

        @Test
        // Muestra este texto en lugar del nombre del método del test
        @DisplayName("testeando referencias de cuentas para que sean iguales usando el método equals")
        void testReferenciaCuenta() {
            Cuenta cuenta1 = new Cuenta("Jhon Doe", new BigDecimal("8900.9997"));
            Cuenta cuenta2 = new Cuenta("Jhon Doe", new BigDecimal("8900.9997"));
            //assertNotEquals(cuenta2, cuenta1);
            assertEquals(cuenta2, cuenta1);
        }
    }

    // Nos permite usar clases anidadas o internas para organizar los tests
    @Nested
    class CuentaOperacionesTest {

        // Etiqueta que nos permite seleccionar y ejecutar un conjunto determinado de tests
        @Tag("cuenta")
        @Test
        void testDebitoCuenta() {
            cuenta.debito(new BigDecimal(100));
            assertNotNull(cuenta.getSaldo());
            assertEquals(900, cuenta.getSaldo().intValue());
            assertEquals("900.12345", cuenta.getSaldo().toPlainString());
        }

        // Etiqueta que nos permite seleccionar y ejecutar un conjunto determinado de tests
        @Tag("cuenta")
        @Test
        void testCreditoCuenta() {
            cuenta.credito(new BigDecimal(100));
            assertNotNull(cuenta.getSaldo());
            assertEquals(1100, cuenta.getSaldo().intValue());
            assertEquals("1100.12345", cuenta.getSaldo().toPlainString());
        }

        // Etiqueta que nos permite seleccionar y ejecutar un conjunto determinado de tests
        @Tag("cuenta")
        @Tag("banco")
        @Test
        void testTransferirDineroCuentas() {
            Cuenta cuenta1 = new Cuenta("Jhon Doe", new BigDecimal("2500"));
            Cuenta cuenta2 = new Cuenta("Andres", new BigDecimal("1500.8989"));
            Banco banco = new Banco();
            banco.setNombre("Banco del Estado");
            banco.transferir(cuenta2, cuenta1, new BigDecimal(500));
            assertEquals("1000.8989", cuenta2.getSaldo().toPlainString());
            assertEquals("3000", cuenta1.getSaldo().toPlainString());
        }
    }

    // Etiqueta que nos permite seleccionar y ejecutar un conjunto determinado de tests
    @Tag("cuenta")
    @Tag("error")
    @Test
    void testDineroInsuficienteException() {
        Exception exception = assertThrows(DineroInsuficienteException.class,
                () -> cuenta.debito(new BigDecimal(1500)));
        String actual = exception.getMessage();
        String esperado = "Dinero insuficiente";
        assertEquals(esperado, actual);
    }

    @Test
    // Etiqueta que nos permite seleccionar y ejecutar un conjunto determinado de tests
    @Tag("cuenta")
    @Tag("banco")
    // Muestra este texto en lugar del nombre del método del test
    @DisplayName("probando realciones entre las cuentas y el banco con assertAll")
    // Para que se ignore el test y no se ejecute
    //@Disabled
    void testRelacionBancoCuentas() {
        //fail(); // Método que fuerza que el test falle
        Cuenta cuenta1 = new Cuenta("Jhon Doe", new BigDecimal("2500"));
        Cuenta cuenta2 = new Cuenta("Andres", new BigDecimal("1500.8989"));
        Banco banco = new Banco();
        banco.setNombre("Banco del Estado");
        banco.addCuenta(cuenta1);
        banco.addCuenta(cuenta2);
        banco.transferir(cuenta2, cuenta1, new BigDecimal(500));
        // Cuando tenemos muchos "asserts" y alguno falla, aquellos "asserts" que están a continuación del "assert"
        // que ha fallado no se ejecutan.
        // Entonces, para que todos los "asserts" se ejecuten independientemente de si alguno ha fallado, usamos
        // el método "assertAll".
        /*assertEquals("1000.8989", cuenta2.getSaldo().toPlainString());
        assertEquals("3000", cuenta1.getSaldo().toPlainString());
        assertEquals(2, banco.getCuentas().size());
        assertEquals("Banco del Estado", cuenta1.getBanco().getNombre());
        assertTrue(banco.getCuentas().stream()
                .anyMatch(c -> c.getPersona().equals("Andres")));
        assertEquals("Andres", banco.getCuentas().stream()
                .filter(c -> c.getPersona().equals("Andres"))
                .findFirst()
                .get()
                .getPersona());*/
        // Opcionalmente, a los "asserts" podemos añadir mensajes descriptivos que se muestran en caso de fallo
        assertAll(() -> assertEquals("1000.8989", cuenta2.getSaldo().toPlainString(),
                        () -> "el valor del saldo de la cuenta 2 no es el esperado"),
                  () -> assertEquals("3000", cuenta1.getSaldo().toPlainString(),
                          () -> "el valor del saldo de la cuenta 1 no es el esperado"),
                  () -> assertEquals(2, banco.getCuentas().size(),
                          () -> "el banco no tiene las cuentas esperadas"),
                  () -> assertEquals("Banco del Estado", cuenta1.getBanco().getNombre()),
                  () -> assertTrue(banco.getCuentas().stream()
                          .anyMatch(c -> c.getPersona().equals("Andres"))),
                  () -> assertEquals("Andres", banco.getCuentas().stream()
                          .filter(c -> c.getPersona().equals("Andres"))
                          .findFirst()
                          .get()
                          .getPersona()));
    }

    // Nos permite usar clases anidadas o internas para organizar los tests
    @Nested
    class SistemaOperativoTest {

        @Test
        // Este test sólo se ejecuta en máquinas con sistema operativo Windows
        @EnabledOnOs(OS.WINDOWS)
        void testSoloWindows() {
        }

        @Test
        // Este test sólo se ejecuta en máquinas con sistema operativo Linux o Mac
        @EnabledOnOs({OS.LINUX,OS.MAC})
        void testSoloLinuxMac() {
        }

        @Test
        // Este test se ignora o no se ejecuta en máquinas con sistema operativo Windows
        @DisabledOnOs(OS.WINDOWS)
        void testNoWindows() {
        }
    }

    // Nos permite usar clases anidadas o internas para organizar los tests
    @Nested
    class JavaVersionTest {

        @Test
        // Este test sólo se ejecuta en máquinas con Java 8
        @EnabledOnJre(JRE.JAVA_8)
        void testSoloJdk8() {
        }

        @Test
        // Este test sólo se ejecuta en máquinas con Java 12
        @EnabledOnJre(JRE.JAVA_12)
        void testSoloJdk12() {
        }

        @Test
        // Este test se ignora o no se ejecuta en máquinas con Java 12
        @DisabledOnJre(JRE.JAVA_12)
        void testNoJdk12() {
        }
    }

    // Nos permite usar clases anidadas o internas para organizar los tests
    @Nested
    class SystemPropertiesTest {

        // Test que muestra por consola las propiedades del sistema
        @Test
        void imprimirSystemProperties() {
            Properties properties = System.getProperties();
            properties.forEach((k, v) -> System.out.println(k + ":" + v));
        }

        @Test
        // Este test sólo se ejecuta si la propiedad del sistema "java.version" tiene el valor "12.0.2"
        @EnabledIfSystemProperty(named = "java.version", matches = "12.0.2")
        void testJavaVersion() {
        }

        @Test
        // Este test sólo se ejecuta si la propiedad del sistema "java.version" coincide con la expresión regular "12.*.*"
        @EnabledIfSystemProperty(named = "java.version", matches = "12.*.*")
        void testJavaVersion2() {
        }

        @Test
        // Este test se ignora o no se ejecuta si la propiedad del sistema "os.arch" coincide con la expresión
        // regular ".*32.*".
        @DisabledIfSystemProperty(named = "os.arch", matches = ".*32.*")
        void testSolo64() {
        }

        @Test
        // Este test sólo se ejecuta si la propiedad del sistema "os.arch" coincide con la expresión regular ".*32.*"
        @EnabledIfSystemProperty(named = "os.arch", matches = ".*32.*")
        void testNo64() {
        }

        @Test
        // Este test sólo se ejecuta si la propiedad del sistema "user.name" tiene el valor "manuel"
        @EnabledIfSystemProperty(named = "user.name", matches = "manuel")
        void testUsername() {
        }

        @Test
        // Este test sólo se ejecuta si nuestra propiedad personalizada del sistema "ENV" tiene el valor "dev"
        @EnabledIfSystemProperty(named = "ENV", matches = "dev")
        void testDev() {
        }
    }

    @Nested // Nos permite usar clases anidadas o internas para organizar los tests
    class VariablesAmbienteTest {

        // Test que muestra por consola las variables de ambiente del sistema
        @Test
        void imprimirVariablesAmbiente() {
            Map<String, String> variables = System.getenv();
            variables.forEach((k,v) -> System.out.println(k + " = " + v));
        }

        @Test
        // Este test sólo se ejecuta si la variable de ambiente del sistema "JAVA_HOME" coincide con la
        // expresión regular ".*jdk-12.0.2.*".
        @EnabledIfEnvironmentVariable(named = "JAVA_HOME", matches = ".*jdk-12.0.2.*")
        void testJavaHome() {
        }

        @Test
        // Este test sólo se ejecuta si la variable de ambiente del sistema "NUMBER_OF_PROCESSORS" tiene el valor "8".
        @EnabledIfEnvironmentVariable(named = "NUMBER_OF_PROCESSORS", matches = "8")
        void testProcesadores() {
        }

        @Test
        // Este test sólo se ejecuta si nuestra variable de ambiente personalizada del sistema "ENVIRONMENT"
        // tiene el valor "DEV".
        @EnabledIfEnvironmentVariable(named = "ENVIRONMENT", matches = "DEV")
        void testEnv() {
        }

        @Test
        // Este test se ignora o no se ejecuta si nuestra variable de ambiente personalizada del sistema "ENVIRONMENT"
        // tiene el valor "PROD".
        @DisabledIfEnvironmentVariable(named = "ENVIRONMENT", matches = "PROD")
        void testEnvProdDisabled() {
        }
    }

    // ASSUMPTIONS

    @Test
    // Muestra este texto en lugar del nombre del método del test
    @DisplayName("test Saldo Cuenta Dev")
    void testSaldoCuentaDev() {
        boolean esDev = "dev".equals(System.getProperty("ENV"));
        // Si es true, se ejecuta el test.Si es false, se ignora el test y no se ejecuta
        assumeTrue(esDev);
        assertNotNull(cuenta.getSaldo());
        assertEquals(1000.12345, cuenta.getSaldo().doubleValue());
        assertFalse(cuenta.getSaldo().compareTo(BigDecimal.ZERO) < 0);
        assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    // Muestra este texto en lugar del nombre del método del test
    @DisplayName("test Saldo Cuenta Dev 2")
    void testSaldoCuentaDev2() {
        boolean esDev = "dev".equals(System.getProperty("ENV"));
        // Si es true, se ejecutan los "asserts" que se incluyen dentro de la función lambda.En caso
        // contrario, no se ejecutan.
        assumingThat(esDev, () -> {
            assertNotNull(cuenta.getSaldo());
            assertEquals(1000.12345, cuenta.getSaldo().doubleValue());
        });
        // Si hay "asserts" a partir de este punto, sí se ejecutan
        assertFalse(cuenta.getSaldo().compareTo(BigDecimal.ZERO) < 0);
        assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
    }

    // REPETICIÓN DE PRUEBAS

    // Para repetir un número determinado de veces un test.
    // Podemos personalizar el texto de cada repetición usando el atributo "name" de la anotación @RepeatedTest.
    // "currentRepetition" nos da la repetición actual y "totalRepetitions" nos da el número total de repeticiones.
    // "displayname" nos da el texto añadido en la anotación @DisplayName.
    // El argumento de entrada de tipo "RepetitionInfo" se inyecta automáticamente y nos da información sobre las repeticiones.
    @RepeatedTest(value = 5, name = "{displayName} -  Repetición número {currentRepetition} de {totalRepetitions}")
    // Muestra este texto en lugar del nombre del método del test
    @DisplayName("probando Debito Cuenta Repetir")
    void testDebitoCuentaRepetir(RepetitionInfo info) {
        if(info.getCurrentRepetition() == 3)
            System.out.println("estamos en la repetición " + info.getCurrentRepetition());
        cuenta.debito(new BigDecimal(100));
        assertNotNull(cuenta.getSaldo());
        assertEquals(900, cuenta.getSaldo().intValue());
        assertEquals("900.12345", cuenta.getSaldo().toPlainString());
    }

    @Test
    void testDebitoCuenta() {
        cuenta.debito(new BigDecimal(100));
        assertNotNull(cuenta.getSaldo());
        assertEquals(900, cuenta.getSaldo().intValue());
        assertEquals("900.12345", cuenta.getSaldo().toPlainString());
    }

    // PRUEBAS PARAMETRIZADAS

    // Etiqueta que nos permite seleccionar y ejecutar un conjunto determinado de tests.En este caso, esta etiqueta se
    // aplica a todos los test que se incluyen en esta clase interna.
    @Tag("param")
    // Nos permite usar clases anidadas o internas para organizar los tests
    @Nested
    class PruebasParametrizadasTest {

        // Para ejecutar pruebas parametrizadas.
        // Los valores se inyectan automáticamente en el argumento de entrada de tipo String "monto" en cada repetición.
        // El test se ejecuta una vez por cada valor del parámetro. En este caso, se repite 6 veces.
        // Podemos personalizar el texto de cada repetición usando el atributo "name" de la anotación @ParameterizedTest.
        // "index" nos da la repetición actual y "0" y "argumentsWithNames" nos da el valor del parámetro para esa repetición.
        @ParameterizedTest(name = "número {index} ejecutando con valor {0} - {argumentsWithNames}")
        // Establecemos los valores para el argumento de entrada "monto"
        @ValueSource(strings = {"100", "200", "300", "500", "700", "1000"})
        void testDebitoCuentaValueSource(String monto) {
            cuenta.debito(new BigDecimal(monto));
            assertNotNull(cuenta.getSaldo());
            assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
        }

        // Para ejecutar pruebas parametrizadas.
        // Los valores se inyectan automáticamente en los argumentos de entrada de tipo String "indice" y "monto" en
        // cada repetición.
        // El test se ejecuta una vez por cada valor del parámetro. En este caso, se repite 6 veces.
        // Podemos personalizar el texto de cada repetición usando el atributo "name" de la anotación @ParameterizedTest.
        // "index" nos da la repetición actual y "0" y "argumentsWithNames" nos da el valor del parámetro para esa repetición.
        @ParameterizedTest(name = "número {index} ejecutando con valor {0} - {argumentsWithNames}")
        // Establecemos los valores para los argumentos de entrada "indice" y "monto"
        @CsvSource({"1,100", "2,200", "3,300", "4,500", "5,700", "6,1000"})
        void testDebitoCuentaCsvSource(String indice, String monto) {
            System.out.println(indice + " -> " + monto);
            cuenta.debito(new BigDecimal(monto));
            assertNotNull(cuenta.getSaldo());
            assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
        }
        // Para ejecutar pruebas parametrizadas.
        // Los valores se inyectan automáticamente en los argumentos de entrada de tipo String "saldo", "monto",
        // "esperado" y "actual" en cada repetición.
        // El test se ejecuta una vez por cada valor del parámetro. En este caso, se repite 6 veces.
        // Podemos personalizar el texto de cada repetición usando el atributo "name" de la anotación @ParameterizedTest.
        // "index" nos da la repetición actual y "0" y "argumentsWithNames" nos da el valor del parámetro para esa repetición.
        @ParameterizedTest(name = "número {index} ejecutando con valor {0} - {argumentsWithNames}")
        // Establecemos los valores para los argumentos de entrada "saldo", "monto", "esperado" y "actual"
        @CsvSource({"200,100,Jhon,Jhon", "250,200,Pepe,Pepe", "330,300,Maria,Maria", "510,500,Pepa,Pepa",
                "750,700,Lucas,Lucas", "1070.12345,1000.12345,Cata,Cata"})
        void testDebitoCuentaCsvSource2(String saldo, String monto, String esperado, String actual) {
            System.out.println(saldo + " -> " + monto);
            cuenta.setSaldo(new BigDecimal(saldo));
            cuenta.setPersona(actual);
            cuenta.debito(new BigDecimal(monto));
            assertNotNull(cuenta.getSaldo());
            assertNotNull(cuenta.getPersona());
            assertEquals(esperado, actual);
            assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
        }

        // Para ejecutar pruebas parametrizadas.
        // Los valores se inyectan automáticamente en el argumento de entrada de tipo String "monto" en cada repetición.
        // El test se ejecuta una vez por cada valor del parámetro. En este caso, se repite 6 veces.
        // Podemos personalizar el texto de cada repetición usando el atributo "name" de la anotación @ParameterizedTest
        // "index" nos da la repetición actual y "0" y "argumentsWithNames" nos da el valor del parámetro para esa repetición.
        @ParameterizedTest(name = "número {index} ejecutando con valor {0} - {argumentsWithNames}")
        // Los valores para el argumento de entrada "monto" se obtienen del archivo "data.csv"
        @CsvFileSource(resources = "/data.csv")
        void testDebitoCuentaCsvFileSource(String monto) {
            cuenta.debito(new BigDecimal(monto));
            assertNotNull(cuenta.getSaldo());
            assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
        }

        // Para ejecutar pruebas parametrizadas.
        // Los valores se inyectan automáticamente en los argumentos de entrada de tipo String "saldo", "monto",
        // "esperado" y "actual" en cada repetición.
        // El test se ejecuta una vez por cada valor del parámetro. En este caso, se repite 6 veces.
        // Podemos personalizar el texto de cada repetición usando el atributo "name" de la anotación @ParameterizedTest
        // "index" nos da la repetición actual y "0" y "argumentsWithNames" nos da el valor del parámetro para esa repetición.
        @ParameterizedTest(name = "número {index} ejecutando con valor {0} - {argumentsWithNames}")
        // Los valores para los argumentos de entrada "saldo", "monto", "esperado" y "actual" se obtienen del archivo
        // "data2.csv".
        @CsvFileSource(resources = "/data2.csv")
        void testDebitoCuentaCsvFileSource2(String saldo, String monto, String esperado, String actual) {
            cuenta.setSaldo(new BigDecimal(saldo));
            cuenta.setPersona(actual);
            cuenta.debito(new BigDecimal(monto));
            assertNotNull(cuenta.getSaldo());
            assertNotNull(cuenta.getPersona());
            assertEquals(esperado, actual);
            assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
        }
    }

    // Para ejecutar pruebas parametrizadas
    // Los valores se inyectan automáticamente en el argumento de entrada de tipo String "monto" en cada repetición
    // El test se ejecuta una vez por cada valor del parámetro. En este caso, se repite 6 veces
    // Podemos personalizar el texto de cada repetición usando el atributo "name" de la anotación @ParameterizedTest
    // "index" nos da la repetición actual y "0" y "argumentsWithNames" nos da el valor del parámetro para esa repetición
    @ParameterizedTest(name = "número {index} ejecutando con valor {0} - {argumentsWithNames}")
    // Los valores para el argumento de entrada "monto" se obtienen del método "montoList"(El método para esta
    // anotación debe ser estático).
    @MethodSource("montoList")
    // Etiqueta que nos permite seleccionar y ejecutar un conjunto determinado de tests
    @Tag("param")
    void testDebitoCuentaMethodSource(String monto) {
        cuenta.debito(new BigDecimal(monto));
        assertNotNull(cuenta.getSaldo());
        assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
    }

    static List<String> montoList() {
        return Arrays.asList("100", "200", "300", "500", "700", "1000");
    }

    // PRUEBAS CON TIMEOUT

    // Etiqueta que nos permite seleccionar y ejecutar un conjunto determinado de tests.En este caso, esta etiqueta se
    // aplica a todos los test que se incluyen en esta clase interna.
    @Tag("timeout")
    // Nos permite usar clases anidadas o internas para organizar los tests
    @Nested
    class EjemploTimeoutTest {

        @Test
        // Para establecer un timeout para la ejecución de la prueba. Si la prueba no finaliza antes de 1 seg, falla
        @Timeout(1)
        void testTimeout() throws InterruptedException {
            // Interrumpe la ejecución de la prueba durante 100 ms
            TimeUnit.MILLISECONDS.sleep(100);
        }

        @Test
        // Para establecer un timeout para la ejecución de la prueba. Si la prueba no finaliza antes de 1000 ms, falla
        @Timeout(value = 1000, unit = TimeUnit.MILLISECONDS)
        void testTimeout2() throws InterruptedException {
            // Interrumpe la ejecución de la prueba durante 900 ms
            TimeUnit.MILLISECONDS.sleep(900);
        }

        @Test
        void testTimeoutAssertions() {
            // Si la prueba no finaliza antes de 5 seg, falla.
            // Interrumpe la ejecución de la prueba durante 4000 ms
            assertTimeout(Duration.ofSeconds(5), () -> TimeUnit.MILLISECONDS.sleep(4000));
        }
    }
}