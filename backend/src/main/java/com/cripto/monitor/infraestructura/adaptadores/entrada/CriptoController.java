package com.cripto.monitor.infraestructura.adaptadores.entrada;

import com.cripto.monitor.dominio.modelo.Criptomoneda;
import com.cripto.monitor.dominio.modelo.ResumenMercado;
import com.cripto.monitor.dominio.modelo.SnapshotPrecio;
import com.cripto.monitor.dominio.puertos.entrada.CriptoUseCase;
import com.cripto.monitor.infraestructura.adaptadores.salida.SseNotificadorCriptoAdapter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/criptos")
@CrossOrigin(origins = "*")
public class CriptoController {

    private final CriptoUseCase criptoUseCase;
    private final SseNotificadorCriptoAdapter sseNotificador;

    public CriptoController(CriptoUseCase criptoUseCase, SseNotificadorCriptoAdapter sseNotificador) {
        this.criptoUseCase = criptoUseCase;
        this.sseNotificador = sseNotificador;
    }

    @GetMapping
    public List<Criptomoneda> buscarMonedas(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "TODAS") String tendencia,
            @RequestParam(defaultValue = "RANK") String orden,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "30") int tamano) {
        return criptoUseCase.buscarMonedas(q, tendencia, orden, pagina, tamano);
    }

    @GetMapping("/{monedaId}/snapshots")
    public List<SnapshotPrecio> obtenerSnapshots(
            @PathVariable String monedaId,
            @RequestParam(defaultValue = "20") int limite) {
        return criptoUseCase.obtenerSnapshots(monedaId, limite);
    }

    @PostMapping("/sincronizar")
    public Map<String, Object> sincronizarMercado(
            @RequestParam(defaultValue = "1000") int limite,
            @RequestParam(defaultValue = "100") int snapshotsTop) {
        int actualizadas = criptoUseCase.sincronizarMercado(limite, snapshotsTop);
        ResumenMercado resumen = criptoUseCase.obtenerResumen();
        return Map.of(
                "actualizadas", actualizadas,
                "resumen", resumen
        );
    }

    @PostMapping("/{monedaId}/like")
    public ResponseEntity<Criptomoneda> darLike(@PathVariable String monedaId) {
        return criptoUseCase.darLike(monedaId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{monedaId}/alerta")
    public ResponseEntity<Criptomoneda> actualizarAlerta(
            @PathVariable String monedaId,
            @RequestBody AlertaCriptoRequest request) {
        return criptoUseCase.actualizarAlerta(monedaId, request.precioObjetivoUsd(), request.destacada())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/recalcular-etiquetas")
    public Map<String, Object> recalcularEtiquetasMasivo() {
        return criptoUseCase.recalcularEtiquetasMasivo();
    }

    @DeleteMapping("/snapshots/purgar")
    public Map<String, Object> purgarSnapshots(@RequestParam(defaultValue = "1500") int mantenerUltimos) {
        return criptoUseCase.purgarSnapshotsAntiguos(mantenerUltimos);
    }

    @PostMapping("/reporte-bi")
    public Map<String, Object> generarReporteBi() {
        return criptoUseCase.generarYGuardarReporteBi();
    }

    public record AlertaCriptoRequest(Double precioObjetivoUsd, Boolean destacada) {}

    @GetMapping("/resumen")
    public ResumenMercado obtenerResumen() {
        return criptoUseCase.obtenerResumen();
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamTiempoReal() {
        return sseNotificador.suscribir();
    }
}

