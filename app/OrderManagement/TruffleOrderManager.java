package OrderManagement;

import OrderManagement.datatransferobjects.Trade;
import OrderManagement.exceptions.WrongTradeFormatException;

/**
 * Created by user on 8/17/2014.
 */
public class TruffleOrderManager {
    OrderBroker broker;

    public TruffleOrderManager(){
        broker = new OrderJMSBroker();
    }

    public boolean submitTrade(int id, String tickerSymbol, boolean isBuyOrder, int size, double price){
        Trade trade = new Trade(id, tickerSymbol, isBuyOrder, size, price);
        try {
            broker.submitTrade(trade);
            return true;
        } catch (WrongTradeFormatException e) {
            return false;
        }
    }
}
