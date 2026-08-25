/*
Costantini Marco , 762497 , VA
Colombo Davide , 760761 , VA
Bonza Lorenzo , 760667 , VA
Piloni Luca , 762374 , VA
*/

package src;
import java.io.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Gestisce gli utenti del sistema CineMax.
 * Carica, registra e autentica gli utenti dal file CSV.
 * Supporta la cifratura Base64 delle password per la sicurezza.
 */
public class GestoreUtenti {
    // Specifica: file denominato Utenti
    private static final String FILE_UTENTI = "data/Utenti.csv"; 
    private static final String DELIMITATORE = ",";
    private List<Utente> listaUtenti;

    /**
     * Costruttore che inizializza il gestore e carica tutti gli utenti dal file CSV.
     */
    public GestoreUtenti() {
        listaUtenti = new ArrayList<>();
        caricaUtentiDaCSV();
    }

    /**
     * Cifra una password utilizzando la codifica Base64 come richiesto dalle specifiche.
     * @param password la password in chiaro
     * @return la password cifrata in Base64
     */
    private String cifraPassword(String password) {
        return Base64.getEncoder().encodeToString(password.getBytes());
    }

    /**
     * Carica tutti gli utenti dal file CSV e li mantiene in memoria.
     * Se il file non esiste, visualizza un messaggio di avviso.
     */
    private void caricaUtentiDaCSV() {
        File file = new File(FILE_UTENTI);
        if (!file.exists()) {
            System.out.println("File Utenti.csv non trovato." );
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String linea;
            boolean primaLinea = true;
            
            while ((linea = br.readLine()) != null) {
                // Salto l'intestazione del CSV
                if (primaLinea) {
                    primaLinea = false;
                    continue; 
                }
                
                String[] dati = linea.split(DELIMITATORE);
                if (dati.length == 7) {
                    Utente u = new Utente(dati[0], dati[1], dati[2], dati[3], 
                                          dati[4], dati[5], Ruolo.valueOf(dati[6]));
                    listaUtenti.add(u);
                }
            }
        } catch (IOException e) {
            System.out.println("Errore durante la lettura del file Utenti.csv");
        }
    }
    /**
     * Formatta e scrive una riga CSV per un utente nel file.
     * @param pw il PrintWriter del file CSV
     * @param u l'utente da scrivere
     */
    private void scriviRigaUtente(PrintWriter pw, Utente u) {
        pw.println(u.getNome() + DELIMITATORE + 
                   u.getCognome() + DELIMITATORE + 
                   u.getUsername() + DELIMITATORE + 
                   u.getPasswordCifrata() + DELIMITATORE + 
                   u.getDataNascita() + DELIMITATORE + 
                   u.getLuogoDomicilio() + DELIMITATORE + 
                   u.getRuolo().name());
    }

    /**
     * Registra un nuovo utente nel sistema.
     * Verifica l'unicita dello username, crea l'account con il ruolo specificato e lo salva immediatamente nel CSV.
     * @param nome il nome dell'utente
     * @param cognome il cognome dell'utente
     * @param username lo username univoco dell'utente
     * @param password la password in chiaro (sara cifrata)
     * @param dataNascita la data di nascita dell'utente
     * @param luogoDomicilio il luogo di domicilio dell'utente
     * @param ruolo il ruolo dell'utente (CLIENTE, PROIEZIONISTA, BIGLIETTAIO)
     */
    public void registraUtente(String nome, String cognome, String username, String password, 
                                String dataNascita, String luogoDomicilio, Ruolo ruolo) {
        
        // Controllo se l'username esiste già
        for (Utente u : listaUtenti) {
            if (u.getUsername().equals(username)) {
                System.out.println("Errore: Username già in uso. Scegline un altro.");
                return;
            }
        }
        
        // Creo l'oggetto usando il ruolo passato come parametro
        Utente nuovoUtente = new Utente(nome, cognome, username, cifraPassword(password), 
                                         dataNascita, luogoDomicilio, ruolo);
        
        // Lo aggiungo alla lista in memoria
        listaUtenti.add(nuovoUtente);
        
        // Lo SCRIVO IMMEDIATAMENTE nel file in modalità "append" (true)
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_UTENTI, true))) {
            scriviRigaUtente(pw, nuovoUtente);
            System.out.println("Registrazione completata con successo! Account creato come: " + ruolo.name());
        } catch (IOException e) {
            System.out.println("Errore durante la scrittura sul file Utenti.csv");
        }
    }

    /**
     * Autentica un utente verificando le credenziali.
     * La password viene cifrata e confrontata con quella memorizzata.
     * @param username lo username dell'utente
     * @param password la password in chiaro
     * @return l'oggetto Utente se le credenziali sono corrette, null altrimenti
     */
    public Utente login(String username, String password) {
        String pwdCifrata = cifraPassword(password);
        for (Utente u : listaUtenti) {
            if (u.getUsername().equals(username) && u.getPasswordCifrata().equals(pwdCifrata)) {
                System.out.println("Login effettuato con successo per l'utente: " + username);
                return u;
            }
        }
        System.out.println("Login fallito. Credenziali errate.");
        return null; // Login fallito
    }

    /**
     * Recupera un utente cercandolo per username.
     * @param username lo username dell'utente da cercare
     * @return l'oggetto Utente trovato, o null se non esiste
     */
    public Utente getUtenteByUsername(String username) {
        for (Utente u : listaUtenti) {
            if (u.getUsername().equals(username)) {
                return u;
            }
        }
        return null;
    }
}