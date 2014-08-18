package OrderManagement.callbacks;

import OrderManagement.datatransferobjects.Trade;

/**
 * Created by user on 8/18/2014.
 */
public interface OnOrderConfirmationReceivedListener {
    void onOrderConfirmationReceived(Trade trade);
}