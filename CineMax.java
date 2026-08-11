import java.util.List;
import java.util.Scanner;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class CineMax {
    private static Scanner scanner = new Scanner(System.in);
    private static GestoreUtenti gestoreUtenti = new GestoreUtenti();
    private static GestoreProiezioni gestoreProiezioni = new GestoreProiezioni();
    private static GestorePrenotazioni gestorePrenotazioni = new GestorePrenotazioni(); 
    private static Utente utenteLoggato = null;

    public static void main(String[] args) {

        // Sincronizza i database all'avvio per ricalcolare i posti corretti
        sincronizzaPostiOccupati();

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
                    menuProiezionista(); // <-- MODIFICA QUI per attivare il menu
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
            System.out.println("0. Logout e torna al menu principale");
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
                
                int numBiglietti = leggiInteroPositivo("\nQuanti biglietti vuoi prenotare per questo film? (0 per annullare): ", true);
                
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

        // Controllo: la proiezione non deve essere nel passato
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime dataProiezione = LocalDateTime.parse(p.getDataOraProiezione(), formatter);
        if (dataProiezione.isBefore(LocalDateTime.now())) {
            System.out.println("Errore: Non puoi cancellare una prenotazione per un film già passato o iniziato.");
            return;
        }
        
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

        // Controllo: la proiezione originale non deve essere nel passato
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime dataProiezione = LocalDateTime.parse(p.getDataOraProiezione(), formatter);
        if (dataProiezione.isBefore(LocalDateTime.now())) {
            System.out.println("Errore: Non puoi modificare una prenotazione per un film già passato o iniziato.");
            return;
        }

        System.out.println("Stai modificando il film: " + p.getTitoloFilm());
        String filtroData = leggiData("Inserisci la NUOVA data che desideri cercare");

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

    // --- MENU GUEST ---
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
        boolean esci = false;
        while (!esci) {
            System.out.println("\n--- Accesso Guest ---");
            System.out.println("1. Cerca una proiezione (per titolo)");
            System.out.println("0. Torna al Menu Principale");
            System.out.print("Scegli un'opzione: ");
            
            String scelta = scanner.nextLine();
            switch (scelta) {
                case "1":
                    eseguiRicercaGuest();
                    break;
                case "0":
                    esci = true;
                    break;
                default:
                    System.out.println("Opzione non valida.");
            }
        }
    }

    private static void eseguiRicercaGuest() {
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
            System.out.println("0. Logout e torna al menu principale");
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
        // Recupera la data odierna automaticamente dal sistema
        String oggi = LocalDate.now().toString(); 
        
        System.out.println("\n--- Prenotazioni per OGGI (" + oggi + ") ---");
        boolean trovate = false;
        
        for (Prenotazione p : gestorePrenotazioni.getTutteLePrenotazioni()) {
            // Controlla se la data/ora della proiezione contiene la data di oggi
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

    // --- MENU PROIEZIONISTA ---
    private static void menuProiezionista() {
        boolean esci = false;
        while (!esci) {
            System.out.println("\n--- Area Proiezionista ---");
            System.out.println("1. Aggiungi una proiezione");
            System.out.println("2. Modifica la data di una proiezione");
            System.out.println("3. Elimina una proiezione");
            System.out.println("0. Logout e torna al menu principale");
            System.out.print("Scegli un'opzione: ");
            
            String scelta = scanner.nextLine();
            switch (scelta) {
                case "1": eseguiAggiungiProiezione(); break;
                case "2": eseguiModificaProiezione(); break;
                case "3": eseguiEliminaProiezione(); break;
                case "0":
                    esci = true;
                    System.out.println("Logout in corso...");
                    break;
                default: System.out.println("Opzione non valida.");
            }
        }
    }

    private static void eseguiAggiungiProiezione() {
        System.out.println("\n--- Aggiungi Proiezione ---");
        String dataOra = leggiDataOra("Data e Ora della nuova proiezione");
        
        // Controllo sovrapposizione
        for (Proiezione p : gestoreProiezioni.getListaProiezioni()) {
            if (p.getDataOra().equals(dataOra)) {
                System.out.println("Errore: Esiste già una proiezione in questa data e ora!");
                return;
            }
        }
        
        System.out.print("Titolo del film: "); String titolo = scanner.nextLine();
        System.out.print("Genere: "); String genere = scanner.nextLine();
        System.out.print("Regista: "); String regista = scanner.nextLine();
        
        // Usiamo i nostri nuovi validatori infallibili!
        int anno = leggiInteroPositivo("Anno (es. 2023): ", false);
        int durata = leggiInteroPositivo("Durata in min (es. 120): ", false);
        int eta = leggiInteroPositivo("Età minima (es. 14, usa 0 per tutti): ", true);
        double costo = leggiDoublePositivo("Costo biglietto (es. 8.50): ");

        Proiezione nuova = new Proiezione(dataOra, titolo, genere, regista, anno, durata, eta, costo);
        gestoreProiezioni.aggiungiProiezione(nuova);
        System.out.println("Proiezione aggiunta con successo e salvata nel palinsesto!");
    }

    private static void eseguiModificaProiezione() {
        System.out.print("\nInserisci il titolo del film da modificare: ");
        String titolo = scanner.nextLine();
        List<Proiezione> risultati = gestoreProiezioni.cercaPerTitolo(titolo);
        
        if (risultati.isEmpty()) {
            System.out.println("Nessuna proiezione trovata con questo titolo.");
            return;
        }

        for (int i = 0; i < risultati.size(); i++) {
            System.out.println((i + 1) + ". " + risultati.get(i).getTitolo() + " - " + risultati.get(i).getDataOra());
        }

        System.out.print("Scegli la proiezione da modificare (0 per annullare): ");
        try {
            int scelta = Integer.parseInt(scanner.nextLine());
            if (scelta > 0 && scelta <= risultati.size()) {
                Proiezione p = risultati.get(scelta - 1);
                
                // Controllo prenotazioni esistenti (Specifiche pag. 13)
                if (haPrenotazioni(p)) {
                    System.out.println("Errore: Non puoi modificare una proiezione che ha già delle prenotazioni attive!");
                    return;
                }
                
                String nuovaData = leggiDataOra("Inserisci la nuova Data e Ora");
                p.setDataOra(nuovaData);
                gestoreProiezioni.riscriviFileCSV();
                System.out.println("Data della proiezione modificata con successo!");
            }
        } catch (NumberFormatException e) {
            System.out.println("Input non valido.");
        }
    }

    private static void eseguiEliminaProiezione() {
        System.out.print("\nInserisci il titolo del film da eliminare dal palinsesto: ");
        String titolo = scanner.nextLine();
        List<Proiezione> risultati = gestoreProiezioni.cercaPerTitolo(titolo);
        
        if (risultati.isEmpty()) {
            System.out.println("Nessuna proiezione trovata.");
            return;
        }

        for (int i = 0; i < risultati.size(); i++) {
            System.out.println((i + 1) + ". " + risultati.get(i).getTitolo() + " - " + risultati.get(i).getDataOra());
        }

        System.out.print("Scegli la proiezione da eliminare in modo definitivo (0 per annullare): ");
        try {
            int scelta = Integer.parseInt(scanner.nextLine());
            if (scelta > 0 && scelta <= risultati.size()) {
                Proiezione p = risultati.get(scelta - 1);
                
                // Controllo prenotazioni esistenti (Specifiche pag. 13)
                if (haPrenotazioni(p)) {
                    System.out.println("Errore: Non puoi eliminare una proiezione che ha già delle prenotazioni!");
                    return;
                }
                
                gestoreProiezioni.eliminaProiezione(p);
                System.out.println("Proiezione eliminata con successo dal palinsesto!");
            }
        } catch (NumberFormatException e) {
            System.out.println("Input non valido.");
        }
    }

    // Metodo di supporto per bloccare modifiche/cancellazioni se ci sono biglietti venduti
    private static boolean haPrenotazioni(Proiezione p) {
        for (Prenotazione pren : gestorePrenotazioni.getTutteLePrenotazioni()) {
            if (pren.getTitoloFilm().equals(p.getTitolo()) && pren.getDataOraProiezione().equals(p.getDataOra())) {
                return true;
            }
        }
        return false;
    }

    // All'avvio del programma, ricalcola i posti occupati leggendoli dallo storico prenotazioni
    private static void sincronizzaPostiOccupati() {
        for (Prenotazione p : gestorePrenotazioni.getTutteLePrenotazioni()) {
            for (Proiezione proj : gestoreProiezioni.getListaProiezioni()) {
                if (proj.getTitolo().equals(p.getTitoloFilm()) && proj.getDataOra().equals(p.getDataOraProiezione())) {
                    proj.prenotaPosti(p.getNumeroBiglietti());
                }
            }
        }
    }

    // --- METODI DI SUPPORTO PER VALIDAZIONE DATE ---
    
    private static String leggiData(String messaggio) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        while (true) {
            System.out.print(messaggio + " (Formato richiesto: AAAA-MM-GG, es. 2026-05-15): ");
            String input = scanner.nextLine();
            try {
                // Tenta di convertire la stringa in una data reale. Se fallisce, va nel catch.
                LocalDate data = LocalDate.parse(input, formatter);
                return data.toString(); // Restituisce la stringa sicura e formattata
            } catch (DateTimeParseException e) {
                System.out.println("Errore: Formato data non valido. Controlla la struttura con i trattini oppure se la data è errata o inesistente.");
            }
        }
    }

    private static String leggiDataOra(String messaggio) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        while (true) {
            System.out.print(messaggio + " (Formato: AAAA-MM-GG HH:MM, es. 2026-05-15 21:30): ");
            String input = scanner.nextLine();
            try {
                // Tenta di convertire la stringa in data+ora.
                LocalDateTime dataOra = LocalDateTime.parse(input, formatter);
                return dataOra.format(formatter); // Restituisce la stringa perfetta
            } catch (DateTimeParseException e) {
                System.out.println("Errore: Formato non valido. Controlla la struttura con i trattini oppure se la data e l'ora sono errate.");
            }
        }
    }

    // --- METODI DI SUPPORTO PER VALIDAZIONE NUMERI ---

    private static int leggiInteroPositivo(String messaggio, boolean consentiZero) {
        while (true) {
            System.out.print(messaggio);
            try {
                int valore = Integer.parseInt(scanner.nextLine());
                if (valore > 0 || (valore == 0 && consentiZero)) {
                    return valore;
                } else {
                    System.out.println("Errore: Inserisci un numero " + (consentiZero ? "maggiore o uguale a zero." : "maggiore di zero."));
                }
            } catch (NumberFormatException e) {
                System.out.println("Errore: Formato non valido. Inserisci un numero intero.");
            }
        }
    }

    private static double leggiDoublePositivo(String messaggio) {
        while (true) {
            System.out.print(messaggio);
            try {
                // .replace() aiuta gli utenti che per sbaglio scrivono 8,50 con la virgola
                String input = scanner.nextLine().replace(",", ".");
                double valore = Double.parseDouble(input);
                if (valore >= 0) {
                    return valore;
                } else {
                    System.out.println("Errore: Inserisci un costo maggiore o uguale a zero.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Errore: Formato non valido. Inserisci un valore numerico.");
            }
        }
    }
}