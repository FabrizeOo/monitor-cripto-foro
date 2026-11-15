package com.cripto.monitor.infraestructura.configuracion;

import com.cripto.monitor.dominio.puertos.entrada.CriptoUseCase;
import com.cripto.monitor.dominio.puertos.salida.CriptoRepositorioPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class InicializadorCripto implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(InicializadorCripto.class);

    private final CriptoUseCase criptoUseCase;
    private final CriptoRepositorioPort repositorioPort;

    @Value("${app.seed-on-startup:true}")
    private boolean seedOnStartup;

    @Value("${app.seed-limit:1500}")
    private int seedLimit;

    public InicializadorCripto(CriptoUseCase criptoUseCase, CriptoRepositorioPort repositorioPort) {
        this.criptoUseCase = criptoUseCase;
        this.repositorioPort = repositorioPort;
    }

    @Override
    public void run(String... args) {
        if (!seedOnStartup) {
            return;
        }
        try {
            long existentes = repositorioPort.contarTotal();
            if (existentes == 0) {
                log.info("MongoDB vacío. Importando {} criptomonedas JSON desde CoinPaprika...", seedLimit);
                int importadas = criptoUseCase.sincronizarMercado(seedLimit, 150);
                log.info("Carga inicial completada: {} monedas + 150 snapshots guardados.", importadas);
            } else {
                log.info("MongoDB ya contiene {} criptomonedas.", existentes);
            }
        } catch (Exception ex) {
            log.warn("No se pudo ejecutar la carga inicial automática (verifica tu MONGODB_URI): {}", ex.getMessage());
        }
    }
}

