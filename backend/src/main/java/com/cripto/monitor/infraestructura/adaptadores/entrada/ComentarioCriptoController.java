package com.cripto.monitor.infraestructura.adaptadores.entrada;

import com.cripto.monitor.dominio.modelo.ComentarioCripto;
import com.cripto.monitor.dominio.puertos.entrada.ComentarioCriptoUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comentarios")
@CrossOrigin(origins = "*")
public class ComentarioCriptoController {

    private final ComentarioCriptoUseCase comentarioUseCase;

    public ComentarioCriptoController(ComentarioCriptoUseCase comentarioUseCase) {
        this.comentarioUseCase = comentarioUseCase;
    }

    @GetMapping
    public List<ComentarioCripto> listarRecientes(@RequestParam(defaultValue = "35") int limite) {
        return comentarioUseCase.listarRecientes(limite);
    }

    @GetMapping("/moneda/{monedaId}")
    public List<ComentarioCripto> listarPorMoneda(@PathVariable String monedaId) {
        return comentarioUseCase.listarPorMoneda(monedaId);
    }

    @PostMapping
    public ResponseEntity<ComentarioCripto> publicarComentario(@RequestBody NuevoComentarioCriptoRequest request) {
        ComentarioCripto creado = comentarioUseCase.publicarComentario(
                request.monedaId(),
                request.autor(),
                request.postura(),
                request.contenido()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    public record NuevoComentarioCriptoRequest(
            String monedaId,
            String autor,
            String postura,
            String contenido
    ) {}
}

