/**
 * Copyright (C) 2013, Easiio, Inc.
 * All Rights Reserved.
 */
package com.android.contacts_master.logging;


import com.android.contacts_master.Build;

public class LogLevel {
	
	public static final int LEVEL_MARKET = 0;
	public static final int LEVEL_DEV = 1;
	
    public static final boolean MARKET = (Build.LOG_LEVEL >= LogLevel.LEVEL_MARKET);
    public static final boolean DEV = (Build.LOG_LEVEL >= LogLevel.LEVEL_DEV);
    
}
