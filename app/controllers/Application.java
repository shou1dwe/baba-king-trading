package controllers;

import OrderManagement.TruffleOrderManager;
import org.apache.activemq.ActiveMQConnectionFactory;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import views.html.index;
import views.html.strategies;
import views.html.test;

import javax.jms.*;
import java.util.Map;

public class Application extends Controller {

    private static TruffleOrderManager orderManager = new TruffleOrderManager();

    public static Result index() {
        return ok(index.render("Really? Your new application is ready."));
    }

    public static Result strategies() {
        return ok(strategies.render(""));
    }

    public static Result test() {
        return ok(test.render());
    }

    public static Result submitBuyTrade(){
        return submitTrade(request(), true);
    }

    public static Result submitSellTrade(){
        return submitTrade(request(), false);
    }

    private static Result submitTrade(Http.Request request, boolean isBuyOrder){
        Map<String, String[]> params = request().body().asFormUrlEncoded();
        String tickerSymbol = params.get("ticker")[0];
        System.out.println(String.format("TickerSymbol: %s", tickerSymbol));
        Integer size = Integer.parseInt(params.get("size")[0]);
        System.out.println(String.format("Size: %d",size));
        Double price = Double.parseDouble(params.get("price")[0]);
        System.out.println(String.format("Price: %f",price));

        int id = 0;
        boolean result = orderManager.submitTrade(id, tickerSymbol, isBuyOrder, size, price);
        if(result){
            return ok(test.render());
        } else {
            return internalServerError();
        }
    }

    // TODO Test Method -- to be deleted
    private static void initOrderBrokerReplyQueueListener() {
        final String QUEUE_NAME = "OrderBroker";
        ConnectionFactory factory = new ActiveMQConnectionFactory();
        Connection connection = null;
        try {
            connection = factory.createConnection();
            connection.start();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination destination = session.createQueue(QUEUE_NAME);
            MessageConsumer consumer = session.createConsumer(destination);
            consumer.setMessageListener(new MessageListener() {
                @Override
                public void onMessage(Message message) {
                    if(message instanceof TextMessage) {
                        try {
                            System.out.println("Received "  + ((TextMessage) message).getText());
                        } catch (JMSException e) {
                            System.out.println("Can't extract text from received message" + e);
                        }
                    }
                    else
                        System.out.println("Unexpected non-text message received.");
                }
            });

            try{ Thread.sleep(1000); } catch(InterruptedException e) {}
        } catch(JMSException ex) {
            ex.printStackTrace();
        } finally { try { if(connection != null) connection.close(); } catch (JMSException e) {} }
    }
}
