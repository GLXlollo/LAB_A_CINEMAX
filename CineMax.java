import java.util.List;
import java.util.Scanner;

public class CineMax {
    private static Scanner scanner = new Scanner(System.in);
    private static GestoreUtenti gestoreUtenti = new GestoreUtenti();
    private static GestoreProiezioni gestoreProiezioni = new GestoreProiezioni();
    private static GestorePrenotazioni gestorePrenotazioni = new GestorePrenotazioni(); 
    private static Utente utenteLoggato = null;

    public static void main(String[] args) {
        boolean esci = false;
        System.out.println("=== Benvenuto in CineMax ===");

        while (!esci) {
            System.out.println("\n1. Login");
            System.out.println("2. Registrati come nuovo cliente");
            System.out.println("3. Entra come utente Guest");
            System.out.println("0. Esci");
            System.out.print("Scegli un'opzione: ");
            
            String scelta = scanner.nextLine();

            switch (scelta) {
                case "1":
                    eseguiLogin();
                    break;
                case "2":
                    eseguiRegistrazione();
                    break;
                case "3":
                    menuGuest();
                    break;
                case "0":
                    esci = true;
                    System.out.println("Arrivederci!");
                    break;
                default:
                    System.out.println("Opzione non valida.");
            }
        }
        scanner.close();
    }

    private static void eseguiLogin() {
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        utenteLoggato = gestoreUtenti.login(username, password);

        if (utenteLoggato != null) {
            System.out.println("\nLogin effettuato! Benvenuto " + utenteLoggato.getNome());
            
            // Smistamento degli utenti (Specifiche pag. 7-8)
            switch (utenteLoggato.getRuolo()) {
                case CLIENTE:
                    menuCliente();
                    break;
                case PROIEZIONISTA:
                    System.out.println("Menu Proiezionista (da implementare)");
                    break;
                case BIGLIETTAIO:
                    System.out.println("Menu Bigliettaio (da implementare)");
                    break;
            }
            utenteLoggato = null; // Logout
        } else {
            System.out.println("Credenziali errate.");
        }
    }

    // --- MENU CLIENTE ---
    private static void menuCliente() {
        boolean esci = false;
        while (!esci) {
            System.out.println("\n--- Area Cliente ---");
            System.out.println("1. Cerca e prenota una proiezione");
            System.out.println("2. Visualizza le tue prenotazioni");
            System.out.println("0. Logout");
            System.out.print("Scegli un'opzione: ");
            
            String scelta = scanner.nextLine();
            switch (scelta) {
                case "1":
                    eseguiPrenotazioneCliente();
                    break;
                case "2":
                    visualizzaPrenotazioniCliente();
                    break;
                case "0":
                    esci = true;
                    System.out.println("Logout in corso...");
                    break;
                default:
                    System.out.println("Opzione non valida.");
            }
        }
    }

    private static void eseguiPrenotazioneCliente() {
        System.out.print("Inserisci il titolo del film da cercare: ");
        String titolo = scanner.nextLine();

        List<Proiezione> risultati = gestoreProiezioni.cercaPerTitolo(titolo);

        if (risultati.isEmpty()) {
            System.out.println("Nessuna proiezione trovata.");
            return;
        }

        System.out.println("\nProiezioni trovate:");
        for (int i = 0; i < risultati.size(); i++) {
            System.out.println((i + 1) + ". " + risultati.get(i).getTitolo() + " - " + risultati.get(i).getDataOra() + " (Posti liberi: " + risultati.get(i).getPostiLiberi() + ")");
        }

        System.out.print("\nScegli la proiezione per vedere i dettagli e prenotare (o 0 per annullare): ");
        try {
            int scelta = Integer.parseInt(scanner.nextLine());
            if (scelta > 0 && scelta <= risultati.size()) {
                Proiezione p = risultati.get(scelta - 1);
                visualizzaProiezione(p); 
                
                System.out.print("\nQuanti biglietti vuoi prenotare per questo film? (0 per annullare): ");
                int numBiglietti = Integer.parseInt(scanner.nextLine());
                
                if (numBiglietti > 0) {
                    gestorePrenotazioni.creaPrenotazione(utenteLoggato.getUsername(), p, numBiglietti);
                } else if (numBiglietti != 0) {
                    System.out.println("Numero di biglietti non valido.");
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("Input non valido. Inserisci un numero.");
        }
    }

    private static void visualizzaPrenotazioniCliente() {
        System.out.println("\n--- Le tue Prenotazioni ---");
        List<Prenotazione> miePrenotazioni = gestorePrenotazioni.getPrenotazioniPerUtente(utenteLoggato.getUsername());
        
        if (miePrenotazioni.isEmpty()) {
            System.out.println("Non hai ancora effettuato prenotazioni.");
        } else {
            for (Prenotazione p : miePrenotazioni) {
                System.out.println(p.toString());
            }
        }
    }

    // --- MENU GUEST ED EXTRA ---
    private static void eseguiRegistrazione() {
        System.out.println("\n--- Registrazione Cliente ---");
        System.out.print("Nome: ");
        String nome = scanner.nextLine();
        System.out.print("Cognome: ");
        String cognome = scanner.nextLine();
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        System.out.print("Data di nascita (facoltativa, premi invio per saltare): ");
        String dataNascita = scanner.nextLine();
        System.out.print("Luogo del domicilio: ");
        String domicilio = scanner.nextLine();

        gestoreUtenti.registraCliente(nome, cognome, username, password, dataNascita, domicilio);
    }

    private static void menuGuest() {
        System.out.println("\n--- Accesso Guest ---");
        System.out.print("Inserisci il titolo del film da cercare: ");
        String titolo = scanner.nextLine();

        List<Proiezione> risultati = gestoreProiezioni.cercaPerTitolo(titolo);

        if (risultati.isEmpty()) {
            System.out.println("Nessuna proiezione trovata.");
            return;
        }

        System.out.println("\nProiezioni trovate:");
        for (int i = 0; i < risultati.size(); i++) {
            System.out.println((i + 1) + ". " + risultati.get(i).getTitolo() + " - " + risultati.get(i).getDataOra());
        }

        System.out.print("\nInserisci il numero della proiezione per i dettagli (o 0 per annullare): ");
        try {
            int scelta = Integer.parseInt(scanner.nextLine());
            if (scelta > 0 && scelta <= risultati.size()) {
                visualizzaProiezione(risultati.get(scelta - 1));
            } else if (scelta != 0) {
                System.out.println("Scelta non valida.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Input non valido. Inserisci un numero.");
        }
    }

    private static void visualizzaProiezione(Proiezione p) {
        System.out.println("\n--- Dettagli Proiezione ---");
        System.out.println("Titolo: " + p.getTitolo());
        System.out.println("Genere: " + p.getGenere());
        System.out.println("Regista: " + p.getRegista());
        System.out.println("Anno: " + p.getAnno());
        System.out.println("Durata: " + p.getDurata() + " min");
        System.out.println("Data e Ora: " + p.getDataOra());
        System.out.println("Costo biglietto: €" + p.getCostoBiglietto());
        System.out.println("Posti liberi: " + p.getPostiLiberi());
        System.out.println("---------------------------");
    }
}