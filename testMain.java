import java.time.LocalDate;
import java.util.List;

public class testMain {

    public static void main(String[] args) {
        GestoreProiezioni g = new GestoreProiezioni();
        LocalDate l = CineMax.leggiData("inserisci data");
        System.out.println(l.toString());
        List<Proiezione> lista = g.cercaDopoInizio(l);
        for (Proiezione proiezione : lista) {
            System.out.println(proiezione.getTitolo() + " " + proiezione.getDataOra() );
        }
    }
}
