package com.cripto.monitor.dominio.modelo;

import java.time.Instant;

/**
 * Comentario comunitario con postura de mercado (estilo X / StockTwits).
 */
public class ComentarioCripto {

    private String id;
    private String monedaId;
    private String simbolo;
    private String autor;
    private String postura;
    private String contenido;
    private Instant fechaCreacion;

    public ComentarioCripto() {
    }

    public ComentarioCripto(
            String id,
            String monedaId,
            String simbolo,
            String autor,
            String postura,
            String contenido,
            Instant fechaCreacion) {
        this.id = id;
        this.monedaId = monedaId;
        this.simbolo = simbolo;
        this.autor = autor;
        this.postura = postura;
        this.contenido = contenido;
        this.fechaCreacion = fechaCreacion;
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

