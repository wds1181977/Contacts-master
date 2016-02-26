package com.android.contacts_master.app;



import android.app.Application;
import android.content.Context;

import com.android.contacts_master.logging.LogLevel;
import com.android.contacts_master.logging.MarketLog;

public class App extends Application{

	private static final String TAG = "Contacts-master";
	
	private static Context sApplicationContext = null;

	@Override
    public void onCreate() {
        super.onCreate();        
        sApplicationContext = getApplicationContext();
	}
	
	@Override
    public void onLowMemory(){
    	super.onLowMemory();
    	   MarketLog.w(TAG, "onLowMemory:");
    }
    
    @Override
    public void onTerminate() {
        super.onTerminate();
        
        if (LogLevel.MARKET) {
            MarketLog.w(TAG, "onTerminate: " );
        }        
        
        sApplicationContext = null;
    }
    
    public static Context getContextQuickCall(){    	
        return sApplicationContext;
    }
}
