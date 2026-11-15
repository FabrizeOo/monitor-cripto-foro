package com.cripto.monitor.dominio.puertos.salida;

import com.cripto.monitor.dominio.modelo.Criptomoneda;
import com.cripto.monitor.dominio.modelo.ResumenMercado;

import java.util.List;
import java.util.Optional;

public interface CriptoRepositorioPort {

    void guardarLote(List<Criptomoneda> monedas);

    List<Criptomoneda> buscarConFiltros(String texto, String tendencia, String orden, int pagina, int tamano);

    Optional<Criptomoneda> buscarPorId(String monedaId);

    Optional<Criptomoneda> incrementarLike(String monedaId);

    long contarTotal();

    ResumenMercado calcularResumen(long totalSnapshots, long totalComentarios);
}

