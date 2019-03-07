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
package co.aoscp.miservices.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import co.aoscp.miservices.MiManager;

public class MiReceiver extends BroadcastReceiver {

    private static final String TAG = "MiReceiver";

	private MiManager mMiManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Boot completed, starting weather update loop");
		mMiManager = new MiManager(context.getApplicationContext());
		mMiManager.getQsWeather().setBootCompleted();
    }
}