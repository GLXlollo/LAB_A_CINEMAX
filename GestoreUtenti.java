import java.io.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class GestoreUtenti {
    // Specifica: file denominato Utenti
    private static final String FILE_UTENTI = "Utenti.csv"; 
    private static final String DELIMITATORE = ",";
    private List<Utente> listaUtenti;

    public GestoreUtenti() {
        listaUtenti = new ArrayList<>();
        caricaUtentiDaCSV();
    }

    // Metodo per cifrare la password come da specifiche
    private String cifraPassword(String password) {
        return Base64.getEncoder().encodeToString(password.getBytes());
    }

    // Carica gli utenti in memoria all'avvio dell'applicazione
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
    // Metodo di supporto per formattare la riga CSV
    private void scriviRigaUtente(PrintWriter pw, Utente u) {
        pw.println(u.getNome() + DELIMITATORE + 
                   u.getCognome() + DELIMITATORE + 
                   u.getUsername() + DELIMITATORE + 
                   u.getPasswordCifrata() + DELIMITATORE + 
                   u.getDataNascita() + DELIMITATORE + 
                   u.getLuogoDomicilio() + DELIMITATORE + 
                   u.getRuolo().name());
    }

    // Funzionalità di registrazione cliente: scrive SUBITO nel CSV
    public void registraCliente(String nome, String cognome, String username, String password, 
                                String dataNascita, String luogoDomicilio) {
        
        // Controllo se l'username esiste già
        for (Utente u : listaUtenti) {
            if (u.getUsername().equals(username)) {
                System.out.println("Errore: Username già in uso. Scegline un altro.");
                return;
            }
        }
        
        // Creo l'oggetto
        Utente nuovoCliente = new Utente(nome, cognome, username, cifraPassword(password), 
                                         dataNascita, luogoDomicilio, Ruolo.CLIENTE);
        
        // Lo aggiungo alla lista in memoria
        listaUtenti.add(nuovoCliente);
        
        // Lo SCRIVO IMMEDIATAMENTE nel file in modalità "append" (true)
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_UTENTI, true))) {
            scriviRigaUtente(pw, nuovoCliente);
            System.out.println("Registrazione completata e salvata nel file Utenti.csv!");
        } catch (IOException e) {
            System.out.println("Errore durante la scrittura sul file Utenti.csv");
        }
    }

    // Funzionalità di login
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
}