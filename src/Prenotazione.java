/*
Costantini Marco , 762497 , VA
Colombo Davide , 760761 , VA
Bonza Lorenzo , 760667 , VA
Piloni Luca , 762374 , VA
*/

package src;
import java.io.Serializable;
import java.util.UUID;

/**
 * Rappresenta una prenotazione di biglietti per una proiezione.
 * Contiene i dati del cliente, del film, della data e dei costi associati.
 */
public class Prenotazione implements Serializable {
    private static final long serialVersionUID = 1L;

    private String codiceUnivoco;
    private String usernameCliente;
    private String titoloFilm;
    private String dataOraProiezione;
    private int numeroBiglietti;
    private double costoUnitario;
    private double costoTotale;
    
    /**
     * Costruttore per creare una nuova prenotazione.
     * Genera automaticamente un codice univoco di 8 caratteri basato su UUID.
     * Calcola il costo totale moltiplicando il numero di biglietti per il costo unitario.
     * @param usernameCliente lo username del cliente che prenota
     * @param titoloFilm il titolo del film
     * @param dataOraProiezione la data e l'ora della proiezione (formato: yyyy-MM-dd HH:mm:ss)
     * @param numeroBiglietti il numero di biglietti prenotati
     * @param costoUnitario il prezzo di un singolo biglietto
     */
    public Prenotazione(String usernameCliente, String titoloFilm, String dataOraProiezione, 
                        int numeroBiglietti, double costoUnitario) {
        // Le specifiche richiedono la generazione di un codice univoco
        this.codiceUnivoco = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.usernameCliente = usernameCliente;
        this.titoloFilm = titoloFilm;
        this.dataOraProiezione = dataOraProiezione;
        this.numeroBiglietti = numeroBiglietti;
        this.costoUnitario = costoUnitario;
        this.costoTotale = numeroBiglietti * costoUnitario;
    }

    /**
     * Costruttore per caricare una prenotazione dal file CSV.
     * Utilizza i dati completi gia salvati, senza generare un nuovo codice univoco.
     * @param codiceUnivoco il codice univoco della prenotazione (da file)
     * @param usernameCliente lo username del cliente
     * @param titoloFilm il titolo del film
     * @param dataOraProiezione la data e l'ora della proiezione
     * @param numeroBiglietti il numero di biglietti
     * @param costoUnitario il prezzo unitario del biglietto
     * @param costoTotale il costo totale della prenotazione
     */
    public Prenotazione(String codiceUnivoco, String usernameCliente, String titoloFilm, 
                        String dataOraProiezione, int numeroBiglietti, double costoUnitario, double costoTotale) {
        this.codiceUnivoco = codiceUnivoco;
        this.usernameCliente = usernameCliente;
        this.titoloFilm = titoloFilm;
        this.dataOraProiezione = dataOraProiezione;
        this.numeroBiglietti = numeroBiglietti;
        this.costoUnitario = costoUnitario;
        this.costoTotale = costoTotale;
    }

    /**
     * Restituisce il codice univoco della prenotazione.
     * @return il codice univoco
     */
    public String getCodiceUnivoco() { return codiceUnivoco; }
    
    /**
     * Restituisce lo username del cliente che ha effettuato la prenotazione.
     * @return lo username del cliente
     */
    public String getUsernameCliente() { return usernameCliente; }
    
    /**
     * Restituisce il titolo del film prenotato.
     * @return il titolo del film
     */
    public String getTitoloFilm() { return titoloFilm; }
    
    /**
     * Restituisce la data e l'ora della proiezione.
     * @return la data e l'ora nel formato yyyy-MM-dd HH:mm:ss
     */
    public String getDataOraProiezione() { return dataOraProiezione.trim(); }
    
    /**
     * Restituisce il numero di biglietti prenotati.
     * @return il numero di biglietti
     */
    public int getNumeroBiglietti() { return numeroBiglietti; }
    
    /**
     * Restituisce il costo unitario del biglietto.
     * @return il costo di un singolo biglietto
     */
    public double getCostoUnitario() { return costoUnitario; }
    
    /**
     * Restituisce il costo totale della prenotazione.
     * @return il costo totale (numero biglietti * costo unitario)
     */
    public double getCostoTotale() { return costoTotale; }

    /**
     * Modifica la data e l'ora della proiezione in caso di cambio della prenotazione.
     * @param nuovaDataOra la nuova data e ora nel formato yyyy-MM-dd HH:mm:ss
     */
    public void setDataOraProiezione(String nuovaDataOra) { 
        this.dataOraProiezione = nuovaDataOra; 
    }

    @Override
    public String toString() {
        return "Codice: " + codiceUnivoco + " | Film: " + titoloFilm + " (" + dataOraProiezione + 
               ") | Biglietti: " + numeroBiglietti + " | Totale: Euro " + costoTotale;
    }
}
