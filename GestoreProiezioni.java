import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GestoreProiezioni {
    // Il nome del file indicato dalle specifiche
    private static final String FILE_PROIEZIONI = "proiezioni.csv"; 
    private static final String DELIMITATORE = ";"; // Modifica se il file fornito usa la virgola
    private List<Proiezione> listaProiezioni;

    public GestoreProiezioni() {
        listaProiezioni = new ArrayList<>();
        caricaProiezioniDaCSV();
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
                // Controllo che ci siano tutti gli 8 campi previsti
                if (dati.length >= 8) {
                    try {
                        String dataOra = dati[0].trim();
                        String titolo = dati[1].trim();
                        String genere = dati[2].trim();
                        String regista = dati[3].trim();
                        int anno = Integer.parseInt(dati[4].trim());
                        int durata = Integer.parseInt(dati[5].trim());
                        int etaMinima = Integer.parseInt(dati[6].trim());
                        double costo = Double.parseDouble(dati[7].trim().replace(",", "."));

                        Proiezione p = new Proiezione(dataOra, titolo, genere, regista, anno, durata, etaMinima, costo);
                        listaProiezioni.add(p);
                    } catch (NumberFormatException e) {
                        System.out.println("Errore nel formato numerico di una riga in " + FILE_PROIEZIONI);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Errore durante la lettura del file " + FILE_PROIEZIONI);
        }
    }

    // Ricerca parziale per titolo (Funzionalità Guest)
    public void cercaPerTitolo(String titoloCercato) {
        boolean trovato = false;
        String titoloLower = titoloCercato.toLowerCase();

        for (Proiezione p : listaProiezioni) {
            if (p.getTitolo().toLowerCase().contains(titoloLower)) {
                System.out.println(p.toString());
                trovato = true;
            }
        }

        if (!trovato) {
            System.out.println("Nessuna proiezione trovata per il titolo: " + titoloCercato);
        }
    }
    
    // Ritorna l'intera lista (ci servirà in seguito)
    public List<Proiezione> getListaProiezioni() {
        return listaProiezioni;
    }
}