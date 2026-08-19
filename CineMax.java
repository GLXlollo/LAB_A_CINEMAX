import java.util.List;
import java.util.Scanner;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

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
            System.out.println("2. Registrati come nuovo utente (cliente, bigliettaio o proiezionista)");
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
            
            // Smistamento degli utenti
            switch (utenteLoggato.getRuolo()) {
                case CLIENTE:
                    menuCliente();
                    break;
                case PROIEZIONISTA:
                    menuProiezionista();
                    break;
                case BIGLIETTAIO:
                    menuBigliettaio(); 
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
        GestoreProiezioni future = gestoreProiezioni.futureProiz();
        String filtro = "";
        System.out.print("\nInserisci il titolo del film: ");
        filtro = scanner.nextLine().toLowerCase();
        List<Proiezione> titolo = future.cercaPerTitolo(filtro);
        boolean esci = false;
        while(!esci) {
            int count = 0;
            try {
                if(titolo.isEmpty()) {  
                    System.out.println("Nessuna proiezione futura trovata per questa ricerca.");
                    System.out.println("Desideri riprovare?");
                    System.out.println("1: Sì");
                    System.out.println("2: No (Torna al menù guest)");

                    String esito = scanner.nextLine();
                    switch(esito) {
                        case "1": eseguiPrenotazioneCliente(); break;
                        case "2": return;
                        default: System.out.println("Opzione non valida."); break;
                    }
                }
                    
                for (Proiezione proiezione : titolo) {
                    System.out.println((count + 1) + ". " + proiezione.getTitolo() + " - " + proiezione.getDataOra());
                    count++;
                }
                System.out.print("\nScegli la proiezione per vedere i dettagli e prenotare (o 0 per annullare): ");
                int scelta = Integer.parseInt(scanner.nextLine());
                if (scelta > 0 && scelta <= titolo.size()) {
                    Proiezione p = titolo.get(scelta - 1);
                    visualizzaProiezione(p); 
                
                    // Usiamo il nostro validatore per impedire numeri negativi!
                    int numBiglietti = leggiInteroPositivo("\nQuanti biglietti vuoi prenotare per questo film? (0 per annullare): ", true);
                
                    if (numBiglietti > 0) {
                        gestorePrenotazioni.creaPrenotazione(utenteLoggato.getUsername(), p, numBiglietti);
                        
                    }
                    esci=true;
                }
            } catch (NumberFormatException e) {
                System.out.println("Input non valido. Inserisci un numero.");
            }
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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime dataProiezione = LocalDateTime.parse(p.getDataOraProiezione(), formatter);
        if (dataProiezione.isBefore(LocalDateTime.now())) {
            System.out.println("Errore: Non puoi modificare una prenotazione per un film già passato o iniziato.");
            return;
        }

        System.out.println("Stai modificando il film: " + p.getTitoloFilm() + " Prenotato per il giorno: " + p.getDataOraProiezione());
        

        // Cerchiamo le proiezioni dello STESSO film, ma filtrate per la nuova data
        GestoreProiezioni future = gestoreProiezioni.futureProiz();
        List<Proiezione> risultati = future.cercaPerTitolo(p.getTitoloFilm());
        System.out.println("\nNuove proiezioni disponibili per " + p.getTitoloFilm() + ":");
        int count = 1;
        for (Proiezione proj : risultati) {   
            System.out.println(count + ". " + proj.getDataOra() + " (Posti liberi: " + proj.getPostiLiberi() + ")");
            count++;    
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
                    
                    if (indiceTemporaneo == scelta) {
                        nuovaProj = proj;
                        break;
                    }
                    indiceTemporaneo++;
                    
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

    private static void eseguiRegistrazione() {
        System.out.println("\n--- Registrazione Nuovo Utente ---");
        
        Ruolo ruoloScelto = null;
        while (ruoloScelto == null) {
            System.out.println("Scegli il tipo di account che vuoi creare:");
            System.out.println("1. Cliente");
            System.out.println("2. Proiezionista (Richiede PIN Aziendale)");
            System.out.println("3. Bigliettaio (Richiede PIN Aziendale)");
            System.out.println("0. Annulla e torna al menu principale");
            System.out.print("Scelta: ");
            
            String sceltaRuolo = scanner.nextLine();
            switch (sceltaRuolo) {
                case "1": 
                    ruoloScelto = Ruolo.CLIENTE; 
                    break;
                case "2": 
                    System.out.print("Inserisci il PIN di sicurezza aziendale: ");
                    if (scanner.nextLine().equals("PROJ2026")) {
                        ruoloScelto = Ruolo.PROIEZIONISTA;
                    } else {
                        System.out.println("PIN Errato. Registrazione dipendente annullata.\n");
                    }
                    break;
                case "3": 
                    System.out.print("Inserisci il PIN di sicurezza aziendale: ");
                    if (scanner.nextLine().equals("TICK2026")) {
                        ruoloScelto = Ruolo.BIGLIETTAIO;
                    } else {
                        System.out.println("PIN Errato. Registrazione dipendente annullata.\n");
                    }
                    break;
                case "0": 
                    System.out.println("Registrazione annullata.");
                    return; // Esce e torna al menu
                default: 
                    System.out.println("Opzione non valida. Riprova.\n");
            }
        }

        // Se il programma arriva qui, l'utente ha il permesso di registrarsi
        System.out.print("Nome: ");
        String nome = scanner.nextLine();
        System.out.print("Cognome: ");
        String cognome = scanner.nextLine();
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        System.out.print("Data di nascita (Formato: AAAA-MM-DD facoltativa, premi invio per saltare): ");
        String dataNascita = scanner.nextLine();
        System.out.print("Luogo del domicilio: ");
        String domicilio = scanner.nextLine();

        // Registra l'utente con il ruolo autorizzato (Cliente, oppure Staff se aveva il PIN)
        gestoreUtenti.registraUtente(nome, cognome, username, password, dataNascita, domicilio, ruoloScelto);
    }

    // --- MENU GUEST ---
    private static void menuGuest() {
        boolean esci = false;
        while (!esci) {
            System.out.println("\n--- Accesso Guest ---");
            System.out.println("1. Cerca per Titolo");
            System.out.println("2. Cerca per Genere");
            System.out.println("3. Cerca per Data");
            System.out.println("0. Torna al Menu Principale");
            System.out.print("Scegli un'opzione: ");
            
            String scelta = scanner.nextLine();
            switch (scelta) {
                case "1": eseguiRicercaGuest("TITOLO"); break;
                case "2": eseguiRicercaGuest("GENERE"); break;
                case "3": eseguiRicercaGuest("DATA"); break;
                case "0":
                    esci = true;
                    break;
                default:
                    System.out.println("\nOpzione non valida.");
            }
        }
    }

    private static void eseguiRicercaGuest(String tipoRicerca) {
        GestoreProiezioni future = gestoreProiezioni.futureProiz();
        String filtro = "";
        if (tipoRicerca.equals("TITOLO")) {
            System.out.print("\nInserisci il titolo del film: ");
            filtro = scanner.nextLine().toLowerCase();
            List<Proiezione> titolo = future.cercaPerTitolo(filtro);
            boolean esci = false;
            while(!esci) {
                
                int count = 0;
                try {
                    if(titolo.isEmpty()) {  
                        System.out.println("Nessuna proiezione futura trovata per questa ricerca.");
                        System.out.println("Desideri riprovare?");
                        System.out.println("1: Sì");
                        System.out.println("2: No (Torna al menù guest)");

                        String esito = scanner.nextLine();
                        switch(esito) {
                            case "1": eseguiRicercaGuest(tipoRicerca); break;
                            case "2": return;
                            default: System.out.println("Opzione non valida."); break;
                        }
                    }
                    for (Proiezione proiezione : titolo) {
                    System.out.println((count + 1) + ". " + proiezione.getTitolo() + " - " + proiezione.getDataOra());
                    count++;
                    }
                    System.out.print("\nInserisci il numero della proiezione per i dettagli (o 0 per annullare): ");
                    int scelta = Integer.parseInt(scanner.nextLine());
                    if (scelta > 0 && scelta <= titolo.size()) {
                        visualizzaProiezione(titolo.get(scelta - 1));
                        esci=true;
                    } else if (scelta != 0) {
                        System.out.println("\nScelta non valida.\n");
                    }else if (scelta == 0) {
                    break;
                    }
                } catch (NumberFormatException e) {
                System.out.println("\nInput non valido. Inserisci un numero.\n");
                }
            }
        }  else if (tipoRicerca.equals("GENERE")) {
            System.out.print("Inserisci il genere (scegli tra Action, Animation, Thriller, Drama, Biography, Comedy, Crime, Film-Noir, Horror): ");
            filtro = scanner.nextLine().toLowerCase();
            List<Proiezione> titolo = future.cercaPerGenere(filtro);
            boolean esci = false;
            while(!esci) {
                int count = 0;
                try {
                    if(titolo.isEmpty()) {  
                        System.out.println("Nessuna proiezione futura trovata per questa ricerca.");
                        System.out.println("Desideri riprovare?");
                        System.out.println("1: Sì");
                        System.out.println("2: No (Torna al menù guest)");

                        String esito = scanner.nextLine();
                        switch(esito) {
                            case "1": eseguiRicercaGuest(tipoRicerca);
                            case "2": return;
                            default: System.out.println("Opzione non valida."); break;
                        }
                    }
                    for (Proiezione proiezione : titolo) {
                    System.out.println((count + 1) + ". " + proiezione.getTitolo() + " - " + proiezione.getDataOra());
                    count++;
                    }
                    System.out.print("\nInserisci il numero della proiezione per i dettagli (o 0 per annullare): ");
                    int scelta = Integer.parseInt(scanner.nextLine());
                    if (scelta > 0 && scelta <= titolo.size()) {
                    visualizzaProiezione(titolo.get(scelta - 1));
                    esci=true;
                    } else if (scelta != 0) {
                    System.out.println("\nScelta non valida.\n");
                    } else if (scelta == 0) {
                    break;
                    }
                } catch (NumberFormatException e) {
                System.out.println("\nInput non valido. Inserisci un numero.\n");
                }
            }
        } else if (tipoRicerca.equals("DATA")) {
            
            System.out.println("Cerca film TRA due date");
            System.out.println("Desideri usare la data odierna come prima data?");
            System.out.println("1) Sì");
            System.out.println("2) No");
            boolean esci = false;
            LocalDate dataInizio = LocalDate.now();
            while(!esci) {
                String esito = scanner.nextLine();
                
                switch(esito) {
                    case "1": esci=true; break;
                    case "2": {
                         dataInizio = leggiData("Inserisci la data");
                        esci=true; 
                        break;
                    }
                    default: System.out.println("Opzione non valida.");
                }
            }
            System.out.println("Inserisci la seconda data: ");
            LocalDate dataFine=leggiData("Inserisci la data");
            List<Proiezione> titolo = gestoreProiezioni.cercaTraDate(dataInizio, dataFine);
            esci = false;
            while(!esci) {
                int count = 0;
                try {
                    if(titolo.isEmpty()) {  
                        System.out.println("Nessuna proiezione trovata per questa ricerca.");
                        System.out.println("Desideri riprovare?");
                        System.out.println("1: Sì");
                        System.out.println("2: No (Torna al menù guest)");

                        String esito = scanner.next();
                        switch(esito) {
                            case "1": eseguiRicercaGuest(tipoRicerca); break;
                            case "2": return;
                            default: System.out.println("Opzione non valida."); break;
                        }
                    }
                    for (Proiezione proiezione : titolo) {
                    System.out.println((count + 1) + ". " + proiezione.getTitolo() + " - " + proiezione.getDataOra());
                    count++;
                    }
                    System.out.print("\nInserisci il numero della proiezione per i dettagli (o 0 per annullare): ");
                    int scelta = Integer.parseInt(scanner.next());
                    if (scelta > 0 && scelta <= titolo.size()) {
                    visualizzaProiezione(titolo.get(scelta - 1));
                    esci=true;
                    } else if (scelta != 0) {
                    System.out.println("\nScelta non valida.\n");
                    } else if (scelta == 0) {
                    break;
                    }
                } catch (NumberFormatException e) {
                System.out.println("\nInput non valido. Inserisci un numero.\n");
                }
            }
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
        System.out.println("Costo biglietto: Euro " + p.getCostoBiglietto());
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

        
        System.out.println("\n--- Cerca Prenotazione ---");
        System.out.println("1. Per Codice Prenotazione");
        System.out.println("2. Per Titolo film");
        System.out.println("3. Per Nome o Cognome cliente");
        System.out.println("0. Torna al menù");
        System.out.print("Scegli il criterio: ");

        String criterio = scanner.nextLine();
        switch (criterio) {   
            case "1":
                System.out.println("Inserisci Codice: "); 
                break;
            case "2":
                System.out.println("Inserisci Titolo: ");
                break;
            case "3":
                System.out.println("Inserisci nome o cognome cliente: ");
                break;
            case "0":
                return;
            default:
                System.out.println("Opzione non valida.");
                cercaPrenotazioneBigliettaio();
        } 
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
        System.out.println("Biglietti: " + p.getNumeroBiglietti() + " | Costo unitario: Euro " + p.getCostoUnitario());
        System.out.println("Costo Totale: Euro " + p.getCostoTotale());
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
        LocalDateTime dataOra = leggiDataOra("Data e Ora della nuova proiezione");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        // Controllo sovrapposizione
        for (Proiezione p : gestoreProiezioni.getListaProiezioni()) {
            LocalDateTime dataProiez = LocalDateTime.parse(p.getDataOra(), formatter);
            if (dataProiez.equals(dataOra)) {
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


        
        Proiezione nuova = new Proiezione(dataOra.format(formatter), titolo, genere, regista, anno, durata, eta, costo);
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
                
                LocalDateTime nuovaData = leggiDataOra("Inserisci la nuova Data e Ora");
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                p.setDataOra(nuovaData.format(formatter));
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
    
    protected static LocalDate leggiData(String messaggio) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        while (true) {
            System.out.print(messaggio + " (Formato richiesto: AAAA-MM-GG, es. 2026-05-15): ");
            String input = scanner.next();
            try {
                // Tenta di convertire la stringa in una data reale. Se fallisce, va nel catch.
                LocalDate data = LocalDate.parse(input, formatter);
                return data; // Restituisce la stringa sicura e formattata
            } catch (DateTimeParseException e) {
                System.out.println("Errore: Formato data non valido. Controlla la struttura con i trattini oppure se la data è errata o inesistente.");
            }
        }
    }

    private static LocalDateTime leggiDataOra(String messaggio) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        while (true) {
            System.out.print(messaggio + " (Formato: AAAA-MM-GG HH:MM, es. 2026-05-15 21:30): ");
            String input = scanner.nextLine();
            try {
                // Tenta di convertire la stringa in data+ora.
                input = input.concat(":00");
                LocalDateTime dataOra = LocalDateTime.parse(input, formatter);
                return dataOra; // Restituisce la stringa perfetta
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