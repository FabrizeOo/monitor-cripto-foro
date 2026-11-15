package com.cripto.monitor.dominio.modelo;

import java.time.Instant;

/**
 * Registro histórico de precio capturado periódicamente para análisis en MongoDB.
 */
public class SnapshotPrecio {

    private String id;
    private String monedaId;
    private String simbolo;
    private double precioUsd;
    private double cambio24h;
    private double volumen24h;
    private Instant fechaCaptura;

    public SnapshotPrecio() {
    }

    public SnapshotPrecio(
            String id,
            String monedaId,
            String simbolo,
            double precioUsd,
            double cambio24h,
            double volumen24h,
            Instant fechaCaptura) {
        this.id = id;
        this.monedaId = monedaId;
        this.simbolo = simbolo;
        this.precioUsd = precioUsd;
        this.cambio24h = cambio24h;
        this.volumen24h = volumen24h;
        this.fechaCaptura = fechaCaptura;
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

