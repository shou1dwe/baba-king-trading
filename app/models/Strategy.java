package models;

import play.db.ebean.Model;

import javax.annotation.Nullable;
import javax.persistence.*;

/**
 * Created by user on 8/18/2014.
 */

@Entity
public class Strategy extends Model{

    @Id
    public String stratId;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(name="ticker")
    public Stock stock;
    public String name;
    public int vol;
    public int remainingVol;
    public double lossPercent;
    public double profitPercent;
    public String extras;
    @Column(nullable = true)
    public Boolean isClose;
    //0:close 1:open 2:activated 3:deactivated
    public boolean isDeleted;

    public Strategy(String stratId, Stock stock, int vol, int remainingVol, double lossPercent, double profitPercent, String extras, Template templateId) {
        this.stratId = stratId;
        this.stock = stock;
        this.vol = vol;
        this.remainingVol = remainingVol;
        this.lossPercent = lossPercent;
        this.profitPercent = profitPercent;
        this.extras = extras;
        this.templateId = templateId;
        this.isClose=null;
        this.isDeleted=false;
    }

    @ManyToOne
    @JoinTable(name="tempId")
    public Template templateId;

    public static Finder<String, Strategy> find = new Finder<String, Strategy>(
            String.class, Strategy.class
    );
}
