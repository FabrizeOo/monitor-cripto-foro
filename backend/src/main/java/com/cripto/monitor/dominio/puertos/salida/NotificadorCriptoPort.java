package com.cripto.monitor.dominio.puertos.salida;

public interface NotificadorCriptoPort {

    void publicarEvento(String tipoEvento, Object payload);
}

