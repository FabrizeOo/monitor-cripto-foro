package com.cripto.monitor.infraestructura.adaptadores.salida.mongo;

import com.cripto.monitor.dominio.modelo.SnapshotPrecio;
import com.cripto.monitor.dominio.puertos.salida.SnapshotRepositorioPort;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class MongoSnapshotAdapter implements SnapshotRepositorioPort {

    private final SpringSnapshotRepository repository;

    public MongoSnapshotAdapter(SpringSnapshotRepository repository) {
        this.repository = repository;
    }

    @Override
    public void guardarLote(List<SnapshotPrecio> snapshots) {
        if (snapshots == null || snapshots.isEmpty()) {
            return;
        }
        List<SnapshotPrecioDocumento> docs = snapshots.stream().map(s -> {
            SnapshotPrecioDocumento doc = new SnapshotPrecioDocumento();
            doc.setMonedaId(s.getMonedaId());
            doc.setSimbolo(s.getSimbolo());
            doc.setPrecioUsd(s.getPrecioUsd());
            doc.setCambio24h(s.getCambio24h());
            doc.setVolumen24h(s.getVolumen24h());
            doc.setFechaCaptura(s.getFechaCaptura());
            return doc;
        }).collect(Collectors.toList());

        repository.saveAll(docs);
    }

    @Override
    public List<SnapshotPrecio> listarPorMoneda(String monedaId, int limite) {
        return repository.findByMonedaIdOrderByFechaCapturaDesc(monedaId, PageRequest.of(0, limite))
                .stream()
                .map(doc -> new SnapshotPrecio(
                        doc.getId(),
                        doc.getMonedaId(),
                        doc.getSimbolo(),
                        doc.getPrecioUsd(),
                        doc.getCambio24h(),
                        doc.getVolumen24h(),
                        doc.getFechaCaptura()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public long contarTotal() {
        return repository.count();
    }
}

