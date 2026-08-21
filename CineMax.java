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

    /**
     * Metodo principale che avvia l'applicazione CineMax.
     * Presenta un menu di login con tre opzioni: login utente registrato, registrazione nuovo utente, accesso guest.
     * Sincronizza i posti occupati all'avvio leggendo lo storico prenotazioni.
     */
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

    /**
     * Gestisce il processo di login per gli utenti registrati.
     * Richiede username e password, autentica l'utente e lo indirizza al menu appropriato in base al suo ruolo (Cliente, Proiezionista, Bigliettaio).
     * Effettua automaticamente il logout dopo il completamento delle operazioni.
     */
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

    /**
     * Presenta il menu principale per i clienti.
     * Consente di cercare e prenotare proiezioni, visualizzare le proprie prenotazioni, modificarle o eliminarle.
     */
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

    /**
     * Permette al cliente di cercare film per titolo e prenotare biglietti.
     * Visualizza le proiezioni future corrispondenti al titolo inserito e consente di selezionare una per completare la prenotazione.
     * Controlla la disponibilità di posti prima di confermare la prenotazione.
     */
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
                
                if (scelta == 0) {
                    System.out.println("Prenotazione annullata. Ritorno al menu.");
                    return; // Esce e torna al menu cliente
                } else if (scelta > 0 && scelta <= titolo.size()) {
                    Proiezione p = titolo.get(scelta - 1);
                    visualizzaProiezione(p); 
                
                    // Usiamo il nostro validatore per impedire numeri negativi!
                    int numBiglietti = leggiInteroPositivo("\nQuanti biglietti vuoi prenotare per questo film? (0 per annullare): ", true);
                
                    if (numBiglietti > 0) {
                        gestorePrenotazioni.creaPrenotazione(utenteLoggato.getUsername(), p, numBiglietti);
                    }
                    esci = true;
                } else {
                    // Stampa l'errore e non cambia "esci". Il while ripartirà e stamperà di nuovo la lista!
                    System.out.println("Errore: devi inserire uno dei numeri in elenco (da 1 a " + titolo.size() + ") oppure 0 per annullare.\n");
                }
            } catch (NumberFormatException e) {
                System.out.println("Input non valido. Inserisci un numero.");
            }
        }
    }

    /**
     * Visualizza tutte le prenotazioni effettuate dal cliente attualmente loggato.
     * Mostra i dettagli completi di ogni prenotazione oppure comunica se non ci sono prenotazioni.
     */
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

    /**
     * Consente al cliente di cancellare una sua prenotazione.
     * Richiede il codice univoco della prenotazione, verifica che appartenga al cliente e che la proiezione non sia già avvenuta.
     * Libera i posti precedentemente occupati e rimuove la prenotazione dal sistema.
     */
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

    /**
     * Permette al cliente di modificare la data e l'ora di una sua prenotazione.
     * Verifica che la prenotazione appartenga al cliente, che non sia per un film già passato, e che la nuova proiezione abbia posti disponibili.
     * Libera i posti dalla vecchia proiezione e li occupa nella nuova.
     */
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

        // Ciclo infinito: ne usciamo solo con 'return' se l'utente digita 0 o fa la modifica con successo
        while (true) {
            System.out.print("\nScegli il numero della nuova proiezione (o 0 per annullare): ");
            try {
                int scelta = Integer.parseInt(scanner.nextLine());
                
                if (scelta == 0) {
                    System.out.println("Modifica annullata. Ritorno al menu principale.");
                    return; // Interrompe il metodo e torna al menu
                } else if (scelta > 0 && scelta < count) {
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
                        return; // Modifica fatta, usciamo dal metodo!
                    } else {
                        System.out.println("Errore: La nuova proiezione non ha abbastanza posti.");
                        return;
                    }
                } else {
                    System.out.println("Errore: devi inserire uno dei numeri in elenco (da 1 a " + (count - 1) + ") oppure 0 per tornare al menu.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Input non valido. Inserisci un numero.");
            }
        }
    }

    /**
     * Gestisce il processo di registrazione di nuovi utenti.
     * Richiede la scelta del tipo di account (Cliente, Proiezionista, Bigliettaio) e, per i dipendenti, verifica il PIN aziendale.
     * Raccoglie i dati personali e crea un nuovo account nel sistema.
     */
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

    /**
     * Presenta il menu per gli utenti guest (non registrati).
     * Consente la ricerca di proiezioni per titolo, genere o data, senza possibilità di prenotare.
     */
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

    /**
     * Esegue una ricerca di proiezioni future in base al tipo di ricerca specificato.
     * Supporta tre modalità: ricerca per titolo, ricerca per genere, ricerca per intervallo di date.
     * Visualizza i risultati e consente di selezionare una proiezione per vederne i dettagli completi.
     * @param tipoRicerca il tipo di ricerca da eseguire: "TITOLO", "GENERE" o "DATA"
     */
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

    /**
     * Visualizza i dettagli completi di una proiezione.
     * Mostra titolo, genere, regista, anno, durata, data/ora, costo biglietto e posti liberi disponibili.
     * @param p la proiezione di cui visualizzare i dettagli
     */
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

    /**
     * Presenta il menu principale per i bigliettai.
     * Consente di visualizzare le prenotazioni di oggi e di cercare prenotazioni specifiche per codice, titolo o cliente.
     */
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

    /**
     * Visualizza tutte le prenotazioni per le proiezioni in programma oggi.
     * Recupera la data odierna dal sistema e filtra le prenotazioni per quella data.
     */
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

    /**
     * Consente al bigliettaio di cercare prenotazioni nel sistema.
     * Supporta tre criteri di ricerca: per codice prenotazione, per titolo film, per nome o cognome cliente.
     * Visualizza i dettagli di tutte le prenotazioni corrispondenti ai criteri inseriti.
     */
    private static void cercaPrenotazioneBigliettaio() {
        // Usiamo il ciclo instead della ricorsione!
        while (true) {
            System.out.println("\n--- Cerca Prenotazione ---");
            System.out.println("1. Per Codice Prenotazione");
            System.out.println("2. Per Titolo film");
            System.out.println("3. Per Nome o Cognome cliente");
            System.out.println("0. Torna al menù");
            System.out.print("Scegli il criterio: ");

            String criterio = scanner.nextLine().trim();
            
            // Gestiamo subito l'uscita o l'errore
            if (criterio.equals("0")) {
                return; // Esce e torna al menu principale
            } else if (!criterio.equals("1") && !criterio.equals("2") && !criterio.equals("3")) {
                System.out.println("Opzione non valida. Riprova.");
                continue; // Ricomincia il ciclo da capo
            }

            // Chiediamo l'input specifico senza fare switch inutili
            if (criterio.equals("1")) {
                System.out.print("Inserisci Codice: "); 
            } else if (criterio.equals("2")) {
                System.out.print("Inserisci Titolo: ");
            } else if (criterio.equals("3")) {
                System.out.print("Inserisci nome o cognome cliente: ");
            }
            
            String testo = scanner.nextLine().toLowerCase().trim();
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
            
            // RICERCA FINITA: usciamo dal metodo e torniamo dritti al menu Bigliettaio!
            return; 
        }
    }

    /**
     * Stampa a schermo i dettagli completi di una prenotazione.
     * Mostra codice, cliente, film, orario, numero di biglietti, costo unitario e costo totale.
     * @param p la prenotazione di cui stampare i dettagli
     */
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

    /**
     * Presenta il menu principale per i proiezionisti.
     * Consente di aggiungere, modificare o eliminare proiezioni dal palinsesto.
     */
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

    /**
     * Permette al proiezionista di aggiungere una nuova proiezione al palinsesto.
     * Richiede data/ora, titolo, genere, regista, anno, durata, fascia d'età e costo del biglietto.
     * Verifica che non esista già una proiezione nella stessa data e ora e salva i dati nel sistema.
     */
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

    /**
     * Permette al proiezionista di modificare la data e l'ora di una proiezione esistente.
     * Verifica che non ci siano prenotazioni attive sulla proiezione prima di permettere la modifica.
     * Aggiorna la data e l'ora nel sistema e salva le modifiche nel file.
     */
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

        // Ciclo infinito per bloccare l'utente finché non inserisce un numero valido
        while (true) {
            System.out.print("\nScegli la proiezione da modificare (0 per annullare): ");
            try {
                int scelta = Integer.parseInt(scanner.nextLine());
                
                if (scelta == 0) {
                    System.out.println("Modifica annullata. Ritorno al menu principale.");
                    return; // Esce dal ciclo e dal metodo
                } else if (scelta > 0 && scelta <= risultati.size()) {
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
                    
                    return; // Modifica fatta, usciamo dal ciclo
                } else {
                    // Messaggio di errore se mette un numero troppo alto (senza uscire dal menu)
                    System.out.println("Errore: devi inserire uno dei numeri in elenco (da 1 a " + risultati.size() + ") oppure 0 per tornare al menu.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Input non valido. Inserisci un numero.");
            }
        }
    }

    /**
     * Permette al proiezionista di eliminare una proiezione dal palinsesto.
     * Verifica che non ci siano prenotazioni attive sulla proiezione prima di permettere l'eliminazione.
     * Rimuove la proiezione dal sistema e salva le modifiche nel file.
     */
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

        // Ciclo infinito per bloccare l'utente finché non inserisce un numero valido
        while (true) {
            System.out.print("\nScegli la proiezione da eliminare in modo definitivo (0 per annullare): ");
            try {
                int scelta = Integer.parseInt(scanner.nextLine());
                
                if (scelta == 0) {
                    System.out.println("Eliminazione annullata. Ritorno al menu principale.");
                    return; // Esce dal ciclo e dal metodo
                } else if (scelta > 0 && scelta <= risultati.size()) {
                    Proiezione p = risultati.get(scelta - 1);
                    
                    // Controllo prenotazioni esistenti (Specifiche pag. 13)
                    if (haPrenotazioni(p)) {
                        System.out.println("Errore: Non puoi eliminare una proiezione che ha già delle prenotazioni!");
                        return;
                    }
                    
                    // --- IL TUO CODICE DI ELIMINAZIONE INVARIATO ---
                    gestoreProiezioni.eliminaProiezione(p);
                    System.out.println("Proiezione eliminata con successo dal palinsesto!");
                    
                    return; // Eliminazione fatta, usciamo dal ciclo
                } else {
                    // Messaggio di errore se mette un numero sballato
                    System.out.println("Errore: devi inserire uno dei numeri in elenco (da 1 a " + risultati.size() + ") oppure 0 per tornare al menu.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Input non valido. Inserisci un numero.");
            }
        }
    }

    /**
     * Verifica se esiste almeno una prenotazione attiva per una determinata proiezione.
     * Utilizzato per bloccare la modifica o l'eliminazione di una proiezione che ha già biglietti venduti.
     * @param p la proiezione da controllare
     * @return true se ci sono prenotazioni attive, false altrimenti
     */
    // Metodo di supporto per bloccare modifiche/cancellazioni se ci sono biglietti venduti
    private static boolean haPrenotazioni(Proiezione p) {
        for (Prenotazione pren : gestorePrenotazioni.getTutteLePrenotazioni()) {
            if (pren.getTitoloFilm().equals(p.getTitolo()) && pren.getDataOraProiezione().equals(p.getDataOra())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sincronizza lo stato dei posti occupati nelle proiezioni leggendo lo storico delle prenotazioni.
     * Eseguito all'avvio dell'applicazione per ricalcolare i posti corretti in caso di chiusure impreviste.
     * Ripete tutte le prenotazioni storiche per ricalcolare lo stato di ogni proiezione.
     */
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

    /**
     * Legge una data da input dell'utente con validazione del formato.
     * Il formato richiesto è AAAA-MM-GG (ad es. 2026-05-15).
     * Continua a chiedere l'input finché l'utente non inserisce una data valida.
     * @param messaggio il messaggio da visualizzare prima di richiedere la data
     * @return la LocalDate parsificata e validata
     */
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

    /**
     * Legge una data e un'ora da input dell'utente con validazione del formato.
     * Il formato richiesto è AAAA-MM-GG HH:MM (ad es. 2026-05-15 21:30).
     * Continua a chiedere l'input finché l'utente non inserisce una data e ora valide.
     * @param messaggio il messaggio da visualizzare prima di richiedere la data e l'ora
     * @return la LocalDateTime parsificata e validata
     */
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

    /**
     * Legge un numero intero positivo da input dell'utente con validazione.
     * Continua a chiedere l'input finché l'utente non inserisce un numero intero valido.
     * @param messaggio il messaggio da visualizzare prima di richiedere il numero
     * @param consentiZero se true, accetta anche lo zero; se false, richiede un numero strettamente positivo
     * @return il numero intero validato
     */
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

    /**
     * Legge un numero decimale positivo da input dell'utente con validazione.
     * Accetta sia il punto che la virgola come separatore decimale.
     * Continua a chiedere l'input finché l'utente non inserisce un numero decimale valido.
     * @param messaggio il messaggio da visualizzare prima di richiedere il numero
     * @return il numero decimale validato (>= 0)
     */
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