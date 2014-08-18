import OrderManagement.TruffleOrderManager;
import play.Application;
import play.GlobalSettings;

import java.lang.Override;

public class Global extends GlobalSettings{
    @Override
    public void onStart(Application app) {
        new TruffleOrderManager().registerOrderConfirmationListener();
        System.out.println("Application Started");
    }
}