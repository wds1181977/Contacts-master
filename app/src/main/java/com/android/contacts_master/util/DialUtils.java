package com.android.contacts_master.util;



import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import com.android.contacts_master.logging.DevLog;
import com.android.contacts_master.logging.LogLevel;

public class DialUtils {

	private static final String TAG = "[ZHUANG]DialUtils";
	
	public static void sendMessage(Activity activity, String number){
		if(LogLevel.DEV){
			DevLog.d(TAG, "sendMessage number = " + number);
		}
		try{
			Uri uri = Uri.parse("smsto:" + number);            
			Intent it = new Intent(Intent.ACTION_SENDTO, uri);            
			activity.startActivity(it);
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	public static void callNumber(Activity activity, String number){
		if(LogLevel.DEV){
			DevLog.d(TAG, "callNumber number = " + number);
		}
		try{
			Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + number));
			activity.startActivity(intent);
		} catch (Exception e){
			e.printStackTrace();
		}
	}
}
