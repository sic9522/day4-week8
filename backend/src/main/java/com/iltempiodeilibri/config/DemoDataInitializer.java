package com.iltempiodeilibri.config;

import com.iltempiodeilibri.entities.Ruolo;
import com.iltempiodeilibri.entities.RuoloUtente;
import com.iltempiodeilibri.entities.Sede;
import com.iltempiodeilibri.entities.User;
import com.iltempiodeilibri.repositories.RuoloRepository;
import com.iltempiodeilibri.repositories.RuoloUtenteRepository;
import com.iltempiodeilibri.repositories.SedeRepository;
import com.iltempiodeilibri.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

/**
 * Popola il database con sedi, admin e clienti finti, per avere qualcosa su cui lavorare.
 * <p>
 * Si attiva solo con {@code app.demo.enabled=true}. In produzione va spento: crea utenze
 * con password note, e un account chiamato "admin" con una password prevedibile è
 * esattamente il primo tentativo di chiunque provi a entrare.
 */
@Slf4j
@Component
@Order(2)
@ConditionalOnProperty(name = "app.demo.enabled", havingValue = "true")
@RequiredArgsConstructor
public class DemoDataInitializer implements ApplicationRunner {

    // Credenziali dimostrative: fuori dal codice, così non finiscono su git
    @Value("${app.demo.admin.username:admin}")
    private String adminUsername;

    @Value("${app.demo.admin.password:}")
    private String adminPassword;

    @Value("${app.demo.cliente.password:}")
    private String clientePassword;

    private record Filiale(String nome, String via, String citta, String cap) {
    }

    private static final List<Filiale> SEDI = List.of(
            new Filiale("Biblioteca Acilia", "Via di Acilia 153", "Roma", "00125"),
            new Filiale("Biblioteca Ostiense", "Via Ostiense 210", "Roma", "00154"),
            new Filiale("Biblioteca Trastevere", "Via della Lungaretta 82", "Roma", "00153"),
            new Filiale("Biblioteca Monti", "Via dei Serpenti 41", "Roma", "00184"),
            new Filiale("Biblioteca Testaccio", "Via Galvani 28", "Roma", "00153"),
            new Filiale("Biblioteca Garbatella", "Piazza Benedetto Brin 6", "Roma", "00154"),
            new Filiale("Biblioteca Prati", "Via Cola di Rienzo 118", "Roma", "00192"),
            new Filiale("Biblioteca San Lorenzo", "Via dei Volsci 51", "Roma", "00185"),
            new Filiale("Biblioteca Eur", "Viale Europa 190", "Roma", "00144"),
            new Filiale("Biblioteca Tiburtina", "Via Tiburtina 402", "Roma", "00159"));

    private static final List<String[]> ADMIN = List.of(
            new String[]{null, "Rossella", "Manzi"},   // il primo prende lo username dalle variabili
            new String[]{"tfabbri", "Tommaso", "Fabbri"},
            new String[]{"iconti", "Ilaria", "Conti"},
            new String[]{"nrizzo", "Nicola", "Rizzo"},
            new String[]{"slombardi", "Sara", "Lombardi"},
            new String[]{"gmariani", "Giorgio", "Mariani"},
            new String[]{"evitali", "Emma", "Vitali", },
            new String[]{"pdonati", "Paolo", "Donati"},
            new String[]{"lgatti", "Lucia", "Gatti"},
            new String[]{"mbenedetti", "Matteo", "Benedetti"});

    private static final List<String> NOMI = List.of(
            "Giulia", "Marco", "Elena", "Samuele", "Chiara", "Davide", "Federica", "Luca", "Martina", "Alessandro",
            "Sofia", "Andrea", "Beatrice", "Riccardo", "Valentina", "Stefano", "Alice", "Filippo", "Camilla", "Lorenzo");

    private static final List<String> COGNOMI = List.of(
            "Ferraro", "De Santis", "Bruni", "Ricci", "Amato", "Costa", "Sala", "Pellegrini", "Greco", "Vitale",
            "Moretti", "Barbieri", "Fontana", "Caruso", "Villa", "Serra", "Rinaldi", "Farina", "Monti", "Grassi");

    private final SedeRepository sedeRepository;
    private final UserRepository userRepository;
    private final RuoloRepository ruoloRepository;
    private final RuoloUtenteRepository ruoloUtenteRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (sedeRepository.count() > 0) {
            log.info("Dati dimostrativi già presenti: non tocco nulla");
            return;
        }

        // Nessuna password di riserva scritta nel codice: un default prevedibile su una repo
        // pubblica e' un'utenza regalata. Senza ADMIN_PASSWORD e CLIENTE_PASSWORD non si crea nulla.
        if (adminPassword.isBlank() || clientePassword.isBlank()) {
            log.warn("Dati dimostrativi saltati: mancano ADMIN_PASSWORD e/o CLIENTE_PASSWORD");
            return;
        }

        // Le password si cifrano una volta sola e si riusa l'hash: sono tutte uguali,
        // e 110 giri di BCrypt all'avvio non li regala nessuno
        String hashAdmin = passwordEncoder.encode(adminPassword);
        String hashCliente = passwordEncoder.encode(clientePassword);

        Ruolo ruoloAdmin = ruoloRepository.findByRuolo(Ruolo.ADMIN).orElseThrow();
        Ruolo ruoloUser = ruoloRepository.findByRuolo(Ruolo.USER).orElseThrow();

        int contatoreClienti = 0;

        for (int i = 0; i < SEDI.size(); i++) {
            Filiale f = SEDI.get(i);
            Sede sede = sedeRepository.save(new Sede(f.nome(), f.via(), f.citta(), f.cap()));

            String[] a = ADMIN.get(i);
            String utente = a[0] != null ? a[0] : adminUsername;
            salva(utente, a[1], a[2], utente + "@iltempiodeilibri.it", hashAdmin, sede, ruoloAdmin,
                    f.via() + ", " + f.citta());

            // Dieci clienti per ogni sede
            for (int k = 0; k < 10; k++) {
                String nome = NOMI.get(contatoreClienti % NOMI.size());
                String cognome = COGNOMI.get((contatoreClienti * 7 + i) % COGNOMI.size());
                String username = (cognome.replace(" ", "").toLowerCase(Locale.ROOT) + nome.charAt(0) + contatoreClienti);
                salva(username, nome, cognome, username + "@posta.it", hashCliente, sede, ruoloUser,
                        "Via Esempio " + (10 + contatoreClienti) + ", " + f.citta());
                contatoreClienti++;
            }
        }

        log.warn("Dati dimostrativi creati: {} sedi, {} admin, {} clienti. Spegni app.demo.enabled prima di andare in produzione.",
                SEDI.size(), ADMIN.size(), contatoreClienti);
    }

    private void salva(String utenteGrezzo, String nome, String cognome, String emailGrezza,
                       String hash, Sede sede, Ruolo ruolo, String indirizzo) {
        // Username ed email vanno salvati in minuscolo: il login li normalizza così
        // prima di cercarli, e una maiuscola qui renderebbe l'utenza inaccessibile
        String username = utenteGrezzo.toLowerCase(Locale.ROOT);
        String email = emailGrezza.toLowerCase(Locale.ROOT);

        if (userRepository.existsByUsername(username) || userRepository.existsByEmail(email)) {
            return;
        }
        User u = new User();
        u.setUsername(username);
        u.setNome(nome);
        u.setCognome(cognome);
        u.setEmail(email);
        u.setPassword(hash);
        u.setDataDiNascita(LocalDate.of(1980 + (Math.abs(username.hashCode()) % 25), 1 + (Math.abs(username.hashCode()) % 12), 1 + (Math.abs(username.hashCode()) % 28)));
        u.setIndirizzo(indirizzo);
        u.setSede(sede);
        userRepository.save(u);
        ruoloUtenteRepository.save(new RuoloUtente(u, ruolo));
    }
}
