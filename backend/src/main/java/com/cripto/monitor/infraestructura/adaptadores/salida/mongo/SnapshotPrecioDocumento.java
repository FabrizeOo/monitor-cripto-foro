package com.cripto.monitor.infraestructura.adaptadores.salida.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "snapshots_precios")
public class SnapshotPrecioDocumento {

    @Id
    private String id;

    @Indexed
    private String monedaId;

    private String simbolo;
    private double precioUsd;
    private double cambio24h;
    private double volumen24h;

    @Indexed
    private Instant fechaCaptura;

    public SnapshotPrecioDocumento() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getMonedaId() { return monedaId; }
    public void setMonedaId(String monedaId) { this.monedaId = monedaId; }

    public String getSimbolo() { return simbolo; }
    public void setSimbolo(String simbolo) { this.simbolo = simbolo; }

    public double getPrecioUsd() { return precioUsd; }
    public void setPrecioUsd(double precioUsd) { this.precioUsd = precioUsd; }

    public double getCambio24h() { return cambio24h; }
    public void setCambio24h(double cambio24h) { this.cambio24h = cambio24h; }

    public double getVolumen24h() { return volumen24h; }
    public void setVolumen24h(double volumen24h) { this.volumen24h = volumen24h; }

    public Instant getFechaCaptura() { return fechaCaptura; }
    public void setFechaCaptura(Instant fechaCaptura) { this.fechaCaptura = fechaCaptura; }
}

