package com.cripto.monitor.infraestructura.adaptadores.salida.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface SpringCriptoRepository extends MongoRepository<CriptoDocumento, String> {
}

