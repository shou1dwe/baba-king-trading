package OrderManagement;

import OrderManagement.datatransferobjects.Trade;
import OrderManagement.exceptions.WrongTradeFormatException;
import OrderManagement.callbacks.OnOrderConfirmationReceivedListener;
import org.apache.activemq.ActiveMQConnectionFactory;
import play.libs.Akka;

import javax.jms.*;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.concurrent.TimeUnit;

/**
 * Created by user on 8/17/2014.
 */
public class OrderJMSBroker implements OrderBroker {
    public static final String QUEUE_REQUEST = "OrderBroker";
    private final String QUEUE_RESPONSE = "OrderBroker_Reply";
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
            System.out.println("Attempting to open ActiveMQ Connection...");
            connection.start();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public Trade submitTradeAsync(Trade trade) throws WrongTradeFormatException {
       return submitTrade(false, trade);
    }

    public Trade submitTradeSync(Trade trade) throws WrongTradeFormatException {
        return submitTrade(true, trade);
    }

    public Trade submitTrade(boolean isSync, Trade trade) throws WrongTradeFormatException {
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
            message.setJMSCorrelationID(String.valueOf(trade.getId()));
            producer.send(message);
            if(isSync) {
                Destination replyDestination = session.createQueue(QUEUE_RESPONSE);
                MessageConsumer consumer = session.createConsumer(replyDestination);
                Message replyMessage = consumer.receive();
                Trade tradeReply = unparseTradeToXML(((TextMessage) replyMessage).getText());
                return tradeReply;
            }
        } catch(JMSException ex) {
            ex.printStackTrace();
        } finally {
            try {
                session.close();

            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void initOrderBrokerReplyQueueListener(final OnOrderConfirmationReceivedListener listener) {
//        Session session = null;
//        try {
//             session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE); // no transaction
//             Destination destination = session.createQueue(QUEUE_RESPONSE);
//             MessageConsumer consumer = session.createConsumer(destination);
//             consumer.setMessageListener(new OrderConfirmedListener(listener));
//
//        } catch(JMSException ex) {
//            ex.printStackTrace();
//        } finally {
//            try {
//                if(connection != null) connection.close();
//            } catch (JMSException e) {}
//        }
        Akka.system().scheduler().schedule(
                scala.concurrent.duration.Duration.create(0, TimeUnit.MILLISECONDS),
                scala.concurrent.duration.Duration.create(2, TimeUnit.SECONDS),
                new Runnable() {
                    @Override
                    public void run() {
                        Session session = null;
                        try {
                            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE); // no transaction
                            Destination destination = session.createQueue(QUEUE_RESPONSE);
                            MessageConsumer consumer = session.createConsumer(destination);
                            Message replyMessage = consumer.receive(1000);
                            if(replyMessage!=null) {
                                Trade tradeReply = unparseTradeToXML(((TextMessage) replyMessage).getText());
                                listener.onOrderConfirmationReceived(tradeReply);
                            }
                        } catch(JMSException ex) {
                            ex.printStackTrace();
                        } finally {
                            try {
                                if(session != null) session.close();
                            } catch (JMSException e) {}
                        }
                    }
                },
                Akka.system().dispatcher());

    }

    private String parseTradeToXML(Trade trade){
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

    private Trade unparseTradeToXML(String text) {
        StringReader in = null;
        try {
            in = new StringReader (text);
            Unmarshaller unmarshaller = context.createUnmarshaller ();
            return (Trade) unmarshaller.unmarshal (in);
        } catch (Exception ex) {
        } finally {
            if(in!= null) in.close();
        }

        return null;
    }

    private class OrderConfirmedListener implements MessageListener {
        private final OnOrderConfirmationReceivedListener onOrderConfirmationReceivedListener;

        private OrderConfirmedListener(OnOrderConfirmationReceivedListener onOrderConfirmationReceivedListener) {
            this.onOrderConfirmationReceivedListener = onOrderConfirmationReceivedListener;
        }

        @Override
        public void onMessage(Message message) {
            if(message instanceof TextMessage) {
                try {
                    Trade trade = unparseTradeToXML(((TextMessage) message).getText());
                    onOrderConfirmationReceivedListener.onOrderConfirmationReceived(trade);
                    System.out.println("Received " + ((TextMessage) message).getText());
                    try{
                        Thread.sleep(1000);
                    } catch(InterruptedException e) {}

                } catch (JMSException e) {
                    System.out.println("Can't extract text from received message" + e);
                }
            }
            else
                System.out.println("Unexpected non-text message received.");
        }
    }
}
