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

import co.aoscp.miservices.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class EventsController {

    private static final String TAG = "EventsController";
    private static final boolean DEBUG = false;

    private static final int EVENT_CHECK_INTERVAL = 300000; // 5 minutes
	private static final String SETTING_DEVICE_INTRO_COMPLETED = "device_introduction_completed";

    private static final String LOVEGOOD_PACKAGE = "com.android.launcher3";
	private static final String INTENT_ACTION_INTERACTED = "co.aoscp.lovegood.quickspace.ACTION_INTERACTED";

    private Context mContext;
    private static EventsController sController;

    private boolean mIsRunning;
    private boolean mIsScreenOn = true;

    private EventListener mListener;
    private long mLastUpdated;

    private boolean mIsFirstTime;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                return;
            }
            if (DEBUG) Log.d(TAG, "Received intent: " + intent.getAction());
            if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
                onScreenOn();
            } else if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
                checkForEvents();
            } else if (Intent.ACTION_TIME_CHANGED.equals(intent.getAction())
                    || Intent.ACTION_TIMEZONE_CHANGED.equals(intent.getAction())) {
                checkForEvents();
            }
        }
    };

	private BroadcastReceiver mInteractionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) return;
            if (INTENT_ACTION_INTERACTED.equals(intent.getAction())) {
                postUpdate();
            }
        }
    };

    public interface EventListener {
        void onNewEvent();
    }

    public static EventsController get(Context context, boolean withReceiver) {
        if (sController == null) {
            sController = new EventsController(context.getApplicationContext(), withReceiver);
        }
        return sController;
    }

    private EventsController(Context context, boolean withReceiver) {
        mContext = context;
        if (withReceiver) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_SCREEN_ON);
            filter.addAction(Intent.ACTION_BOOT_COMPLETED);
            filter.addAction(Intent.ACTION_TIME_CHANGED);
            filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
            filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
            mContext.registerReceiver(mReceiver, filter);
			mContext.registerReceiver(mInteractionReceiver, Bits.getPackageIntentInfo(LOVEGOOD_PACKAGE, INTENT_ACTION_INTERACTED));
        }
    }

    public void checkForEvents() {
		deviceIntroEvent();
    }

    private void postUpdate() {
        if (mIsRunning) return;
        mIsRunning = true;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (mListener != null) {
                    try {
                        mListener.onNewEvent();
                    } catch (Exception ignored) {
                    }
                }
                mLastUpdated = System.currentTimeMillis();
            }
        });
        thread.setPriority(Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        mIsRunning = false;
    }

    private boolean needsUpdate() {
        boolean expired = System.currentTimeMillis() - mLastUpdated > EVENT_CHECK_INTERVAL;
        return expired;
    }

    private void onScreenOn() {
        if (mIsScreenOn){
            return;
        }
        if (DEBUG) Log.d(TAG, "onScreenOn");
        mIsScreenOn = true;
        if (!mIsRunning) {
            if (needsUpdate()) {
                if (DEBUG) Log.d(TAG, "Needs update, triggering checkForEvents");
                checkForEvents();
            }
        }
    }

    public int getEventType() {
		checkForEvents();
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

    public void addEventListener(EventListener listener) {
        mListener = listener;
    }

	private void deviceIntroEvent() {
		mIsFirstTime = Settings.System.getInt(mContext.getContentResolver(), SETTING_DEVICE_INTRO_COMPLETED, 0) == 0;
		//postUpdate();
	}
}
