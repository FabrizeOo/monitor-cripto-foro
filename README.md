# Monitor Cripto & Foro en Tiempo Real (Proyecto 5)

Sistema **NoSQL con MongoDB**, **Arquitectura Hexagonal en Java 21 (Spring Boot)** y **Frontend HTML/JS desacoplado** con snapshots periódicos, consultas en tiempo real (Server-Sent Events) y foro estilo X/StockTwits.

## Estructura
- `backend/` — API REST + SSE + Scheduler en Java 21 con Arquitectura Hexagonal (`dominio`, `aplicacion`, `infraestructura`).
  - Consume JSON masivo desde **CoinPaprika API** (`https://api.coinpaprika.com/v1/tickers`).
  - Almacena 3 colecciones en **MongoDB**: `criptomonedas`, `snapshots_precios` (historial que crece cada 90s) y `comentarios_cripto`.
- `frontend/` — Sitio estático (`index.html`, `styles.css`, `app.js`) listo para subir a **Vercel**.

## 1. Configurar MongoDB Atlas (Gratis)
1. Crea un clúster gratuito (**M0 Free**) en [MongoDB Atlas](https://www.mongodb.com/atlas).
2. Crea un usuario en **Database Access** y habilita `0.0.0.0/0` en **Network Access**.
3. Copia tu URI de conexión, por ejemplo:
   `mongodb+srv://usuario:clave@cluster0.xxxxx.mongodb.net/monitor_cripto?retryWrites=true&w=majority`

## 2. Ejecutar en Local
En `backend/`, define `MONGODB_URI` y ejecuta con Java 21:

```powershell
$env:MONGODB_URI="mongodb+srv://usuario:clave@cluster0.xxxxx.mongodb.net/monitor_cripto?retryWrites=true&w=majority"
cd backend
mvn spring-boot:run
```

Al iniciar por primera vez, si MongoDB está vacío, importará **1,500 criptomonedas + 150 snapshots históricos** automáticamente, y cada 90 segundos guardará nuevos snapshots actualizando el HTML en vivo.

Luego abre `frontend/index.html` con Live Server (o cualquier servidor estático).

## 3. Desplegar en la Nube
- **Backend (Render / Railway):** Despliega la carpeta `backend/` con el `Dockerfile` incluido y configura la variable `MONGODB_URI`.
- **Frontend (Vercel):** Sube la carpeta `frontend/` a Vercel. Abre tu web en Vercel, pulsa **⚙️ API** arriba a la derecha (o edita `DEFAULT_API_URL` en `frontend/app.js`) y pega la URL pública de tu backend.

