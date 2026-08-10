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
                    menuBigliettaio(); // <-- MODIFICA QUI per attivare il menu
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
            System.out.println("3. Modifica la data di una prenotazione");
            System.out.println("4. Cancella una prenotazione");
            System.out.println("0. Logout");
            System.out.print("Scegli un'opzione: ");
            
            String scelta = scanner.nextLine();
            switch (scelta) {
                case "1": eseguiPrenotazioneCliente(); break;
                case "2": visualizzaPrenotazioniCliente(); break;
                case "3": eseguiModificaPrenotazioneCliente(); break;
                case "4": eseguiEliminaPrenotazioneCliente(); break;
                case "0":
                    esci = true;
                    System.out.println("Logout in corso...");
                    break;
                default: System.out.println("Opzione non valida.");
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

    private static void eseguiEliminaPrenotazioneCliente() {
        System.out.print("\nInserisci il CODICE della prenotazione da cancellare: ");
        String codice = scanner.nextLine();

        Prenotazione p = gestorePrenotazioni.getPrenotazioneByCodice(codice);

        // Controllo che esista e sia del cliente attualmente loggato
        if (p == null || !p.getUsernameCliente().equals(utenteLoggato.getUsername())) {
            System.out.println("Errore: Prenotazione non trovata o non ti appartiene.");
            return;
        }

        // NOTA: Da specifiche (pag. 12) andrebbe verificato che la data sia corretta rispetto a oggi
        
        // Prima di cancellare, dobbiamo "restituire" i posti liberi alla proiezione originale
        for (Proiezione proj : gestoreProiezioni.getListaProiezioni()) {
            if (proj.getTitolo().equals(p.getTitoloFilm()) && proj.getDataOra().equals(p.getDataOraProiezione())) {
                proj.liberaPosti(p.getNumeroBiglietti());
                break;
            }
        }

        gestorePrenotazioni.eliminaPrenotazione(p);
        System.out.println("Prenotazione " + codice + " eliminata con successo! I posti sono stati liberati.");
    }

    private static void eseguiModificaPrenotazioneCliente() {
        System.out.print("\nInserisci il CODICE della prenotazione da modificare: ");
        String codice = scanner.nextLine();

        Prenotazione p = gestorePrenotazioni.getPrenotazioneByCodice(codice);

        if (p == null || !p.getUsernameCliente().equals(utenteLoggato.getUsername())) {
            System.out.println("Errore: Prenotazione non trovata o non ti appartiene.");
            return;
        }

        System.out.println("Stai modificando il film: " + p.getTitoloFilm());
        System.out.print("Inserisci la NUOVA data/ora che desideri (es. 2026-05-15): ");
        String filtroData = scanner.nextLine();

        // Cerchiamo le proiezioni dello STESSO film, ma filtrate per la nuova data
        List<Proiezione> risultati = gestoreProiezioni.cercaPerTitolo(p.getTitoloFilm());
        System.out.println("\nNuove proiezioni disponibili per " + p.getTitoloFilm() + ":");
        int count = 1;
        for (Proiezione proj : risultati) {
            if (proj.getDataOra().contains(filtroData)) {
                System.out.println(count + ". " + proj.getDataOra() + " (Posti liberi: " + proj.getPostiLiberi() + ")");
                count++;
            }
        }

        if (count == 1) {
            System.out.println("Nessuna nuova proiezione trovata corrispondente alla ricerca.");
            return;
        }

        System.out.print("Scegli il numero della nuova proiezione (o 0 per annullare): ");
        try {
            int scelta = Integer.parseInt(scanner.nextLine());
            if (scelta > 0 && scelta < count) {
                // Troviamo la proiezione scelta dall'utente (aggiustando l'indice)
                Proiezione nuovaProj = null;
                int indiceTemporaneo = 1;
                for (Proiezione proj : risultati) {
                    if (proj.getDataOra().contains(filtroData)) {
                        if (indiceTemporaneo == scelta) {
                            nuovaProj = proj;
                            break;
                        }
                        indiceTemporaneo++;
                    }
                }
                
                if (nuovaProj != null && nuovaProj.getPostiLiberi() >= p.getNumeroBiglietti()) {
                    // 1. Liberiamo i vecchi posti
                    for (Proiezione proj : gestoreProiezioni.getListaProiezioni()) {
                        if (proj.getTitolo().equals(p.getTitoloFilm()) && proj.getDataOra().equals(p.getDataOraProiezione())) {
                            proj.liberaPosti(p.getNumeroBiglietti());
                            break;
                        }
                    }
                    // 2. Occupiamo i posti nella nuova data
                    nuovaProj.prenotaPosti(p.getNumeroBiglietti());
                    
                    // 3. Modifichiamo la data sulla prenotazione e salviamo il file
                    p.setDataOraProiezione(nuovaProj.getDataOra());
                    gestorePrenotazioni.riscriviFileCSV();

                    System.out.println("Data modificata con successo!");
                } else {
                    System.out.println("Errore: La nuova proiezione non ha abbastanza posti.");
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("Input non valido.");
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

    // --- MENU BIGLIETTAIO ---
    private static void menuBigliettaio() {
        boolean esci = false;
        while (!esci) {
            System.out.println("\n--- Area Bigliettaio ---");
            System.out.println("1. Visualizza prenotazioni di oggi");
            System.out.println("2. Cerca una prenotazione");
            System.out.println("0. Logout");
            System.out.print("Scegli un'opzione: ");
            
            String scelta = scanner.nextLine();
            switch (scelta) {
                case "1":
                    visualizzaPrenotazioniOggi();
                    break;
                case "2":
                    cercaPrenotazioneBigliettaio();
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

    private static void visualizzaPrenotazioniOggi() {
        // Pagina 8: Visualizzare le prenotazioni nella data odierna
        System.out.print("\nInserisci la data di oggi (es. 2026-05-15): ");
        String oggi = scanner.nextLine();
        
        System.out.println("\n--- Prenotazioni per il " + oggi + " ---");
        boolean trovate = false;
        for (Prenotazione p : gestorePrenotazioni.getTutteLePrenotazioni()) {
            if (p.getDataOraProiezione().contains(oggi)) {
                stampaDettagliPrenotazione(p);
                trovate = true;
            }
        }
        if (!trovate) {
            System.out.println("Nessuna prenotazione trovata per oggi.");
        }
    }

    private static void cercaPrenotazioneBigliettaio() {
        // Pagina 11: Ricerca per codice, nome, o titolo
        System.out.println("\n--- Cerca Prenotazione ---");
        System.out.println("1. Per Codice Prenotazione");
        System.out.println("2. Per Titolo film");
        System.out.println("3. Per Nome o Cognome cliente");
        System.out.print("Scegli il criterio: ");
        String criterio = scanner.nextLine();
        
        System.out.print("Inserisci il testo da cercare: ");
        String testo = scanner.nextLine().toLowerCase();
        
        boolean trovate = false;
        for (Prenotazione p : gestorePrenotazioni.getTutteLePrenotazioni()) {
            boolean match = false;
            
            if (criterio.equals("1") && p.getCodiceUnivoco().toLowerCase().equals(testo)) {
                match = true;
            } else if (criterio.equals("2") && p.getTitoloFilm().toLowerCase().contains(testo)) {
                match = true;
            } else if (criterio.equals("3")) {
                Utente u = gestoreUtenti.getUtenteByUsername(p.getUsernameCliente());
                if (u != null && (u.getNome().toLowerCase().contains(testo) || u.getCognome().toLowerCase().contains(testo))) {
                    match = true;
                }
            }
            
            if (match) {
                stampaDettagliPrenotazione(p);
                trovate = true;
            }
        }
        if (!trovate) {
            System.out.println("Nessuna corrispondenza trovata.");
        }
    }

    // Visualizzazione dettagliata della prenotazione
    private static void stampaDettagliPrenotazione(Prenotazione p) {
        Utente u = gestoreUtenti.getUtenteByUsername(p.getUsernameCliente());
        String nomeCompleto = (u != null) ? (u.getNome() + " " + u.getCognome()) : "Utente Sconosciuto";
        
        System.out.println("\n[Codice: " + p.getCodiceUnivoco() + "]");
        System.out.println("Cliente: " + nomeCompleto);
        System.out.println("Film: " + p.getTitoloFilm() + " | Orario: " + p.getDataOraProiezione());
        System.out.println("Biglietti: " + p.getNumeroBiglietti() + " | Costo unitario: €" + p.getCostoUnitario());
        System.out.println("Costo Totale: €" + p.getCostoTotale());
        System.out.println("-------------------------------------------------");
    }
}