// URL del backend (cámbiala por tu URL de Render/Railway al subir a Vercel)
const API_BASE_URL = new URLSearchParams(window.location.search).get("api") || "http://localhost:8080";

let paginaActual = 0;
const TAMANO_PAGINA = 30;
let monedaSeleccionadaId = "MERCADO";
let monedasActuales = [];
let eventSource = null;
let debounceTimer = null;

const el = {
    tickerTrack: document.getElementById("tickerTrack"),
    marketStatus: document.getElementById("marketStatus"),
    statusLabel: document.getElementById("statusLabel"),
    btnActualizarPrecios: document.getElementById("btnActualizarPrecios"),
    statMonedas: document.getElementById("statMonedas"),
    statPromedio: document.getElementById("statPromedio"),
    statComentarios: document.getElementById("statComentarios"),
    statSentimientoTexto: document.getElementById("statSentimientoTexto"),
    barAlza: document.getElementById("barAlza"),
    radarCards: document.getElementById("radarCards"),
    inputBuscar: document.getElementById("inputBuscar"),
    selectTendencia: document.getElementById("selectTendencia"),
    selectOrden: document.getElementById("selectOrden"),
    tablaMonedas: document.getElementById("tablaMonedas"),
    btnPrev: document.getElementById("btnPrev"),
    btnNext: document.getElementById("btnNext"),
    paginaLabel: document.getElementById("paginaLabel"),
    formComentario: document.getElementById("formComentario"),
    targetLabel: document.getElementById("targetLabel"),
    btnLimpiarTarget: document.getElementById("btnLimpiarTarget"),
    inputAutor: document.getElementById("inputAutor"),
    inputContenido: document.getElementById("inputContenido"),
    selectPostura: document.getElementById("selectPostura"),
    feedComentarios: document.getElementById("feedComentarios"),
    modalDetalle: document.getElementById("modalDetalle"),
    btnCerrarDetalle: document.getElementById("btnCerrarDetalle"),
    detalleContenido: document.getElementById("detalleContenido")
};

function iniciar() {
    registrarEventosUI();
    cargarTodo();
    cargarRadarOportunidades();
    conectarTiempoReal();
}

function registrarEventosUI() {
    el.inputBuscar.addEventListener("input", () => {
        clearTimeout(debounceTimer);
        debounceTimer = setTimeout(() => {
            paginaActual = 0;
            cargarMonedas();
        }, 240);
    });

    el.selectTendencia.addEventListener("change", () => {
        paginaActual = 0;
        cargarMonedas();
    });

    el.selectOrden.addEventListener("change", () => {
        paginaActual = 0;
        cargarMonedas();
    });

    el.btnPrev.addEventListener("click", () => {
        if (paginaActual > 0) {
            paginaActual--;
            cargarMonedas();
        }
    });

    el.btnNext.addEventListener("click", () => {
        paginaActual++;
        cargarMonedas();
    });

    el.btnActualizarPrecios.addEventListener("click", actualizarPrecios);
    el.formComentario.addEventListener("submit", enviarComentario);
    el.btnLimpiarTarget.addEventListener("click", () => seleccionarObjetivo("MERCADO", "MERCADO GENERAL"));
    el.btnCerrarDetalle.addEventListener("click", () => el.modalDetalle.classList.add("hidden"));
    el.modalDetalle.addEventListener("click", (e) => {
        if (e.target === el.modalDetalle) el.modalDetalle.classList.add("hidden");
    });
}

async function cargarTodo() {
    await Promise.all([cargarResumen(), cargarMonedas(), cargarComentarios()]);
}

async function cargarResumen() {
    try {
        const res = await fetch(`${API_BASE_URL}/api/criptos/resumen`);
        if (!res.ok) throw new Error();
        pintarResumen(await res.json());
    } catch {
        // Silencioso
    }
}

function pintarResumen(r) {
    el.statMonedas.textContent = (r.totalMonedas ?? 0).toLocaleString("es-ES");
    el.statComentarios.textContent = (r.totalComentarios ?? 0).toLocaleString("es-ES");

    const prom = r.promedioCambio24h ?? 0;
    el.statPromedio.textContent = `${prom >= 0 ? "+" : ""}${prom}%`;
    el.statPromedio.className = `kpi-val ${prom >= 0 ? "pos" : "neg"}`;

    const alza = r.monedasEnAlza ?? 0;
    const baja = r.monedasEnBaja ?? 0;
    const total = alza + baja;
    const pctAlza = total > 0 ? Math.round((alza / total) * 100) : 50;
    el.statSentimientoTexto.textContent = `${pctAlza}% alcista (${alza} vs ${baja})`;
    el.barAlza.style.width = `${pctAlza}%`;
}

async function cargarRadarOportunidades() {
    try {
        const res = await fetch(`${API_BASE_URL}/api/criptos?orden=RANK&pagina=0&tamano=60`);
        if (!res.ok) throw new Error();
        const top60 = await res.json();
        if (!top60.length) return;

        pintarTickerTape(top60.slice(0, 16));

        const mejorMomentum = [...top60].sort((a, b) => b.cambio24h - a.cambio24h)[0];
        const mayorDescuentoAth = [...top60]
            .filter(m => m.athPrecio > 0 && m.porcentajeDesdeAth < -20)
            .sort((a, b) => a.porcentajeDesdeAth - b.porcentajeDesdeAth)[0] || top60[1];
        const mayorVolumen = [...top60].sort((a, b) => b.volumen24h - a.volumen24h)[0];

        const tarjetas = [
            {
                badge: "🚀 Mejor momentum hoy",
                cls: "badge-momentum",
                moneda: mejorMomentum,
                razon: `Sube ${formatearPorcentaje(mejorMomentum.cambio24h)} en 24h con fuerte interés comprador.`
            },
            {
                badge: "💎 Oportunidad vs máximo histórico",
                cls: "badge-rebote",
                moneda: mayorDescuentoAth,
                razon: `Cotiza un ${Math.abs(mayorDescuentoAth.porcentajeDesdeAth).toFixed(1)}% debajo de su máximo histórico.`
            },
            {
                badge: "📊 Mayor liquidez del día",
                cls: "badge-comunidad",
                moneda: mayorVolumen,
                razon: `Mueve ${formatearCompacto(mayorVolumen.volumen24h)} en 24h; alta estabilidad de mercado.`
            }
        ];

        el.radarCards.innerHTML = tarjetas.map((t, idx) => `
            <article class="radar-card" style="animation-delay:${idx * 80}ms" onclick="abrirAnalisis('${escaparHtml(t.moneda.id)}')">
                <span class="radar-badge ${t.cls}">${t.badge}</span>
                <div class="radar-title">
                    <strong>${escaparHtml(t.moneda.nombre)} (${escaparHtml(t.moneda.simbolo)})</strong>
                    <span class="mono">${formatearPrecio(t.moneda.precioUsd)}</span>
                </div>
                <p class="radar-reason">${t.razon}</p>
            </article>
        `).join("");
    } catch {
        // Silencioso
    }
}

function pintarTickerTape(lista) {
    const items = lista.map(m => `
        <span class="ticker-item" onclick="abrirAnalisis('${escaparHtml(m.id)}')">
            <span class="ticker-sym">${escaparHtml(m.simbolo)}</span>
            <span class="mono">${formatearPrecio(m.precioUsd)}</span>
            <span class="${m.cambio24h >= 0 ? "pos" : "neg"}">${formatearPorcentaje(m.cambio24h)}</span>
        </span>
    `).join("");
    el.tickerTrack.innerHTML = items + items;
}

function evaluarSugerencia(m) {
    if (m.cambio24h > 4 && m.cambio7d > 0) {
        return { texto: "Momentum fuerte", clase: "adv-buy", detalle: "Presenta impulso positivo tanto en las últimas 24h como en la semana. Conviene seguir de cerca si mantiene volumen." };
    }
    if (m.porcentajeDesdeAth <= -55 && m.rank <= 50) {
        return { texto: "Zona de descuento", clase: "adv-watch", detalle: "Proyecto consolidado cotizando muy por debajo de su máximo histórico. Interesante para analizar acumulación gradual." };
    }
    if (m.cambio24h < -6) {
        return { texto: "Alta volatilidad", clase: "adv-caution", detalle: "Está sufriendo presión vendedora fuerte hoy. Recomendable esperar confirmación de soporte antes de entrar." };
    }
    return { texto: "Estable / Vigilar", clase: "adv-hold", detalle: "Comportamiento lateral dentro del rango normal del mercado." };
}

async function cargarMonedas() {
    const params = new URLSearchParams({
        q: el.inputBuscar.value.trim(),
        tendencia: el.selectTendencia.value,
        orden: el.selectOrden.value,
        pagina: paginaActual,
        tamano: TAMANO_PAGINA
    });

    el.paginaLabel.textContent = `Página ${paginaActual + 1}`;
    el.btnPrev.disabled = paginaActual === 0;

    try {
        const res = await fetch(`${API_BASE_URL}/api/criptos?${params.toString()}`);
        if (!res.ok) throw new Error();
        monedasActuales = await res.json();
        pintarMonedas(monedasActuales);
        el.btnNext.disabled = monedasActuales.length < TAMANO_PAGINA;
    } catch {
        el.tablaMonedas.innerHTML = `<tr><td colspan="8" class="empty-row">No se pudieron cargar las cotizaciones en este momento.</td></tr>`;
    }
}

function pintarMonedas(lista) {
    if (!lista || lista.length === 0) {
        el.tablaMonedas.innerHTML = `<tr><td colspan="8" class="empty-row">No encontramos criptomonedas con ese filtro.</td></tr>`;
        return;
    }

    el.tablaMonedas.innerHTML = lista.map(m => {
        const sug = evaluarSugerencia(m);
        return `
        <tr data-coin-id="${escaparHtml(m.id)}" onclick="abrirAnalisis('${escaparHtml(m.id)}')">
            <td>${m.rank}</td>
            <td>
                <div class="asset-cell">
                    <strong>${escaparHtml(m.nombre)}</strong>
                    <span class="asset-sym">${escaparHtml(m.simbolo)}</span>
                </div>
            </td>
            <td><strong>${formatearPrecio(m.precioUsd)}</strong></td>
            <td class="${m.cambio1h >= 0 ? "pos" : "neg"}">${formatearPorcentaje(m.cambio1h)}</td>
            <td class="${m.cambio24h >= 0 ? "pos" : "neg"}">${formatearPorcentaje(m.cambio24h)}</td>
            <td class="${m.cambio7d >= 0 ? "pos" : "neg"}">${formatearPorcentaje(m.cambio7d)}</td>
            <td><span class="advice-pill ${sug.clase}">${sug.texto}</span></td>
            <td onclick="event.stopPropagation()">
                <div class="row-btns">
                    <button class="btn btn-ghost" onclick="darLike('${escaparHtml(m.id)}')" title="Apoyar">
                        🔥 <span class="like-count">${m.likes || 0}</span>
                    </button>
                    <button class="btn btn-ghost" onclick="seleccionarObjetivo('${escaparHtml(m.id)}', '${escaparHtml(m.simbolo)}')" title="Opinar">
                        💬
                    </button>
                </div>
            </td>
        </tr>`;
    }).join("");
}

window.abrirAnalisis = async function (monedaId) {
    let m = monedasActuales.find(x => x.id === monedaId);
    if (!m) {
        try {
            const res = await fetch(`${API_BASE_URL}/api/criptos?q=${encodeURIComponent(monedaId)}&tamano=1`);
            const arr = await res.json();
            m = arr[0];
        } catch {
            return;
        }
    }
    if (!m) return;

    const sug = evaluarSugerencia(m);
    el.detalleContenido.innerHTML = `
        <div style="display:flex; justify-content:space-between; align-items:center; gap:0.5rem; padding-right:2rem;">
            <div>
                <h2>${escaparHtml(m.nombre)} <span class="asset-sym">${escaparHtml(m.simbolo)}</span></h2>
                <p style="color:var(--muted); font-size:0.82rem;">Posición #${m.rank} en el mercado global</p>
            </div>
            <strong class="mono" style="font-size:1.45rem;">${formatearPrecio(m.precioUsd)}</strong>
        </div>

        <div class="advice-banner">
            <strong>Lectura recomendada (${sug.texto}):</strong> ${sug.detalle}
        </div>

        <div class="detail-metrics">
            <div class="metric-box">
                <span>Máximo histórico</span>
                <strong class="mono">${formatearPrecio(m.athPrecio)}</strong>
            </div>
            <div class="metric-box">
                <span>Distancia al máximo</span>
                <strong class="mono neg">${Number(m.porcentajeDesdeAth || 0).toFixed(1)}%</strong>
            </div>
            <div class="metric-box">
                <span>Volumen 24h</span>
                <strong class="mono">${formatearCompacto(m.volumen24h)}</strong>
            </div>
        </div>

        <div class="chart-box">
            <div style="font-size:0.78rem; color:var(--muted); margin-bottom:0.45rem;">Evolución reciente de precio</div>
            <div id="graficoPrecio">Cargando historial...</div>
        </div>

        <div style="margin-top:1rem; display:flex; gap:0.6rem;">
            <button class="btn btn-cyan" style="flex:1" onclick="opinarDesdeModal('${escaparHtml(m.id)}', '${escaparHtml(m.simbolo)}')">
                💬 Opinar sobre ${escaparHtml(m.simbolo)}
            </button>
        </div>
    `;
    el.modalDetalle.classList.remove("hidden");

    try {
        const res = await fetch(`${API_BASE_URL}/api/criptos/${encodeURIComponent(m.id)}/snapshots?limite=20`);
        const historial = await res.json();
        renderizarGraficoSvg(historial.reverse(), m.cambio24h >= 0);
    } catch {
        document.getElementById("graficoPrecio").textContent = "Sin registros recientes todavía.";
    }
};

function renderizarGraficoSvg(puntos, esPositivo) {
    const cont = document.getElementById("graficoPrecio");
    if (!puntos || puntos.length < 2) {
        cont.innerHTML = `<p style="font-size:0.8rem; color:var(--muted);">Aún acumulando puntos de precio para trazar la curva (se registran automáticamente cada minuto).</p>`;
        return;
    }

    const precios = puntos.map(p => p.precioUsd);
    const min = Math.min(...precios);
    const max = Math.max(...precios);
    const rango = max - min || 1;
    const w = 520;
    const h = 100;

    const coords = precios.map((p, i) => {
        const x = (i / (precios.length - 1)) * w;
        const y = h - ((p - min) / rango) * (h - 14) - 7;
        return `${x.toFixed(1)},${y.toFixed(1)}`;
    }).join(" ");

    const color = esPositivo ? "#10b981" : "#f43f5e";
    cont.innerHTML = `
        <svg viewBox="0 0 ${w} ${h}" preserveAspectRatio="none">
            <polyline fill="none" stroke="${color}" stroke-width="2.5" points="${coords}" />
        </svg>
        <div style="display:flex; justify-content:space-between; font-size:0.74rem; color:var(--muted); margin-top:0.35rem;">
            <span>Mín: ${formatearPrecio(min)}</span>
            <span>Máx: ${formatearPrecio(max)}</span>
        </div>
    `;
}

window.opinarDesdeModal = function (id, simbolo) {
    el.modalDetalle.classList.add("hidden");
    window.seleccionarObjetivo(id, simbolo);
};

async function actualizarPrecios() {
    el.btnActualizarPrecios.disabled = true;
    const txt = el.btnActualizarPrecios.textContent;
    el.btnActualizarPrecios.textContent = "⏳ Actualizando...";
    try {
        const res = await fetch(`${API_BASE_URL}/api/criptos/sincronizar?limite=1500&snapshotsTop=150`, { method: "POST" });
        if (!res.ok) throw new Error();
        await Promise.all([cargarTodo(), cargarRadarOportunidades()]);
    } catch {
        alert("No se pudo actualizar los precios en este momento.");
    } finally {
        el.btnActualizarPrecios.disabled = false;
        el.btnActualizarPrecios.textContent = txt;
    }
}

window.darLike = async function (monedaId) {
    try {
        const res = await fetch(`${API_BASE_URL}/api/criptos/${encodeURIComponent(monedaId)}/like`, { method: "POST" });
        if (res.ok) actualizarLikeFila(await res.json());
    } catch (e) {
        console.error(e);
    }
};

function actualizarLikeFila(moneda) {
    const row = document.querySelector(`tr[data-coin-id="${CSS.escape(moneda.id)}"]`);
    if (row) {
        const span = row.querySelector(".like-count");
        if (span) span.textContent = moneda.likes;
    }
}

window.seleccionarObjetivo = function (monedaId, label) {
    monedaSeleccionadaId = monedaId;
    el.targetLabel.querySelector("strong").textContent = label;
    el.btnLimpiarTarget.classList.toggle("hidden", monedaId === "MERCADO");
    if (monedaId !== "MERCADO") el.inputContenido.focus();
};

async function cargarComentarios() {
    try {
        const res = await fetch(`${API_BASE_URL}/api/comentarios?limite=35`);
        if (!res.ok) throw new Error();
        pintarComentarios(await res.json());
    } catch {
        // Silencioso
    }
}

function pintarComentarios(lista) {
    if (!lista || lista.length === 0) {
        el.feedComentarios.innerHTML = `<div class="empty-row">Sé el primero en compartir tu análisis.</div>`;
        return;
    }
    el.feedComentarios.innerHTML = lista.map(renderComentarioHtml).join("");
}

function renderComentarioHtml(c) {
    const fecha = c.fechaCreacion ? new Date(c.fechaCreacion).toLocaleString("es-ES") : "Recién publicado";
    return `
        <div class="post-card">
            <div class="post-head">
                <span class="post-author">${escaparHtml(c.autor)}</span>
                <span class="post-stance stance-${escaparHtml(c.postura)}">${escaparHtml(c.postura)}</span>
            </div>
            <div class="post-symbol">$${escaparHtml(c.simbolo || "MERCADO")}</div>
            <p class="post-body">${escaparHtml(c.contenido)}</p>
            <div class="post-time">${fecha}</div>
        </div>
    `;
}

async function enviarComentario(e) {
    e.preventDefault();
    const payload = {
        monedaId: monedaSeleccionadaId,
        autor: el.inputAutor.value.trim(),
        postura: el.selectPostura.value,
        contenido: el.inputContenido.value.trim()
    };
    if (!payload.contenido) return;

    try {
        const res = await fetch(`${API_BASE_URL}/api/comentarios`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });
        if (res.ok) el.inputContenido.value = "";
    } catch {
        alert("No se pudo publicar tu comentario.");
    }
}

function conectarTiempoReal() {
    if (eventSource) eventSource.close();
    eventSource = new EventSource(`${API_BASE_URL}/api/criptos/stream`);

    eventSource.addEventListener("conectado", () => {
        el.marketStatus.className = "status-chip online";
        el.statusLabel.textContent = "Mercado conectado";
    });

    eventSource.addEventListener("comentario", (event) => {
        const nuevo = JSON.parse(event.data);
        const vacio = el.feedComentarios.querySelector(".empty-row");
        if (vacio) vacio.remove();
        el.feedComentarios.insertAdjacentHTML("afterbegin", renderComentarioHtml(nuevo));
        const actual = Number(el.statComentarios.textContent.replace(/\D/g, "")) || 0;
        el.statComentarios.textContent = (actual + 1).toLocaleString("es-ES");
    });

    eventSource.addEventListener("like", (event) => {
        actualizarLikeFila(JSON.parse(event.data));
    });

    eventSource.addEventListener("mercado", (event) => {
        const data = JSON.parse(event.data);
        if (data.resumen) pintarResumen(data.resumen);
        cargarMonedas();
        cargarRadarOportunidades();
    });

    eventSource.onerror = () => {
        el.marketStatus.className = "status-chip";
        el.statusLabel.textContent = "Reconectando...";
    };
}

function formatearPrecio(n) {
    if (n === null || n === undefined) return "$0.00";
    return n >= 1
        ? `$${Number(n).toLocaleString("en-US", { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
        : `$${Number(n).toFixed(6)}`;
}

function formatearPorcentaje(n) {
    const val = Number(n || 0);
    return `${val >= 0 ? "+" : ""}${val.toFixed(2)}%`;
}

function formatearCompacto(n) {
    const v = Number(n || 0);
    if (v >= 1e9) return `$${(v / 1e9).toFixed(2)}B`;
    if (v >= 1e6) return `$${(v / 1e6).toFixed(2)}M`;
    return `$${v.toLocaleString("en-US", { maximumFractionDigits: 0 })}`;
}

function escaparHtml(texto) {
    if (texto === null || texto === undefined) return "";
    return String(texto)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

iniciar();
