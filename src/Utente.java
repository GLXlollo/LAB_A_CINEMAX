/*
Costantini Marco , 762497 , VA
Colombo Davide , 760761 , VA
Bonza Lorenzo , 760667 , VA
Piloni Luca , 762374 , VA
*/

package src;
import java.io.Serializable;

/**
 * Rappresenta i ruoli disponibili nel sistema CineMax.
 * Definisce le tipologie di account e i relativi permessi di accesso 
 * ai menu e alle funzionalità dell'applicazione.
 */
enum Ruolo {
    CLIENTE, PROIEZIONISTA, BIGLIETTAIO
}

/**
 * Rappresenta un utente registrato nel sistema CineMax.
 * Memorizza le informazioni personali, le credenziali di accesso (in formato sicuro)
 * e il ruolo aziendale o cliente assegnato.
 * <p>
 * La classe implementa {@link Serializable} per consentire il salvataggio 
 * e la lettura degli oggetti da file.
 * </p>
 */
public class Utente implements Serializable {
    private static final long serialVersionUID = 1L; // Necessario per la serializzazione
    
    private String nome;
    private String cognome;
    private String username;
    private String passwordCifrata; // La specifica richiede password cifrata
    private String dataNascita; // Facoltativa
    private String luogoDomicilio;
    private Ruolo ruolo;

    /**
     * Costruisce un nuovo oggetto Utente inizializzando tutti i suoi attributi.
     * 
     * @param nome Il nome di battesimo dell'utente.
     * @param cognome Il cognome dell'utente.
     * @param username Il nome utente univoco utilizzato per il login.
     * @param passwordCifrata La password dell'utente, preventivamente cifrata per ragioni di sicurezza.
     * @param dataNascita La data di nascita dell'utente (campo facoltativo).
     * @param luogoDomicilio L'indirizzo o la città di domicilio dell'utente.
     * @param ruolo Il ruolo assegnato all'utente ({@link Ruolo#CLIENTE}, {@link Ruolo#PROIEZIONISTA}, {@link Ruolo#BIGLIETTAIO}).
     */
    public Utente(String nome, String cognome, String username, String passwordCifrata, 
                  String dataNascita, String luogoDomicilio, Ruolo ruolo) {
        this.nome = nome;
        this.cognome = cognome;
        this.username = username;
        this.passwordCifrata = passwordCifrata;
        this.dataNascita = dataNascita;
        this.luogoDomicilio = luogoDomicilio;
        this.ruolo = ruolo;
    }

    /**
     * Restituisce l'username dell'utente.
     * 
     * @return L'username univoco.
     */
    public String getUsername() { return username; }

    /**
     * Restituisce il cognome dell'utente.
     * 
     * @return Il cognome.
     */
    public String getCognome() { return cognome; }

    /**
     * Restituisce la data di nascita dell'utente.
     * 
     * @return La data di nascita (può essere vuota o nulla se non specificata).
     */
    public String getDataNascita() { return dataNascita; }

    /**
     * Restituisce il luogo di domicilio dell'utente.
     * 
     * @return L'indirizzo o la città di domicilio.
     */
    public String getLuogoDomicilio() { return luogoDomicilio; }

    /**
     * Restituisce la password dell'utente (cifrata).
     * 
     * @return La password cifrata.
     */
    public String getPasswordCifrata() { return passwordCifrata; }

    /**
     * Restituisce il ruolo dell'utente.
     * 
     * @return Il ruolo assegnato.
     */
    public Ruolo getRuolo() { return ruolo; }

    /**
     * Restituisce il nome dell'utente.
     * 
     * @return Il nome.
     */
    public String getNome() { return nome; }
}