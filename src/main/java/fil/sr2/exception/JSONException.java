package fil.sr2.exception;

/**
 * Cette classe permet de relever les exceptions JSON
 *
 * @author pochet
 */
public class JSONException extends Exception {

    public JSONException() {
        super();
    }

    public JSONException(String msg) {
        super(msg);
    }
}