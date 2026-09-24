# day4-week8 - BE + FE (JSX) + PostgreSQL

Esercizio della settimana 8, pronto per il deploy su Render.

| Parte | Tecnologia | In locale | Su Render |
|---|---|---|---|
| Backend | Spring Boot 4.1.1, Java 25, Maven wrapper | `backend` sulla 8080 | Web Service (Docker) |
| Frontend | React 19, Vite, JSX, Bootstrap 5, Router, axios, STOMP | `frontend` sulla 5173 | Static Site |
| Database | PostgreSQL | locale sulla 5432 | Render PostgreSQL |

## Endpoint

| Metodo | Percorso | Cosa fa |
|---|---|---|
| GET | `/api/stato` | nome del database collegato e ora del server |
| GET | `/actuator/health` | health check per Render |

## Avvio in locale

1. PostgreSQL sulla 5432 e database creato:
   ```
   createdb -U postgres day4_week8
   ```
2. Credenziali nelle variabili d'ambiente (una volta sola, poi serve un terminale
   nuovo). Non vanno mai scritte in un file del progetto - vedi `backend/.env.example`:
   ```
   setx POSTGRES "postgres"
   setx POSTGRES_PASSWORD "la-tua-password"
   ```
   Indirizzo diverso dal default: variabile `DB_URL`.
3. Doppio clic su `avvia.cmd` (Windows) o `./avvia.sh` (macOS/Linux), oppure:
   ```
   cd backend && .\mvnw.cmd spring-boot:run
   cd frontend && npm install && npm run dev
   ```
4. http://localhost:5173 - il riquadro deve mostrare `day4_week8`.

## Deploy su Render

1. Repository Git con `backend/`, `frontend/`, `render.yaml` nella radice.
2. **New > Blueprint**, si sceglie la repo: nascono `day4-week8-db`, `day4-week8-be`,
   `day4-week8-fe`.
3. Dopo la prima build si impostano le due variabili `sync: false`, senza `/` finale:

   | Servizio | Variabile | Valore |
   |---|---|---|
   | `day4-week8-be` | `ALLOWED_ORIGIN` | `https://day4-week8-fe.onrender.com` |
   | `day4-week8-fe` | `VITE_API_URL` | `https://day4-week8-be.onrender.com` |

4. **Manual Deploy** di entrambi (`VITE_API_URL` e' letta in fase di build).

## Struttura

```
render.yaml                 blueprint: database + backend + frontend
avvia.cmd / avvia.sh        avvio locale (Windows / macOS-Linux)
backend/
  Dockerfile                usato solo da Render
  src/main/java/com/example/day4_week8/
    Day4Week8Application.java
    config/DatabaseUrl.java   DATABASE_URL -> formato JDBC
    config/CorsConfig.java    origini da ALLOWED_ORIGIN
    controller/StatoController.java  endpoint di prova
  src/main/resources/application.yml
  .env.example              variabili d'ambiente da impostare in locale
frontend/
  src/services/api.js       client axios, base da VITE_API_URL
  src/services/socket.js    client STOMP/SockJS, pronto ma non attivo
  src/routes, pages, layouts, components, hooks, context
  src/pages/Home.jsx        pagina di prova
  .env.example
```
