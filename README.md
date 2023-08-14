# WORDLE

Progetto per il laboratorio di reti che implementa il gioco
di parole WORDLE.

## Requisiti

Requisiti lato server e lato client per una corretta
strutturazione del programma.

### Server

#### Strutture dati

- Albero degli utenti registrati per mantenere l'ordine di
  punteggio.
- Lista degli utenti registrati attualmente online (forse
  no).
- Vocabolario parole per il gioco.
- Istanza del gioco (o generatore di istanze)

#### Connessioni

- RMI

### Client

## Progettazione

### Client

Ogni client richiede delle operazioni al server che risponde
di conseguenza. Il server ad ogni azione del client deve
cambiare il suo stato interno.

Per capire come questo avviene analizziamo le richieste del
client.

- **Registrazione**

  - Il client richiede di registrare un nuovo utente
    fornendo `username` e `password`. Nel caso la
    registrazione vada a buon fine per il client non avviene
    alcun login automatico.
  - Il server controlla se esiste già un altro utente con lo
    stesso `username` e che la `password` non sia nulla. Se
    tutto va a buon fine l'utente ritorna `true` e genera un
    nuovo utente da inserire nella sua struttura dati degli
    utenti.

- **Login**
  - Il client richiede di effettuare il login fornendo
    `username` e `password`.

## Sessioni di gioco

Ogni utente per giocare deve prima fare richiesta di
iniziare una nuova partita.

Questa causa la creazione di una sessione di gioco che
lega l'utente alla parola estratta in quel momento.

### Interfaccia

1. L'utente richiede di iniziare una sessione di gioco.
2. Il server risponde con un messaggio che potrebbe
   segnalare la creazione con successo della sessione o
   un errore.

### Casisitiche

Quando si tenta di avviare una nuova sessione di gioco
possono verificarsi varie casisitiche.

In particolare, perché la sessione venga creata con
successo è necessario che

- L'utente abbia effettuato il login.
- Non sia presente una sessione con la parola corrente
  per lo stesso utente.
- L'utente non abbia già giocato con la parola corrente.

### Gestione delle sessioni lato server

Il server gestisce le sessioni di gioco in base alle
nuove parole estratte, alle richieste dei client e
all'andamento delle varie partite.

- Ogni volta che il thread `estrattore` estrae una
  nuova parola, elimina le sessioni **finite** legate alla
  parola precedente.
- Se l'utente richiede di aprire una nuova sessione di
  gioco quando ne ha già una in atto allora il server
  invia un messaggio d'errore.
- Non so ancora come gestire il logout in sessione
  aperta.
- Se l'utente indovina la parola la sessione viene
  `segnata` come conclusa ma non viene ancora eliminata.
