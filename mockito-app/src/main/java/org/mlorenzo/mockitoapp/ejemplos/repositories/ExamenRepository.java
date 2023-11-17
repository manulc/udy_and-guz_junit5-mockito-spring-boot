package org.mlorenzo.mockitoapp.ejemplos.repositories;

import org.mlorenzo.mockitoapp.ejemplos.models.Examen;

import java.util.List;

public interface ExamenRepository {
    List<Examen> findAll();
    Examen guardar(Examen examen);
}
