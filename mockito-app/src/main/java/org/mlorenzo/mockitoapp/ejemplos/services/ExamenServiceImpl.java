package org.mlorenzo.mockitoapp.ejemplos.services;

import org.mlorenzo.mockitoapp.ejemplos.models.Examen;
import org.mlorenzo.mockitoapp.ejemplos.repositories.ExamenRepository;
import org.mlorenzo.mockitoapp.ejemplos.repositories.PreguntaRepository;

import java.util.List;
import java.util.Optional;

public class ExamenServiceImpl implements ExamenService {
    private ExamenRepository examenRepository;
    private PreguntaRepository preguntaRepository;

    public ExamenServiceImpl(ExamenRepository examenRepository, PreguntaRepository preguntaRepository) {
        this.examenRepository = examenRepository;
        this.preguntaRepository = preguntaRepository;
    }

    @Override
    public Optional<Examen> findExamenPorNombre(String nombre) {
        return examenRepository.findAll().stream().filter(examen -> examen.getNombre().equals(nombre)).findFirst();
    }

    @Override
    public Examen findExamenPorNombreConPreguntas(String nombre) {
        Optional<Examen> oExamen = findExamenPorNombre(nombre);
        Examen examen = null;
        if(oExamen.isPresent()) {
            examen = oExamen.orElseThrow();
            List<String> preguntas = preguntaRepository.findPreguntasPorExamenId(examen.getId());
            // Esta línea se ha puesto aposta para poder hacer ejemplos de tests que cuenten el número de invocaciones
            // con "verify".
            preguntaRepository.findPreguntasPorExamenId(examen.getId());
            examen.setPreguntas(preguntas);
        }
        return examen;
    }

    @Override
    public Examen guardar(Examen examen) {
        if(!examen.getPreguntas().isEmpty())
            preguntaRepository.guardarVarias(examen.getPreguntas());
        return examenRepository.guardar(examen);
    }
}
