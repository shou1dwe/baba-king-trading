package executionmanagement.datatransferobjects;

import java.util.ArrayList;
import java.util.List;

public class TwoMovingAveragesStrategy extends Strategy{
	private int longPeriod;
	private int shortPeriod;

	// handle by the system
	private boolean isShortAvgSmaller; // true if currently short average is
										// smaller than long average
	private boolean isShortAvgLarger;

	public TwoMovingAveragesStrategy() {
	};

	public TwoMovingAveragesStrategy(String id, int longPeriod, int shortPeriod, int volume,
                                     String stock, double percentLoss, double percentProfit,
                                     boolean isShortAvgSmaller, boolean isShortAvgLarger) {
		super(id, volume, volume, stock, percentLoss, percentProfit, new ArrayList<Position>());
		this.longPeriod = longPeriod;
		this.shortPeriod = shortPeriod;
		this.isShortAvgSmaller = isShortAvgSmaller;
		this.isShortAvgLarger = isShortAvgLarger;
		this.isShortAvgSmaller = false;
	}

	public boolean isShortAvgLarger() {
		return isShortAvgLarger;
	}

	public void setShortAvgLarger(boolean isShortAvgLarger) {
		this.isShortAvgLarger = isShortAvgLarger;
	}

	public boolean isShortAvgSmaller() {
		return isShortAvgSmaller;
	}

	public void setShortAvgSmaller(boolean isShortAvgSmaller) {
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
