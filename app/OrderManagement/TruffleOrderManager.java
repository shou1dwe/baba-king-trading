package OrderManagement;

import OrderManagement.callbacks.TruffleOrderConfirmationListener;
import OrderManagement.datatransferobjects.Trade;
import OrderManagement.exceptions.WrongTradeFormatException;
import OrderManagement.callbacks.OnOrderConfirmationReceivedListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by user on 8/17/2014.
 */
public class TruffleOrderManager {
    Map<Integer, TruffleOrderConfirmationListener> listeners = new HashMap<Integer, TruffleOrderConfirmationListener>();
    OrderBroker broker;

    public TruffleOrderManager(){
        broker = new OrderJMSBroker();
    }

    private Trade submitTradeSync(String tickerSymbol, boolean isBuyOrder, int size, double price){
        int id = (int) (System.currentTimeMillis() / 1000L);
        Trade trade = new Trade(id, tickerSymbol, isBuyOrder, size, price);
        try {
            Trade tradeReply = broker.submitTradeSync(trade);
            return tradeReply;
        } catch (WrongTradeFormatException e) {
            return null;
        }
    }

    public Trade submitTrade(String tickerSymbol, boolean isBuyOrder, int size, double price, TruffleOrderConfirmationListener orderConfirmationListener){
        int id = (int) (System.currentTimeMillis() / 1000L);
        Trade trade = new Trade(id, tickerSymbol, isBuyOrder, size, price);
        listeners.put(id, orderConfirmationListener);
        try {
            Trade tradeReply = broker.submitTradeAsync(trade);
            // TODO start a timeout timer
            return tradeReply;
        } catch (WrongTradeFormatException e) {
            return null;
        }
    }

    public void registerOrderConfirmationListener(){
        System.out.println("OrderConfirmationListener Registered");
        broker.initOrderBrokerReplyQueueListener(new OnOrderConfirmationReceivedListener() {
            @Override
            public void onOrderConfirmationReceived(Trade trade) {
                TruffleOrderConfirmationListener listener = listeners.get(trade.getId());
                if(listener != null){
                    listener.onOrderConfirmed(trade);
                }
                listeners.remove(trade.getId());
                // TODO store transaction info in database
                System.out.println("Trade Reply Received " + trade.toString() );
            }
        });
    }
}
