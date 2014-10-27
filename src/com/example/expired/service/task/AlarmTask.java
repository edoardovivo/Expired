package com.example.expired.service.task;

import java.util.Calendar;
import java.util.Date;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
 
import com.example.expired.service.NotifyService;
 
/**
 * Set an alarm for the date passed into the constructor
 * When the alarm is raised it will start the NotifyService
 *
 * This uses the android build in alarm manager *NOTE* if the phone is turned off this alarm will be cancelled
 *
 * This will run on it's own thread.
 *
 * @author paul.blundell
 */ 
public class AlarmTask implements Runnable{
	
	// The date selected for the alarm
    private final Date date;
    //The product name  string
    private final String prodname;
    // The android system alarm manager
    private final AlarmManager am;
    // Your context to retrieve the alarm manager from
    private final Context context;
    //Notification ID = Product ID in the Database
    private final int id;
    
 
    public AlarmTask(Context context, Date dt, String prodname, int id) {
        this.context = context;
        this.am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        this.date = dt;
        this.prodname = prodname;
        this.id = id;
    }
     
    
    public void run() {
        // Request to start are service when the alarm date is upon us
        // We don't start an activity as we just want 
    	//to pop up a notification into the system bar not a full activity
        Intent intent = new Intent(context, NotifyService.class);
        intent.putExtra(NotifyService.INTENT_NOTIFY, true);
        intent.putExtra("PRODNAME", prodname);
        intent.putExtra("NOTIFID", id);
        
        intent.putExtra("DATE", date);
        PendingIntent pendingIntent = PendingIntent.getService(
        		context, 0, intent, 0);
         
        // Sets an alarm - note this alarm will be lost 
        //if the phone is turned off and on again
    
        
        am.set(AlarmManager.RTC, date.getTime(), pendingIntent);
    }

}
