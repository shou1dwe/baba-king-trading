package models;

import play.data.validation.*;
import play.db.ebean.Model;

import javax.persistence.*;

/**
 * Created by user on 8/18/2014.
 */

@Entity
public class Stock extends Model{

    @Id
    public String ticker;

    public Stock(String name){
        this.ticker = name;
    }

    public static Finder<String, Stock> find = new Finder<String, Stock>(
            String.class, Stock.class
    );

}
