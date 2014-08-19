package marketdatamanagement.datatransferobjects;

import java.util.Date;

/**
 * Author: Xiawei
 */
public class Quote {
    private String tickerSymbol;
    private double ask;
    private int askSize;
    private double bid;
    private int bidSize;
    private Date time;

    public Quote() {
    }

    public Quote(String tickerSymbol, double ask, int askSize, double bid, int bidSize, Date time) {
        this.tickerSymbol = tickerSymbol;
        this.ask = ask;
        this.askSize = askSize;
        this.bid = bid;
        this.bidSize = bidSize;
        this.time = time;
    }

    public String getTickerSymbol() {
        return tickerSymbol;
    }

    public void setTickerSymbol(String tickerSymbol) {
        this.tickerSymbol = tickerSymbol;
    }

    public double getAsk() {
        return ask;
    }

    public void setAsk(double ask) {
        this.ask = ask;
    }

    public int getAskSize() {
        return askSize;
    }

    public void setAskSize(int askSize) {
        this.askSize = askSize;
    }

    public double getBid() {
        return bid;
    }

    public void setBid(double bid) {
        this.bid = bid;
    }

    public int getBidSize() {
        return bidSize;
    }

    public void setBidSize(int bidSize) {
        this.bidSize = bidSize;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }
}
