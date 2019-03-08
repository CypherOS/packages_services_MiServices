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
package co.aoscp.miservices;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;

import co.aoscp.miservices.onetime.IControllers;
import co.aoscp.miservices.onetime.MiScreenReceiver;
import co.aoscp.miservices.onetime.MiUpdateReceiver;

import java.util.List;
import java.util.LinkedList;

public class MiServices extends IntentService implements IControllers {
    private static final String TAG = "MiServices";

    private final Context mContext;

    private final PowerManager mPowerManager;
    private final PowerManager.WakeLock mWakeLock;

    private final MiScreenReceiver mScreenReceiver;
	private final MiUpdateReceiver mUpdateReceiver;

    private final List<IControllers> mControllers = new LinkedList<IControllers>();

    public MiServices(Context context) {
        super("MiServices");
        mContext = context;
        Log.d(TAG, "Starting");
        mScreenReceiver = new MiScreenReceiver(context, this);
		mUpdateReceiver = new MiUpdateReceiver(context, this);

        mControllers.add(new EventsController(context));
        mControllers.add(new WeatherController(context));

        mPowerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MiServicesWakeLock");
        updateState();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
    }

    @Override
    public void screenTurnedOn() {
        if (!mWakeLock.isHeld()) {
            mWakeLock.acquire();
        }
        for (IControllers controllers : mControllers) {
            controllers.onScreenOn();
        }
    }

    @Override
    public void screenTurnedOff() {
        if (mWakeLock.isHeld()) {
            mWakeLock.release();
        }
        for (IControllers controllers : mControllers) {
            controllers.onScreenOff();
        }
    }

    public void updateState() {
        if (!mPowerManager.isInteractive()) return;
        if (!mWakeLock.isHeld()) {
            mWakeLock.acquire();
        }
		for (IControllers controllers : mControllers) {
            controllers.onUpdate(false);
        }
    }
}