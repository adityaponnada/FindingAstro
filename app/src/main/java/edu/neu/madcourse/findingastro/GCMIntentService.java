package edu.neu.madcourse.findingastro;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/**
 * Created by adityaponnada on 22/04/16.
 */
public class GCMIntentService extends IntentService{
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    static final String TAG = "GCM_Communication";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *

     */
    public GCMIntentService() {
        super("GCMIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, ">>>>>>>> on HANDLE INTENT");
        Bundle extras = intent.getExtras();
        Log.d(TAG,"MESSAGE >>>>>>>>>>>>>>>>>>" +  String.valueOf(extras.size()) +"   " + extras.getString("message"));

        if (!extras.isEmpty()) {
            String message = extras.getString("message");
            if (message != null) {
                sendNotification(message);
            }
        }

        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GCMBroadcastReveiver.completeWakefulIntent(intent);}

        // Put the message into a notification and post it.
        // This is just one simple example of what you might choose to do with
        // a GCM message.
    public void sendNotification(String message) {
        mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent notificationIntent;
        notificationIntent = new Intent(this, findingastro_challenge.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Log.d("REACHED", "SENT NOTIF FLAGS");
        notificationIntent.putExtra("show_response", "show_response");
        PendingIntent intent = PendingIntent.getActivity(this, 0, new Intent(
                        this, skyview_activity.class),
                PendingIntent.FLAG_UPDATE_CURRENT);
        Log.d("REACHED", "PENDING INTENT");

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                this)
                .setSmallIcon(R.drawable.ic_media_pause)
                .setContentTitle("GCM Message Received")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setContentText(message).setTicker(message)
                .setAutoCancel(true);
        mBuilder.setContentIntent(intent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        Log.d("REACHED", "NOTIF COMPAT");

    }
}
