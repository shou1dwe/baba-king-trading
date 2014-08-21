package executionmanagement.datatransferobjects;

public class Position {
	
	private int id;
	private boolean isGoLong;
    private int usedVolume;
    private int remainingVolume;
    private double strikePrice;
	private double performance;
    private boolean isOpen;
    private boolean isClose;
	private String strategyID;


	public Position() {
	}

    public Position(boolean isGoLong, String strategyID){
        super();
        this.isGoLong = isGoLong;
        this.strategyID = strategyID;

        this.performance = 0;
        this.isOpen = false;
        this.isClose = false;
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
        this.isOpen = false;
		this.isClose = false;
		this.strategyID = strategyID;
    }

    public void openPosition(int volume, double price, int histId){
        this.usedVolume = volume;
        this.remainingVolume = volume;
        this.strikePrice = price;
        this.id = histId;

        this.isOpen = true;
    }

    public void closePosition(){
        this.isClose=true;
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

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean isOpen) {
        this.isOpen = isOpen;
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
