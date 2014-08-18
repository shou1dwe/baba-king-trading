package OrderManagement;

import OrderManagement.datatransferobjects.Trade;
import OrderManagement.exceptions.WrongTradeFormatException;
import OrderManagement.callbacks.OnOrderConfirmationReceivedListener;

/**
 * Created by user on 8/17/2014.
 */
public interface OrderBroker {
    public Trade submitTradeAsync(Trade trade) throws WrongTradeFormatException;

    public Trade submitTradeSync(Trade trade) throws WrongTradeFormatException;

    void initOrderBrokerReplyQueueListener(OnOrderConfirmationReceivedListener listener);
}
