package OrderManagement;

import OrderManagement.datatransferobjects.Trade;
import OrderManagement.exceptions.WrongTradeFormatException;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.IOException;
import java.io.StringWriter;

/**
 * Created by user on 8/17/2014.
 */
public class OrderJMSBroker implements OrderBroker {
    public static final String QUEUE_REQUEST = "OrderBroker";
    private Connection connection;

    private static JAXBContext context;

    static {
        try {
            context = JAXBContext.newInstance (Trade.class);
        }
        catch (Exception ex) {
        }
    }

    public OrderJMSBroker(){
        ConnectionFactory factory = new ActiveMQConnectionFactory(); // default broker, on localhost:61616
        try {
            connection = factory.createConnection();
            connection.start();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public void submitTrade(Trade trade) throws WrongTradeFormatException {
        String tradeXML = parseTradeToXML(trade);
        if (tradeXML == null){
            throw new WrongTradeFormatException("Unable to parse to XML format");
        }
        Session session = null;
        try {
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE); // no transaction
            Destination destination = session.createQueue(QUEUE_REQUEST);
            MessageProducer producer = session.createProducer(destination);
            TextMessage message = session.createTextMessage(tradeXML);
            producer.send(message);
        } catch(JMSException ex) {
            ex.printStackTrace();
        } finally {
            try {
                session.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }

    public String parseTradeToXML(Trade trade){
        StringWriter out = null;
        try {
            out = new StringWriter();
            Marshaller marshaller = context.createMarshaller();
            marshaller.marshal (trade, out);
            return out.toString ();
        } catch (Exception ex) {
            // logging
        } finally {
            try {
                if(out!= null) out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
