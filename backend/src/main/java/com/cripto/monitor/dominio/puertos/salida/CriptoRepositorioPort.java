package com.cripto.monitor.dominio.puertos.salida;

import com.cripto.monitor.dominio.modelo.Criptomoneda;
import com.cripto.monitor.dominio.modelo.ResumenMercado;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface CriptoRepositorioPort {

    void guardarLote(List<Criptomoneda> monedas);

    List<Criptomoneda> buscarConFiltros(String texto, String tendencia, String orden, int pagina, int tamano);

    Optional<Criptomoneda> buscarPorId(String monedaId);

    Optional<Criptomoneda> incrementarLike(String monedaId);

    default Optional<Criptomoneda> actualizarAlerta(String monedaId, Double precioObjetivoUsd, Boolean destacada) {
        return Optional.empty();
    }

    default Map<String, Long> recalcularEtiquetasMasivo() {
        return Map.of();
    }

    default Map<String, Object> generarYGuardarReporteBi(long totalSnapshots, long totalComentarios) {
        return Map.of();
    }

    long contarTotal();

    ResumenMercado calcularResumen(long totalSnapshots, long totalComentarios);
}

