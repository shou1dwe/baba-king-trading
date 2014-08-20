package models;

import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 8/18/2014.
 */

@Entity
public class Stock extends Model{

    @Id
    public String ticker;
    @OneToMany(mappedBy = "stock", fetch = FetchType.EAGER)
    public List<Strategy> strategies;

    public String companyName;
    public String moreInfo;
    public String notes;
    public String exchange;

    public Stock(String ticker, String companyName, String moreInfo, String notes, String exchange) {
        this.ticker = ticker;
        this.companyName = companyName;
        this.moreInfo = moreInfo;
        this.notes = notes;
        this.exchange = exchange;
        this.strategies = new ArrayList<>();
    }

    public Stock(String ticker, List<Strategy> strategies) {
        this.ticker = ticker;
        this.strategies = strategies;
    }

    public static Finder<String, Stock> find = new Finder<String, Stock>(
            String.class, Stock.class
    );

}
