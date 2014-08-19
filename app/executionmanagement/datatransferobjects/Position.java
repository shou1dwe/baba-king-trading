package executionmanagement.datatransferobjects;

public class Position {
	
	private String id;
	private boolean isGoLong;
    private int usedVolume;
    private int remainingVolume;
    private double strikePrice;
	private double performance;
	private boolean isClose;
	private String strategyID;


	public Position() {
	}

	public Position(boolean isGoLong,
			int usedVolume, double strikePrice, String strategyID) {
		super();
		//this.id = id;
		this.isGoLong = isGoLong;
		this.usedVolume = usedVolume;
        this.remainingVolume = usedVolume;
		this.strikePrice = strikePrice;
		this.performance = 0;
		this.isClose = false;
		this.strategyID = strategyID;
    }

    public int getRemainingVolume() {
        return remainingVolume;
    }

    public void setRemainingVolume(int remainingVolume) {
        this.remainingVolume = remainingVolume;
    }

    public String getStrategyID() {
		return strategyID;
	}

	public void setStrategyID(String strategyID) {
		this.strategyID = strategyID;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public boolean isClose() {
		return isClose;
	}

	public void setClose(boolean isClose) {
		this.isClose = isClose;
	}

	public double getPerformance() {
		return performance;
	}

	public void setPerformance(double performance) {
		this.performance = performance;
	}

	public int getUsedVolume() {
		return usedVolume;
	}

	public void setUsedVolume(int usedVolume) {
		this.usedVolume = usedVolume;
	}

	public double getStrikePrice() {
		return strikePrice;
	}

	public void setStrikePrice(double strikePrice) {
		this.strikePrice = strikePrice;
	}

	public boolean isGoLong() {
		return isGoLong;
	}

	public void setGoLong(boolean isGoLong) {
		this.isGoLong = isGoLong;
	}
}
