import java.io.Serializable;

// Enum per definire i ruoli richiesti dalle specifiche
enum Ruolo {
    CLIENTE, PROIEZIONISTA, BIGLIETTAIO
}

public class Utente implements Serializable {
    private static final long serialVersionUID = 1L; // Necessario per la serializzazione
    
    private String nome;
    private String cognome;
    private String username;
    private String passwordCifrata; // La specifica richiede password cifrata
    private String dataNascita; // Facoltativa
    private String luogoDomicilio;
    private Ruolo ruolo;

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

    // Getters
    public String getUsername() { return username; }
    public String getPasswordCifrata() { return passwordCifrata; }
    public Ruolo getRuolo() { return ruolo; }
    public String getNome() { return nome; }
}