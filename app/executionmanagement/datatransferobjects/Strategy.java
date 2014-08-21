package executionmanagement.datatransferobjects;

import java.util.List;

/**
 * Author: Xiawei
 */
public class Strategy {
    private String id;
    private int volume;
    private int remaingVolume;
    private String stock;
    private double percentLoss;
    private double percentProfit;
    private List<Position> positions;
    private Boolean isClose;
    //null: deactivated  0:activated/open    1:activated/close

    public Strategy(){}

    public Strategy(String id, int volume, int remaingVolume, String stock, double percentLoss, double percentProfit, List<Position> positions) {
        this.id = id;
        this.volume = volume;
        this.remaingVolume = remaingVolume;
        this.stock = stock;
        this.percentLoss = percentLoss;
        this.percentProfit = percentProfit;
        this.positions = positions;
        this.isClose=null;

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public int getRemaingVolume() {
        return remaingVolume;
    }

    public void setRemaingVolume(int remaingVolume) {
        this.remaingVolume = remaingVolume;
    }

    public String getStock() {
        return stock;
    }

    public void setStock(String stock) {
        this.stock = stock;
    }

    public double getPercentLoss() {
        return percentLoss;
    }

    public void setPercentLoss(double percentLoss) {
        this.percentLoss = percentLoss;
    }

    public double getPercentProfit() {
        return percentProfit;
    }

    public void setPercentProfit(double percentProfit) {
        this.percentProfit = percentProfit;
    }

    public List<Position> getPositions() {
        return positions;
    }

    public void setPositions(List<Position> positions) {
        this.positions = positions;
    }

    public Boolean getIsClose() {
        return isClose;
    }

    public void setIsClose(Boolean isClose) {
        this.isClose = isClose;
    }
}
