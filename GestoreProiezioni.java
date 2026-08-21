import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.io.FileWriter;
import java.io.PrintWriter;


/**
 * Gestisce le proiezioni cinematografiche.
 * Carica, crea, modifica ed elimina proiezioni dal file CSV.
 * Fornisce metodi di ricerca per titolo, genere, data e per le proiezioni future.
 */
public class GestoreProiezioni {
    // Il nome del file indicato dalle specifiche
    private static final String FILE_PROIEZIONI = "proiezioni.csv"; 
    private static final String DELIMITATORE = ","; // Modifica se il file fornito usa la virgola
    private List<Proiezione> listaProiezioni;

    /**
     * Costruttore che inizializza il gestore e carica tutte le proiezioni dal file CSV.
     */
    public GestoreProiezioni() {
        listaProiezioni = new ArrayList<>();
        caricaProiezioniDaCSV();
    }

    /**
     * Costruttore alternativo che inizializza il gestore con una lista di proiezioni fornita.
     * Utilizzato per creare un gestore con un sottoinsieme di proiezioni (es. proiezioni future).
     * @param inpuArrayList la lista di proiezioni da utilizzare
     */
    public GestoreProiezioni(List<Proiezione> inpuArrayList) {
        listaProiezioni = inpuArrayList;
    }

    /**
     * Carica tutte le proiezioni dal file CSV e le mantiene in memoria.
     * Gestisce i casi in cui il titolo contiene virgole (delimitatore) creando un array di 9 campi.
     */
    private void caricaProiezioniDaCSV() {
        File file = new File(FILE_PROIEZIONI);
        if (!file.exists()) {
            System.out.println("Attenzione: File " + FILE_PROIEZIONI + " non trovato.");
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String linea;
            boolean primaLinea = true;

            while ((linea = br.readLine()) != null) {
                // Salta l'intestazione
                if (primaLinea) {
                    primaLinea = false;
                    continue;
                }

                String[] dati = linea.split(DELIMITATORE);
                /*Controllo che ci siano tutti gli 8 campi previsti
                Usare la virgola come delimitatore espone il metodo a creare
                più campi dell'array dati del necessario, questo perchè 
                in alcuni titoli di film del dataset dato è presente una virgola
                Questo crea 1 campo in più, e fa sollevare la NumberformatException
                come conseguenza se si utilizza dati.lenght >= 8 */
                if (dati.length == 8) {
                    try {
                        String dataOra = dati[0].trim();
                        String titolo = dati[1].trim();
                        String genere = dati[2].trim();
                        String regista = dati[3].trim();
                        int anno = Integer.parseInt(dati[4]);
                        int durata = Integer.parseInt(dati[5]);
                        int etaMinima = Integer.parseInt(dati[6]);
                        double costo = Double.parseDouble(dati[7]);

                        Proiezione p = new Proiezione(dataOra, titolo, genere, regista, anno, durata, etaMinima, costo);
                        listaProiezioni.add(p);
                    } catch (NumberFormatException e) {
                        System.out.println("Errore nel formato numerico di una riga in " + FILE_PROIEZIONI);
                        System.out.println(dati[0] + dati[1]);
                    }
                } else if (dati.length == 9) {
                     try {
                        String dataOra = dati[0].trim();
                        String titolo = dati[1].concat(dati[2]).trim(); // riunuiamo il titolo del film
                        String genere = dati[3].trim();
                        String regista = dati[4].trim();
                        int anno = Integer.parseInt(dati[5]);
                        int durata = Integer.parseInt(dati[6]);
                        int etaMinima = Integer.parseInt(dati[7]);
                        double costo = Double.parseDouble(dati[8]);

                        Proiezione p = new Proiezione(dataOra, titolo, genere, regista, anno, durata, etaMinima, costo);
                        listaProiezioni.add(p);
                    } catch (NumberFormatException e) {
                        System.out.println("Errore nel formato numerico di una riga in " + FILE_PROIEZIONI);
                        System.out.println(dati[0] + dati[1]);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Errore durante la lettura del file " + FILE_PROIEZIONI);
        }
    }

    /**
     * Cerca le proiezioni in base a una ricerca parziale del titolo.
     * La ricerca è case-insensitive e accetta sottostringhe.
     * @param titoloCercato il titolo o parte del titolo da cercare
     * @return una lista di proiezioni che corrispondono al criterio
     */
    public List<Proiezione> cercaPerTitolo(String titoloCercato) {
        List<Proiezione> risultati = new ArrayList<Proiezione>();
        String titoloLower = titoloCercato.toLowerCase();

        for (Proiezione p : listaProiezioni) {
            if (p.getTitolo().toLowerCase().contains(titoloLower)) {
                risultati.add(p);
            }
        }
        return risultati;
    }

    // Ricerca per genere (funzionalità Guest)
    /*  Viene utilizzato contains per una ricerca più avanzata che tiene conto che l'utente
    potrebbe non ricordarsi il titolo completo del film che cerca, tuttavia questo deve essere
    scritto in modo corretto, il metodo non accetta errori di ortografia per semplicità */
    public List<Proiezione> cercaPerGenere(String genereCercato) {
        List<Proiezione> risultati = new ArrayList<>();
        for (Proiezione p : listaProiezioni) {
            if(p.getGenere().toLowerCase().contains(genereCercato.toLowerCase()))
                risultati.add(p);
        }
        return risultati;
    }
    
    /**
     * Cerca le proiezioni che si svolgono tra due date specifiche (escluse gli estremi).
     * @param DataInizio la data di inizio intervallo
     * @param DataFine la data di fine intervallo
     * @return una lista di proiezioni che ricadono nel periodo specificato
     */
    public List<Proiezione> cercaTraDate(LocalDate DataInizio, LocalDate DataFine) {
        List<Proiezione> risultati = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (Proiezione p : listaProiezioni) {
            LocalDate l = LocalDate.parse(p.getDataOra().substring(0, 10), formatter);
            if(l.isAfter(DataInizio) && l.isBefore(DataFine)) 
                risultati.add(p);
        }
        return risultati;
    }

    /**
     * Cerca le proiezioni che si svolgono dopo una data specifica.
     * @param DataInizio la data di inizio
     * @return una lista di proiezioni dopo la data specifica
     */
    public List<Proiezione> cercaDopoInizio(LocalDate DataInizio) {
        List<Proiezione> risultati = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (Proiezione p : listaProiezioni) {
            LocalDate l = LocalDate.parse(p.getDataOra().toString().substring(0, 10), formatter);
            if(l.isAfter(DataInizio)) 
                risultati.add(p);
        }
        return risultati;
    }

    /**
     * Cerca le proiezioni che si svolgono prima di una data specifica.
     * @param DataFine la data di fine
     * @return una lista di proiezioni prima della data specifica
     */
    public List<Proiezione> cercaPrimaFine(LocalDate DataFine) {
        List<Proiezione> risultati = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (Proiezione p : listaProiezioni) {
            LocalDate l = LocalDate.parse(p.getDataOra().toString().substring(0, 10), formatter);
            if( l.isBefore(DataFine)) 
                risultati.add(p);
        }
        return risultati;
    }


    /**
     * Restituisce l'intera lista di proiezioni caricata in memoria.
     * @return la lista completa di tutte le proiezioni
     */
    public List<Proiezione> getListaProiezioni() {
        return listaProiezioni;
    }

    /**
     * Aggiunge una nuova proiezione al palinsesto.
     * Salva la proiezione in memoria e aggiorna il file CSV.
     * @param p la proiezione da aggiungere
     */
    // --- METODI PER IL PROIEZIONISTA ---
    public void aggiungiProiezione(Proiezione p) {
        listaProiezioni.add(p);
        riscriviFileCSV();
    }

    /**
     * Elimina una proiezione dal palinsesto.
     * Rimuove la proiezione in memoria e aggiorna il file CSV.
     * @param p la proiezione da eliminare
     */
    public void eliminaProiezione(Proiezione p) {
        listaProiezioni.remove(p);
        riscriviFileCSV();
    }

    /**
     * Riscrive l'intero file CSV delle proiezioni con i dati correnti in memoria.
     * Utilizzato dopo modifiche, aggiunte o eliminazioni di proiezioni.
     */
    public void riscriviFileCSV() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_PROIEZIONI, false))) {
            // Riscrivo l'intestazione
            pw.println("DataOra,Titolo,Genere,Regista,Anno,Durata,EtaMinima,Costo"); 
            for (Proiezione p : listaProiezioni) {
                try {
                    pw.println(p.getDataOra() + DELIMITATORE + p.getTitolo() + DELIMITATORE + 
                    p.getGenere() + DELIMITATORE + p.getRegista() + DELIMITATORE + 
                    p.getAnno() + DELIMITATORE + p.getDurata() + DELIMITATORE + 
                    p.getEtaMinima() + DELIMITATORE + p.getCostoBiglietto());
                } catch (java.lang.StringIndexOutOfBoundsException e) {
                    System.out.println("Errore nel metodo p.getDataOra del film: " + p.getTitolo());
                }
                
            }
        } catch (IOException e) {
            System.out.println("Errore nella riscrittura del file " + FILE_PROIEZIONI);
        } 
    }

    /**
     * Restituisce un nuovo GestoreProiezioni contenente solo le proiezioni future (dopo l'ora attuale).
     * @return un GestoreProiezioni con le sole proiezioni future
     */
    public GestoreProiezioni futureProiz() {
        List<Proiezione> future = new ArrayList<Proiezione>();
        for (Proiezione p : listaProiezioni) {
            LocalDateTime dataP = LocalDateTime.parse(p.getDataOra(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            if(dataP.isAfter(LocalDateTime.now()))
                future.add(p);
        }
        return new GestoreProiezioni(future);
    }
}