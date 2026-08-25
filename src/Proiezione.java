/*
Costantini Marco , 762497 , VA
Colombo Davide , 760761 , VA
Bonza Lorenzo , 760667 , VA
Piloni Luca , 762374 , VA
*/

package src;
import java.io.Serializable;

/**
 * Rappresenta una proiezione cinematografica.
 * Contiene i dettagli del film, della data/ora, i costi e la gestione dei posti disponibili.
 * Ogni sala ha 200 posti totali.
 */
public class Proiezione implements Serializable {
    private static final long serialVersionUID = 1L;

    private String dataOra; // Es. "2026-05-15 21:00"
    private String titolo;
    private String genere;
    private String regista;
    private int anno;
    private int durata; // in minuti
    private int etaMinima;
    private double costoBiglietto;
    
    // La sala ha 200 posti di default
    private final int POSTI_TOTALI = 200;
    private int postiPrenotati = 0;


    /**
     * Costruttore che crea una nuova proiezione con tutti i dati.
     * @param dataOra la data e l'ora della proiezione (formato: yyyy-MM-dd HH:mm:ss)
     * @param titolo il titolo del film
     * @param genere il genere del film
     * @param regista il nome del regista
     * @param anno l'anno di uscita del film
     * @param durata la durata del film in minuti
     * @param etaMinima l'eta minima consigliata per il film
     * @param costoBiglietto il prezzo di un biglietto per questa proiezione
     */
    public Proiezione(String dataOra, String titolo, String genere, String regista, 
                      int anno, int durata, int etaMinima, double costoBiglietto) {
        this.dataOra = dataOra;
        this.titolo = titolo;
        this.genere = genere;
        this.regista = regista;
        this.anno = anno;
        this.durata = durata;
        this.etaMinima = etaMinima;
        this.costoBiglietto = costoBiglietto;
    }

    /**
     * Restituisce la data e l'ora della proiezione.
     * @return la data e l'ora nel formato yyyy-MM-dd HH:mm:ss
     */
    public String getDataOra() { return dataOra; }
    
    /**
     * Restituisce il titolo del film.
     * @return il titolo del film
     */
    public String getTitolo() { return titolo; }
    
    /**
     * Restituisce il genere del film.
     * @return il genere del film
     */
    public String getGenere() { return genere; }
    
    /**
     * Restituisce il nome del regista.
     * @return il nome del regista
     */
    public String getRegista() { return regista; }
    
    /**
     * Restituisce l'anno di uscita del film.
     * @return l'anno di uscita
     */
    public int getAnno() { return anno; }
    
    /**
     * Restituisce la durata del film.
     * @return la durata in minuti
     */
    public int getDurata() { return durata; }
    
    /**
     * Restituisce l'eta minima consigliata.
     * @return l'eta minima
     */
    public int getEtaMinima() { return etaMinima; }
    
    /**
     * Restituisce il costo di un biglietto per questa proiezione.
     * @return il costo in euro
     */
    public double getCostoBiglietto() { return costoBiglietto; }
    
    /**
     * Restituisce il numero di posti ancora disponibili.
     * @return i posti liberi (posti totali - posti prenotati)
     */
    public int getPostiLiberi() { 
        return POSTI_TOTALI - postiPrenotati; 
    }
    
    /**
     * Occupa i posti prenotati per questa proiezione.
     * Aumenta il contatore dei posti prenotati se ce n'e disponibilita.
     * @param numeroPosti il numero di posti da prenotare
     */
    public void prenotaPosti(int numeroPosti) {
        if (getPostiLiberi() >= numeroPosti) {
            postiPrenotati += numeroPosti;
        }
    }
    
    /**
     * Libera i posti precedentemente prenotati.
     * Diminuisce il contatore dei posti prenotati (usato per cancellazioni di prenotazioni).
     * @param numeroPosti il numero di posti da liberare
     */
    public void liberaPosti(int numeroPosti) {
        postiPrenotati -= numeroPosti;
    }

    /**
     * Modifica la data e l'ora della proiezione.
     * @param nuovaDataOra la nuova data e ora nel formato yyyy-MM-dd HH:mm:ss
     */
    public void setDataOra(String nuovaDataOra) {
        this.dataOra = nuovaDataOra;
    }

    @Override
    public String toString() {
        return titolo + " (" + anno + ") - " + genere + " | Regia: " + regista + 
               " | Durata: " + durata + " min | Data/Ora: " + dataOra + 
               " | Costo: €" + costoBiglietto + " | Posti liberi: " + getPostiLiberi();
    }
}