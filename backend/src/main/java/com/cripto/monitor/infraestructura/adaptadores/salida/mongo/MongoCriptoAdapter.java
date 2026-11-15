package com.cripto.monitor.infraestructura.adaptadores.salida.mongo;

import com.cripto.monitor.dominio.modelo.Criptomoneda;
import com.cripto.monitor.dominio.modelo.ResumenMercado;
import com.cripto.monitor.dominio.puertos.salida.CriptoRepositorioPort;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class MongoCriptoAdapter implements CriptoRepositorioPort {

    private final MongoTemplate mongoTemplate;
    private final SpringCriptoRepository repository;

    public MongoCriptoAdapter(MongoTemplate mongoTemplate, SpringCriptoRepository repository) {
        this.mongoTemplate = mongoTemplate;
        this.repository = repository;
    }

    @Override
    public void guardarLote(List<Criptomoneda> monedas) {
        if (monedas == null || monedas.isEmpty()) {
            return;
        }
        BulkOperations bulk = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, CriptoDocumento.class);
        for (Criptomoneda m : monedas) {
            Query query = Query.query(Criteria.where("_id").is(m.getId()));
            Update update = new Update()
                    .set("simbolo", m.getSimbolo())
                    .set("nombre", m.getNombre())
                    .set("rank", m.getRank())
                    .set("precioUsd", m.getPrecioUsd())
                    .set("volumen24h", m.getVolumen24h())
                    .set("marketCap", m.getMarketCap())
                    .set("cambio1h", m.getCambio1h())
                    .set("cambio24h", m.getCambio24h())
                    .set("cambio7d", m.getCambio7d())
                    .set("athPrecio", m.getAthPrecio())
                    .set("porcentajeDesdeAth", m.getPorcentajeDesdeAth())
                    .set("ultimaActualizacion", m.getUltimaActualizacion())
                    .setOnInsert("likes", m.getLikes());
            bulk.upsert(query, update);
        }
        bulk.execute();
    }

    @Override
    public List<Criptomoneda> buscarConFiltros(String texto, String tendencia, String orden, int pagina, int tamano) {
        List<Criteria> criterios = new ArrayList<>();

        if (texto != null && !texto.isBlank()) {
            String escaped = Pattern.quote(texto.trim());
            criterios.add(new Criteria().orOperator(
                    Criteria.where("nombre").regex(escaped, "i"),
                    Criteria.where("simbolo").regex(escaped, "i")
            ));
        }

        if ("ALZA".equalsIgnoreCase(tendencia)) {
            criterios.add(Criteria.where("cambio24h").gt(0));
        } else if ("BAJA".equalsIgnoreCase(tendencia)) {
            criterios.add(Criteria.where("cambio24h").lt(0));
        }

        Query query = new Query();
        if (!criterios.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criterios.toArray(new Criteria[0])));
        }

        Sort sort = switch (orden != null ? orden.toUpperCase() : "RANK") {
            case "GANADORAS" -> Sort.by(Sort.Order.desc("cambio24h"));
            case "PERDEDORAS" -> Sort.by(Sort.Order.asc("cambio24h"));
            case "VOLUMEN" -> Sort.by(Sort.Order.desc("volumen24h"));
            default -> Sort.by(Sort.Order.asc("rank"));
        };

        query.with(sort);
        query.skip((long) pagina * tamano);
        query.limit(tamano);

        return mongoTemplate.find(query, CriptoDocumento.class)
                .stream()
                .map(this::mapearADominio)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Criptomoneda> buscarPorId(String monedaId) {
        return repository.findById(monedaId).map(this::mapearADominio);
    }

    @Override
    public Optional<Criptomoneda> incrementarLike(String monedaId) {
        Query query = Query.query(Criteria.where("_id").is(monedaId));
        Update update = new Update().inc("likes", 1);
        CriptoDocumento modificado = mongoTemplate.findAndModify(
                query,
                update,
                FindAndModifyOptions.options().returnNew(true),
                CriptoDocumento.class
        );
        return Optional.ofNullable(modificado).map(this::mapearADominio);
    }

    @Override
    public long contarTotal() {
        return repository.count();
    }

    @Override
    public ResumenMercado calcularResumen(long totalSnapshots, long totalComentarios) {
        long totalMonedas = repository.count();
        long enAlza = mongoTemplate.count(Query.query(Criteria.where("cambio24h").gt(0)), CriptoDocumento.class);
        long enBaja = mongoTemplate.count(Query.query(Criteria.where("cambio24h").lt(0)), CriptoDocumento.class);

        Aggregation agg = Aggregation.newAggregation(
                Aggregation.group().avg("cambio24h").as("promedio")
        );
        AggregationResults<PromedioAgg> res = mongoTemplate.aggregate(agg, "criptomonedas", PromedioAgg.class);
        PromedioAgg unique = res.getUniqueMappedResult();
        double promedio = unique != null ? Math.round(unique.getPromedio() * 100.0) / 100.0 : 0.0;

        return new ResumenMercado(totalMonedas, totalSnapshots, totalComentarios, enAlza, enBaja, promedio);
    }

    private Criptomoneda mapearADominio(CriptoDocumento doc) {
        return new Criptomoneda(
                doc.getId(),
                doc.getSimbolo(),
                doc.getNombre(),
                doc.getRank(),
                doc.getPrecioUsd(),
                doc.getVolumen24h(),
                doc.getMarketCap(),
                doc.getCambio1h(),
                doc.getCambio24h(),
                doc.getCambio7d(),
                doc.getAthPrecio(),
                doc.getPorcentajeDesdeAth(),
                doc.getUltimaActualizacion(),
                doc.getLikes()
        );
    }

    private static class PromedioAgg {
        private double promedio;
        public double getPromedio() { return promedio; }
    }
}

