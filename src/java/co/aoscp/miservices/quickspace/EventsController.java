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

    private static final int EVENT_CHECK_INTERVAL = 60 * 10 * 1000; // 10 minutes

    private Context mContext;
    private static EventsController sController;

    private boolean mIsRunning;
    private boolean mIsScreenOn = true;
    private long mLastUpdated;

    public static EventsController get(Context context) {
        if (sController == null) {
            sController = new EventsController(context);
        }
        return sController;
    }

    public EventsController(Context context) {
        mContext = context;
        sController = this;
    }

    @Override
    public void onUpdate(boolean reset) {
		// No op
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
        if (needsUpdate()) {
            if (DEBUG) Log.d(TAG, "Needs update, triggering checkForEvents");
            onUpdate(false);
        }
    }

    @Override
    public void onScreenOff() {
        // No op
    }

    @Override
    public void setBootCompleted() {
        onUpdate(false);
    }

    public int getEventType() {
        int eventType = QuickspaceCard.EVENT_NONE;
        return eventType;
    }

    public String getEventTitle() {
        String eventTitle = null;
        return eventTitle;
    }

    public String getEventAction() {
        String eventAction = null;
        return eventAction;
    }
}
