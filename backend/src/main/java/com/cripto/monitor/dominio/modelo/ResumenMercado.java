package com.cripto.monitor.dominio.modelo;

/**
 * Métricas globales del mercado calculadas con agregaciones sobre MongoDB.
 */
public record ResumenMercado(
        long totalMonedas,
        long totalSnapshots,
        long totalComentarios,
        long monedasEnAlza,
        long monedasEnBaja,
        double promedioCambio24h
) {}

