=========================================================
      PROGETTO CINEMAX - LABORATORIO INTERDISCIPLINARE A
=========================================================

1. REQUISITI DI SISTEMA
---------------------------------------------------------
Per compilare ed eseguire l'applicazione è necessario avere 
installato sul proprio computer Java (JDK 8 o versione successiva).


2. INSTALLAZIONE (Clonazione Repository)
---------------------------------------------------------
Il progetto è ospitato su GitHub. Per installarlo localmente, 
aprire il terminale e digitare il seguente comando:

git clone https://github.com/GLXlollo/LAB_A_CINEMAX.git

Una volta clonato, spostarsi all'interno della cartella radice 
del progetto:
cd LAB_A_CINEMAX


3. ESECUZIONE DEL PROGRAMMA (Metodo Rapido)
---------------------------------------------------------
L'applicazione è già stata compilata e pacchettizzata in formato 
eseguibile (.jar). 
Assicurandosi di avere il terminale aperto nella cartella radice 
del progetto, avviare il programma con il seguente comando:

java -jar bin/src/CineMax.jar


4. ISTRUZIONI DI COMPILAZIONE MANUALE
---------------------------------------------------------
Qualora si desiderasse ricompilare il progetto partendo dal 
codice sorgente, seguire questi passaggi dal terminale (nella 
cartella radice del progetto):

- Compilazione dei file .java (da src a bin):
  javac -d bin src/*.java

- Creazione del nuovo pacchetto eseguibile .jar:
  jar cfe bin/src/CineMax.jar src.CineMax -C bin .


5. STRUTTURA DEL PROGETTO
---------------------------------------------------------
Il progetto rispetta la struttura delle directory richiesta:
- /src  : Contiene il codice sorgente (.java)
- /bin  : Contiene il codice eseguibile compilato (.jar e .class)
- /data : Contiene i database CSV (Utenti, Prenotazioni, Proiezioni)
- /doc  : Contiene la documentazione (Manuale Utente, Tecnico e Javadoc)
- autori.txt : Contiene i dati dei membri del team


6. NOTE PER IL COLLAUDO (CREDENZIALI STAFF)
---------------------------------------------------------
Per garantire la sicurezza del sistema, la registrazione di nuovi
Proiezionisti e Bigliettai dall'interfaccia utente è protetta da un 
PIN aziendale (simulando l'autorizzazione di un Amministratore).

Per testare le funzionalità del personale, utilizzare l'opzione "2" 
(Registrati) nel menu principale inserendo i seguenti PIN quando 
richiesto:

- PIN Sicurezza Proiezionista: PROJ2026
- PIN Sicurezza Bigliettaio:   TICK2026