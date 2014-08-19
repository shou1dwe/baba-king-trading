package executionmanagement.datatransferobjects;

import java.util.ArrayList;

public class TwoMovingAveragesStrategy extends Strategy{
	private int longPeriod;
	private int shortPeriod;

	// handle by the system
	private Boolean isShortAvgSmaller;
	private Boolean isShortAvgLarger;

	public TwoMovingAveragesStrategy() {
	};

	public TwoMovingAveragesStrategy(String id, int longPeriod, int shortPeriod, int volume,
                                     String stock, double percentLoss, double percentProfit) {
		super(id, volume, volume, stock, percentLoss, percentProfit, new ArrayList<Position>());
		this.longPeriod = longPeriod;
		this.shortPeriod = shortPeriod;
		this.isShortAvgSmaller = null;
		this.isShortAvgLarger = null;
	}

	public Boolean isShortAvgLarger() {
		return isShortAvgLarger;
	}

	public void setShortAvgLarger(Boolean isShortAvgLarger) {
		this.isShortAvgLarger = isShortAvgLarger;
	}

	public Boolean isShortAvgSmaller() {
		return isShortAvgSmaller;
	}

	public void setShortAvgSmaller(Boolean isShortAvgSmaller) {
		this.isShortAvgSmaller = isShortAvgSmaller;
	}

	public int getLongPeriod() {
		return longPeriod;
	}

	public void setLongPeriod(int longPeriod) {
		this.longPeriod = longPeriod;
	}

	public int getShortPeriod() {
		return shortPeriod;
	}

	public void setShortPeriod(int shortPeriod) {
		this.shortPeriod = shortPeriod;
	}
}
