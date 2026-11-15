package com.cripto.monitor.infraestructura.adaptadores.salida.mongo;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SpringSnapshotRepository extends MongoRepository<SnapshotPrecioDocumento, String> {

    List<SnapshotPrecioDocumento> findByMonedaIdOrderByFechaCapturaDesc(String monedaId, Pageable pageable);
}

