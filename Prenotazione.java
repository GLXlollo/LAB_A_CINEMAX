import java.io.Serializable;
import java.util.UUID;

public class Prenotazione implements Serializable {
    private static final long serialVersionUID = 1L;

    private String codiceUnivoco;
    private String usernameCliente;
    private String titoloFilm;
    private String dataOraProiezione;
    private int numeroBiglietti;
    private double costoUnitario;
    private double costoTotale;
    // Costruttore per creare una NUOVA prenotazione (genera il codice univoco)
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

    // Costruttore per CARICARE una prenotazione dal file (il codice esiste già)
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

    // Getters
    public String getCodiceUnivoco() { return codiceUnivoco; }
    public String getUsernameCliente() { return usernameCliente; }
    public String getTitoloFilm() { return titoloFilm; }
    public String getDataOraProiezione() { return dataOraProiezione.trim(); }
    public int getNumeroBiglietti() { return numeroBiglietti; }
    public double getCostoUnitario() { return costoUnitario; }
    public double getCostoTotale() { return costoTotale; }

    // Metodo per aggiornare la data in caso di modifica
    public void setDataOraProiezione(String nuovaDataOra) { 
        this.dataOraProiezione = nuovaDataOra; 
    }

    @Override
    public String toString() {
        return "Codice: " + codiceUnivoco + " | Film: " + titoloFilm + " (" + dataOraProiezione + 
               ") | Biglietti: " + numeroBiglietti + " | Totale: Euro " + costoTotale;
    }
}
