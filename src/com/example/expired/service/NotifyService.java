package com.example.expired.service;

//import android.R;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.AlertDialog.Builder;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
 
import com.example.expired.R;
import com.example.expired.ui.phone.MainActivity;
import com.example.expired.ui.phone.SecondActivity;
 
/**
 * This service is started when an Alarm has been raised
 *
 * We pop a notification into the status bar for the user to click on
 * When the user clicks the notification a new activity is opened
 *
 * @author paul.blundell
 */
public class NotifyService extends Service{
	/**
     * Class for clients to access
     */
    public class ServiceBinder extends Binder {
        NotifyService getService() {
            return NotifyService.this;
        }
    }
 
    // Unique id to identify the notification.
    private static final int NOTIFICATION = 123;
    // Name of an intent extra we can use to identify if this service was started to create a notification 
    public static final String INTENT_NOTIFY = "com.example.expired.service.INTENT_NOTIFY";
    // The system notification manager
    private NotificationManager mNM;
    
    public String prodname;
    public int notifID;
    public String date;
 
    @Override
    public void onCreate() {
        Log.i("NotifyService", "onCreate()");
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }
 
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received start id " + startId + ": " + intent);
         
        // If this service was started by out AlarmTask intent then we want to show our notification
        if(intent.getBooleanExtra(INTENT_NOTIFY, false)) {
        	prodname = intent.getStringExtra("PRODNAME");
        	notifID = intent.getIntExtra("NOTIFID", 0);
        	date = intent.getStringExtra("DATE");
        	showNotification(notifID, prodname);
        	
        }
            
        
        
        // We don't care if this service is stopped as we have already delivered our notification
        return START_NOT_STICKY;
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
 
    // This is the object that receives interactions from clients
    private final IBinder mBinder = new ServiceBinder();
 
    /**
     * Creates a notification and shows it in the OS drag-down status bar
     */
    @SuppressLint("NewApi")
	private void showNotification(int notifID, String prodname) {
        // This is the 'title' of the notification
        CharSequence title = "Alarm!!";
        // This is the icon to use on the notification
        int icon = R.drawable.ic_launcher;
        // This is the scrolling text of the notification
        String text = "Your product: \n" 
        		+ prodname + "\n" 
        		+ " is going to expire soon!";  
        String text2 = "Date: " + date;
        // What time to show on the notification
        long time = System.currentTimeMillis();
         
        Resources res = this.getResources();
        
        
        Intent resultIntent = new Intent(this, MainActivity.class);
        
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        
        PendingIntent contentIntent = stackBuilder.getPendingIntent(
        		0,
                PendingIntent.FLAG_UPDATE_CURRENT);
        
        
        
        
        
        
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        
        //@SuppressWarnings("deprecation")
		//Notification notification = new Notification(icon, text, time);
 
        // The PendingIntent to launch our activity 
        //if the user selects this notification
        //PendingIntent contentIntent = PendingIntent.getActivity(
        //		this, 0, new Intent(this, MainActivity.class), 0);
        
        /*
        PendingIntent contentIntent = PendingIntent.getActivity(
        			this, 
        			notifID, 
        			new Intent(this, MainActivity.class), 
        			PendingIntent.FLAG_UPDATE_CURRENT);
        */
        
        builder.setContentIntent(contentIntent)
        .setSmallIcon(icon)
        .setLargeIcon(BitmapFactory.decodeResource(res,icon))
        .setTicker("Ticker")
        .setWhen(System.currentTimeMillis())
        .setAutoCancel(true)
        .setContentTitle(text)
        .setContentText(title);
        
        Notification n = builder.build();
        // Set the info for the views that show in the notification panel.
        //n.setLatestEventInfo(this, title, text, contentIntent);
 
        // Clear the notification when it is pressed
        //notification.flags |= Notification.FLAG_AUTO_CANCEL;
         
        // Send the notification to the system.
        n.flags = Notification.FLAG_AUTO_CANCEL;
        mNM.notify(notifID, n);
         
        // Stop the service when we are finished
        stopSelf();
    }
}
