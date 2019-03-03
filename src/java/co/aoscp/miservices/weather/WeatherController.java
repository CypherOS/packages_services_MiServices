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
package co.aoscp.miservices.weather;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Process;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class WeatherController {

    private static final String TAG = "WeatherController";
	private static final boolean DEBUG = false;
	
	private static final int WEATHER_UPDATE_INTERVAL = 60 * 10 * 1000; // 10 minutes

	private Context mContext;
	private static WeatherController sController;

	private String mUpdateIntent;
	private PendingIntent mPendingUpdate;
	private boolean mBootAndUnlocked;
	private boolean mIsRunning;
	private boolean mIsScreenOn = true;

    private List<UpdateListener> mListeners;
    private long mLastUpdated;
    private long mScheduledAlarm = 0;
	private int mUpdateStatus = WeatherProvider.WEATHER_UPDATE_ERROR;
    private AlarmManager mAlarmManager;
	
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                return;
            }
            if (DEBUG) Log.d(TAG, "Received intent: " + intent.getAction());
            if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                onScreenOff();
            } else if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
                onScreenOn();
            } else if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
                mBootAndUnlocked = true;
                postUpdate(false);
            } else if (mUpdateIntent.equals(intent.getAction())) {
                postUpdate(false);
            } else if (Intent.ACTION_TIME_CHANGED.equals(intent.getAction())
                    || Intent.ACTION_TIMEZONE_CHANGED.equals(intent.getAction())) {
                postUpdate(true);
            }
        }
    };

	public interface UpdateListener {
        void onPostUpdate();
    }

    public static WeatherController get(Context context) {
        if (sController == null) {
            sController = new WeatherController(context.getApplicationContext());
        }
        return sController;
    }

    private WeatherController(Context context) {
        mContext = context;
		mUpdateIntent = "updateIntent_" + Integer.toString(getRandomInt());
		mPendingUpdate = PendingIntent.getBroadcast(mContext, getRandomInt(), new Intent(mUpdateIntent), 0);
		IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_BOOT_COMPLETED);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        filter.addAction(mUpdateIntent);
        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        mContext.registerReceiver(mReceiver, filter);
    }

	private int getRandomInt() {
        Random r = new Random();
        return r.nextInt((20000000 - 10000000) + 1) + 10000000;
    }

	private void postUpdate(boolean reset) {
        if (!mBootAndUnlocked) return;
        if (mIsRunning) {
            if (reset) resetSchedule();
            return;
        }
        mIsRunning = true;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                for (UpdateListener listener : mListeners) {
                    try {
                        listener.onPostUpdate();
                    } catch (Exception ignored) {
                    }
                }
                mLastUpdated = System.currentTimeMillis();
                resetSchedule();
            }
        });
        thread.setPriority(Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
		mIsRunning = false;
    }

	private boolean needsUpdate() {
        boolean expired = System.currentTimeMillis() - mLastUpdated > WEATHER_UPDATE_INTERVAL;
        return mUpdateStatus != WeatherProvider.WEATHER_UPDATE_SUCCESS || expired;
    }

	private void onScreenOn() {
        if (!mBootAndUnlocked || mIsScreenOn){
            return;
        }
        if (DEBUG) Log.d(TAG, "onScreenOn");
        mIsScreenOn = true;
        if (!mIsRunning) {
            if (needsUpdate()) {
                if (DEBUG) Log.d(TAG, "Needs update, triggering postUpdate");
                postUpdate(false);
            } else {
                if (DEBUG) Log.d(TAG, "Scheduling update");
                scheduleUpdate();
            }
        }
    }

    private void onScreenOff() {
        if (DEBUG) Log.d(TAG, "onScreenOff");
        mIsScreenOn = false;
        cancelUpdate();
    }

    private void resetSchedule(){
        mScheduledAlarm = 0;
        scheduleUpdate();
    }

    private void scheduleUpdate() {
        if (!mIsScreenOn) {
            return;
        }
        if (System.currentTimeMillis() >= mScheduledAlarm){
            mScheduledAlarm = System.currentTimeMillis() + WEATHER_UPDATE_INTERVAL;
        }
        mAlarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        mAlarmManager.cancel(mPendingUpdate);
        mAlarmManager.setExact(AlarmManager.RTC_WAKEUP, mScheduledAlarm, mPendingUpdate);
        if (DEBUG) Log.d(TAG, "Update scheduled");
    }

    private void cancelUpdate() {
        mAlarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        mAlarmManager.cancel(mPendingUpdate);
        if (DEBUG) Log.d(TAG, "Update scheduling canceled");
    }

	public void setUpdateStatus(int status) {
		mUpdateStatus = status;
	}

	public void addUpdateListener(final UpdateListener listener) {
        mListeners.add(listener);
    }

	public void removeUpdateListener(UpdateListener listener) {
        mListeners.remove(listener);
    }
}
