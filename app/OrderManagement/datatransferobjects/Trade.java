package OrderManagement.datatransferobjects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.sql.Timestamp;
import java.util.Date;

@XmlRootElement(name="trade")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class Trade {
    private int id;

    private Timestamp when;
    private String stock;
    private boolean buy;
    private int size;
    private double price;

    public Trade() {
    }

    public Trade(int id, String stock, boolean buy, int size, double price) {
        this.id = id;
        this.stock = stock;
        this.buy = buy;
        this.size = size;
        this.price = price;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getPrice() {
        return this.price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getSize() {
        return this.size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getStock() {
        return this.stock;
    }

    public void setStock(String stock) {
        this.stock = stock;
    }

    public boolean isBuy() {
        return buy;
    }

    public void setBuy(boolean buy) {
        this.buy = buy;
    }

    public Timestamp getWhen() {
        return this.when;
    }

    public void setWhen(Timestamp when) {
        this.when = when;
    }

    @XmlTransient
    public Date getWhenAsDate() {
        return new Date(when.getTime());
    }

    public void setWhenAsDate(Date date) {
        when = new Timestamp(date.getTime());
    }

    public void setToNow() {
        when = new Timestamp(System.currentTimeMillis());
    }
}
