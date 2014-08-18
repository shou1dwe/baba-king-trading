package OrderManagement.exceptions;

/**
 * Created by user on 8/17/2014.
 */
public class WrongTradeFormatException extends Throwable {
    public WrongTradeFormatException(String errorMessage) {
        super(errorMessage);
    }
}
