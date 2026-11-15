package com.cripto.monitor.infraestructura.adaptadores.salida.mongo;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SpringComentarioCriptoRepository extends MongoRepository<ComentarioCriptoDocumento, String> {

    List<ComentarioCriptoDocumento> findAllByOrderByFechaCreacionDesc(Pageable pageable);

    List<ComentarioCriptoDocumento> findByMonedaIdOrderByFechaCreacionDesc(String monedaId);
}

