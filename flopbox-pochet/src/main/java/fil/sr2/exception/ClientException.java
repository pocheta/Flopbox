package fil.sr2.exception;

/**
 * Cette classe permet de relever les exceptions pour la classe "Client" pour les commandes éxécutés au serveur FTP par un utilisateur Flopbox
 *
 * @author pochet
 */
public class ClientException extends RuntimeException {

    public ClientException() {
        super();
    }

    public ClientException(String msg) {
        super(msg);
    }
}
