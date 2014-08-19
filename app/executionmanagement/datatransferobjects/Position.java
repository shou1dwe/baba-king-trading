package executionmanagement.datatransferobjects;

public class Position {
	
	private String id;
	private boolean isGoLong;
	private int usedVolume;
	private double strikePrice;
	private double performace;
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
		this.strikePrice = strikePrice;
		this.performace = 0;
		this.isClose = false;
		this.strategyID = strategyID;
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

	public double getPerformace() {
		return performace;
	}

	public void setPerformace(double performace) {
		this.performace = performace;
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
