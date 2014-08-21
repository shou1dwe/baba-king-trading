import OrderManagement.TruffleOrderManager;
import executionmanagement.ExecutionManager;
import marketdatamanagement.MarketDataManager;
import play.Application;
import play.GlobalSettings;

import java.lang.Override;

public class Global extends GlobalSettings{
    @Override
    public void onStart(Application app) {
        new TruffleOrderManager().registerOrderConfirmationListener();
        new MarketDataManager().startQuotesUpdate();
        new ExecutionManager().startExecution();
        System.out.println("Application Started");
    }
}