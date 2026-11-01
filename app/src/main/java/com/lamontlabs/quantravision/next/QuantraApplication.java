package com.lamontlabs.quantravision.next;
import android.app.Application;
public final class QuantraApplication extends Application {
    public AppSession session;
    @Override public void onCreate(){super.onCreate();session=new AppSession(this);}
}
