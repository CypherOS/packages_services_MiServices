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
import android.util.Log;

import co.aoscp.miservices.onetime.IControllers;
import co.aoscp.miservices.onetime.MiScreenReceiver;
import co.aoscp.miservices.onetime.MiUpdateReceiver;
import co.aoscp.miservices.quickspace.EventsController;
import co.aoscp.miservices.weather.WeatherController;

import java.util.List;
import java.util.LinkedList;

public class MiServices extends IntentService implements IControllers {
    private static final String TAG = "MiServices";

    private final Context mContext;
    private final MiScreenReceiver mScreenReceiver;
	private final MiUpdateReceiver mUpdateReceiver;

    private final List<IControllers> mControllers = new LinkedList<IControllers>();

    public MiServices(Context context) {
        super("MiServices");
        mContext = context;
        Log.d(TAG, "Starting");
		mControllers.add(new EventsController(context));
        mControllers.add(new WeatherController(context));

        mScreenReceiver = new MiScreenReceiver(context, this);
		mUpdateReceiver = new MiUpdateReceiver(context, this);
        updateState();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
    }

	@Override
    public void onUpdate(boolean reset) {
        for (IControllers controllers : mControllers) {
            controllers.onUpdate(reset);
        }
    }

    @Override
    public void onScreenOn() {
        for (IControllers controllers : mControllers) {
            controllers.onScreenOn();
        }
    }

    @Override
    public void onScreenOff() {
        for (IControllers controllers : mControllers) {
            controllers.onScreenOff();
        }
    }

    public void updateState() {
		for (IControllers controllers : mControllers) {
            controllers.onUpdate(false);
        }
    }
}