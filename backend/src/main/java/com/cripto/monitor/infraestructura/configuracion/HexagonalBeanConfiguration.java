package com.cripto.monitor.infraestructura.configuracion;

import com.cripto.monitor.aplicacion.servicio.MonitorCriptoService;
import com.cripto.monitor.dominio.puertos.salida.ComentarioCriptoRepositorioPort;
import com.cripto.monitor.dominio.puertos.salida.CriptoRepositorioPort;
import com.cripto.monitor.dominio.puertos.salida.FuenteCriptoPort;
import com.cripto.monitor.dominio.puertos.salida.NotificadorCriptoPort;
import com.cripto.monitor.dominio.puertos.salida.SnapshotRepositorioPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HexagonalBeanConfiguration {

    @Bean
    public MonitorCriptoService monitorCriptoService(
            FuenteCriptoPort fuenteCriptoPort,
            CriptoRepositorioPort criptoRepositorioPort,
            SnapshotRepositorioPort snapshotRepositorioPort,
            ComentarioCriptoRepositorioPort comentarioRepositorioPort,
            NotificadorCriptoPort notificadorPort) {
        return new MonitorCriptoService(
                fuenteCriptoPort,
                criptoRepositorioPort,
                snapshotRepositorioPort,
                comentarioRepositorioPort,
                notificadorPort
        );
    }
}

