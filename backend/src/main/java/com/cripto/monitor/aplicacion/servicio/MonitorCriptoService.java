package com.cripto.monitor.aplicacion.servicio;

import com.cripto.monitor.dominio.modelo.ComentarioCripto;
import com.cripto.monitor.dominio.modelo.Criptomoneda;
import com.cripto.monitor.dominio.modelo.ResumenMercado;
import com.cripto.monitor.dominio.modelo.SnapshotPrecio;
import com.cripto.monitor.dominio.puertos.entrada.ComentarioCriptoUseCase;
import com.cripto.monitor.dominio.puertos.entrada.CriptoUseCase;
import com.cripto.monitor.dominio.puertos.salida.ComentarioCriptoRepositorioPort;
import com.cripto.monitor.dominio.puertos.salida.CriptoRepositorioPort;
import com.cripto.monitor.dominio.puertos.salida.FuenteCriptoPort;
import com.cripto.monitor.dominio.puertos.salida.NotificadorCriptoPort;
import com.cripto.monitor.dominio.puertos.salida.SnapshotRepositorioPort;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class MonitorCriptoService implements CriptoUseCase, ComentarioCriptoUseCase {

    private static final Set<String> POSTURAS_VALIDAS = Set.of("ALCISTA", "BAJISTA", "NEUTRAL");

    private final FuenteCriptoPort fuenteCriptoPort;
    private final CriptoRepositorioPort criptoRepositorioPort;
    private final SnapshotRepositorioPort snapshotRepositorioPort;
    private final ComentarioCriptoRepositorioPort comentarioRepositorioPort;
    private final NotificadorCriptoPort notificadorPort;

    public MonitorCriptoService(
            FuenteCriptoPort fuenteCriptoPort,
            CriptoRepositorioPort criptoRepositorioPort,
            SnapshotRepositorioPort snapshotRepositorioPort,
            ComentarioCriptoRepositorioPort comentarioRepositorioPort,
            NotificadorCriptoPort notificadorPort) {
        this.fuenteCriptoPort = fuenteCriptoPort;
        this.criptoRepositorioPort = criptoRepositorioPort;
        this.snapshotRepositorioPort = snapshotRepositorioPort;
        this.comentarioRepositorioPort = comentarioRepositorioPort;
        this.notificadorPort = notificadorPort;
    }

    @Override
    public List<Criptomoneda> buscarMonedas(String texto, String tendencia, String orden, int pagina, int tamano) {
        int paginaSegura = Math.max(0, pagina);
        int tamanoSeguro = Math.min(Math.max(1, tamano), 100);
        return criptoRepositorioPort.buscarConFiltros(texto, tendencia, orden, paginaSegura, tamanoSeguro);
    }

    @Override
    public List<SnapshotPrecio> obtenerSnapshots(String monedaId, int limite) {
        int limiteSeguro = Math.min(Math.max(1, limite), 50);
        return snapshotRepositorioPort.listarPorMoneda(monedaId, limiteSeguro);
    }

    @Override
    public int sincronizarMercado(int limiteMonedas, int snapshotsTopN) {
        int limiteSeguro = Math.min(Math.max(10, limiteMonedas), 2500);
        List<Criptomoneda> monedas = fuenteCriptoPort.descargarMercado(limiteSeguro);
        if (monedas.isEmpty()) {
            return 0;
        }

        criptoRepositorioPort.guardarLote(monedas);

        int topSnapshots = Math.min(Math.max(0, snapshotsTopN), monedas.size());
        if (topSnapshots > 0) {
            Instant ahora = Instant.now();
            List<SnapshotPrecio> snapshots = monedas.stream()
                    .limit(topSnapshots)
                    .map(m -> new SnapshotPrecio(
                            null,
                            m.getId(),
                            m.getSimbolo(),
                            m.getPrecioUsd(),
                            m.getCambio24h(),
                            m.getVolumen24h(),
                            ahora
                    ))
                    .collect(Collectors.toList());
            snapshotRepositorioPort.guardarLote(snapshots);
        }

        ResumenMercado resumen = obtenerResumen();
        notificadorPort.publicarEvento("mercado", Map.of(
                "actualizadas", monedas.size(),
                "resumen", resumen
        ));

        return monedas.size();
    }

    @Override
    public Optional<Criptomoneda> darLike(String monedaId) {
        Optional<Criptomoneda> actualizada = criptoRepositorioPort.incrementarLike(monedaId);
        actualizada.ifPresent(moneda -> notificadorPort.publicarEvento("like", moneda));
        return actualizada;
    }

    @Override
    public Optional<Criptomoneda> actualizarAlerta(String monedaId, Double precioObjetivoUsd, Boolean destacada) {
        Optional<Criptomoneda> actualizada = criptoRepositorioPort.actualizarAlerta(monedaId, precioObjetivoUsd, destacada);
        actualizada.ifPresent(moneda -> notificadorPort.publicarEvento("alerta", moneda));
        return actualizada;
    }

    @Override
    public Map<String, Object> recalcularEtiquetasMasivo() {
        Map<String, Long> conteos = criptoRepositorioPort.recalcularEtiquetasMasivo();
        long totalModificadas = conteos.values().stream().mapToLong(Long::longValue).sum();
        Map<String, Object> respuesta = Map.of(
                "totalModificadas", totalModificadas,
                "porEtiqueta", conteos
        );
        notificadorPort.publicarEvento("etiquetas", respuesta);
        return respuesta;
    }

    @Override
    public Map<String, Object> purgarSnapshotsAntiguos(int mantenerUltimos) {
        int mantenerSeguro = Math.max(100, mantenerUltimos);
        long eliminados = snapshotRepositorioPort.purgarAntiguos(mantenerSeguro);
        long restantes = snapshotRepositorioPort.contarTotal();
        ResumenMercado resumen = obtenerResumen();
        Map<String, Object> respuesta = Map.of(
                "eliminados", eliminados,
                "restantes", restantes,
                "resumen", resumen
        );
        notificadorPort.publicarEvento("mercado", Map.of("actualizadas", 0, "resumen", resumen));
        return respuesta;
    }

    @Override
    public Map<String, Object> generarYGuardarReporteBi() {
        long totalSnapshots = snapshotRepositorioPort.contarTotal();
        long totalComentarios = comentarioRepositorioPort.contarTotal();
        return criptoRepositorioPort.generarYGuardarReporteBi(totalSnapshots, totalComentarios);
    }

    @Override
    public ResumenMercado obtenerResumen() {
        long totalSnapshots = snapshotRepositorioPort.contarTotal();
        long totalComentarios = comentarioRepositorioPort.contarTotal();
        return criptoRepositorioPort.calcularResumen(totalSnapshots, totalComentarios);
    }

    @Override
    public List<ComentarioCripto> listarRecientes(int limite) {
        int limiteSeguro = Math.min(Math.max(1, limite), 100);
        return comentarioRepositorioPort.listarRecientes(limiteSeguro);
    }

    @Override
    public List<ComentarioCripto> listarPorMoneda(String monedaId) {
        return comentarioRepositorioPort.listarPorMoneda(monedaId);
    }

    @Override
    public ComentarioCripto publicarComentario(String monedaId, String autor, String postura, String contenido) {
        if (contenido == null || contenido.isBlank()) {
            throw new IllegalArgumentException("El comentario no puede estar vacío");
        }

        String simbolo = "MERCADO";
        if (monedaId != null && !"MERCADO".equalsIgnoreCase(monedaId)) {
            simbolo = criptoRepositorioPort.buscarPorId(monedaId)
                    .map(Criptomoneda::getSimbolo)
                    .orElse("MERCADO");
        }

        String autorLimpio = (autor == null || autor.isBlank()) ? "Trader Anónimo" : autor.trim();
        String posturaLimpia = postura != null ? postura.trim().toUpperCase() : "NEUTRAL";
        if (!POSTURAS_VALIDAS.contains(posturaLimpia)) {
            posturaLimpia = "NEUTRAL";
        }

        ComentarioCripto nuevo = new ComentarioCripto(
                null,
                monedaId != null && !monedaId.isBlank() ? monedaId : "MERCADO",
                simbolo,
                autorLimpio,
                posturaLimpia,
                contenido.trim(),
                Instant.now()
        );

        ComentarioCripto guardado = comentarioRepositorioPort.guardar(nuevo);
        notificadorPort.publicarEvento("comentario", guardado);
        return guardado;
    }

    @Override
    public Optional<ComentarioCripto> editarComentario(String id, String postura, String contenido) {
        if (contenido == null || contenido.isBlank()) {
            throw new IllegalArgumentException("El comentario no puede estar vacío");
        }
        String posturaLimpia = postura != null ? postura.trim().toUpperCase() : "NEUTRAL";
        if (!POSTURAS_VALIDAS.contains(posturaLimpia)) {
            posturaLimpia = "NEUTRAL";
        }
        Optional<ComentarioCripto> editado = comentarioRepositorioPort.actualizar(id, posturaLimpia, contenido.trim());
        editado.ifPresent(c -> notificadorPort.publicarEvento("comentario_actualizado", c));
        return editado;
    }

    @Override
    public boolean eliminarComentario(String id) {
        boolean eliminado = comentarioRepositorioPort.eliminarPorId(id);
        if (eliminado) {
            notificadorPort.publicarEvento("comentario_eliminado", Map.of("id", id));
        }
        return eliminado;
    }
}

