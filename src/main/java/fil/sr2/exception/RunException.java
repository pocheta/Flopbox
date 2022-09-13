package fil.sr2.exception;

/**
 * Cette classe permet de relever les exceptions lors du lancement du client
 *
 * @author pochet
 */
public class RunException extends Exception {

    public RunException() {
        super();
    }

    public RunException(String msg) {
        super(msg);
    }
}
