package OrderManagement;

import OrderManagement.datatransferobjects.Trade;
import OrderManagement.exceptions.WrongTradeFormatException;

/**
 * Created by user on 8/17/2014.
 */
public interface OrderBroker {
    void submitTrade(Trade trade) throws WrongTradeFormatException;
}
