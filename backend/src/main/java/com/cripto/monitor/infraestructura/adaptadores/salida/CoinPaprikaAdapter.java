package com.cripto.monitor.infraestructura.adaptadores.salida;

import com.cripto.monitor.dominio.modelo.Criptomoneda;
import com.cripto.monitor.dominio.puertos.salida.FuenteCriptoPort;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CoinPaprikaAdapter implements FuenteCriptoPort {

    private static final Logger log = LoggerFactory.getLogger(CoinPaprikaAdapter.class);
    private static final String API_URL = "https://api.coinpaprika.com/v1/tickers?limit=";

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public List<Criptomoneda> descargarMercado(int limite) {
        try {
            PaprikaTicker[] respuesta = restTemplate.getForObject(API_URL + limite, PaprikaTicker[].class);
            if (respuesta == null || respuesta.length == 0) {
                return Collections.emptyList();
            }
            return Arrays.stream(respuesta)
                    .filter(t -> t.id() != null && t.quotes() != null && t.quotes().usd() != null)
                    .map(this::mapearADominio)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            log.warn("Error consultando CoinPaprika API: {}", ex.getMessage());
            return Collections.emptyList();
        }
    }

    private Criptomoneda mapearADominio(PaprikaTicker t) {
        UsdQuote usd = t.quotes().usd();
        Instant fecha = Instant.now();
        if (t.last_updated() != null && !t.last_updated().isBlank()) {
            try {
                fecha = Instant.parse(t.last_updated());
            } catch (Exception ignored) {
            }
        }

        return new Criptomoneda(
                t.id(),
                t.symbol() != null ? t.symbol() : "",
                t.name() != null ? t.name() : t.id(),
                t.rank(),
                usd.price(),
                usd.volume_24h(),
                usd.market_cap(),
                usd.percent_change_1h(),
                usd.percent_change_24h(),
                usd.percent_change_7d(),
                usd.ath_price(),
                usd.percent_from_price_ath(),
                fecha,
                0L
        );
    }

    private record PaprikaTicker(
            String id,
            String name,
            String symbol,
            int rank,
            String last_updated,
            Quotes quotes
    ) {}

    private record Quotes(
            @JsonProperty("USD") UsdQuote usd
    ) {}

    private record UsdQuote(
            double price,
            double volume_24h,
            double market_cap,
            double percent_change_1h,
            double percent_change_24h,
            double percent_change_7d,
            double ath_price,
            double percent_from_price_ath
    ) {}
}

