import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class GestorePrenotazioni {
    private static final String FILE_PRENOTAZIONI = "Prenotazioni.csv";
    private static final String DELIMITATORE = ";";
    private List<Prenotazione> listaPrenotazioni;

    public GestorePrenotazioni() {
        listaPrenotazioni = new ArrayList<>();
        caricaPrenotazioniDaCSV();
    }

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

    // Funzionalità creaPrenotazione() richiesta dalle specifiche
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

    // Funzionalità per recuperare solo le prenotazioni del cliente loggato (visualizzaPrenotazioni)
    public List<Prenotazione> getPrenotazioniPerUtente(String username) {
        List<Prenotazione> risultato = new ArrayList<>();
        for (Prenotazione p : listaPrenotazioni) {
            if (p.getUsernameCliente().equals(username)) {
                risultato.add(p);
            }
        }
        return risultato;
    }

    // Restituisce l'intera lista per le ricerche del bigliettaio
    public List<Prenotazione> getTutteLePrenotazioni() {
        return listaPrenotazioni;
    }

    // Recupera una specifica prenotazione tramite il suo codice
    public Prenotazione getPrenotazioneByCodice(String codice) {
        for (Prenotazione p : listaPrenotazioni) {
            if (p.getCodiceUnivoco().equalsIgnoreCase(codice)) {
                return p;
            }
        }
        return null;
    }

    // Riscrive tutto il file (necessario dopo aver eliminato o modificato un dato in memoria)
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

    // Elimina la prenotazione dalla memoria e aggiorna il file
    public void eliminaPrenotazione(Prenotazione p) {
        listaPrenotazioni.remove(p);
        riscriviFileCSV();
    }
}