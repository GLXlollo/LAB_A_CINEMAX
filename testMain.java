
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import java.util.Scanner;

public class testMain {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String messaggio = "inserisci data";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        boolean esci = false;
        while (!esci) {
            System.out.print(messaggio + " (Formato: AAAA-MM-GG HH:MM, es. 2026-05-15 21:30): ");
            String input = scanner.nextLine();
            try {
                System.out.println("la tua stringa: " + input);
                // Tenta di convertire la stringa in data+ora.
                input = input.concat(":00");
                System.out.println("La tua stringa concatenata: " + input);
                LocalDateTime dataOra = LocalDateTime.parse(input, formatter);
                System.out.println("Oggetto LocalDate "+ dataOra.format(formatter)); // Restituisce la stringa perfetta
                esci=true;
            } catch (DateTimeParseException e) {
                System.out.println("Errore: Formato non valido. Controlla la struttura con i trattini oppure se la data e l'ora sono errate.");
            } 
        }
        scanner.close();
    }
}
