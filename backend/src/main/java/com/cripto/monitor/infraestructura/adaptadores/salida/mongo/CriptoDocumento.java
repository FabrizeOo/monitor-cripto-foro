package com.cripto.monitor.infraestructura.adaptadores.salida.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "criptomonedas")
public class CriptoDocumento {

    @Id
    private String id;

    @Indexed
    private String simbolo;

    @Indexed
    private String nombre;

    @Indexed
    private int rank;

    private double precioUsd;
    private double volumen24h;
    private double marketCap;
    private double cambio1h;

    @Indexed
    private double cambio24h;

    private double cambio7d;
    private double athPrecio;
    private double porcentajeDesdeAth;
    private Instant ultimaActualizacion;
    private long likes;

    public CriptoDocumento() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSimbolo() { return simbolo; }
    public void setSimbolo(String simbolo) { this.simbolo = simbolo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }

    public double getPrecioUsd() { return precioUsd; }
    public void setPrecioUsd(double precioUsd) { this.precioUsd = precioUsd; }

    public double getVolumen24h() { return volumen24h; }
    public void setVolumen24h(double volumen24h) { this.volumen24h = volumen24h; }

    public double getMarketCap() { return marketCap; }
    public void setMarketCap(double marketCap) { this.marketCap = marketCap; }

    public double getCambio1h() { return cambio1h; }
    public void setCambio1h(double cambio1h) { this.cambio1h = cambio1h; }

    public double getCambio24h() { return cambio24h; }
    public void setCambio24h(double cambio24h) { this.cambio24h = cambio24h; }

    public double getCambio7d() { return cambio7d; }
    public void setCambio7d(double cambio7d) { this.cambio7d = cambio7d; }

    public double getAthPrecio() { return athPrecio; }
    public void setAthPrecio(double athPrecio) { this.athPrecio = athPrecio; }

    public double getPorcentajeDesdeAth() { return porcentajeDesdeAth; }
    public void setPorcentajeDesdeAth(double porcentajeDesdeAth) { this.porcentajeDesdeAth = porcentajeDesdeAth; }

    public Instant getUltimaActualizacion() { return ultimaActualizacion; }
    public void setUltimaActualizacion(Instant ultimaActualizacion) { this.ultimaActualizacion = ultimaActualizacion; }

    public long getLikes() { return likes; }
    public void setLikes(long likes) { this.likes = likes; }
}

