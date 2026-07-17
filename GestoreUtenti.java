import java.io.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class GestoreUtenti {
    private static final String FILE_UTENTI = "Utenti.dat"; 
    private List<Utente> listaUtenti;

    public GestoreUtenti() {
        listaUtenti = new ArrayList<>();
        caricaUtenti();
    }

    // Metodo per cifrare la password (usiamo Base64 come esempio semplice)
    private String cifraPassword(String password) {
        return Base64.getEncoder().encodeToString(password.getBytes());
    }

    @SuppressWarnings("unchecked")
    private void caricaUtenti() {
        File file = new File(FILE_UTENTI);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                listaUtenti = (List<Utente>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Errore nel caricamento del file Utenti.");
            }
        } else {
           
            popolaDipendentiIniziali();
            salvaUtenti();
        }
    }

    private void salvaUtenti() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_UTENTI))) {
            oos.writeObject(listaUtenti);
        } catch (IOException e) {
            System.out.println("Errore nel salvataggio del file Utenti.");
        }
    }
    private void popolaDipendentiIniziali() {
        for (int i = 1; i <= 2; i++) {
            listaUtenti.add(new Utente("Proiezionista", ""+i, "proiezionista"+i, 
                            cifraPassword("pass"), "", "Cinema", Ruolo.PROIEZIONISTA));
        }
        for (int i = 1; i <= 5; i++) {
            listaUtenti.add(new Utente("Bigliettaio", ""+i, "bigliettaio"+i, 
                            cifraPassword("pass"), "", "Cinema", Ruolo.BIGLIETTAIO));
        }
        System.out.println("File Utenti creato con i dipendenti di default.");
    }

    // Funzionalità di registrazione cliente
    public void registraCliente(String nome, String cognome, String username, String password, 
                                String dataNascita, String luogoDomicilio) {
        // Controllo se l'username esiste già
        for (Utente u : listaUtenti) {
            if (u.getUsername().equals(username)) {
                System.out.println("Errore: Username già in uso.");
                return;
            }
        }
        Utente nuovoCliente = new Utente(nome, cognome, username, cifraPassword(password), 
                                         dataNascita, luogoDomicilio, Ruolo.CLIENTE);
        listaUtenti.add(nuovoCliente);
        salvaUtenti();
        System.out.println("Registrazione completata con successo!");
    }

    // Funzionalità di login
    public Utente login(String username, String password) {
        String pwdCifrata = cifraPassword(password);
        for (Utente u : listaUtenti) {
            if (u.getUsername().equals(username) && u.getPasswordCifrata().equals(pwdCifrata)) {
                return u;
            }
        }
        return null; // Login fallito
    }
}