/**
 * Copyright (C) 2013, Easiio, Inc.
 * All Rights Reserved.
 */
package com.android.contacts_master.util;


import com.android.contacts_master.app.App;

public class DensityUtils {
//
//	public static int dp_px(float dpValue) {
//        final float scale = QuickCallApp.getContextQuickCall().getResources().getDisplayMetrics().density;
//     //   return (int) (dpValue * scale + 0.5f);
//        return 0;
//    }
	
	public static int px_dp(float pxValue) {  
        final float scale = App.getContextQuickCall().getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);  
    } 

}
