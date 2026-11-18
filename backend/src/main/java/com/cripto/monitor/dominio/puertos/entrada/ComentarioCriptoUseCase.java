package com.cripto.monitor.dominio.puertos.entrada;

import com.cripto.monitor.dominio.modelo.ComentarioCripto;

import java.util.List;
import java.util.Optional;

public interface ComentarioCriptoUseCase {

    List<ComentarioCripto> listarRecientes(int limite);

    List<ComentarioCripto> listarPorMoneda(String monedaId);

    ComentarioCripto publicarComentario(String monedaId, String autor, String postura, String contenido);

    Optional<ComentarioCripto> editarComentario(String id, String postura, String contenido);

    boolean eliminarComentario(String id);
}

