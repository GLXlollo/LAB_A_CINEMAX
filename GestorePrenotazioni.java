import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestisce le prenotazioni dei clienti.
 * Carica, crea, modifica ed elimina prenotazioni dal file CSV.
 */
public class GestorePrenotazioni {
    private static final String FILE_PRENOTAZIONI = "Prenotazioni.csv";
    private static final String DELIMITATORE = ";";
    private List<Prenotazione> listaPrenotazioni;

    /**
     * Costruttore che inizializza il gestore e carica tutte le prenotazioni dal file CSV.
     */
    public GestorePrenotazioni() {
        listaPrenotazioni = new ArrayList<>();
        caricaPrenotazioniDaCSV();
    }

    /**
     * Carica tutte le prenotazioni dal file CSV e le mantiene in memoria.
     * Se il file non esiste, lo crea con l'intestazione appropriata.
     */
    private void caricaPrenotazioniDaCSV() {
        File file = new File(FILE_PRENOTAZIONI);

        if (!file.exists()) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
                // Genera il file fisico e scrive subito la riga di intestazione
                pw.println("Codice;Username;TitoloFilm;DataOra;NumeroBiglietti;CostoUnitario;CostoTotale");
            } catch (IOException e) {
                System.err.println("Errore nella creazione del file " + FILE_PRENOTAZIONI);
            }
            return; // Esce perché il file è appena nato ed è vuoto
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String linea;
            boolean primaLinea = true;

            while ((linea = br.readLine()) != null) {
                if (primaLinea) {
                    primaLinea = false;
                    continue; 
                }
                
                String[] dati = linea.split(DELIMITATORE);
                if (dati.length == 7) {
                    Prenotazione p = new Prenotazione(
                            dati[0], dati[1], dati[2], dati[3], 
                            Integer.parseInt(dati[4]), 
                            Double.parseDouble(dati[5]), 
                            Double.parseDouble(dati[6])
                    );
                    listaPrenotazioni.add(p);
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.out.println("Errore durante la lettura del file " + FILE_PRENOTAZIONI);
        }
    }

    /**
     * Crea una nuova prenotazione per un cliente su una proiezione specifica.
     * Verifica la disponibilita di posti, crea la prenotazione, occupa i posti e la salva nel CSV.
     * @param usernameCliente lo username del cliente che prenota
     * @param proiezione la proiezione per cui prenotare
     * @param numeroBiglietti il numero di biglietti da prenotare
     */
    public void creaPrenotazione(String usernameCliente, Proiezione proiezione, int numeroBiglietti) {
        if (proiezione.getPostiLiberi() < numeroBiglietti) {
            System.out.println("Errore: non ci sono abbastanza posti liberi. Posti rimasti: " + proiezione.getPostiLiberi());
            return;
        }

        Prenotazione nuovaPrenotazione = new Prenotazione(
                usernameCliente, proiezione.getTitolo(), proiezione.getDataOra(), 
                numeroBiglietti, proiezione.getCostoBiglietto()
        );

        listaPrenotazioni.add(nuovaPrenotazione);
        proiezione.prenotaPosti(numeroBiglietti); 

        salvaPrenotazioneSuCSV(nuovaPrenotazione);
        System.out.println("Prenotazione effettuata con successo! Il tuo codice è: " + nuovaPrenotazione.getCodiceUnivoco());
    }

    /**
     * Salva una singola prenotazione nel file CSV in modalita append.
     * Se il file non esiste, lo crea con l'intestazione.
     * @param p la prenotazione da salvare
     */
    private void salvaPrenotazioneSuCSV(Prenotazione p) {
        boolean fileEsiste = new File(FILE_PRENOTAZIONI).exists();

        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_PRENOTAZIONI, true))) {
            if (!fileEsiste) {
                pw.println("Codice;Username;TitoloFilm;DataOra;NumeroBiglietti;CostoUnitario;CostoTotale");
            }
            pw.println(p.getCodiceUnivoco() + DELIMITATORE + p.getUsernameCliente() + DELIMITATORE + 
                       p.getTitoloFilm() + DELIMITATORE + p.getDataOraProiezione() + DELIMITATORE + 
                       p.getNumeroBiglietti() + DELIMITATORE + p.getCostoUnitario() + DELIMITATORE + 
                       p.getCostoTotale());
        } catch (IOException e) {
            System.out.println("Errore nel salvataggio della prenotazione.");
        }
    }

    /**
     * Recupera tutte le prenotazioni effettuate da un cliente specifico.
     * @param username lo username del cliente
     * @return una lista di prenotazioni del cliente
     */
    public List<Prenotazione> getPrenotazioniPerUtente(String username) {
        List<Prenotazione> risultato = new ArrayList<>();
        for (Prenotazione p : listaPrenotazioni) {
            if (p.getUsernameCliente().equals(username)) {
                risultato.add(p);
            }
        }
        return risultato;
    }

    /**
     * Restituisce tutte le prenotazioni presenti nel sistema.
     * @return la lista completa di tutte le prenotazioni
     */
    public List<Prenotazione> getTutteLePrenotazioni() {
        return listaPrenotazioni;
    }

    /**
     * Recupera una prenotazione specifica cercandola per codice univoco.
     * @param codice il codice univoco della prenotazione
     * @return l'oggetto Prenotazione trovato, o null se non esiste
     */
    public Prenotazione getPrenotazioneByCodice(String codice) {
        for (Prenotazione p : listaPrenotazioni) {
            if (p.getCodiceUnivoco().equalsIgnoreCase(codice)) {
                return p;
            }
        }
        return null;
    }

    /**
     * Riscrive l'intero file CSV con le prenotazioni correnti in memoria.
     * Utilizzato dopo modifiche o eliminazioni per sincronizzare il file con lo stato in memoria.
     */
    public void riscriviFileCSV() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_PRENOTAZIONI, false))) {
            pw.println("Codice;Username;TitoloFilm;DataOra;NumeroBiglietti;CostoUnitario;CostoTotale");
            for (Prenotazione p : listaPrenotazioni) {
                pw.println(p.getCodiceUnivoco() + DELIMITATORE + p.getUsernameCliente() + DELIMITATORE + 
                           p.getTitoloFilm() + DELIMITATORE + p.getDataOraProiezione() + DELIMITATORE + 
                           p.getNumeroBiglietti() + DELIMITATORE + p.getCostoUnitario() + DELIMITATORE + 
                           p.getCostoTotale());
            }
        } catch (IOException e) {
            System.out.println("Errore nella riscrittura del file Prenotazioni.csv");
        }
    }

    /**
     * Elimina una prenotazione dal sistema e sincronizza il file CSV.
     * @param p la prenotazione da eliminare
     */
    public void eliminaPrenotazione(Prenotazione p) {
        listaPrenotazioni.remove(p);
        riscriviFileCSV();
    }
}