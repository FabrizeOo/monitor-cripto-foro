package com.cripto.monitor.infraestructura.adaptadores.entrada;

import com.cripto.monitor.dominio.puertos.entrada.CriptoUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SincronizadorMercadoScheduler {

    private static final Logger log = LoggerFactory.getLogger(SincronizadorMercadoScheduler.class);

    private final CriptoUseCase criptoUseCase;

    @Value("${app.scheduler.enabled:true}")
    private boolean schedulerEnabled;

    public SincronizadorMercadoScheduler(CriptoUseCase criptoUseCase) {
        this.criptoUseCase = criptoUseCase;
    }

    @Scheduled(fixedDelayString = "${app.scheduler.interval-ms:90000}", initialDelay = 90000)
    public void capturarSnapshotPeriodico() {
        if (!schedulerEnabled) {
            return;
        }
        try {
            int actualizadas = criptoUseCase.sincronizarMercado(500, 50);
            log.debug("Snapshot periódico guardado en MongoDB ({} monedas actualizadas).", actualizadas);
        } catch (Exception ex) {
            log.warn("Error en sincronización periódica: {}", ex.getMessage());
        }
    }
}

