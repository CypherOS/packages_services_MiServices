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

import static co.aoscp.miservices.weather.utils.Constants.DEBUG;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Process;
import android.util.Log;

import co.aoscp.miservices.Bits;
import co.aoscp.miservices.onetime.IControllers;
import co.aoscp.miservices.quickspace.QuickspaceCard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class WeatherController implements IControllers {

    private static final String TAG = "WeatherController";

    private static final int WEATHER_UPDATE_INTERVAL = 60 * 10 * 1000; // 10 minutes

    private Context mContext;
    private static WeatherController sController;

    private PendingIntent mPendingUpdate;
    private boolean mIsRunning;
    private boolean mIsScreenOn = true;

    private UpdateListener mListener;
    private long mLastUpdated;
    private long mScheduledAlarm = 0;
    private int mUpdateStatus = QuickspaceCard.WEATHER_UPDATE_ERROR;
    private AlarmManager mAlarmManager;

    public interface UpdateListener {
        void onPostUpdate();
    }

    public WeatherController(Context context) {
        mContext = context;
        mPendingUpdate = PendingIntent.getBroadcast(mContext, getRandomInt(), new Intent(Bits.QUICKSPACE_UPDATE_ACTION), 0);
    }

    private int getRandomInt() {
        Random r = new Random();
        return r.nextInt((20000000 - 10000000) + 1) + 10000000;
    }

    @Override
    public void onUpdate(boolean reset) {
        if (mIsRunning) {
            if (reset) resetSchedule();
            return;
        }
        mIsRunning = true;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (mListener != null) {
                    try {
                        mListener.onPostUpdate();
                    } catch (Exception ignored) {
                    }
                }
                mLastUpdated = System.currentTimeMillis();
				mIsRunning = false;
                resetSchedule();
            }
        });
        thread.setPriority(Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
    }

    private boolean needsUpdate() {
        boolean expired = System.currentTimeMillis() - mLastUpdated > WEATHER_UPDATE_INTERVAL;
        return expired;
    }

    @Override
    public void onScreenOn() {
        if (mIsScreenOn) {
			if (DEBUG) Log.d(TAG, "Either the boot isn't complete or screen is already on");
            return;
        }
        if (DEBUG) Log.d(TAG, "onScreenOn");
        mIsScreenOn = true;
        if (!mIsRunning) {
            if (needsUpdate()) {
                if (DEBUG) Log.d(TAG, "Needs update, triggering onUpdate");
                onUpdate(false);
            } else {
                if (DEBUG) Log.d(TAG, "Scheduling update");
                scheduleUpdate();
            }
        } else {
			if (DEBUG) Log.d(TAG, "Update thread is still running");
		}
    }

    @Override
    public void onScreenOff() {
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

    public void addUpdateListener(UpdateListener listener) {
        mListener = listener;
    }
}
