package org.mlorenzo.mockitoapp.ejemplos;

import org.mlorenzo.mockitoapp.ejemplos.models.Examen;

import java.util.Arrays;
import java.util.List;

public class Datos {
    public static final List<Examen> EXAMENES = Arrays.asList(new Examen(5L, "Matemáticas"),
        new Examen(6L, "Lenguaje"), new Examen(7L, "Historia"));
    public static final List<Examen> EXAMENES_ID_NULL = Arrays.asList(new Examen(null, "Matemáticas"),
            new Examen(null, "Lenguaje"), new Examen(null, "Historia"));
    public static final List<Examen> EXAMENES_ID_NEGATIVOS = Arrays.asList(new Examen(-5L, "Matemáticas"),
            new Examen(-6L, "Lenguaje"), new Examen(-7L, "Historia"));
    public static final List<String> PREGUNTAS = Arrays.asList("Aritmética", "Integrales", "Derivadas", "Trigonometría",
            "Geometría");
    // El id es null porque vamos a simular en las pruebas que se genera de forma autoincremental
    public static final Examen EXAMEN = new Examen(null, "Física");
}
