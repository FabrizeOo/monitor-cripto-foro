package com.cripto.monitor.infraestructura.adaptadores.salida.mongo;

import com.cripto.monitor.dominio.modelo.SnapshotPrecio;
import com.cripto.monitor.dominio.puertos.salida.SnapshotRepositorioPort;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class MongoSnapshotAdapter implements SnapshotRepositorioPort {

    private final SpringSnapshotRepository repository;
    private final MongoTemplate mongoTemplate;

    public MongoSnapshotAdapter(SpringSnapshotRepository repository, MongoTemplate mongoTemplate) {
        this.repository = repository;
        this.mongoTemplate = mongoTemplate;
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
    public long purgarAntiguos(int mantenerUltimos) {
        long total = repository.count();
        if (total <= mantenerUltimos) {
            return 0;
        }
        Query queryAntiguos = new Query()
                .with(Sort.by(Sort.Order.desc("fechaCaptura"), Sort.Order.desc("_id")))
                .skip(mantenerUltimos);
        queryAntiguos.fields().include("_id");
        List<String> idsEliminar = mongoTemplate.find(queryAntiguos, SnapshotPrecioDocumento.class)
                .stream()
                .map(SnapshotPrecioDocumento::getId)
                .collect(Collectors.toList());
        if (idsEliminar.isEmpty()) {
            return 0;
        }
        return mongoTemplate.remove(
                Query.query(Criteria.where("_id").in(idsEliminar)),
                SnapshotPrecioDocumento.class
        ).getDeletedCount();
    }

    @Override
    public long contarTotal() {
        return repository.count();
    }
}

