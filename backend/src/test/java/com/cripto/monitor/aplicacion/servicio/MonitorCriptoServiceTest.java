package com.cripto.monitor.aplicacion.servicio;

import com.cripto.monitor.dominio.modelo.ComentarioCripto;
import com.cripto.monitor.dominio.modelo.Criptomoneda;
import com.cripto.monitor.dominio.modelo.ResumenMercado;
import com.cripto.monitor.dominio.modelo.SnapshotPrecio;
import com.cripto.monitor.dominio.puertos.salida.ComentarioCriptoRepositorioPort;
import com.cripto.monitor.dominio.puertos.salida.CriptoRepositorioPort;
import com.cripto.monitor.dominio.puertos.salida.FuenteCriptoPort;
import com.cripto.monitor.dominio.puertos.salida.NotificadorCriptoPort;
import com.cripto.monitor.dominio.puertos.salida.SnapshotRepositorioPort;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class MonitorCriptoServiceTest {

    @Test
    void sincronizarMercadoGeneraSnapshotsYPublicaComentarios() {
        List<Criptomoneda> monedasGuardadas = new ArrayList<>();
        List<SnapshotPrecio> snapshotsGuardados = new ArrayList<>();
        List<String> eventos = new ArrayList<>();

        FuenteCriptoPort fuente = limite -> List.of(
                new Criptomoneda("btc-bitcoin", "BTC", "Bitcoin", 1, 63700.0, 2.9e10, 1.2e12, 0.5, -2.1, 4.0, 126000.0, -49.0, Instant.now(), 0)
        );

        CriptoRepositorioPort repoCripto = new CriptoRepositorioPort() {
            @Override public void guardarLote(List<Criptomoneda> m) { monedasGuardadas.addAll(m); }
            @Override public List<Criptomoneda> buscarConFiltros(String t, String te, String o, int p, int ta) { return monedasGuardadas; }
            @Override public Optional<Criptomoneda> buscarPorId(String id) { return monedasGuardadas.stream().filter(m -> m.getId().equals(id)).findFirst(); }
            @Override public Optional<Criptomoneda> incrementarLike(String id) { return Optional.empty(); }
            @Override public long contarTotal() { return monedasGuardadas.size(); }
            @Override public ResumenMercado calcularResumen(long s, long c) { return new ResumenMercado(monedasGuardadas.size(), s, c, 0, 1, -2.1); }
        };

        SnapshotRepositorioPort repoSnap = new SnapshotRepositorioPort() {
            @Override public void guardarLote(List<SnapshotPrecio> s) { snapshotsGuardados.addAll(s); }
            @Override public List<SnapshotPrecio> listarPorMoneda(String id, int l) { return snapshotsGuardados; }
            @Override public long contarTotal() { return snapshotsGuardados.size(); }
        };

        ComentarioCriptoRepositorioPort repoCom = new ComentarioCriptoRepositorioPort() {
            @Override public ComentarioCripto guardar(ComentarioCripto c) { c.setId("com-1"); return c; }
            @Override public List<ComentarioCripto> listarRecientes(int l) { return List.of(); }
            @Override public List<ComentarioCripto> listarPorMoneda(String id) { return List.of(); }
            @Override public long contarTotal() { return 1; }
        };

        NotificadorCriptoPort notificador = (tipo, payload) -> eventos.add(tipo);

        MonitorCriptoService service = new MonitorCriptoService(fuente, repoCripto, repoSnap, repoCom, notificador);

        int actualizadas = service.sincronizarMercado(100, 10);
        assertEquals(1, actualizadas);
        assertEquals(1, snapshotsGuardados.size());

        ComentarioCripto comentario = service.publicarComentario("btc-bitcoin", "Trader", "alcista", "Buen soporte");
        assertEquals("BTC", comentario.getSimbolo());
        assertEquals("ALCISTA", comentario.getPostura());
        assertEquals(List.of("mercado", "comentario"), eventos);
    }
}

