package com.iltempiodeilibri.config;

import com.iltempiodeilibri.entities.Genere;
import com.iltempiodeilibri.entities.Libro;
import com.iltempiodeilibri.repositories.GenereRepository;
import com.iltempiodeilibri.repositories.LibroRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Year;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Riempie il catalogo con libri veri, divisi per genere.
 * <p>
 * Le copertine arrivano da Open Library a partire dall'ISBN: nessuna chiave, nessuna quota.
 * Se un ISBN non ha copertina l'immagine non carica e resta la copertina tipografica —
 * per questo il frontend nasconde l'immagine invece di mostrare il segnaposto rotto.
 */
@Slf4j
@Component
@Order(3)
@ConditionalOnProperty(name = "app.demo.enabled", havingValue = "true")
@RequiredArgsConstructor
public class DemoCatalogoInitializer implements ApplicationRunner {

    private record Volume(String isbn, String titolo, String autore, String editore, int anno,
                          int pagine, String genere, String prezzo, int copie, boolean rigida,
                          String descrizione) {
    }

    private static final List<Volume> CATALOGO = List.of(
            // ---- Narrativa ----
            new Volume("9788806159245", "Le città invisibili", "Italo Calvino", "Einaudi", 1972, 168, "Narrativa", "12.50", 4, false,
                    "Marco Polo descrive a Kublai Khan cinquantacinque città che non esistono. Ogni città è un modo di leggere il desiderio, la memoria, lo scambio."),
            new Volume("9788804668237", "Il barone rampante", "Italo Calvino", "Mondadori", 1957, 272, "Narrativa", "13.00", 3, false,
                    "A dodici anni Cosimo sale su un leccio e non scende più. Vive tutta la vita sugli alberi, e da lassù partecipa al secolo dei Lumi."),
            new Volume("9788806220266", "Il sentiero dei nidi di ragno", "Italo Calvino", "Einaudi", 1947, 180, "Narrativa", "11.50", 2, false,
                    "La Resistenza vista da Pin, un ragazzino che non capisce la guerra ma ne conosce benissimo gli adulti."),
            new Volume("9788807880599", "Il Gattopardo", "Giuseppe Tomasi di Lampedusa", "Feltrinelli", 1958, 304, "Narrativa", "14.00", 3, true,
                    "La Sicilia dello sbarco garibaldino vista dal principe di Salina, che guarda la propria classe finire e decide di non opporsi."),

            // ---- Romanzo ----
            new Volume("9788817004954", "La coscienza di Zeno", "Italo Svevo", "Rizzoli", 1923, 448, "Romanzo", "12.00", 5, false,
                    "Zeno Cosini scrive le sue memorie per il dottore che lo cura dal fumo. Mente quasi sempre, e proprio lì si legge la verità."),
            new Volume("9788866320326", "L'amica geniale", "Elena Ferrante", "edizioni e/o", 2011, 331, "Romanzo", "16.50", 4, false,
                    "Lila e Lenù crescono in un rione di Napoli negli anni Cinquanta. Un'amicizia che è anche una gara, per sessant'anni."),
            new Volume("9788817060202", "Il deserto dei Tartari", "Dino Buzzati", "Rizzoli", 1940, 230, "Romanzo", "12.50", 3, true,
                    "Il tenente Drogo arriva alla Fortezza Bastiani per una breve destinazione e ci resta la vita, ad aspettare un nemico che non arriva."),
            new Volume("9788804583660", "La solitudine dei numeri primi", "Paolo Giordano", "Mondadori", 2008, 304, "Romanzo", "13.50", 2, false,
                    "Alice e Mattia sono due primi gemelli: vicinissimi, mai consecutivi. Si sfiorano per vent'anni senza mai toccarsi."),
            new Volume("9788806220235", "La luna e i falò", "Cesare Pavese", "Einaudi", 1950, 200, "Romanzo", "11.00", 3, false,
                    "Anguilla torna nelle Langhe dopo l'America e cerca il paese che ricordava. Trova le tracce della guerra civile."),
            new Volume("9788811811732", "Il fu Mattia Pascal", "Luigi Pirandello", "Garzanti", 1904, 288, "Romanzo", "10.50", 4, false,
                    "Dato per morto per errore, Mattia si costruisce un'altra vita con un altro nome. Scopre che senza documenti non si esiste."),

            // ---- Giallo ----
            new Volume("9788845292613", "Il nome della rosa", "Umberto Eco", "Bompiani", 1980, 512, "Giallo", "15.00", 3, true,
                    "Sette giorni in un'abbazia del 1327, sette morti e una biblioteca a labirinto. Guglielmo da Baskerville indaga mentre l'Inquisizione arriva."),
            new Volume("9788838933851", "La forma dell'acqua", "Andrea Camilleri", "Sellerio", 1994, 192, "Giallo", "13.00", 4, false,
                    "Il primo Montalbano. Un ingegnere trovato morto in un'auto alla mannara, e una verità che a Vigàta conviene a tutti tenere storta."),
            new Volume("9788838922565", "Il cane di terracotta", "Andrea Camilleri", "Sellerio", 1996, 272, "Giallo", "13.00", 3, false,
                    "Due corpi in una grotta murata da cinquant'anni, con accanto un cane di terracotta. Montalbano indaga un delitto che nessuno ha denunciato."),
            new Volume("9788804665137", "Quer pasticciaccio brutto de via Merulana", "Carlo Emilio Gadda", "Mondadori", 1957, 320, "Giallo", "14.00", 2, false,
                    "Un furto e un omicidio in un palazzo romano, raccontati in una lingua che mescola dialetti e registri fino a diventare il vero protagonista."),

            // ---- Fantascienza ----
            new Volume("9788804677536", "Le meraviglie del possibile", "Sergio Solmi", "Mondadori", 1959, 480, "Fantascienza", "16.00", 2, false,
                    "L'antologia che ha portato la fantascienza in Italia, con i racconti che hanno formato intere generazioni di lettori."),
            new Volume("9788804707141", "Cronache marziane", "Ray Bradbury", "Mondadori", 1950, 256, "Fantascienza", "13.50", 3, false,
                    "La colonizzazione di Marte raccontata come una serie di racconti: nostalgia, colonialismo e case che continuano a funzionare senza nessuno."),
            new Volume("9788845292699", "Solaris", "Stanisław Lem", "Sellerio", 1961, 240, "Fantascienza", "14.00", 2, true,
                    "Un oceano pensante su un pianeta lontano restituisce agli scienziati i loro ricordi più dolorosi, in carne e ossa."),

            // ---- Saggistica ----
            new Volume("9788806219352", "Se questo è un uomo", "Primo Levi", "Einaudi", 1947, 214, "Saggistica", "12.00", 5, false,
                    "Undici mesi a Monowitz, raccontati senza una parola di troppo. Il libro che ha insegnato all'Italia come si scrive una testimonianza."),
            new Volume("9788806220211", "I sommersi e i salvati", "Primo Levi", "Einaudi", 1986, 200, "Saggistica", "12.50", 3, false,
                    "Quarant'anni dopo, Levi torna sul lager per capire la zona grigia: non i mostri, ma le persone comuni che hanno collaborato."),
            new Volume("9788804663560", "Gomorra", "Roberto Saviano", "Mondadori", 2006, 331, "Saggistica", "15.00", 3, false,
                    "Cemento, rifiuti, camorra e tessile a Casal di Principe. Un'inchiesta in prima persona costata all'autore la libertà."),
            new Volume("9788806221942", "Lettere a una professoressa", "Scuola di Barbiana", "Einaudi", 1967, 168, "Saggistica", "11.00", 2, false,
                    "Otto ragazzi di montagna scrivono alla scuola che li ha bocciati. Il testo che ha messo in discussione l'idea italiana di merito."),

            // ---- Memoriale ----
            new Volume("9788806220228", "Lessico famigliare", "Natalia Ginzburg", "Einaudi", 1963, 221, "Memoriale", "12.00", 3, false,
                    "Una famiglia torinese antifascista raccontata attraverso le frasi che ripeteva. La storia d'Italia entra dalla porta di cucina."),
            new Volume("9788806220204", "Cristo si è fermato a Eboli", "Carlo Levi", "Einaudi", 1945, 232, "Memoriale", "12.50", 3, false,
                    "Il confino in Lucania di un medico torinese. Un Sud che i manuali non nominavano, descritto senza pietismo."),
            new Volume("9788807811685", "Novecento", "Alessandro Baricco", "Feltrinelli", 1994, 62, "Memoriale", "9.00", 5, false,
                    "Danny Boodmann T.D. Lemon Novecento nasce su un transatlantico e non scende mai a terra. Suona un pianoforte che si muove col mare."),

            // ---- Poesia ----
            new Volume("9788804518495", "Ossi di seppia", "Eugenio Montale", "Mondadori", 1925, 160, "Poesia", "11.00", 3, false,
                    "La prima raccolta di Montale: la costa ligure, il male di vivere, e una lingua che rifiuta ogni consolazione facile."),
            new Volume("9788806220259", "Il porto sepolto", "Giuseppe Ungaretti", "Einaudi", 1916, 96, "Poesia", "10.00", 2, false,
                    "Le poesie scritte in trincea sul Carso, brevissime, ridotte all'osso perché sotto le bombe non c'è tempo per le frasi lunghe."),
            new Volume("9788806220242", "Trasumanar e organizzar", "Pier Paolo Pasolini", "Einaudi", 1971, 208, "Poesia", "13.00", 2, false,
                    "L'ultima raccolta di Pasolini: poesia civile, invettiva e disperazione per un'Italia che stava cambiando pelle."),

            // ---- Classico ----
            new Volume("9788811362258", "I promessi sposi", "Alessandro Manzoni", "Garzanti", 1840, 720, "Classico", "14.00", 6, true,
                    "Renzo e Lucia, due promessi sposi che un signorotto vuole separare. Sotto, il romanzo di come funziona davvero il potere."),
            new Volume("9788806220181", "La Divina Commedia", "Dante Alighieri", "Einaudi", 1321, 928, "Classico", "22.00", 4, true,
                    "Il viaggio attraverso Inferno, Purgatorio e Paradiso. Il libro da cui viene mezza lingua che parliamo."),
            new Volume("9788817168366", "Il Decameron", "Giovanni Boccaccio", "Rizzoli", 1353, 1120, "Classico", "18.00", 2, true,
                    "Dieci giovani in fuga dalla peste si raccontano cento novelle. Comicità, beffe e mercanti al posto dei cavalieri."),

            // ---- Ragazzi ----
            new Volume("9788804669067", "Le avventure di Pinocchio", "Carlo Collodi", "Mondadori", 1883, 160, "Ragazzi", "10.00", 6, false,
                    "Un pezzo di legno che ride e piange, e un percorso di fame, bugie e pentimenti molto più duro di come lo ricordi."),
            new Volume("9788804703594", "Il giornalino di Gian Burrasca", "Vamba", "Mondadori", 1907, 288, "Ragazzi", "11.00", 3, false,
                    "Il diario di un bambino che combina guai per onestà, e viene punito ogni volta che dice la verità agli adulti."),
            new Volume("9788804728771", "Favole al telefono", "Gianni Rodari", "Mondadori", 1962, 192, "Ragazzi", "11.50", 4, false,
                    "Un commesso viaggiatore telefona ogni sera alla figlia per raccontarle una favola. Devono essere corte: la telefonata costa."),

            // ---- Fumetto ----
            new Volume("9788804671336", "Corto Maltese: Una ballata del mare salato", "Hugo Pratt", "Rizzoli Lizard", 1967, 168, "Fumetto", "19.00", 2, true,
                    "Il primo Corto Maltese: Pacifico, 1913, avventurieri e disertori. Il fumetto italiano diventa letteratura."),
            new Volume("9788804691211", "Dylan Dog: L'alba dei morti viventi", "Tiziano Sclavi", "Bonelli", 1986, 100, "Fumetto", "8.50", 3, false,
                    "Il primo numero dell'indagatore dell'incubo: zombie a Londra, e una malinconia che il fumetto popolare non aveva mai avuto."),
            new Volume("9788817087605", "Kobane Calling", "Zerocalcare", "Bao Publishing", 2016, 272, "Fumetto", "18.00", 3, false,
                    "Un fumettista romano al confine turco-siriano. Reportage e autoironia, senza nessuna delle pose del giornalismo di guerra."));

    private final LibroRepository libroRepository;
    private final GenereRepository genereRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (libroRepository.count() > 0) {
            log.info("Catalogo già popolato: non tocco nulla");
            return;
        }

        // Un genere per volta, riusato da tutti i libri che gli appartengono
        Map<String, Genere> generi = new LinkedHashMap<>();
        CATALOGO.forEach(v -> generi.computeIfAbsent(v.genere(), nome ->
                genereRepository.findByNomeIgnoreCase(nome).orElseGet(() -> genereRepository.save(new Genere(nome)))));

        CATALOGO.forEach(v -> {
            Libro l = new Libro();
            l.setIsbn(new BigDecimal(v.isbn()));
            l.setTitolo(v.titolo());
            l.setAutore(v.autore());
            l.setCasaEditrice(v.editore());
            l.setAnnoDiUscita(Year.of(v.anno()));
            l.setPagine(v.pagine());
            l.setPrezzo(new BigDecimal(v.prezzo()));
            l.setCopieTotali(v.copie());
            l.setCopieDisponibili(v.copie());
            l.setCopertinaRigida(v.rigida());
            l.setDescrizione(v.descrizione());
            l.setGenere(generi.get(v.genere()));
            // Open Library serve le copertine per ISBN, senza chiave e senza quota
            // default=false fa rispondere 404 invece di un segnaposto trasparente: così il
            // frontend se ne accorge e lascia la copertina tipografica, invece di coprirla col vuoto
            l.setPath("https://covers.openlibrary.org/b/isbn/" + v.isbn() + "-L.jpg?default=false");
            libroRepository.save(l);
        });

        log.info("Catalogo dimostrativo creato: {} libri in {} generi", CATALOGO.size(), generi.size());
    }
}
