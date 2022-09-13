package fil.sr2.exception;

/**
 * Cette classe permet de relever les exceptions pour la classe "Account" pour les comptes Flopbox
 *
 * @author pochet
 */
public class AccountException extends RuntimeException {

    public AccountException() {
        super();
    }

    public AccountException(String msg) {
        super(msg);
    }
}
