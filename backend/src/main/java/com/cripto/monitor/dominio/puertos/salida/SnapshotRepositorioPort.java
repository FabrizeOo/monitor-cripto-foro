package com.cripto.monitor.dominio.puertos.salida;

import com.cripto.monitor.dominio.modelo.SnapshotPrecio;

import java.util.List;

public interface SnapshotRepositorioPort {

    void guardarLote(List<SnapshotPrecio> snapshots);

    List<SnapshotPrecio> listarPorMoneda(String monedaId, int limite);

    default long purgarAntiguos(int mantenerUltimos) {
        return 0;
    }

    long contarTotal();
}

