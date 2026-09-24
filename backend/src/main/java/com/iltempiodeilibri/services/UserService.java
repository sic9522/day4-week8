package com.iltempiodeilibri.services;

import com.iltempiodeilibri.dto.LoginRequest;
import com.iltempiodeilibri.dto.LoginResponse;
import com.iltempiodeilibri.dto.NuovoAdminRequest;
import com.iltempiodeilibri.dto.NuovoClienteRequest;
import com.iltempiodeilibri.dto.PageResponse;
import com.iltempiodeilibri.dto.UserResponse;
import com.iltempiodeilibri.dto.UserSearchParams;
import com.iltempiodeilibri.entities.Ruolo;
import com.iltempiodeilibri.entities.RuoloUtente;
import com.iltempiodeilibri.entities.Sede;
import com.iltempiodeilibri.entities.User;
import com.iltempiodeilibri.repositories.RuoloRepository;
import com.iltempiodeilibri.repositories.RuoloUtenteRepository;
import com.iltempiodeilibri.repositories.SedeRepository;
import com.iltempiodeilibri.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    // Colonne ordinabili: nome nel parametro sort -> proprietà JPA
    private static final Map<String, String> SORT_CONSENTITI = Map.of(
            "cognome", "cognome",
            "nome", "nome",
            "email", "email",
            "username", "username",
            "createdAt", "createdAt");
    private static final Sort SORT_PREDEFINITO = Sort.by(Sort.Direction.ASC, "cognome", "nome");

    private final UserRepository userRepository;
    private final RuoloRepository ruoloRepository;
    private final RuoloUtenteRepository ruoloUtenteRepository;
    private final SedeRepository sedeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        String identificativo = normalizza(request.username());
        // Si accetta sia il nome utente sia l'email. Stesso errore in tutti i casi:
        // non riveliamo quali utenze esistono
        User user = userRepository.findByUsername(identificativo)
                .or(() -> userRepository.findByEmail(identificativo))
                .filter(u -> passwordEncoder.matches(request.password(), u.getPassword()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenziali non valide"));
        return jwtService.emetti(user, nomiRuoli(user));
    }

    // Il SuperUser nomina un admin e insieme indica la sede che gestirà: se non esiste la crea
    @Transactional
    public UserResponse creaAdmin(NuovoAdminRequest r) {
        Sede sede = sedeRepository.findByNomeIgnoreCase(r.nomeNegozio().trim())
                .orElseGet(() -> sedeRepository.save(new Sede(
                        r.nomeNegozio().trim(), r.via().trim(), r.citta().trim(), r.cap().trim())));

        return crea(r.email(), r.username(), r.password(), r.dataDiNascita(),
                r.nome(), r.cognome(), r.indirizzo(), sede, Ruolo.ADMIN);
    }

    // Un cliente nasce in due modi: lo iscrive un admin, oppure si registra da solo.
    // In entrambi i casi sceglie la sede, e in entrambi i casi il ruolo è User e basta.
    @Transactional
    public UserResponse creaCliente(NuovoClienteRequest r) {
        Sede sede = sedeRepository.findById(r.sedeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sede non trovata"));

        return crea(r.email(), r.username(), r.password(), r.dataDiNascita(),
                r.nome(), r.cognome(), r.indirizzo(), sede, Ruolo.USER);
    }

    private UserResponse crea(String emailGrezza, String usernameGrezzo, String password,
                              LocalDate dataDiNascita, String nome, String cognome,
                              String indirizzo, Sede sede, String nomeRuolo) {
        String email = normalizza(emailGrezza);
        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email già registrata");
        }

        String username = usernameGrezzo == null || usernameGrezzo.isBlank()
                ? null
                : normalizza(usernameGrezzo);
        if (username != null && userRepository.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Nome utente già in uso");
        }

        User user = new User();
        user.setEmail(email);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setDataDiNascita(dataDiNascita);
        user.setNome(nome);
        user.setCognome(cognome);
        user.setIndirizzo(indirizzo);
        user.setSede(sede);
        userRepository.save(user);

        Ruolo ruolo = ruoloRepository.findByRuolo(nomeRuolo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Ruolo " + nomeRuolo + " assente"));
        ruoloUtenteRepository.save(new RuoloUtente(user, ruolo));

        return UserResponse.of(user, List.of(nomeRuolo));
    }

    @Transactional
    public LoginResponse refresh(UUID userId, String tokenAttuale) {
        User user = trovaUtente(userId);
        jwtService.revoca(tokenAttuale);
        return jwtService.emetti(user, nomiRuoli(user));
    }

    public void logout(String token) {
        jwtService.revoca(token);
    }

    @Transactional(readOnly = true)
    public UserResponse me(UUID userId) {
        User user = trovaUtente(userId);
        return UserResponse.of(user, nomiRuoli(user));
    }

    @Transactional(readOnly = true)
    public UserResponse dettaglio(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utente non trovato"));
        return UserResponse.of(user, nomiRuoli(user));
    }

    /**
     * Elenco utenti per la console.
     * <p>
     * Se chi chiede è un admin di sede, la ricerca viene forzata sulla sua sede: il parametro
     * che arriva dal browser non basta, perché chiunque può riscriverlo. Il SuperUser non ha
     * sede e vede tutti.
     */
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> cerca(UserSearchParams params, UUID chiamanteId, Pageable pageable) {
        User chiamante = trovaUtente(chiamanteId);
        UserSearchParams effettivi = chiamante.getSede() == null
                ? params
                : new UserSearchParams(params.q(), params.ruolo(), chiamante.getSede().getId());

        Sort sort = SearchUtils.traduciSort(pageable.getSort(), SORT_CONSENTITI, SORT_PREDEFINITO);
        Pageable richiesta = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        return PageResponse.of(userRepository
                .findAll(UserSpecifications.da(effettivi), richiesta)
                .map(u -> UserResponse.of(u, nomiRuoli(u))));
    }

    private User trovaUtente(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utente non trovato"));
    }

    private List<String> nomiRuoli(User user) {
        return ruoloUtenteRepository.findByUser(user).stream()
                .map(ru -> ru.getRuolo().getRuolo())
                .toList();
    }

    private static String normalizza(String testo) {
        return testo.trim().toLowerCase(Locale.ROOT);
    }
}
