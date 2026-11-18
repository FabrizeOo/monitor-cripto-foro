package com.cripto.monitor.dominio.puertos.salida;

import com.cripto.monitor.dominio.modelo.ComentarioCripto;

import java.util.List;
import java.util.Optional;

public interface ComentarioCriptoRepositorioPort {

    ComentarioCripto guardar(ComentarioCripto comentario);

    List<ComentarioCripto> listarRecientes(int limite);

    List<ComentarioCripto> listarPorMoneda(String monedaId);

    default Optional<ComentarioCripto> actualizar(String id, String postura, String contenido) {
        return Optional.empty();
    }

    default boolean eliminarPorId(String id) {
        return false;
    }

    long contarTotal();
}

