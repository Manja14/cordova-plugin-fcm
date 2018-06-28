package com.gae.scaffolder.plugin;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.WindowManager;
import android.view.Window;
import android.os.PowerManager;

import java.util.Map;
import java.util.HashMap;

import static android.view.WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON;
import static android.view.WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD;
import static android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
import static android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON;
import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

public class FCMPluginActivity extends Activity {
    private static String TAG = "FCMPlugin";
    private PowerManager.WakeLock wl;

    /*
     * this activity will be started if the user touches a notification that we own. 
     * We send it's data off to the push plugin for processing.
     * If needed, we boot up the main activity to kickstart the application. 
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "==> FCMPluginActivity onCreate");

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "FCMPluginActivity");
        if ((wl != null) && (wl.isHeld() == false)) 
        {
            wl.acquire(); 
        }

        Window window = getWindow();
        window.addFlags(
                        FLAG_ALLOW_LOCK_WHILE_SCREEN_ON |
                        FLAG_SHOW_WHEN_LOCKED |
                        FLAG_TURN_SCREEN_ON |
                        FLAG_DISMISS_KEYGUARD |
                        FLAG_KEEP_SCREEN_ON
                );
        
        // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
        //     setShowWhenLocked(true);
        //     setTurnScreenOn(true);
        //     KeyguardManager keyguardManager = (KeyguardManager)getSystemService(Context.KEYGUARD_SERVICE);
        //     keyguardManager.requestDismissKeyguard(this, null);
        // } else {
        //     this.window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
        //             WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
        //             WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        // }
		
		Map<String, Object> data = new HashMap<String, Object>();
        if (getIntent().getExtras() != null) {
			Log.d(TAG, "==> USER TAPPED NOTFICATION");
			data.put("wasTapped", true);
			for (String key : getIntent().getExtras().keySet()) {
                String value = getIntent().getExtras().getString(key);
                Log.d(TAG, "\tKey: " + key + " Value: " + value);
				data.put(key, value);
            }
        }
		
		FCMPlugin.sendPushPayload(data);

        finish();

        forceMainActivityReload();
    }

    private void forceMainActivityReload() {
        PackageManager pm = getPackageManager();
        Intent launchIntent = pm.getLaunchIntentForPackage(getApplicationContext().getPackageName());
        startActivity(launchIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
		Log.d(TAG, "==> FCMPluginActivity onResume");
        final NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }
	
	@Override
	public void onStart() {
		super.onStart();
		Log.d(TAG, "==> FCMPluginActivity onStart");
	}
	
	@Override
	public void onStop() {
        super.onStop();
        wl.release();
		Log.d(TAG, "==> FCMPluginActivity onStop");
	}

}