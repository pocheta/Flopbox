package fil.sr2.exception;

/**
 * Cette classe permet de relever les exceptions pour la classe "Server" pour les serveurs FTP
 *
 * @author pochet
 */
public class ServerException extends RuntimeException {

    public ServerException() {
        super();
    }

    public ServerException(String msg) {
        super(msg);
    }
}
