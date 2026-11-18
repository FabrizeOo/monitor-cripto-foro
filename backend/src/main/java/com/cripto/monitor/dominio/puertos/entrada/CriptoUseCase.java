package com.cripto.monitor.dominio.puertos.entrada;

import com.cripto.monitor.dominio.modelo.Criptomoneda;
import com.cripto.monitor.dominio.modelo.ResumenMercado;
import com.cripto.monitor.dominio.modelo.SnapshotPrecio;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface CriptoUseCase {

    List<Criptomoneda> buscarMonedas(String texto, String tendencia, String orden, int pagina, int tamano);

    List<SnapshotPrecio> obtenerSnapshots(String monedaId, int limite);

    int sincronizarMercado(int limiteMonedas, int snapshotsTopN);

    Optional<Criptomoneda> darLike(String monedaId);

    Optional<Criptomoneda> actualizarAlerta(String monedaId, Double precioObjetivoUsd, Boolean destacada);

    Map<String, Object> recalcularEtiquetasMasivo();

    Map<String, Object> purgarSnapshotsAntiguos(int mantenerUltimos);

    Map<String, Object> generarYGuardarReporteBi();

    ResumenMercado obtenerResumen();
}

