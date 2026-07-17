import java.util.Scanner;

public class CineMax {
    private static Scanner scanner = new Scanner(System.in);
    private static GestoreUtenti gestoreUtenti = new GestoreUtenti();
    private static Utente utenteLoggato = null;

    public static void main(String[] args) {
        boolean esci = false;

        System.out.println("=== Benvenuto in CineMax ===");

        while (!esci) {
            System.out.println("\n1. Login");
            System.out.println("2. Registrati come nuovo cliente");
            System.out.println("3. Entra come utente Guest");
            System.out.println("0. Esci");
            System.out.print("Scegli un'opzione: ");
            
            String scelta = scanner.nextLine();

            switch (scelta) {
                case "1":
                    eseguiLogin();
                    break;
                case "2":
                    eseguiRegistrazione();
                    break;
                case "3":
                    menuGuest();
                    break;
                case "0":
                    esci = true;
                    System.out.println("Arrivederci!");
                    break;
                default:
                    System.out.println("Opzione non valida.");
            }
        }
        scanner.close();
    }

    private static void eseguiLogin() {
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        utenteLoggato = gestoreUtenti.login(username, password);

        if (utenteLoggato != null) {
            System.out.println("Login effettuato! Benvenuto " + utenteLoggato.getNome());

            utenteLoggato = null; 
        } else {
            System.out.println("Credenziali errate.");
        }
    }

    private static void eseguiRegistrazione() {
        System.out.println("\n--- Registrazione Cliente registraCliente() ---");
        System.out.print("Nome: ");
        String nome = scanner.nextLine();
        System.out.print("Cognome: ");
        String cognome = scanner.nextLine();
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        System.out.print("Data di nascita (facoltativa, premi invio per saltare): ");
        String dataNascita = scanner.nextLine();
        System.out.print("Luogo del domicilio: ");
        String domicilio = scanner.nextLine();

        gestoreUtenti.registraCliente(nome, cognome, username, password, dataNascita, domicilio);
    }

    private static void menuGuest() {
        System.out.println("\n--- Accesso Guest ---");
        System.out.print("Inserisci il titolo del film da cercare: ");
        String titolo = scanner.nextLine();
        System.out.println("Cerco il film '" + titolo + "'... (funzionalità da implementare leggendo proiezioni.csv)");
    }
    
}
