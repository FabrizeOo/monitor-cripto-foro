package com.cripto.monitor.infraestructura.adaptadores.salida.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "comentarios_cripto")
public class ComentarioCriptoDocumento {

    @Id
    private String id;

    @Indexed
    private String monedaId;

    private String simbolo;
    private String autor;
    private String postura;
    private String contenido;

    @Indexed
    private Instant fechaCreacion;

    public ComentarioCriptoDocumento() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getMonedaId() { return monedaId; }
    public void setMonedaId(String monedaId) { this.monedaId = monedaId; }

    public String getSimbolo() { return simbolo; }
    public void setSimbolo(String simbolo) { this.simbolo = simbolo; }

    public String getAutor() { return autor; }
    public void setAutor(String autor) { this.autor = autor; }

    public String getPostura() { return postura; }
    public void setPostura(String postura) { this.postura = postura; }

    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }

    public Instant getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Instant fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}

