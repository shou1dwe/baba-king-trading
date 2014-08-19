package models;

import play.db.ebean.Model;

import javax.persistence.*;

/**
 * Created by user on 8/18/2014.
 */

@Entity
public class Strategy extends Model{

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public int stratId;

    @ManyToOne
    @JoinTable(name="ticker")
    public Stock stock;
    public int vol;
    public int remainingVol;
    public double lossPercent;
    public double profitPercent;
    public String extras;

    public Strategy(Stock stock, int vol, int remainingVol, double lossPercent, double profitPercent, String extras, Template templateId) {
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

    public static Finder<Integer, Strategy> find = new Finder<Integer, Strategy>(
            Integer.class, Strategy.class
    );
}
