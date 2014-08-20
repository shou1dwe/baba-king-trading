package marketdatamanagement.callbacks;

import marketdatamanagement.datatransferobjects.Quote;

/**
 * Author: Xiawei
 */
public interface OnSpotPriceReceiveListener {
    void onSpotPriceReceived(Quote quote);
}
