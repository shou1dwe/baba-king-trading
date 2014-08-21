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
    private double lastTradePrice;
    private Date time;
    private double change;

    public Quote() {
    }

    public Quote(String tickerSymbol, double ask, int askSize, double bid, int bidSize, double lastTradePrice, double change, Date time) {
        this.tickerSymbol = tickerSymbol != null ? tickerSymbol.toUpperCase() : null;
        this.ask = ask;
        this.askSize = askSize;
        this.bid = bid;
        this.bidSize = bidSize;
        this.lastTradePrice = lastTradePrice;
        this.time = time;
        this.change = change;
    }

    public double getLastTradePrice() {
        return lastTradePrice;
    }

    public void setLastTradePrice(double lastTradePrice) {
        this.lastTradePrice = lastTradePrice;
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

    public double getChange() {
        return change;
    }

    public void setChange(double change) {
        this.change = change;
    }

    @Override
    public String toString() {
        return "Quote{" +
                "tickerSymbol='" + tickerSymbol + '\'' +
                ", ask=" + ask +
                ", askSize=" + askSize +
                ", bid=" + bid +
                ", bidSize=" + bidSize +
                ", lastTradePrice=" + lastTradePrice +
                ", time=" + time +
                '}';
    }
}
