import java.io.Serializable;

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

    // Getters
    public String getDataOra() { return dataOra.substring(1, 20); }
    public String getTitolo() { return titolo; }
    public String getGenere() { return genere; }
    public String getRegista() { return regista; }
    public int getAnno() { return anno; }
    public int getDurata() { return durata; }
    public int getEtaMinima() { return etaMinima; }
    public double getCostoBiglietto() { return costoBiglietto; }
    
    // Metodi per i posti
    public int getPostiLiberi() { 
        return POSTI_TOTALI - postiPrenotati; 
    }
    
    public void prenotaPosti(int numeroPosti) {
        if (getPostiLiberi() >= numeroPosti) {
            postiPrenotati += numeroPosti;
        }
    }
    
    public void liberaPosti(int numeroPosti) {
        postiPrenotati -= numeroPosti;
    }

    // Metodo per permettere al proiezionista di cambiare orario
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