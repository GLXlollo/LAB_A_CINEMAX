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


public class GestoreProiezioni {
    // Il nome del file indicato dalle specifiche
    private static final String FILE_PROIEZIONI = "proiezioni.csv"; 
    private static final String DELIMITATORE = ","; // Modifica se il file fornito usa la virgola
    private List<Proiezione> listaProiezioni;

    public GestoreProiezioni() {
        listaProiezioni = new ArrayList<>();
        caricaProiezioniDaCSV();
    }

    public GestoreProiezioni(List<Proiezione> inpuArrayList) {
        listaProiezioni = inpuArrayList;
    }

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

    // Ricerca parziale per titolo (Funzionalità Guest)
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
    
    // Ricerca tra 2 date (Funzionalità Guest)
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

    public List<Proiezione> cercaPrimaFine( LocalDate DataFine) {
        List<Proiezione> risultati = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (Proiezione p : listaProiezioni) {
            LocalDate l = LocalDate.parse(p.getDataOra().toString().substring(0, 10), formatter);
            if( l.isBefore(DataFine)) 
                risultati.add(p);
        }
        return risultati;
    }


    // Ritorna l'intera lista (ci servirà in seguito)
    public List<Proiezione> getListaProiezioni() {
        return listaProiezioni;
    }

    // --- METODI PER IL PROIEZIONISTA ---
    public void aggiungiProiezione(Proiezione p) {
        listaProiezioni.add(p);
        riscriviFileCSV();
    }

    public void eliminaProiezione(Proiezione p) {
        listaProiezioni.remove(p);
        riscriviFileCSV();
    }

    public void riscriviFileCSV() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_PROIEZIONI, false))) {
            // Riscrivo l'intestazione
            pw.println("DataOra,Titolo,Genere,Regista,Anno,Durata,EtaMinima,Costo"); 
            for (Proiezione p : listaProiezioni) {
                pw.println(p.getDataOra() + DELIMITATORE + p.getTitolo() + DELIMITATORE + 
                           p.getGenere() + DELIMITATORE + p.getRegista() + DELIMITATORE + 
                           p.getAnno() + DELIMITATORE + p.getDurata() + DELIMITATORE + 
                           p.getEtaMinima() + DELIMITATORE + p.getCostoBiglietto());
            }
        } catch (IOException e) {
            System.out.println("Errore nella riscrittura del file " + FILE_PROIEZIONI);
        }
    }

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