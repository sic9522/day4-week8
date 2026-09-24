# day4-week8 - BE + FE (JSX) + PostgreSQL

Esercizio della settimana 8, pronto per il deploy su Render.

| Parte | Tecnologia | In locale | Su Render |
|---|---|---|---|
| Backend | Spring Boot 4.1.1, Java 25, Security + JWT, WebSocket | `backend` sulla 8080 | Web Service (Docker) |
| Frontend | React 19, Vite, JSX, Bootstrap 5, Router, axios, STOMP | `frontend` sulla 5173 | Static Site |
| Database | PostgreSQL | locale sulla 5432 | Render PostgreSQL |

## Endpoint

| Metodo | Percorso | Cosa fa |
|---|---|---|
| GET | `/api/stato` | nome del database collegato e ora del server |
| GET | `/actuator/health` | health check per Render |

Tutti gli endpoint sono aperti: non esiste ancora un login. La struttura per farlo
c'e' gia' (vedi sotto), manca solo l'entita' `User` con il suo `AuthController`.

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
   setx JWT "<openssl rand -base64 48>"
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
   | `day4-week8-be` | `JWT` | la chiave generata con `openssl rand -base64 48` |
   | `day4-week8-fe` | `VITE_API_URL` | `https://day4-week8-be.onrender.com` |

4. **Manual Deploy** di entrambi (`VITE_API_URL` e' letta in fase di build).

## Quando arriva il login

Il resto e' gia' al suo posto: `JwtService.generateToken(username, ruolo)` firma,
`JwtAuthFilter` autentica ogni richiesta col token, `PasswordEncoder` (BCrypt) e' un bean,
`/api/auth/**` e' aperto e `@EnableMethodSecurity` rende vivo `@PreAuthorize`.
Mancano solo l'entita' `User` in `entities/`, il suo repository, `AuthService` e
`AuthController`; poi si stringono i permessi in `SecurityConfig`.
Lato frontend il token si salva in `localStorage` come `token`: l'interceptor di
`services/api.js` lo allega gia' da solo a ogni chiamata.

## Struttura

```
render.yaml                 blueprint: database + backend + frontend
avvia.cmd / avvia.sh        avvio locale (Windows / macOS-Linux)
backend/
  Dockerfile                usato solo da Render
  src/main/java/com/example/day4_week8/
    Day4Week8Application.java
    config/DatabaseUrl.java      DATABASE_URL -> formato JDBC
    config/WebSocketConfig.java  STOMP su /ws, broker su /topic
    controller/StatoController.java  endpoint di prova
    security/JwtService.java     firma e verifica dei token
    security/JwtAuthFilter.java  legge l'header Authorization
    security/SecurityConfig.java permessi, CORS da ALLOWED_ORIGIN, BCrypt
    exceptions/                  3 eccezioni + ErrorHandler, un solo formato d'errore
    payloads/request, payloads/response, entities, repository, service (vuoti)
  src/main/resources/application.yml
  .env.example              variabili d'ambiente da impostare in locale
frontend/
  src/services/api.js       client axios, base da VITE_API_URL
  src/services/socket.js    client STOMP/SockJS, pronto ma non attivo
  src/routes, pages, layouts, components, hooks, context
  src/pages/Home.jsx        pagina di prova
  .env.example
```
