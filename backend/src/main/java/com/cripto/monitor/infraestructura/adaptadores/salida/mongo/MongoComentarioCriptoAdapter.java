package com.cripto.monitor.infraestructura.adaptadores.salida.mongo;

import com.cripto.monitor.dominio.modelo.ComentarioCripto;
import com.cripto.monitor.dominio.puertos.salida.ComentarioCriptoRepositorioPort;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class MongoComentarioCriptoAdapter implements ComentarioCriptoRepositorioPort {

    private final SpringComentarioCriptoRepository repository;

    public MongoComentarioCriptoAdapter(SpringComentarioCriptoRepository repository) {
        this.repository = repository;
    }

    @Override
    public ComentarioCripto guardar(ComentarioCripto comentario) {
        ComentarioCriptoDocumento doc = new ComentarioCriptoDocumento();
        doc.setMonedaId(comentario.getMonedaId());
        doc.setSimbolo(comentario.getSimbolo());
        doc.setAutor(comentario.getAutor());
        doc.setPostura(comentario.getPostura());
        doc.setContenido(comentario.getContenido());
        doc.setFechaCreacion(comentario.getFechaCreacion());

        ComentarioCriptoDocumento guardado = repository.save(doc);
        return mapearADominio(guardado);
    }

    @Override
    public List<ComentarioCripto> listarRecientes(int limite) {
        return repository.findAllByOrderByFechaCreacionDesc(PageRequest.of(0, limite))
                .stream()
                .map(this::mapearADominio)
                .collect(Collectors.toList());
    }

    @Override
    public List<ComentarioCripto> listarPorMoneda(String monedaId) {
        return repository.findByMonedaIdOrderByFechaCreacionDesc(monedaId)
                .stream()
                .map(this::mapearADominio)
                .collect(Collectors.toList());
    }

    @Override
    public long contarTotal() {
        return repository.count();
    }

    private ComentarioCripto mapearADominio(ComentarioCriptoDocumento doc) {
        return new ComentarioCripto(
                doc.getId(),
                doc.getMonedaId(),
                doc.getSimbolo(),
                doc.getAutor(),
                doc.getPostura(),
                doc.getContenido(),
                doc.getFechaCreacion()
        );
    }
}

