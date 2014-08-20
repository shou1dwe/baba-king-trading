package models;

import play.db.ebean.Model;

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
    public int vol;
    public int remainingVol;
    public double lossPercent;
    public double profitPercent;
    public String extras;

    public Strategy(String stratId, Stock stock, int vol, int remainingVol, double lossPercent, double profitPercent, String extras, Template templateId) {
        this.stratId = stratId;
        this.stock = stock;
        this.vol = vol;
        this.remainingVol = remainingVol;
        this.lossPercent = lossPercent;
        this.profitPercent = profitPercent;
        this.extras = extras;
        this.templateId = templateId;
    }

    @ManyToOne
    @JoinTable(name="tempId")
    public Template templateId;

    public static Finder<String, Strategy> find = new Finder<String, Strategy>(
            String.class, Strategy.class
    );
}
