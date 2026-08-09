import java.time.LocalDate;
import java.util.List;

public class testMain {

    public static void main(String[] args) {
        GestoreProiezioni g = new GestoreProiezioni();
        List<Proiezione> l = g.cercaPerData(LocalDate.of(2027, 12, 23), LocalDate.of(2027, 12, 27));
       for(Proiezione tmp : l) {
        System.out.println(tmp.getTitolo());
       }

    }
}
