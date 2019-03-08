/*
 * Copyright 2019 CypherOS
 *
 * MiServices is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MiServices is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MiServices.  If not, see <http://www.gnu.org/licenses/>.
 */
package co.aoscp.miservices.quickspace;

import static co.aoscp.miservices.weather.utils.Constants.DEBUG;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Process;
import android.provider.Settings;
import android.util.Log;

import co.aoscp.miservices.Bits;
import co.aoscp.miservices.R;
import co.aoscp.miservices.onetime.IControllers;
import co.aoscp.miservices.providers.QuickspaceProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class EventsController implements IControllers {

    private static final String TAG = "EventsController";

    private static final int EVENT_CHECK_INTERVAL = 300000; // 5 minutes
    private static final String SETTING_DEVICE_INTRO_COMPLETED = "device_introduction_completed";

    private static final String LOVEGOOD_PACKAGE = "com.android.launcher3";
    private static final String INTENT_ACTION_INTERACTED = "co.aoscp.lovegood.quickspace.ACTION_INTERACTED";

    private Context mContext;
    private static EventsController sController;

    private boolean mIsRunning;
    private boolean mIsScreenOn = true;
	private boolean mIsRegistered = false;
    private long mLastUpdated;

    private boolean mIsFirstTime;

    private BroadcastReceiver mInteractionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) return;
            if (INTENT_ACTION_INTERACTED.equals(intent.getAction())) {
                onUpdate(false);
            }
        }
    };

    public EventsController(Context context) {
        mContext = context;
		if (mIsRegistered) {
			context.unregisterReceiver(mInteractionReceiver);
			mIsRegistered = false;
		}
        mContext.registerReceiver(mInteractionReceiver, Bits.getPackageIntentInfo(LOVEGOOD_PACKAGE, INTENT_ACTION_INTERACTED));
		mIsRegistered = true;
    }

    public void checkForEvents() {
        deviceIntroEvent();
    }

    @Override
    public void onUpdate(boolean reset) {
        if (mIsRunning) return;
        mIsRunning = true;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
				checkForEvents();
                mContext.getContentResolver().notifyChange(QuickspaceProvider.QUICKSPACE_URI, null /* observer */);
                mLastUpdated = System.currentTimeMillis();
				mIsRunning = false;
				if (DEBUG) Log.d(TAG, "Notified observers");
            }
        });
        thread.setPriority(Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
    }

    private boolean needsUpdate() {
        boolean expired = System.currentTimeMillis() - mLastUpdated > EVENT_CHECK_INTERVAL;
        return expired;
    }

    @Override
    public void onScreenOn() {
        if (mIsScreenOn){
            return;
        }
        if (DEBUG) Log.d(TAG, "onScreenOn");
        mIsScreenOn = true;
        if (!mIsRunning) {
            if (needsUpdate()) {
                if (DEBUG) Log.d(TAG, "Needs update, triggering checkForEvents");
                onUpdate(false);
            }
        }
    }

	@Override
    public void onScreenOff() {
        // No op
    }

    public int getEventType() {
        int eventType = QuickspaceCard.EVENT_NONE;
        if (mIsFirstTime) {
            eventType = QuickspaceCard.EVENT_FIRST_TIME;
        }
        return eventType;
    }

    public String getEventTitle() {
        String eventTitle = null;
        if (mIsFirstTime) {
            eventTitle = mContext.getString(R.string.quick_event_first_time);
        }
        return eventTitle;
    }

    public String getEventAction() {
        String eventAction = null;
        if (mIsFirstTime) {
            eventAction = mContext.getString(R.string.quick_event_first_time_action);
        }
        return eventAction;
    }

    private void deviceIntroEvent() {
        mIsFirstTime = Settings.System.getInt(mContext.getContentResolver(), SETTING_DEVICE_INTRO_COMPLETED, 0) == 0;
    }
}
