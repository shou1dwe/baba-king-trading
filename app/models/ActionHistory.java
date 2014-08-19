package models;

import play.data.format.Formats;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by user on 8/18/2014.
 */

@Entity
public class ActionHistory extends Model {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public int histId;

    @ManyToOne
    @JoinTable(name="stratId")
    public Strategy stratId;

    public double performance;
    public boolean isLong;
    public boolean isClose;
    public double strikePrice;
    public double closePrice;
    @Formats.DateTime(pattern="yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    public Date actionTime;

    public ActionHistory(Strategy stratId, double performance, boolean isLong, boolean isClose, double strikePrice, double closePrice, Date actionTime) {
        this.stratId = stratId;
        this.performance = performance;
        this.isLong = isLong;
        this.isClose = isClose;
        this.strikePrice = strikePrice;
        this.closePrice = closePrice;
        this.actionTime = actionTime;
    }

    public static Finder<Integer, ActionHistory> find = new Finder<Integer, ActionHistory>(
            Integer.class, ActionHistory.class
    );

    public int getHistId() {
        return histId;
    }

    public void setHistId(int histId) {
        this.histId = histId;
    }
}
