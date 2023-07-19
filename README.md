# WORDLE

Progetto per il laboratorio di reti che implementa il gioco
di parole WORDLE.

## Requisiti

Requisiti lato server e lato client per una corretta
strutturazione del programma.

### Server

#### Strutture dati

- Lista o mappa degli utenti registrati.
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
