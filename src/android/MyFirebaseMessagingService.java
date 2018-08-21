package com.gae.scaffolder.plugin;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import com.hmkcode.android.sqlite.MySQLiteHelper;

/**
 * Created by Felipe Echanique on 08/06/2016.
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCMPlugin";

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // TODO(developer): Handle FCM messages here.
        // If the application is in the foreground handle both data and notification messages here.
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
        Log.d(TAG, "==> MyFirebaseMessagingService onMessageReceived");
        MySQLiteHelper db = new MySQLiteHelper(getApplicationContext());
		
		if( remoteMessage.getNotification() != null){
			Log.d(TAG, "\tNotification Title: " + remoteMessage.getNotification().getTitle());
			Log.d(TAG, "\tNotification Message: " + remoteMessage.getNotification().getBody());
		}
		
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("wasTapped", false);
		for (String key : remoteMessage.getData().keySet()) {
                Object value = remoteMessage.getData().get(key);
                Log.d(TAG, "\tKey: " + key + " Value: " + value);
				data.put(key, value);
        }
		
		Log.d(TAG, "\tNotification Data: " + data.toString());
        FCMPlugin.sendPushPayload( data );

        if(data.containsKey("latitude") && data.containsKey("longitude") && data.containsKey("message"))
        {
            if(data.containsKey("interventionId"))
            {
                String interventionId = String.valueOf(data.get("interventionId"));
                List<String> interventionsTmp = db.getAllRecords("activeInterventions", 1);
                if(interventionsTmp == null || interventionsTmp.size() == 0)
                {
                    startActivity(data);
                    return;
                }

                for(String intervention: interventionsTmp)
                {
                    if(intervention.equals(interventionId))
                    {
                        Log.d(TAG, "Intervention with key: " + interventionId + " already exits.");
                        return;
                    }
                }
            }
            
            startActivity(data);
        }
        else if(data.containsKey("phoneNumber") && data.containsKey("save"))
        {
            Log.d(TAG, "data.containsKey('phoneNumber')");

            String number = String.valueOf(data.get("phoneNumber"));
            Log.d(TAG, number);
            db.savePhoneNumber(number);

            Log.d(TAG, "number saved");

            List<String> phoneNumbersTmp = db.getAllRecords("phoneNumbers", 1);
            for(String tmp : phoneNumbersTmp)
            {
                Log.d(TAG, "phone number: " + tmp);
            }
        }
        else if(data.containsKey("phoneNumber") && data.containsKey("remove"))
        {
            String number = String.valueOf(data.get("phoneNumber"));
            Log.d(TAG, number);
            db.removePhoneNumber(number);

            Log.d(TAG, "number removed");

            List<String> phoneNumbersTmp = db.getAllRecords("phoneNumbers", 1);
            for(String tmp : phoneNumbersTmp)
            {
                Log.d(TAG, "phone number: " + tmp);
            }

        }
        else if(data.containsKey("keywordId")  && data.containsKey("save"))
        {
            Log.d(TAG, "data.containsKey('keyword')");

            String keywordId = String.valueOf(data.get("keywordId"));
            String keyword = String.valueOf(data.get("keyword"));
            Log.d(TAG, "keywordId: " + keywordId);
            Log.d(TAG, "keyword:" + keyword);
            db.saveKeyword(keywordId, keyword);

            Log.d(TAG, "keyword saved");

            List<String> keywordsTmp = db.getAllRecords("keywords", 2);
            for(String tmp : keywordsTmp)
            {
                Log.d(TAG, "keyword: " + tmp);
            }
        }
        else if(data.containsKey("keywordId")  && data.containsKey("remove"))
        {
            Log.d(TAG, "data.containsKey('keyword')");

            String keywordId = String.valueOf(data.get("keywordId"));
            Log.d(TAG, keywordId);
            db.removeKeyword(keywordId);

            Log.d(TAG, "keyword removed");

            List<String> keywordsTmp = db.getAllRecords("keywords", 2);
            for(String tmp : keywordsTmp)
            {
                Log.d(TAG, "keyword: " + tmp);
            }
        }
        else if(data.containsKey("blacklistKeywordId")  && data.containsKey("save"))
        {
            Log.d(TAG, "data.containsKey('blacklistKeywordId')");

            String keywordId = String.valueOf(data.get("blacklistKeywordId"));
            String keyword = String.valueOf(data.get("blacklistKeyword"));
            Log.d(TAG, "blacklistKeywordId: " + keywordId);
            Log.d(TAG, "blacklistKeyword:" + keyword);
            db.saveBlacklistKeyword(keywordId, keyword);

            Log.d(TAG, "blacklistKeyword saved");

            List<String> blacklistTmp = db.getAllRecords("blacklist", 2);
            for(String tmp : blacklistTmp)
            {
                Log.d(TAG, "blacklistKeyword: " + tmp);
            }
        }
        else if(data.containsKey("blacklistKeywordId")  && data.containsKey("remove"))
        {
            Log.d(TAG, "data.containsKey('blacklistKeywordId')");

            String keywordId = String.valueOf(data.get("blacklistKeywordId"));
            Log.d(TAG, keywordId);
            db.removeBlacklisKeyword(keywordId);

            Log.d(TAG, "blacklistKeywordId removed");

            List<String> blacklistTmp = db.getAllRecords("blacklist", 2);
            for(String tmp : blacklistTmp)
            {
                Log.d(TAG, "blacklistKeyword: " + tmp);
            }
        }
        //sendNotification(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody(), remoteMessage.getData());
    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private void sendNotification(String title, String messageBody, Map<String, Object> data) {
        Intent intent = new Intent(this, FCMPluginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		for (String key : data.keySet()) {
			intent.putExtra(key, data.get(key).toString());
		}
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(getApplicationInfo().icon)
                .setContentTitle(title)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

    /**
     * Open app when received FCM message.
     *
     * @param data FCM data.
     */
    private void startActivity(Map<String, Object> data)
    {
        Log.d(TAG, "startActivity");
        Intent intent = new Intent(this, FCMPluginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		for (String key : data.keySet()) {
			intent.putExtra(key, data.get(key).toString());
        }
        Log.d(TAG, "before startActivity");
        this.startActivity(intent);
        Log.d(TAG, "activityStarted");
    }
}
