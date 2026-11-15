package com.cripto.monitor.dominio.puertos.salida;

import com.cripto.monitor.dominio.modelo.ComentarioCripto;

import java.util.List;

public interface ComentarioCriptoRepositorioPort {

    ComentarioCripto guardar(ComentarioCripto comentario);

    List<ComentarioCripto> listarRecientes(int limite);

    List<ComentarioCripto> listarPorMoneda(String monedaId);

    long contarTotal();
}

