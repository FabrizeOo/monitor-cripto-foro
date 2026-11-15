package com.cripto.monitor.dominio.puertos.salida;

import com.cripto.monitor.dominio.modelo.Criptomoneda;

import java.util.List;

public interface FuenteCriptoPort {

    List<Criptomoneda> descargarMercado(int limite);
}

